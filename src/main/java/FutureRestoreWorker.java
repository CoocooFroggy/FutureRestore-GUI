import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {
    public static class ProcessWorker extends SwingWorker<Void, String> {
        private String futureRestoreFilePath;
        private ArrayList<String> allArgs;
        private JTextArea ta;
        private JProgressBar pb;

        public ProcessWorker(String futureRestoreFilePath, ArrayList<String> allArgs, JTextArea ta, JProgressBar pb) {
            this.futureRestoreFilePath = futureRestoreFilePath;
            this.allArgs = allArgs;
            this.ta = ta;
            this.pb = pb;
        }

        @Override
        protected Void doInBackground() throws Exception {
            ProcessBuilder processBuilder = new ProcessBuilder();
            allArgs.add(0, futureRestoreFilePath);
            String[] allArgsArray = Arrays.copyOf(allArgs.toArray(), allArgs.toArray().length, String[].class);
            processBuilder.command(allArgsArray);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read Process Stream Output
            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            System.out.println("About to read lines");
            while ((line = is.readLine()) != null) {
                if (line.contains("[A")) {
                    System.out.println("Not adding " + line);
                    Pattern pattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        //Set progress bar to parsed value
                        pb.setValue(Integer.parseInt(matcher.group(1)));
                    }
                } else {
                    System.out.println("Appending " + line);
                    ta.append(line + "\n");
                }
            }
            is.close();
            process.destroy();
            System.out.println("Killing futurerestore");
            return null;
        }

    }
}
