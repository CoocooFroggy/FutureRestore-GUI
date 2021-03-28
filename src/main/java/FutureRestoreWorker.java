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
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(allArgsArray);
        processBuilder.redirectErrorStream(true);
        futureRestoreProcess = processBuilder.start();

        futureRestoreProcess.getOutputStream().close();
//        futureRestoreProcess.getErrorStream().close();

        // Read Process Stream Output
        BufferedReader reader = new BufferedReader(new InputStreamReader(futureRestoreProcess.getInputStream()));

        //Log automatically
        String homeDirectory = System.getProperty("user.home");
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
            final Matcher matcher = Pattern.compile("(\\[DOWN\\] downloading file)|(downloading SEP)|(downloading SE firmware)|(downloading Baseband)|(downloading Rose firmware)|(Checking BuildIdentity)|(downloading Savage)|(downloading Veridian DigestMap)|(downloading Veridian FirmwareMap)|(Entering recovery mode)|(code=([0-9]+))|(Extracting BuildManifest from iPSW)|(\\[IMG4TOOL\\] checking hash for)|(Extracting filesystem from iPSW)|(Sending iBEC)|(Sending NORData)|(Unmounting filesystems)|(Sending FDR Trust data now)|(Sending filesystem now)|(Verifying restore)|(Checking filesystems)|(Mounting filesystems)|(Flashing firmware)|(Requesting FUD data)|(Updating baseband)|(Sending SystemImageRootHash now)|(Status: Restore Finished)|(what=(.*))|(Waiting for device to disconnect)|(Connecting to FDR)|(About to send NOR)|(Connecting to ASR)|(waiting for message)").matcher(line);
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        switch (i) {
                            case 1:
                                currentTaskTextField.setText("Downloading firmwares.json...");
                                break;
                            case 2:
                                currentTaskTextField.setText("Downloading SEP...");
                                break;
                            case 3:
                                currentTaskTextField.setText("Downloading SE firmware...");
                                break;
                            case 4:
                                currentTaskTextField.setText("Downloading baseband...");
                                break;
                            case 5:
                                currentTaskTextField.setText("Downloading Rose firmware...");
                                break;
                            case 6:
                                currentTaskTextField.setText("Checking BuildIdentity...");
                                break;
                            case 7:
                                currentTaskTextField.setText("Downloading Savage...");
                                break;
                            case 8:
                                currentTaskTextField.setText("Downloading Veridian DigestMap...");
                                break;
                            case 9:
                                currentTaskTextField.setText("Downloading Veridian FirmwareMap...");
                                break;
                            case 10:
                                currentTaskTextField.setText("Entering recovery mode...");
                                break;
                            case 11: // Error codes, same as case 12
                            case 12: {
                                //Parse error codes
                                switch (matcher.group(12)) {
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
                                                "https://github.com/marijuanARM/futurerestore#restoring-on-windows-10", "iBEC Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                                        if (response == JOptionPane.YES_OPTION) {
                                            openWebpage("https://github.com/marijuanARM/futurerestore#restoring-on-windows-10");
                                        }

                                        break;
                                    }
                                    case "44498961": {
                                        Object[] choices = {"Open link", "Ok"};

                                        int response = JOptionPane.showOptionDialog(mainMenuView, "Looks like you got an APTicket—APNonce mismatch error. This is a common error.\n" +
                                                        "Ensure you've set the correct generator on your device that corresponds with your blob's APNonce and try again.\n" +
                                                        "If you need more help, try the #futurerestore-help channel in the r/jailbreak Discord server.\n" +
                                                        "https://discord.gg/GaCUYSDGt9",
                                                "APTicket does not match APNonce", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
                                        if (response == JOptionPane.YES_OPTION) {
                                            openWebpage("https://discord.gg/GaCUYSDGt9");
                                        }

                                        break;
                                    }
                                }
                                break;
                            }
                            case 13:
                                currentTaskTextField.setText("Extracting BuildManifest from iPSW...");
                                break;
                            case 14:
                                currentTaskTextField.setText("Checking hashes...");
                                break;
                            case 15:
                                currentTaskTextField.setText("Extracting filesystem from iPSW...");
                                break;
                            case 16:
                                currentTaskTextField.setText("Sending iBEC...");
                                break;
                            case 17:
                                currentTaskTextField.setText("Sending NORData...");
                                break;
                            case 18:
                                currentTaskTextField.setText("Unmounting filesystems...");
                                break;
                            case 19:
                                currentTaskTextField.setText("Sending FDR trust data...");
                                break;
                            case 20:
                                currentTaskTextField.setText("Sending filesystem...");
                                break;
                            case 21:
                                currentTaskTextField.setText("Verifying restore...");
                                break;
                            case 22:
                                currentTaskTextField.setText("Checking filesystems...");
                                break;
                            case 23:
                                currentTaskTextField.setText("Mounting filesystems");
                                break;
                            case 24:
                                currentTaskTextField.setText("Flashing firmware...");
                                break;
                            case 25:
                                currentTaskTextField.setText("Requesting FUD data...");
                                break;
                            case 26:
                                currentTaskTextField.setText("Updating baseband...");
                                break;
                            case 27:
                                currentTaskTextField.setText("Sending SystemImageRootHash...");
                                break;
                            case 28:
                                currentTaskTextField.setText("Restore Finished!");
                                break;
                            case 29:
                            case 30:
                                currentTaskTextField.setText(matcher.group(30));
                                break;
                            //Cases 29 and 30 are error messages
                            case 31:
                                currentTaskTextField.setText("Waiting for device to disconnect...");
                                break;
                            case 32:
                                currentTaskTextField.setText("Connecting to FDR client...");
                                break;
                            case 33:
                                currentTaskTextField.setText("About to send NOR data...");
                                break;
                            case 34:
                                currentTaskTextField.setText("Connecting to ASR...");
                                break;
                            case 35:
                                currentTaskTextField.setText("Waiting for message from FDR...");
                                break;
                        }

                        break;
                    }
                }
                appendToLog(logTextArea, writer, line);
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
                builder.append(value);
            }
            String fullCommand = builder.toString();
            uploadLog(logPath, logName, fullCommand);
        }

        SwingUtilities.invokeLater(() -> {
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

    public static boolean openWebpage(String uriString) {
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            System.out.println("Unable to create link for " + uriString);
            e.printStackTrace();
        }
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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
