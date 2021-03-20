import javax.swing.*;

public class JavaChecker {
    public static boolean checkJava8() {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8.")) {
            return true;
        } else {
            JFrame frame = new JFrame("Wrong Java version!");
            //Centers it on screen
            frame.setLocationRelativeTo(null);

            JOptionPane.showMessageDialog(frame, "You have an unsupported version of Java.\n" +
                    "You're using Java " + javaVersion + " but you need any version of Java 8 (1.8.x).");

            return false;
        }
    }
}
