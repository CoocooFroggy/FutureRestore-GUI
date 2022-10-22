import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {

    public static Process futureRestoreProcess;
    private static String[] allArgsArray;
    private static String logName;
    private static String logPath;
    private static boolean hasRecoveryRestarted = false;

    public static void runFutureRestore(String futureRestoreFilePath, ArrayList<String> allArgs, MainMenu mainMenu) throws IOException {
        JPanel mainMenuView = mainMenu.getMainMenuView();
        JTextArea logTextArea = mainMenu.getLogTextArea();
        JTextField currentTaskTextField = mainMenu.getCurrentTaskTextField();
        JProgressBar logProgressBar = mainMenu.getLogProgressBar();
        JButton startFutureRestoreButton = mainMenu.getStartFutureRestoreButton();
        JButton stopFutureRestoreButton = mainMenu.getStopFutureRestoreUnsafeButton();

        ArrayList<String> argsAndFR = (ArrayList<String>) allArgs.clone();
        argsAndFR.add(0, futureRestoreFilePath);
        allArgsArray = Arrays.copyOf(argsAndFR.toArray(), argsAndFR.toArray().length, String[].class);

        String homeDirectory = System.getProperty("user.home");
        File frGuiDirectory = new File(homeDirectory + "/FutureRestoreGUI");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(allArgsArray);
        // Merge error stream into the OutputStream
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(frGuiDirectory);

        futureRestoreProcess = processBuilder.start();

        // Remember that OutputStream is our input into FutureRestore — which needs none
        futureRestoreProcess.getOutputStream().close();
//        futureRestoreProcess.getErrorStream().close();

        // Read Process Stream Output
        BufferedReader reader = new BufferedReader(new InputStreamReader(futureRestoreProcess.getInputStream()));

        // Log automatically
        File frGuiLogsDirectory = new File(homeDirectory + "/FutureRestoreGUI/logs");
        if (!frGuiLogsDirectory.exists())
            frGuiLogsDirectory.mkdir();
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeString = dateTime.toString().replaceAll(":", ".");
        System.out.println("Date and time is " + dateTimeString);

        logName = "FRLog_" + dateTimeString + ".txt";
        logPath = frGuiLogsDirectory + "/" + logName;
        FileWriter writer = new FileWriter(logPath);

        appendToLog(logTextArea, writer, "FutureRestore GUI Log – " + dateTimeString);
        appendToLog(logTextArea, writer, "Command ran: " + getFullCommandString());

        // Count the number of FDR timeouts
        int fdrTimeouts = 0;

        final Pattern progressBarPattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
        final HashMap<String, String> parseableMessages = new HashMap<>() {{
            // Normal status messages during restore
            put("[DOWN] downloading file", "Downloading firmwares.json...");
            put("downloading SEP", "Downloading SEP...");
            put("downloading SE firmware", "Downloading SE firmware...");
            put("downloading Baseband", "Downloading Baseband...");
            put("downloading Rose firmware", "Downloading Rose firmware...");
            put("Checking BuildIdentity", "Checking BuildIdentity...");
            put("downloading Savage", "Downloading Savage...");
            put("downloading Veridian DigestMap", "Downloading Veridian DigestMap...");
            put("downloading Veridian FirmwareMap", "Downloading Veridian FirmwareMap...");
            put("Entering recovery mode", "Entering recovery mode...");
            put("Extracting BuildManifest from iPSW", "Extracting BuildManifest from iPSW...");
            put("[IMG4TOOL] checking hash for", "Checking hashes...");
            put("Extracting filesystem from iPSW", "Extracting filesystem from iPSW...");
            put("Sending iBEC", "Sending iBEC...");
            put("Sending NORData", "Sending NORData...");
            put("Unmounting filesystems", "Unmounting filesystems...");
            put("Sending FDR Trust data now", "Sending FDR trust data...");
            put("Sending filesystem now", "Sending filesystem...");
            put("Verifying restore", "Verifying restore...");
            put("Checking filesystems", "Checking filesystems...");
            put("Mounting filesystems", "Mounting filesystems...");
            put("Flashing firmware", "Flashing firmware...");
            put("Requesting FUD data", "Requesting FUD data...");
            put("Updating baseband", "Updating Baseband...");
            put("Sending SystemImageRootHash now", "Sending SystemImageRootHash...");
            put("Waiting for device to disconnect", "Waiting for device to disconnect...");
            put("Connecting to FDR", "Connecting to FDR client...");
            put("About to send NOR", "About to send NOR data...");
            put("Connecting to ASR", "Connecting to ASR...");
//                put("waiting for message", "Waiting for message from FDR...");

            // Special messages
            put("Status: Restore Finished", "Restore Finished!");
            put("what=", null);
            put("code=", null);
            put("unknown option -- ", null);
            put("unrecognized option `", null);
            put("timeout waiting for command", null);
        }};

        String line;
        while ((line = reader.readLine()) != null) {
            // Parse messages
            for (Map.Entry<String, String> entrySet : parseableMessages.entrySet()) {
                String futureRestorePossibleMatch = entrySet.getKey();
                String fancyLog = entrySet.getValue();
                if (line.contains(futureRestorePossibleMatch)) {
                    // If there's a normal value
                    if (fancyLog != null) {
                        currentTaskTextField.setText(fancyLog);
                    }
                    // Otherwise, error was parsed
                    else {
                        switch (futureRestorePossibleMatch) {
                            case "code=" -> {
                                String code = line.replaceFirst("code=", "");
                                // Parse error codes
                                switch (code) {
                                    // Unable to enter recovery mode
                                    case "9043985" -> {
                                        if (!hasRecoveryRestarted) {
                                            hasRecoveryRestarted = true;
                                            // Ensure current process is killed
                                            if (FutureRestoreWorker.futureRestoreProcess != null && FutureRestoreWorker.futureRestoreProcess.isAlive())
                                                futureRestoreProcess.destroy();
                                            // Restart
                                            runFutureRestore(futureRestoreFilePath, allArgs, mainMenu);
                                            return;
                                        }
                                    }

                                    // iBEC Error
                                    case "64684049" -> {
                                        Object[] choices = {"Open link", "Ok"};

                                        int response = JOptionPane.showOptionDialog(mainMenuView, """
                                                Looks like you got an iBEC error. This is a common error and easily fixable.
                                                A solution for this error is available here:
                                                https://github.com/futurerestore/futurerestore#restoring-on-windows-10""", "iBEC Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                                        if (response == JOptionPane.YES_OPTION) {
                                            boolean openWebpageResult = FRUtils.openWebpage("https://github.com/futurerestore/futurerestore#restoring-on-windows-10", null);
                                            if (!openWebpageResult)
                                                appendToLog(logTextArea, writer, "Unable to open URL in your web browser. URL copied to clipboard, please open it manually.");
                                        }
                                    }

                                    // AP Nonce mismatch
                                    case "44498961" -> {
                                        Object[] choices = {"Open link", "Ok"};

                                        int response = JOptionPane.showOptionDialog(mainMenuView, """
                                                        Looks like you got an APTicket—APNonce mismatch error. This is a common error.
                                                        Ensure you've set the correct generator on your device that corresponds with your blob's APNonce and try again.
                                                        If you need more help, follow the steps to set generator on "ios.cfw.guide".
                                                        https://ios.cfw.guide/futurerestore#getting-started""",
                                                "APTicket does not match APNonce", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
                                        if (response == JOptionPane.YES_OPTION) {
                                            boolean openWebpageResult = FRUtils.openWebpage("https://ios.cfw.guide/futurerestore#getting-started", null);
                                            if (!openWebpageResult)
                                                appendToLog(logTextArea, writer, "Unable to open URL in your web browser. URL copied to clipboard, please open it manually.");
                                        }
                                    }
                                }
                            }
                            case "what=" -> {
                                String error = line.replaceFirst("what=", "");
                                currentTaskTextField.setText(error);
                            }
                            case "unknown option -- " -> {
                                Pattern pattern = Pattern.compile("(?<=unknown option -- )\\S+");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    JOptionPane.showMessageDialog(mainMenuView,
                                            "Looks like there is no --" + matcher.group() + " argument on this version of FutureRestore.\n" +
                                                    "Ensure you're using a FutureRestore version which supports this argument, or turn off the option.",
                                            "FutureRestore Unknown Option", JOptionPane.ERROR_MESSAGE);
                                }
                                String finalLine = line;
                                SwingUtilities.invokeLater(() -> currentTaskTextField.setText(finalLine));
                            }
                            case "unrecognized option `" -> {
                                Pattern pattern = Pattern.compile("(?<=unrecognized option `).*(?=')");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    JOptionPane.showMessageDialog(mainMenuView,
                                            "Looks like there is no " + matcher.group() + " argument on this version of FutureRestore.\n" +
                                                    "Ensure you're using a FutureRestore version which supports this argument, or turn off the option.",
                                            "FutureRestore Unknown Option", JOptionPane.ERROR_MESSAGE);
                                }
                                String finalLine = line;
                                SwingUtilities.invokeLater(() -> currentTaskTextField.setText(finalLine));
                            }
                            case "timeout waiting for command" -> {
                                fdrTimeouts++;
                                if (fdrTimeouts > 50) {
                                    logTextArea.append("Stopping FutureRestore—FDR looped over 50 times.\n");
                                    futureRestoreProcess.destroy();
                                    logTextArea.append("FutureRestore process killed.\n");
                                    reader.close();
                                    writer.close();

                                    uploadLogsIfNecessary();

                                    SwingUtilities.invokeLater(() -> {
                                        currentTaskTextField.setText("FDR looped over 50 times");
                                        startFutureRestoreButton.setEnabled(true);
                                        stopFutureRestoreButton.setText("Stop FutureRestore");
                                    });
                                    return;
                                }
                            }
                        }
                    }
                    break;
                }
            }

            // If it is a progress bar
            if (line.contains("[A")) {
                Matcher progressBarMatcher = progressBarPattern.matcher(line);
                if (progressBarMatcher.find()) {
                    // Set progress bar to parsed value
                    logProgressBar.setValue(Integer.parseInt(progressBarMatcher.group(1)));
                }
            } else {
                if (logProgressBar.getValue() != 0)
                    logProgressBar.setValue(0);
                appendToLog(logTextArea, writer, line);
            }
        }

        reader.close();
        writer.close();

        System.out.println("FutureRestore process ended.");
        logTextArea.append("FutureRestore process ended.\n");

        uploadLogsIfNecessary();

        SwingUtilities.invokeLater(() -> {
            // Clear text field if there was no real information
            if (currentTaskTextField.getText().contains("Starting FutureRestore"))
                currentTaskTextField.setText("");
            startFutureRestoreButton.setEnabled(true);
            stopFutureRestoreButton.setText("Stop FutureRestore");
        });

    }

    public static void uploadLogsIfNecessary() throws IOException {
        if (allArgsArray == null) {
            allArgsArray = new String[]{"No FutureRestore args"};
        }

        if (MainMenu.properties.getProperty("upload_logs").equals("true")) {
            String fullCommand = getFullCommandString();
            uploadLog(logPath, logName, fullCommand);
        }
    }

    private static String getFullCommandString() {
        // Make all args into a String
        StringBuilder builder = new StringBuilder();
        for (String value : allArgsArray) {
            builder.append(value).append(" ");
        }
        return builder.toString();
    }

    /*
     * Utilities *
     */

    public static void appendToLog(JTextArea logTextArea, FileWriter writer, String string) throws IOException {
        string += "\n";
        logTextArea.append(string);
        writer.append(string);
    }

    public static void uploadLog(String logPath, String logName, String command) throws IOException {
        String discordName = MainMenu.properties.getProperty("discord_name");
        Map<String, Object> rootJson = new HashMap<>();

        File logFile = new File(logPath);
        String logString;
        try {
            logString = new Scanner(logFile).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.out.println("Unable to read log.");
            e.printStackTrace();
            return;
        }

        Gson gson = new Gson();
        rootJson.put("command", command);
        rootJson.put("log", logString);
        rootJson.put("logName", logName);
        rootJson.put("discord", discordName);
        rootJson.put("guiVersion", Main.futureRestoreGUIVersion);
        String rootJsonString = gson.toJson(rootJson);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://coocoofroggy.me/frgui/frlogs/upload");
        StringEntity requestEntity = new StringEntity(rootJsonString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        httpPost.addHeader("Authorization", "CoocooFroggy rocks");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseString;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (ParseException e) {
            System.err.println("Unable to upload log.");
            e.printStackTrace();
            return;
        }
        System.out.println(responseString);
        response.close();
        httpClient.close();
    }
}
