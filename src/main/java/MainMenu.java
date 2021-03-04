import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.google.gson.Gson;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenu {
    private JButton selectBlobFileButton;
    private JButton selectTargetIPSWFileButton;
    private JCheckBox updateUCheckBox;
    private JCheckBox waitWCheckBox;
    private JCheckBox debugDCheckBox;
    private JPanel mainMenuView;
    private JButton startFutureRestoreButton;
    private JButton selectFutureRestoreBinaryExecutableButton;
    private JButton selectBuildManifestButton;
    private JButton exitRecoveryButton;
    private JTextArea logTextArea;
    private JComboBox basebandComboBox;
    private JTextField basebandTextField;
    private JComboBox sepComboBox;
    private JTextField sepTextField;
    private JScrollPane logScrollPane;

    private String futureRestoreFilePath;
    private String blobName;
    private String blobFilePath;
    private String targetIpswName;
    private String targetIpswPath;
    private String sepFilePath;
    private String basebandFilePath;
    private String buildManifestPath;
    private String sepState = "latest";
    private String bbState = "latest";

    private boolean optionUpdateState = false;
    private boolean optionWaitState = false;
    private boolean optionDebugState = true;

    public MainMenu() {
        $$$setupUI$$$();
        selectFutureRestoreBinaryExecutableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser
                final JFileChooser futureRestoreFileChooser = new JFileChooser();
                //In response to a button click:
                int returnVal = futureRestoreFileChooser.showOpenDialog(mainMenuView);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = futureRestoreFileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                    appendToLog("Set " + file.getAbsolutePath() + " to FutureRestore executable.");
                    futureRestoreFilePath = file.getAbsolutePath();
                    //Set name of button to blob file name
                    selectFutureRestoreBinaryExecutableButton.setText("✓ " + file.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }
        });
        selectBlobFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser
                final JFileChooser blobFileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Blob File (SHSH2)", "shsh2");
                blobFileChooser.setFileFilter(filter);
                //In response to a button click:
                int returnVal = blobFileChooser.showOpenDialog(mainMenuView);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = blobFileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                    appendToLog("Set " + file.getAbsolutePath() + " to SHSH2 blob.");
                    blobFilePath = file.getAbsolutePath();
                    blobName = file.getName();
                    selectBlobFileButton.setText("✓ " + file.getName());

                } else {
                    System.out.println("Cancelled");
                }

            }
        });
        selectTargetIPSWFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser
                final JFileChooser targetIpswFileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("iOS Firmware (IPSW)", "ipsw");
                targetIpswFileChooser.setFileFilter(filter);
                //In response to a button click:
                int returnVal = targetIpswFileChooser.showOpenDialog(mainMenuView);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = targetIpswFileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                    appendToLog("Set " + file.getAbsolutePath() + " to target IPSW.");
                    targetIpswPath = file.getAbsolutePath();
                    targetIpswName = file.getName();
                    //Set name of button to ipsw file name
                    selectTargetIPSWFileButton.setText("✓ " + file.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }
        });

        selectBuildManifestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser
                final JFileChooser buildManifestFileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("BuildManifest (plist)", "plist");
                buildManifestFileChooser.setFileFilter(filter);
                //In response to a button click:
                int returnVal = buildManifestFileChooser.showOpenDialog(mainMenuView);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = buildManifestFileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                    appendToLog("Set " + file.getAbsolutePath() + " to BuildManinfest.");
                    buildManifestPath = file.getAbsolutePath();
                    //Set name of button to ipsw file name
                    selectBuildManifestButton.setText("✓ " + file.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }
        });

        ActionListener optionsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionUpdateState = updateUCheckBox.isSelected();
                optionWaitState = waitWCheckBox.isSelected();
                optionDebugState = debugDCheckBox.isSelected();
            }
        };
        updateUCheckBox.addActionListener(optionsListener);
        waitWCheckBox.addActionListener(optionsListener);
        debugDCheckBox.addActionListener(optionsListener);

        startFutureRestoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Disable interaction
                mainMenuView.setEnabled(false);

                //Ensure they have FutureRestore selected
                if (futureRestoreFilePath == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Please select a FutureRestore executable.", "No FutureRestore Selected", JOptionPane.ERROR_MESSAGE);
                    mainMenuView.setEnabled(true);
                    return;
                }

                //Ensure they actually selected a blob, IPSW, and buildmanifest if needed
                if (blobFilePath == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Select a blob file.", "Error", JOptionPane.ERROR_MESSAGE);
                    mainMenuView.setEnabled(true);
                    return;
                }
                if (targetIpswPath == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Select an IPSW file.", "Error", JOptionPane.ERROR_MESSAGE);
                    mainMenuView.setEnabled(true);
                    return;
                }
                if (bbState.equals("manual") || sepState.equals("manual")) {
                    if (buildManifestPath == null) {
                        JOptionPane.showMessageDialog(mainMenuView, "Select a BuildManifest file.", "Error", JOptionPane.ERROR_MESSAGE);
                        mainMenuView.setEnabled(true);
                        return;
                    }
                }

                //If blob name has a build number in it
                Pattern blobPattern = Pattern.compile("[0-9]{16}_.*?_.*?_.*?-([0-9A-Z]+)_");
                Matcher blobMatcher = blobPattern.matcher(blobName);
                String blobBuild = null;
                if (blobMatcher.find()) {
                    System.out.println("Blob build is " + blobMatcher.group(1));
                    blobBuild = blobMatcher.group(1);
                }

                //If IPSW has a build name in it
                Pattern ipswPattern = Pattern.compile(".*?_.*?_([0-9A-Z]+)_");
                Matcher ipswMatcher = ipswPattern.matcher(targetIpswName);
                String targetIpswBuild = null;
                if (ipswMatcher.find()) {
                    System.out.println("IPSW build is " + ipswMatcher.group(1));
                    targetIpswBuild = ipswMatcher.group(1);
                }

                //If they're different
                if (blobBuild != null && targetIpswBuild != null)
                    if (!blobBuild.equals(targetIpswBuild)) {
                        JOptionPane.showMessageDialog(mainMenuView, "The build in your blob name, " + blobBuild + ", does not match the one in the IPSW name, " + targetIpswBuild + ". Ensure you have the right blob and IPSW before continuing.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }

                //Check FutureRestore version
                Runtime runtime = Runtime.getRuntime();
                String version = null;
                try {
                    Process process = runtime.exec(futureRestoreFilePath);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    Pattern pattern = Pattern.compile("Version: [0-9a-z]+ - ([0-9]+)");
                    String s;
                    //Only check the first 5 lines
                    for (int i = 0; i < 5; i++) {
                        s = bufferedReader.readLine();
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find())
                            version = matcher.group(1);
                    }

                } catch (IOException ioException) {
                    System.out.println("Unable to check FutureRestore version.");
                    ioException.printStackTrace();
                }

                if (version == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Unable to check FutureRestore version from selected executable. Manually ensure you have the latest version.", "Warning", JOptionPane.ERROR_MESSAGE);
                } else {
                    int response = JOptionPane.showConfirmDialog(mainMenuView, "Your FutureRestore's version: v" + version + ". Would you like to ensure this is the latest version on marijuanARM's fork?", "FutureRestore Version", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        try {
                            String latestVersion = getLatestFutureRestore();
                            if (version.equals(latestVersion)) {
                                JOptionPane.showMessageDialog(mainMenuView, "You're up to date! The latest version is " + latestVersion + ".");
                            } else {
                                JOptionPane.showMessageDialog(mainMenuView, "You're not on the latest version. The latest version is " + latestVersion + ", and you're on " + version + ".", "Version Mismatch", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (IOException ioException) {
                            System.out.println("Unable to check for latest FutureRestore");
                            JOptionPane.showMessageDialog(mainMenuView, "Unable to fetch FutureRestore's latest version. Manually check that " + version + " is the latest.", "Unable to Fetch Version", JOptionPane.WARNING_MESSAGE);
                            ioException.printStackTrace();
                        }
                    }
                }

                //TODO: BuildManifest
                //Build their final command
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(" -t " + blobFilePath);

                if (optionUpdateState)
                    stringBuilder.append(" -u");
                if (optionWaitState)
                    stringBuilder.append(" -w");
                if (optionDebugState)
                    stringBuilder.append(" -d");

                switch (sepState) {
                    case "latest":
                        stringBuilder.append(" --latest-sep");
                        break;
                    case "manual":
                        stringBuilder.append(" -s " + sepFilePath);
                        stringBuilder.append(" -m " + buildManifestPath);
                        break;
                }

                switch (bbState) {
                    case "latest":
                        stringBuilder.append(" --latest-baseband");
                        break;
                    case "manual":
                        stringBuilder.append(" -b " + basebandFilePath);
                        stringBuilder.append(" -p " + buildManifestPath);
                        break;
                    case "none":
                        stringBuilder.append(" --no-baseband");
                        break;
                }

                stringBuilder.append(" " + targetIpswPath);

                String finalCommand = stringBuilder.toString();

                StringSelection stringSelection = new StringSelection(finalCommand);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                //Run command
                runCommand(finalCommand);
            }
        });
        exitRecoveryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //If they haven't selected a futurerestore yet
                if (futureRestoreFilePath == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Please select a FutureRestore executable in order to exit recovery.", "No FutureRestore Selected", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                runCommand("--exit recovery");
            }
        });
        basebandComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (basebandComboBox.getSelectedItem().toString()) {
                    case "Latest Baseband":
                        bbState = "latest";
                        basebandTextField.setText("✓ (No file)");
                        if (sepState.equals("latest"))
                            selectBuildManifestButton.setEnabled(false);
                        break;
                    case "Manual Baseband":
                        if (chooseBbfw()) {
                            bbState = "manual";
                            selectBuildManifestButton.setEnabled(true);
                        } else {
                            bbState = "latest";
                            basebandComboBox.setSelectedItem("Latest Baseband");
                            if (sepState.equals("latest"))
                                selectBuildManifestButton.setEnabled(false);
                        }
                        break;
                    case "No Baseband":
                        bbState = "none";
                        basebandTextField.setText("✓ (No file)");
                        if (sepState.equals("latest"))
                            selectBuildManifestButton.setEnabled(false);
                        break;
                }
            }
        });
        sepComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (sepComboBox.getSelectedItem().toString()) {
                    case "Latest SEP":
                        sepState = "latest";
                        sepTextField.setText("✓ (No file)");
                        if (bbState.equals("latest") || bbState.equals("none"))
                            selectBuildManifestButton.setEnabled(false);
                        break;
                    case "Manual SEP":
                        if (chooseSep()) {
                            sepState = "manual";
                            selectBuildManifestButton.setEnabled(true);
                        } else {
                            sepState = "latest";
                            sepComboBox.setSelectedItem("Latest SEP");
                            if (bbState.equals("latest") || bbState.equals("none"))
                                selectBuildManifestButton.setEnabled(false);
                        }
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        String systemTheme = "light";
        try {
            systemTheme = getSystemTheme();
        } catch (IOException e) {
            System.out.println("Unable to determine if computer is in dark or light mode.");
            e.printStackTrace();
        }

        //Must set L&F before we create instance of MainMenu
        switch (systemTheme) {
            case "dark":
                FlatDarculaLaf.install();
                break;
            case "light":
                FlatIntelliJLaf.install();
                break;
        }

        String finalSystemTheme = systemTheme;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("FutureRestore GUI");

                MainMenu mainMenuInstance = new MainMenu();

                //Auto scroll log
                new SmartScroller(mainMenuInstance.logScrollPane, SmartScroller.VERTICAL, SmartScroller.END);

                frame.setContentPane(mainMenuInstance.mainMenuView);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);

                //Prepare for dark mode
                if (finalSystemTheme.equals("dark"))
                    turnDark(mainMenuInstance);

                //Centers it on screen
                frame.setVisible(true);

            }
        });

    }

    static String getSystemTheme() throws IOException {
        //Get light or dark mode
        //If on mac
        String mode;
        if (System.getProperty("os.name").equals("Mac OS X")) {
            Process process = Runtime.getRuntime().exec("defaults read -g AppleInterfaceStyle");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String macTheme = reader.readLine();
            if (macTheme != null) {
                //dark mode
                return "dark";
            } else {
                return "mac";
            }
        } else {
            final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
            final String REGISTRY_VALUE = "AppsUseLightTheme";
            if
            (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) &&
                    Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE) == 0)
                return "dark";
            else
                return "light";
        }

    }

    static void turnDark(MainMenu mainMenu) {
        JPanel mainMenuView = mainMenu.mainMenuView;
        JTextArea logTextArea = mainMenu.logTextArea;
        JScrollPane logScrollPane = mainMenu.logScrollPane;
        JButton startFutureRestoreButton = mainMenu.startFutureRestoreButton;


        mainMenuView.setBackground(new Color(40, 40, 40));
        logTextArea.setBackground(new Color(20, 20, 20));
        logTextArea.setForeground(new Color(200, 200, 200));
        logScrollPane.setBorder(null);

        //Loop through all components to make this faster
        for (Component c : mainMenuView.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(new Color(200, 200, 200));
                continue;
            }

            if (c instanceof JButton) {
                c.setBackground(new Color(60, 60, 60));
                c.setForeground(new Color(200, 200, 200));
                if (c == startFutureRestoreButton)
                    c.setBackground(new Color(38, 85, 163));
                continue;
            }

            if (c instanceof JTextField) {
                c.setBackground(new Color(60, 60, 60));
                c.setForeground(new Color(200, 200, 200));
            }

            if (c instanceof JCheckBox) {
                c.setBackground(new Color(40, 40, 40));
                c.setForeground(new Color(200, 200, 200));
            }

            if (c instanceof JComboBox) {
                c.setBackground(new Color(60, 60, 60));
                c.setForeground(new Color(200, 200, 200));
            }
        }
    }

    boolean chooseBbfw() {
        //Create a file chooser
        final JFileChooser basebandFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Baseband Firmware (BBFW)", "bbfw");
        basebandFileChooser.setFileFilter(filter);
        //In response to a button click:
        int returnVal = basebandFileChooser.showOpenDialog(mainMenuView);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = basebandFileChooser.getSelectedFile();
            //This is where a real application would open the file.
            appendToLog("Set " + file.getAbsolutePath() + " to baseband firmware.");
            basebandTextField.setText("✓ " + file.getName());
            basebandFilePath = file.getAbsolutePath();
            return true;
        } else {
            System.out.println("Cancelled");
            return false;
        }
    }

    boolean chooseSep() {
        //Create a file chooser
        final JFileChooser sepFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SEP (im4p)", "im4p");
        sepFileChooser.setFileFilter(filter);
        //In response to a button click:
        int returnVal = sepFileChooser.showOpenDialog(mainMenuView);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = sepFileChooser.getSelectedFile();
            //This is where a real application would open the file.
            appendToLog("Set " + file.getAbsolutePath() + " to SEP IM4P.");
            sepTextField.setText("✓ " + file.getName());
            sepFilePath = file.getAbsolutePath();
            return true;
        } else {
            System.out.println("Cancelled");
            return false;
        }
    }

    int lineNumber = 1;

    void runCommand(String command) {

        System.out.println("Going to run now");
        new FutureRestoreWorker.ProcessWorker(futureRestoreFilePath + " " + command, logTextArea).execute();

//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process process = runtime.exec(futureRestoreFilePath + " " + command);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String s;
//            while ((s = bufferedReader.readLine()) != null) {
//                System.out.println(s);
//                logTextArea.append("[" + lineNumber + "] " + s + "\n");
//                lineNumber++;
//            }
//            mainMenuView.setEnabled(true);
//        } catch (IOException ioException) {
//            System.out.println("Unable to run command.");
//            ioException.printStackTrace();
//        }
    }

    void appendToLog(String string) {
        logTextArea.append("[" + lineNumber + "] " + string + "\n");
        lineNumber++;
    }

    String getLatestFutureRestore() throws IOException {
        //Vars
        URL url = new URL("https://api.github.com/repos/marijuanARM/futurerestore/releases");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        Gson gson = new Gson();

        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        ArrayList<Map<String, Object>> result = gson.fromJson(content.toString(), ArrayList.class);
        Map<String, Object> newestRelease = result.get(0);
        String newestTag = (String) newestRelease.get("tag_name");
        System.out.println("Newest version: " + newestTag);

        return newestTag;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainMenuView = new JPanel();
        mainMenuView.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Blob");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainMenuView.add(label1, gbc);
        selectTargetIPSWFileButton = new JButton();
        selectTargetIPSWFileButton.setText("Select Target IPSW File...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectTargetIPSWFileButton, gbc);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Target IPSW");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainMenuView.add(label2, gbc);
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Options");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainMenuView.add(label3, gbc);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.BOLD, -1, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("FutureRestore");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        mainMenuView.add(label4, gbc);
        selectFutureRestoreBinaryExecutableButton = new JButton();
        selectFutureRestoreBinaryExecutableButton.setText("Select FutureRestore Binary/Executable...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectFutureRestoreBinaryExecutableButton, gbc);
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, Font.BOLD, 28, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("FutureRestore GUI");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 0, 0);
        mainMenuView.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("(Working title.) by CoocooFroggy");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 10, 0);
        mainMenuView.add(label6, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 1;
        gbc.insets = new Insets(0, 0, 0, 14);
        mainMenuView.add(separator1, gbc);
        logScrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.gridwidth = 6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainMenuView.add(logScrollPane, gbc);
        logTextArea = new JTextArea();
        logTextArea.setColumns(0);
        logTextArea.setEditable(false);
        Font logTextAreaFont = this.$$$getFont$$$("Andale Mono", -1, -1, logTextArea.getFont());
        if (logTextAreaFont != null) logTextArea.setFont(logTextAreaFont);
        logTextArea.setLineWrap(true);
        logTextArea.setRows(30);
        logTextArea.setText("");
        logTextArea.setWrapStyleWord(true);
        logScrollPane.setViewportView(logTextArea);
        selectBlobFileButton = new JButton();
        selectBlobFileButton.setHideActionText(false);
        selectBlobFileButton.setHorizontalAlignment(0);
        selectBlobFileButton.setText("Select Blob File...");
        selectBlobFileButton.setVerticalAlignment(0);
        selectBlobFileButton.setVerticalTextPosition(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectBlobFileButton, gbc);
        basebandComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Latest Baseband");
        defaultComboBoxModel1.addElement("Manual Baseband");
        defaultComboBoxModel1.addElement("No Baseband");
        basebandComboBox.setModel(defaultComboBoxModel1);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(basebandComboBox, gbc);
        basebandTextField = new JTextField();
        basebandTextField.setEditable(false);
        basebandTextField.setText("✓ (No file)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(basebandTextField, gbc);
        sepComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Latest SEP");
        defaultComboBoxModel2.addElement("Manual SEP");
        sepComboBox.setModel(defaultComboBoxModel2);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(sepComboBox, gbc);
        sepTextField = new JTextField();
        sepTextField.setEditable(false);
        sepTextField.setText("✓ (No file)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(sepTextField, gbc);
        final JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 1;
        mainMenuView.add(separator2, gbc);
        final JSeparator separator3 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 1;
        mainMenuView.add(separator3, gbc);
        final JSeparator separator4 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 1;
        mainMenuView.add(separator4, gbc);
        waitWCheckBox = new JCheckBox();
        waitWCheckBox.setText("Wait (-w)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(waitWCheckBox, gbc);
        debugDCheckBox = new JCheckBox();
        debugDCheckBox.setSelected(true);
        debugDCheckBox.setText("Debug (-d)");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(debugDCheckBox, gbc);
        selectBuildManifestButton = new JButton();
        selectBuildManifestButton.setEnabled(false);
        selectBuildManifestButton.setText("Select BuildManifest...");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        mainMenuView.add(selectBuildManifestButton, gbc);
        updateUCheckBox = new JCheckBox();
        updateUCheckBox.setText("Update (-u)");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(updateUCheckBox, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Arguments");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(label7, gbc);
        final JSeparator separator5 = new JSeparator();
        separator5.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 1;
        mainMenuView.add(separator5, gbc);
        startFutureRestoreButton = new JButton();
        Font startFutureRestoreButtonFont = this.$$$getFont$$$(null, Font.BOLD, 16, startFutureRestoreButton.getFont());
        if (startFutureRestoreButtonFont != null) startFutureRestoreButton.setFont(startFutureRestoreButtonFont);
        startFutureRestoreButton.setText("Start FutureRestore");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(startFutureRestoreButton, gbc);
        exitRecoveryButton = new JButton();
        exitRecoveryButton.setText("Exit Recovery");
        exitRecoveryButton.setVerticalAlignment(0);
        exitRecoveryButton.setVerticalTextPosition(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.BOTH;
        mainMenuView.add(exitRecoveryButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainMenuView;
    }

}
