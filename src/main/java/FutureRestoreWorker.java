import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {

    public static class ProcessWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() throws Exception {

            return null;
        }

    }

    public static Process futureRestoreProcess;
    static boolean hasRecoveryRestarted = false;

    static void runFutureRestore(String futureRestoreFilePath, ArrayList<String> allArgs, JPanel mainMenuView, JTextArea logTextArea, JProgressBar logProgressBar, JTextField currentTaskTextField, JButton startFutureRestoreButton) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        ArrayList<String> argsAndFR = (ArrayList<String>) allArgs.clone();
        argsAndFR.add(0, futureRestoreFilePath);
        String[] allArgsArray = Arrays.copyOf(argsAndFR.toArray(), argsAndFR.toArray().length, String[].class);
        processBuilder.command(allArgsArray);
        processBuilder.redirectErrorStream(true);
        futureRestoreProcess = processBuilder.start();

        futureRestoreProcess.getOutputStream().close();
        futureRestoreProcess.getErrorStream().close();

                // Read Process Stream Output
                BufferedReader reader = new BufferedReader(new InputStreamReader(futureRestoreProcess.getInputStream()));

                String line = null;
                while (true) {
                    System.out.println(line);
                    try {
                        if ((line = reader.readLine()) == null) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Parse messages
                    final Matcher matcher = Pattern.compile("(\\[DOWN\\] downloading file)|(downloading SEP)|(downloading SE firmware)|(downloading Baseband)|(downloading Rose firmware)|(Checking BuildIdentity)|(downloading Savage)|(downloading Veridian DigestMap)|(downloading Veridian FirmwareMap)|(Entering recovery mode)|(code=([0-9]+))|(Extracting BuildManifest from iPSW)|(\\[IMG4TOOL\\] checking hash for)|(Extracting filesystem from iPSW)|(Sending iBEC)|(Sending NORData)|(Unmounting filesystems)|(Sending FDR Trust data now)|(Sending filesystem now)|(Verifying restore)|(Checking filesystems)|(Mounting filesystems)|(Flashing firmware)|(Requesting FUD data)|(Updating baseband)|(Sending SystemImageRootHash now)|(Status: Restore Finished)|(what=(.*))|(Waiting for device to disconnect)").matcher(line);
                    if (matcher.find()) {
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            if (matcher.group(i) != null) {
                                System.out.println("Matches group " + i);

                                switch (i) {
                                    case 1: currentTaskTextField.setText("Downloading firmwares.json..."); break;
                                    case 2: currentTaskTextField.setText("Downloading SEP..."); break;
                                    case 3: currentTaskTextField.setText("Downloading SE firmware..."); break;
                                    case 4: currentTaskTextField.setText("Downloading baseband..."); break;
                                    case 5: currentTaskTextField.setText("Downloading Rose firmware..."); break;
                                    case 6: currentTaskTextField.setText("Checking BuildIdentity..."); break;
                                    case 7: currentTaskTextField.setText("Downloading Savage..."); break;
                                    case 8: currentTaskTextField.setText("Downloading Veridian DigestMap..."); break;
                                    case 9: currentTaskTextField.setText("Downloading Veridian FirmwareMap..."); break;
                                    case 10: currentTaskTextField.setText("Entering recovery mode..."); break;
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
                                                            runFutureRestore(futureRestoreFilePath, allArgs, mainMenuView, logTextArea, logProgressBar, currentTaskTextField, startFutureRestoreButton);
                                                        } catch (IOException | InterruptedException e) {
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
                                                Object defaultChoice = choices[0];

                                                int response = JOptionPane.showOptionDialog(mainMenuView, "Looks like you got an iBEC error. This is a common error and easily fixable.\n" +
                                                        "A solution for this error is available here:\n" +
                                                        "https://github.com/marijuanARM/futurerestore#restoring-on-windows-10", "iBEC Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                                                if (response == JOptionPane.YES_OPTION) {
                                                    openWebpage("https://github.com/marijuanARM/futurerestore#restoring-on-windows-10");
                                                }

                                                break;
                                            }
                                        }
                                        break;
                                    }
                                    case 13: currentTaskTextField.setText("Extracting BuildManifest from iPSW..."); break;
                                    case 14: currentTaskTextField.setText("Checking hashes..."); break;
                                    case 15: currentTaskTextField.setText("Extracting filesystem from iPSW..."); break;
                                    case 16: currentTaskTextField.setText("Sending iBEC..."); break;
                                    case 17: currentTaskTextField.setText("Sending NORData..."); break;
                                    case 18: currentTaskTextField.setText("Unmounting filesystems..."); break;
                                    case 19: currentTaskTextField.setText("Sending FDR trust data..."); break;
                                    case 20: currentTaskTextField.setText("Sending filesystem..."); break;
                                    case 21: currentTaskTextField.setText("Verifying restore..."); break;
                                    case 22: currentTaskTextField.setText("Checking filesystems..."); break;
                                    case 23: currentTaskTextField.setText("Mounting filesystems"); break;
                                    case 24: currentTaskTextField.setText("Flashing firmware..."); break;
                                    case 25: currentTaskTextField.setText("Requesting FUD data..."); break;
                                    case 26: currentTaskTextField.setText("Updating baseband..."); break;
                                    case 27: currentTaskTextField.setText("Sending SystemImageRootHash..."); break;
                                    case 28: currentTaskTextField.setText("Restore Finished!"); break;
                                    case 29:
                                    case 30: currentTaskTextField.setText(matcher.group(30)); break;
                                    //Cases 29 and 30 are error messages
                                    case 31: currentTaskTextField.setText("Waiting for device to disconnect..."); break;
                                }

                                break;
                            }
                        }
                        logTextArea.append(line + "\n");
                    }

                    //If it is a progress bar
                    if (line.contains("[A")) {
                        Pattern pattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
                        Matcher progressBarMatcher = pattern.matcher(line);
                        if (progressBarMatcher.find()) {
                            //Set progress bar to parsed value
                            logProgressBar.setValue(Integer.parseInt(progressBarMatcher.group(1)));
                        }
                    } else {
                        logProgressBar.setValue(0);
                        logTextArea.append(line + "\n");
                    }


                }
                System.out.println("Done reading, closing reader");
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        futureRestoreProcess.waitFor();
        System.out.println("FutureRestore process ended.");
        startFutureRestoreButton.setEnabled(true);
    }

    /*
    * Utilities *
     */

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
}
