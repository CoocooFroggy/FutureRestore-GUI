import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class FutureRestoreWorker {
    public static class ProcessWorker extends SwingWorker<Void, String> {
        private JTextArea ta;
        private String command;

        public ProcessWorker(String command, JTextArea ta) {
            this.command = command;
            this.ta = ta;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Process p = Runtime.getRuntime().exec(command);
            // Read Process Stream Output
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = is.readLine()) != null) {
                ta.append(line + "\n");
            }
            is.close();
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
