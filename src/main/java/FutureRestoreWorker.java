import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {

    public static Process futureRestoreProcess;
    static boolean hasRecoveryRestarted = false;

    static void runFutureRestore(String futureRestoreFilePath, ArrayList<String> allArgs, JPanel mainMenuView, JTextArea logTextArea, JProgressBar logProgressBar, JTextField currentTaskTextField, JButton startFutureRestoreButton, JButton stopFutureRestoreButton) throws IOException, InterruptedException, TimeoutException {
        ArrayList<String> argsAndFR = (ArrayList<String>) allArgs.clone();
        argsAndFR.add(0, futureRestoreFilePath);
        String[] allArgsArray = Arrays.copyOf(argsAndFR.toArray(), argsAndFR.toArray().length, String[].class);

        String homeDirectory = System.getProperty("user.home");
        File frGuiDirectory = new File(homeDirectory + "/FutureRestoreGUI");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(allArgsArray);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(frGuiDirectory);

        futureRestoreProcess = processBuilder.start();

        futureRestoreProcess.getOutputStream().close();
//        futureRestoreProcess.getErrorStream().close();

        // Read Process Stream Output
        BufferedReader reader = new BufferedReader(new InputStreamReader(futureRestoreProcess.getInputStream()));

        //Log automatically
        File frGuiLogsDirectory = new File(homeDirectory + "/FutureRestoreGUI/logs");
        if (!frGuiLogsDirectory.exists())
            frGuiLogsDirectory.mkdir();
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeString = dateTime.toString().replaceAll(":", ".");
        System.out.println("Date and time is " + dateTimeString);

        String logName = "FRLog_" + dateTimeString + ".txt";
        String logPath = frGuiLogsDirectory + "/" + logName;
        FileWriter writer = new FileWriter(logPath);

        String line;
        while ((line = reader.readLine()) != null) {
            //Parse messages
            Pattern progressBarPattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
            HashMap<String, String> parseableMessages = new HashMap<>() {{
                //Normal status messages during restore
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
                put("waiting for message", "Waiting for message from FDR...");

                //Special messages
                put("Status: Restore Finished", "Restore Finished!");
                put("what=", null);
                put("code=", null);
                put("unknown option -- use-pwndfu", null);
            }};

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
                        if (futureRestorePossibleMatch.equals("code=")) {
                            String code = line.replaceFirst("code=", "");
                            //Parse error codes
                            switch (code) {
                                //Unable to enter recovery mode
                                case "9043985": {
                                    if (!hasRecoveryRestarted) {
                                        hasRecoveryRestarted = true;
                                        //Ensure current process is killed
                                        if (futureRestoreProcess.isAlive())
                                            futureRestoreProcess.destroy();
                                        //Restart
                                        new Thread(() -> {
                                            try {
                                                runFutureRestore(futureRestoreFilePath, allArgs, mainMenuView, logTextArea, logProgressBar, currentTaskTextField, startFutureRestoreButton, stopFutureRestoreButton);
                                            } catch (IOException | InterruptedException | TimeoutException e) {
                                                System.out.println("Unable to rerun FutureRestore.");
                                                e.printStackTrace();
                                            }
                                        }).start();
                                        return;
                                    }
                                    break;
                                }
                                //iBEC Error
                                case "64684049": {
                                    Object[] choices = {"Open link", "Ok"};

                                    int response = JOptionPane.showOptionDialog(mainMenuView, "Looks like you got an iBEC error. This is a common error and easily fixable.\n" +
                                            "A solution for this error is available here:\n" +
                                            "https://github.com/m1stadev/futurerestore#restoring-on-windows-10", "iBEC Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                                    if (response == JOptionPane.YES_OPTION) {
                                        FRUtils.openWebpage("https://github.com/m1stadev/futurerestore#restoring-on-windows-10");
                                    }

                                    break;
                                }
                                // AP Nonce mismatch
                                case "44498961": {
                                    Object[] choices = {"Open link", "Ok"};

                                    int response = JOptionPane.showOptionDialog(mainMenuView, "Looks like you got an APTicket—APNonce mismatch error. This is a common error.\n" +
                                                    "Ensure you've set the correct generator on your device that corresponds with your blob's APNonce and try again.\n" +
                                                    "If you need more help, follow the steps to set generator on \"ios.cfw.guide\".\n" +
                                                    "https://ios.cfw.guide/futurerestore#getting-started",
                                            "APTicket does not match APNonce", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
                                    if (response == JOptionPane.YES_OPTION) {
                                        FRUtils.openWebpage("https://ios.cfw.guide/futurerestore#getting-started");
                                    }

                                    break;
                                }
                            }
                        }
                        if (futureRestorePossibleMatch.equals("what=")) {
                            String error = line.replaceFirst("what=", "");
                            currentTaskTextField.setText(error);
                        }
                        if (futureRestorePossibleMatch.equals("unknown option -- use-pwndfu")) {
                            JOptionPane.showMessageDialog(mainMenuView,
                                    "Looks like there is no pwndfu argument on this version of FutureRestore.\n" +
                                    "Ensure you're using a FutureRestore version which supports this argument, or turn off \"Pwned Restore.\"",
                                    "FutureRestore PWNDFU Unknown", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }

            //If it is a progress bar
            if (line.contains("[A")) {
                Matcher progressBarMatcher = progressBarPattern.matcher(line);
                if (progressBarMatcher.find()) {
                    //Set progress bar to parsed value
                    logProgressBar.setValue(Integer.parseInt(progressBarMatcher.group(1)));
                }
            } else {
                logProgressBar.setValue(0);
                appendToLog(logTextArea, writer, line);
            }
        }


        System.out.println("Done reading, closing reader");
        reader.close();
        writer.close();

        System.out.println("FutureRestore process ended.");
        logTextArea.append("FutureRestore process ended.\n");

        if (MainMenu.properties.getProperty("upload_logs").equals("true")) {
            //Make all args into a String
            StringBuilder builder = new StringBuilder();
            for (String value : allArgsArray) {
                builder.append(value + " ");
            }
            String fullCommand = builder.toString();
            uploadLog(logPath, logName, fullCommand);
        }

        SwingUtilities.invokeLater(() -> {
            // Clear text field if there was no real information
            if (currentTaskTextField.getText().contains("Starting FutureRestore"))
                currentTaskTextField.setText("");
            startFutureRestoreButton.setEnabled(true);
            stopFutureRestoreButton.setText("Stop FutureRestore");
        });

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
        rootJson.put("guiVersion", MainMenu.futureRestoreGUIVersion);
        String rootJsonString = gson.toJson(rootJson);

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://futurerestorelogserver.eastus.cloudapp.azure.com:6969/upload");
        StringEntity requestEntity = new StringEntity(rootJsonString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        httpPost.addHeader("authorization", "CoocooFroggy rocks");

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(responseString);

    }
}
