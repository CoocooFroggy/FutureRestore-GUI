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
        private JTextArea logTextArea;
        private JProgressBar logProgressBar;
        private JTextField currentTaskTextField;

        public ProcessWorker(String futureRestoreFilePath, ArrayList<String> allArgs, JTextArea logTextArea, JProgressBar logProgressBar, JTextField currentTaskTextField) {
            this.futureRestoreFilePath = futureRestoreFilePath;
            this.allArgs = allArgs;
            this.logTextArea = logTextArea;
            this.logProgressBar = logProgressBar;
            this.currentTaskTextField = currentTaskTextField;
        }

        public static Process futureRestoreProcess;

        @Override
        protected Void doInBackground() throws Exception {
            ProcessBuilder processBuilder = new ProcessBuilder();
            allArgs.add(0, futureRestoreFilePath);
            String[] allArgsArray = Arrays.copyOf(allArgs.toArray(), allArgs.toArray().length, String[].class);
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
                        final Matcher matcher = Pattern.compile("(\\[DOWN\\] downloading file)|(downloading SEP)|(downloading SE firmware)|(downloading Baseband)|(downloading Rose firmware)|(Checking BuildIdentity)|(downloading Savage)|(downloading Veridian DigestMap)|(downloading Veridian FirmwareMap)|(Entering recovery mode)|(what=(.*?)\\n)|(Extracting BuildManifest from iPSW)|(\\[IMG4TOOL\\] checking buildidentity)|(\\[IMG4TOOL\\] checking hash for)|(Extracting filesystem from iPSW)|(Sending iBEC)|(Sending NORData)|(Unmounting filesystems)|(Sending FDR Trust data now)|(Sending filesystem now)|(Verifying restore)|(Checking filesystems)|(Mounting filesystems)|(Flashing firmware)|(Requesting FUD data)|(Updating baseband)|(Sending SystemImageRootHash now)|(Status: Restore Finished)").matcher(line);
                        if (matcher.find()) {
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                System.out.println("Checking if " + line + " matches group " + i);
                                if (matcher.group(i) != null) {
                                    System.out.println("Matches group " + i);
                                    //TODO: Switch statement, set text of current task
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
