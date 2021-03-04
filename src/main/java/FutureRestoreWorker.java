import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureRestoreWorker {
    public static class ProcessWorker extends SwingWorker<Void, String> {
        private String command;
        private JTextArea ta;
        private JProgressBar pb;

        public ProcessWorker(String command, JTextArea ta, JProgressBar pb) {
            this.command = command;
            this.ta = ta;
            this.pb = pb;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Process p = Runtime.getRuntime().exec(command);
            // Read Process Stream Output
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = is.readLine()) != null) {
                if (line.contains("[A")) {
                    System.out.println("Weird line");
                    Pattern pattern = Pattern.compile("\u001B\\[A\u001B\\[J([0-9]{3})");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        pb.setValue(Integer.parseInt(matcher.group(1)));
                    }

//                    ta.append(line + "\n");
                }
                else
                    ta.append(line + "\n");
            }
            is.close();
            p.destroy();
            System.out.println("Killing futurerestore");
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String string : chunks) {
                ta.append(string + "\n");
            }
        }

    }
}
