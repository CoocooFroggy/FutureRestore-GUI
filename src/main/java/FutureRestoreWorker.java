import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {
    public static class ProcessWorker extends SwingWorker<Void, String> {
        private String futureRestoreFilePath;
        private ArrayList<String> allArgs;
        private JPanel mainMenuView;
        private JTextArea logTextArea;
        private JProgressBar logProgressBar;
        private JTextField currentTaskTextField;

        public ProcessWorker(String futureRestoreFilePath, ArrayList<String> allArgs, JPanel mainMenuView, JTextArea logTextArea, JProgressBar logProgressBar, JTextField currentTaskTextField) {
            this.futureRestoreFilePath = futureRestoreFilePath;
            this.allArgs = allArgs;
            this.mainMenuView = mainMenuView;
            this.logTextArea = logTextArea;
            this.logProgressBar = logProgressBar;
            this.currentTaskTextField = currentTaskTextField;
        }

        public static Process futureRestoreProcess;
        boolean hasRecoveryRestarted = false;

        @Override
        protected Void doInBackground() throws Exception {

            ProcessBuilder processBuilder = new ProcessBuilder();
            ArrayList<String> argsAndFR = (ArrayList<String>) allArgs.clone();
            argsAndFR.add(0, futureRestoreFilePath);
            String[] allArgsArray = Arrays.copyOf(argsAndFR.toArray(), argsAndFR.toArray().length, String[].class);
            processBuilder.command(allArgsArray);
            processBuilder.redirectErrorStream(true);
            futureRestoreProcess = processBuilder.start();

            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    // Read Process Stream Output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(futureRestoreProcess.getInputStream()));
                    String line = null;
                    while (true) {
                        try {
                            if ((line = reader.readLine()) == null) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Parse messages
                        final Matcher matcher = Pattern.compile("(\\[DOWN\\] downloading file)|(downloading SEP)|(downloading SE firmware)|(downloading Baseband)|(downloading Rose firmware)|(Checking BuildIdentity)|(downloading Savage)|(downloading Veridian DigestMap)|(downloading Veridian FirmwareMap)|(Entering recovery mode)|(code=([0-9]+))|(Extracting BuildManifest from iPSW)|(\\[IMG4TOOL\\] checking hash for)|(Extracting filesystem from iPSW)|(Sending iBEC)|(Sending NORData)|(Unmounting filesystems)|(Sending FDR Trust data now)|(Sending filesystem now)|(Verifying restore)|(Checking filesystems)|(Mounting filesystems)|(Flashing firmware)|(Requesting FUD data)|(Updating baseband)|(Sending SystemImageRootHash now)|(Status: Restore Finished)|(what=(.*))|(Waiting for device to disconnect)").matcher(line);
                        if (matcher.find()) {
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                System.out.println("Checking if " + line + " matches group " + i);
                                if (matcher.group(i) != null) {
                                    System.out.println("Matches group " + i);
                                    //TODO: Switch statement, set text of current task

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
                                                        new FutureRestoreWorker.ProcessWorker(futureRestoreFilePath, allArgs, mainMenuView, logTextArea, logProgressBar, currentTaskTextField).execute();
                                                        return;
                                                    }
                                                    break;
                                                }
                                                //iBec Error
                                                case "64684049": {
                                                    //TODO: popup and link to ibec
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

                        if (line.contains("[A")) {
                            //If it is a progress bar
                            Pattern pattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
                            Matcher progressBarMatcher = pattern.matcher(line);
                            if (progressBarMatcher.find()) {
                                //Set progress bar to parsed value
                                logProgressBar.setValue(Integer.parseInt(progressBarMatcher.group(1)));
                            }
                        } else {
                            logTextArea.append(line + "\n");
                        }
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            ioThread.start();
            futureRestoreProcess.waitFor();
            System.out.println("Killing futurerestore");
            return null;
        }

    }
}
