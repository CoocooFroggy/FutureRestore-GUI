import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.github.rjeschke.txtmark.Processor;
import com.google.gson.Gson;
import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenu {
    static String futureRestoreGUIVersion = "";

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
    private JProgressBar logProgressBar;
    private JTextField currentTaskTextField;
    private JButton stopFutureRestoreUnsafeButton;
    private JButton downloadFutureRestoreButton;
    private JButton settingsButton;
    private JLabel authorAndVersionLabel;
    private JCheckBox pwndfuCheckBox;
    private JTabbedPane tabbedPane;
    private JButton nextButtonFiles;
    private JButton nextButtonOptions;
    private JCheckBox noIbssCheckBox;
    private JCheckBox setNonceCheckBox;
    private JLabel noIbssLabel;
    private JLabel setNonceLabel;

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

    private boolean optionDebugState = true;
    private boolean optionUpdateState = false;
    private boolean optionWaitState = false;
    private boolean optionPwndfuState = false;
    private boolean optionNoIbssState = false;
    private boolean optionSetNonceState = false;

    public MainMenu() {
        selectFutureRestoreBinaryExecutableButton.addActionListener(e -> Platform.runLater(() -> {
            FRUtils.setEnabled(mainMenuView, false, true);
            // Create a file chooser
            FileChooser futureRestoreFileChooser = new FileChooser();
            // Open dialogue and set the return file
            File file = futureRestoreFileChooser.showOpenDialog(null);

            if (file != null) {
                messageToLog("Set " + file.getAbsolutePath() + " to FutureRestore executable.");
                futureRestoreFilePath = file.getAbsolutePath();
                properties.setProperty("previous_futurerestore", futureRestoreFilePath);
                savePreferences();
                // Set name of button to blob file name
                selectFutureRestoreBinaryExecutableButton.setText("✓ " + file.getName());
            } else
                System.out.println("Cancelled");
            FRUtils.setEnabled(mainMenuView, true, true);
            mainMenuFrame.requestFocus();
        }));
        downloadFutureRestoreButton.addActionListener(event -> {
            final String osName = System.getProperty("os.name").toLowerCase();
            final String osArch = System.getProperty("os.arch").toLowerCase();
            String urlString = null;

            if (osName.contains("mac")) {
                try {
                    Map<String, String> result;
                    if (properties.getProperty("futurerestore_beta").equals("false")) {
                        result = getLatestFrDownload("mac");
                    } else {
                        result = getLatestFrBetaDownload("mac", osArch);
                    }
                    urlString = result.get("link");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainMenuView, "Unable to download FutureRestore.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
            } else if (osName.contains("win")) {
                try {
                    Map<String, String> result;
                    if (properties.getProperty("futurerestore_beta").equals("false")) {
                        result = getLatestFrDownload("win");
                    } else {
                        result = getLatestFrBetaDownload("win", osArch);
                    }
                    urlString = result.get("link");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainMenuView, "Unable to download FutureRestore.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
            } else if (osName.contains("linux")) {
                try {
                    Map<String, String> result;
                    if (properties.getProperty("futurerestore_beta").equals("false")) {
                        result = getLatestFrDownload("linux");
                    } else {
                        result = getLatestFrBetaDownload("linux", osArch);
                    }
                    urlString = result.get("link");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainMenuView, "Unable to download FutureRestore.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
            } else {
                Object[] choices = {"Open link", "Ok"};
                Object defaultChoice = choices[0];

                int response = JOptionPane.showOptionDialog(mainMenuView, "Unknown operating system detected. Please download FutureRestore manually for your operating system.\n" +
                        "https://github.com/m1stadev/futurerestore/releases/latest/", "Download FutureRestore", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);
                if (response == JOptionPane.YES_OPTION) {
                    FRUtils.openWebpage("https://github.com/m1stadev/futurerestore/releases/latest/", this);
                }
            }

            // Pop-up for this error already shown in getLatestFrDownload()
            if (urlString == null)
                return;

            SwingUtilities.invokeLater(() -> {
                currentTaskTextField.setText("Downloading FutureRestore...");
                messageToLog("Downloading FutureRestore...");
            });

            // Download asynchronously
            final String finalUrlString = urlString;
            new Thread(() -> {
                File downloadedFr = downloadFutureRestore(finalUrlString);
                if (downloadedFr == null)
                    return; // We already show an error message in downloadFutureRestore()

                // Now unzip the file
                String homeDirectory = System.getProperty("user.home");
                String finalFrPath = homeDirectory + "/FutureRestoreGUI";
                File futureRestoreExecutable = null;
                try {
                    futureRestoreExecutable = extractFutureRestore(downloadedFr, finalFrPath, osName);
                } catch (IOException exception) {
                    System.out.println("Unable to decompress " + downloadedFr);
                    messageToLog("Unable to decompress " + downloadedFr);
                    exception.printStackTrace();
                }
                // If it fail, set the current task to nothing
                if (futureRestoreExecutable == null) {
                    SwingUtilities.invokeLater(() -> currentTaskTextField.setText(""));
                } else {
                    final File finalFutureRestoreExecutable = futureRestoreExecutable;
                    SwingUtilities.invokeLater(() -> {
                        currentTaskTextField.setText("");
                        messageToLog("Decompressed FutureRestore");
                        futureRestoreFilePath = finalFutureRestoreExecutable.getAbsolutePath();
                        properties.setProperty("previous_futurerestore", futureRestoreFilePath);
                        savePreferences();
                        messageToLog("Set " + finalFutureRestoreExecutable.getAbsolutePath() + " to FutureRestore executable.");
                        // Set name of button to blob file name
                        selectFutureRestoreBinaryExecutableButton.setText("✓ " + finalFutureRestoreExecutable.getName());
                    });
                }
            }).start();
        });

        selectBlobFileButton.addActionListener(e -> Platform.runLater(() -> {
            FRUtils.setEnabled(mainMenuView, false, true);
            // Create a file chooser
            FileChooser blobFileChooser = new FileChooser();
            // Set filter
            FileChooser.ExtensionFilter fileFilter =
                    new FileChooser.ExtensionFilter(
                            "Blob File (SHSH2, SHSH)", "*.shsh2", "*.shsh");
            blobFileChooser.getExtensionFilters().add(fileFilter);
            // Open dialogue and set the return file
            File file = blobFileChooser.showOpenDialog(null);

            if (file != null) {
                messageToLog("Set " + file.getAbsolutePath() + " to SHSH blob.");
                blobFilePath = file.getAbsolutePath();
                blobName = file.getName();
                selectBlobFileButton.setText("✓ " + file.getName());
            } else
                System.out.println("Cancelled");
            FRUtils.setEnabled(mainMenuView, true, true);
            mainMenuFrame.requestFocus();
        }));

        selectTargetIPSWFileButton.addActionListener(e -> Platform.runLater(() -> {
            FRUtils.setEnabled(mainMenuView, false, true);
            // Create a file chooser
            FileChooser targetIpswFileChooser = new FileChooser();
            // Set filter
            FileChooser.ExtensionFilter fileFilter =
                    new FileChooser.ExtensionFilter(
                            "iOS Firmware (IPSW)", "*.ipsw");
            targetIpswFileChooser.getExtensionFilters().add(fileFilter);
            // Open dialogue and set the return file
            File file = targetIpswFileChooser.showOpenDialog(null);

            if (file != null) {
                messageToLog("Set " + file.getAbsolutePath() + " to target IPSW.");
                targetIpswPath = file.getAbsolutePath();
                targetIpswName = file.getName();
                // Set name of button to ipsw file name
                selectTargetIPSWFileButton.setText("✓ " + file.getName());
            } else
                System.out.println("Cancelled");
            FRUtils.setEnabled(mainMenuView, true, true);
            mainMenuFrame.requestFocus();
        }));

        selectBuildManifestButton.addActionListener(e -> Platform.runLater(() -> {
            FRUtils.setEnabled(mainMenuView, false, true);
            // Create a file chooser
            FileChooser targetIpswFileChooser = new FileChooser();
            // Set filter
            FileChooser.ExtensionFilter fileFilter =
                    new FileChooser.ExtensionFilter(
                            "BuildManifest (PList)", "*.plist");
            targetIpswFileChooser.getExtensionFilters().add(fileFilter);
            // Open dialogue and set the return file
            File file = targetIpswFileChooser.showOpenDialog(null);

            if (file != null) {
                messageToLog("Set " + file.getAbsolutePath() + " to BuildManifest.");
                buildManifestPath = file.getAbsolutePath();
                // Set name of button to ipsw file name
                selectBuildManifestButton.setText("✓ " + file.getName());
            } else
                System.out.println("Cancelled");
            FRUtils.setEnabled(mainMenuView, true, true);
            mainMenuFrame.requestFocus();
        }));

        ActionListener optionsListener = e -> {
            optionDebugState = debugDCheckBox.isSelected();
            optionUpdateState = updateUCheckBox.isSelected();
            optionWaitState = waitWCheckBox.isSelected();
            optionPwndfuState = pwndfuCheckBox.isSelected();
            optionNoIbssState = noIbssCheckBox.isSelected();
            optionSetNonceState = setNonceCheckBox.isSelected();

            if (optionPwndfuState) {
                noIbssCheckBox.setEnabled(true);
                noIbssLabel.setEnabled(true);
                setNonceCheckBox.setEnabled(true);
                setNonceLabel.setEnabled(true);
            } else {
                noIbssCheckBox.setSelected(false);
                noIbssCheckBox.setEnabled(false);
                noIbssLabel.setEnabled(false);
                setNonceCheckBox.setSelected(false);
                setNonceCheckBox.setEnabled(false);
                setNonceLabel.setEnabled(false);

                // Since we turn off the switches for pwndfu required items, also turn them off internally
                optionNoIbssState = false;
                optionSetNonceState = false;
            }
        };
        debugDCheckBox.addActionListener(optionsListener);
        updateUCheckBox.addActionListener(optionsListener);
        waitWCheckBox.addActionListener(optionsListener);

        pwndfuCheckBox.addActionListener(optionsListener);
        noIbssCheckBox.addActionListener(optionsListener);
        setNonceCheckBox.addActionListener(optionsListener);

        startFutureRestoreButton.addActionListener(e -> {
            // If FutureRestore is already running, just disable ourselves
            if (FutureRestoreWorker.futureRestoreProcess != null && FutureRestoreWorker.futureRestoreProcess.isAlive()) {
                SwingUtilities.invokeLater(() -> {
                    startFutureRestoreButton.setEnabled(false);
                    // Potentially make the button say unsafe again
                });
                return;
            }

            // Ensure they have FutureRestore selected
            if (futureRestoreFilePath == null) {
                JOptionPane.showMessageDialog(mainMenuView, "Please select a FutureRestore executable.", "No FutureRestore Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Ensure they actually selected a blob, IPSW, and buildmanifest if needed
            if (blobFilePath == null) {
                JOptionPane.showMessageDialog(mainMenuView, "Select a blob file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (targetIpswPath == null) {
                JOptionPane.showMessageDialog(mainMenuView, "Select an IPSW file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bbState.equals("manual") || sepState.equals("manual")) {
                if (buildManifestPath == null) {
                    JOptionPane.showMessageDialog(mainMenuView, "Select a BuildManifest file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // If blob name has a build number in it
            Pattern blobPattern = Pattern.compile("(?<=[_-])[A-Z0-9]{5,10}[a-z]?(?=[_-])");
            Matcher blobMatcher = blobPattern.matcher(blobName);
            String blobBuild = null;
            if (blobMatcher.find()) {
//                System.out.println("Blob build is " + blobMatcher.group(0));
                blobBuild = blobMatcher.group(0);
            }

            // If IPSW has a build name in it
            Pattern ipswPattern = Pattern.compile("(?<=[_-])[A-Z0-9]{5,10}[a-z]?(?=[_-])");
            Matcher ipswMatcher = ipswPattern.matcher(targetIpswName);
            String targetIpswBuild = null;
            if (ipswMatcher.find()) {
//                System.out.println("IPSW build is " + ipswMatcher.group(0));
                targetIpswBuild = ipswMatcher.group(0);
            }

            // If they're different
            if (blobBuild != null && targetIpswBuild != null)
                if (!blobBuild.equals(targetIpswBuild)) {
                    JOptionPane.showMessageDialog(mainMenuView, "The build in your blob name, " + blobBuild + ", does not match the one in the IPSW name, " + targetIpswBuild + ". Ensure you have the right blob and IPSW before continuing.", "Warning", JOptionPane.WARNING_MESSAGE);
                }

            // Build their final command
            ArrayList<String> allArgs = new ArrayList<>();

            allArgs.add("--apticket");
            allArgs.add(blobFilePath);

            if (optionDebugState)
                allArgs.add("--debug");
            if (optionUpdateState)
                allArgs.add("--update");
            if (optionWaitState)
                allArgs.add("--wait");
            if (optionPwndfuState)
                allArgs.add("--use-pwndfu");
            if (optionNoIbssState)
                allArgs.add("--no-ibss");
            if (optionSetNonceState) {
                allArgs.add("--set-nonce");
            }

            switch (sepState) {
                case "latest": {
                    allArgs.add("--latest-sep");
                    break;
                }
                case "manual": {
                    allArgs.add("--sep");
                    allArgs.add(sepFilePath);
                    allArgs.add("--sep-manifest");
                    allArgs.add(buildManifestPath);
                    break;
                }
//                // No SEP is just no arg
//                case "none":
//                    break;
            }

            switch (bbState) {
                case "latest":
                    allArgs.add("--latest-baseband");
                    break;
                case "manual":
                    allArgs.add("--baseband");
                    allArgs.add(basebandFilePath);
                    allArgs.add("--baseband-manifest");
                    allArgs.add(buildManifestPath);
                    break;
                case "none":
                    allArgs.add("--no-baseband");
                    break;
            }

            allArgs.add(targetIpswPath);

            // Run command
            runCommand(allArgs, true);
        });
        exitRecoveryButton.addActionListener(e -> {
            // If they haven't selected a futurerestore yet
            if (futureRestoreFilePath == null) {
                JOptionPane.showMessageDialog(mainMenuView, "Please select a FutureRestore executable in order to exit recovery.", "No FutureRestore Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            runCommand(new ArrayList<>(List.of("--exit-recovery")), false);
        });

        basebandComboBox.addActionListener(e -> {
            switch (basebandComboBox.getSelectedItem().toString()) {
                case "Latest Baseband": {
                    bbState = "latest";
                    basebandTextField.setText("✓ (No file)");
                    if (sepState.equals("latest") || sepState.equals("none"))
                        selectBuildManifestButton.setEnabled(false);
                    break;
                }
                case "Manual Baseband": {
                    Platform.runLater(() -> {
                        FRUtils.setEnabled(mainMenuView, false, true);
                        if (chooseBbfw()) {
                            bbState = "manual";
                            selectBuildManifestButton.setEnabled(true);
                        } else {
                            bbState = "latest";
                            basebandComboBox.setSelectedItem("Latest Baseband");
                            if (sepState.equals("latest") || sepState.equals("none"))
                                selectBuildManifestButton.setEnabled(false);
                        }
                        FRUtils.setEnabled(mainMenuView, true, true);
                    });
                    break;
                }
                case "No Baseband": {
                    bbState = "none";
                    basebandTextField.setText("✓ (No file)");
                    if (sepState.equals("latest") || sepState.equals("none"))
                        selectBuildManifestButton.setEnabled(false);
                    break;
                }
            }
        });
        sepComboBox.addActionListener(e -> {
            switch (sepComboBox.getSelectedItem().toString()) {
                case "Latest SEP": {
                    sepState = "latest";
                    sepTextField.setText("✓ (No file)");
                    if (bbState.equals("latest") || bbState.equals("none"))
                        selectBuildManifestButton.setEnabled(false);
                    break;
                }
                case "Manual SEP": {
                    Platform.runLater(() -> {
                        FRUtils.setEnabled(mainMenuView, false, true);
                        if (chooseSep()) {
                            sepState = "manual";
                            selectBuildManifestButton.setEnabled(true);
                        } else {
                            sepState = "latest";
                            sepComboBox.setSelectedItem("Latest SEP");
                            if (bbState.equals("latest") || bbState.equals("none"))
                                selectBuildManifestButton.setEnabled(false);
                        }
                        FRUtils.setEnabled(mainMenuView, true, true);
                    });
                    break;
                }
                case "No SEP": {
                    sepState = "none";
                    sepTextField.setText("✓ (No file)");
                    if (bbState.equals("latest") || bbState.equals("none"))
                        selectBuildManifestButton.setEnabled(false);
                    break;
                }
            }
        });
        stopFutureRestoreUnsafeButton.addActionListener(e -> {
            Process futureRestoreProcess = FutureRestoreWorker.futureRestoreProcess;

            boolean killed = false;
            if (FutureRestoreWorker.futureRestoreProcess != null && FutureRestoreWorker.futureRestoreProcess.isAlive()) {
                int response = JOptionPane.showConfirmDialog(mainMenuView, "Are you sure you want to stop FutureRestore? This is considered unsafe if the device is currently restoring.", "Stop FutureRestore?", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    futureRestoreProcess.destroy();
                    messageToLog("FutureRestore process killed.");
                    try {
                        FutureRestoreWorker.uploadLogsIfNecessary();
                    } catch (IOException ex) {
                        messageToLog("Unable to upload logs :(");
                        ex.printStackTrace();
                    }
                    killed = true;
                }
            } else {
                killed = true;
            }

            if (killed) {
                SwingUtilities.invokeLater(() -> {
                    startFutureRestoreButton.setEnabled(true);
                    stopFutureRestoreUnsafeButton.setText("Stop FutureRestore");
                    currentTaskTextField.setText("");
                });
            }
        });

        settingsButton.addActionListener(e -> settingsMenuFrame.setVisible(true));

        ActionListener nextButtonListener = e -> tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
        nextButtonFiles.addActionListener(nextButtonListener);
        nextButtonOptions.addActionListener(nextButtonListener);
    }

    public static final Properties properties = new Properties();

    static JFrame mainMenuFrame;
    static JFrame settingsMenuFrame;

    public static void main() {
        // Load and init prefs
        initializePreferences();

        boolean isDarkThemeUsed = false;
        switch (properties.getProperty("theme_preference")) {
            case "auto": {
                final OsThemeDetector detector = OsThemeDetector.getDetector();
                isDarkThemeUsed = detector.isDark();
                // Must set L&F before we create instance of MainMenu
                if (isDarkThemeUsed) {
                    FlatDarculaLaf.setup();
                } else {
//                    //Only set if not Mac
//                    if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                    FlatIntelliJLaf.setup();
                }
                break;
            }
            case "light": {
//                // Only set if not Mac
//                if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                FlatIntelliJLaf.setup();
                break;
            }
            case "dark": {
                isDarkThemeUsed = true;
                FlatDarculaLaf.setup();
                break;
            }

        }

        final boolean finalIsDarkThemeUsed = isDarkThemeUsed;
        SwingUtilities.invokeLater(() -> {
            mainMenuFrame = new JFrame("FutureRestore GUI");
            settingsMenuFrame = new JFrame("Settings");

            MainMenu mainMenuInstance = new MainMenu();
            SettingsMenu settingsMenuInstance = new SettingsMenu();

            // Auto scroll log
            new SmartScroller(mainMenuInstance.logScrollPane, SmartScroller.VERTICAL, SmartScroller.END);

            // For JavaFX
            new JFXPanel();

            // Main Menu
            mainMenuFrame.setContentPane(mainMenuInstance.mainMenuView);
            // End program on close
            mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainMenuFrame.pack();
            // Centers it on screen
            mainMenuFrame.setLocationRelativeTo(null);

            // init SettingsMenu
            SettingsMenu.initializeSettingsMenu(settingsMenuInstance);

            // Settings Menu
            settingsMenuFrame.setContentPane(settingsMenuInstance.settingsMenuView);
            // Set settings frame to invisible on close
            settingsMenuFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            settingsMenuFrame.pack();
            // Centers it on screen
            settingsMenuFrame.setLocationRelativeTo(null);

            // Prepare for dark mode
            if (finalIsDarkThemeUsed)
                turnDark(mainMenuInstance);
            else {
                // Custom light UI setup
                mainMenuInstance.startFutureRestoreButton.setBackground(new Color(150, 200, 255));
                mainMenuInstance.nextButtonFiles.setBackground(new Color(150, 200, 255));
                mainMenuInstance.nextButtonOptions.setBackground(new Color(150, 200, 255));
            }


            // Tell them if they are or are not sharing logs
            if (properties.getProperty("upload_logs").equals("true")) {
                mainMenuInstance.messageToLog("Help improve FutureRestore by sharing logs: Enabled");
            } else {
                mainMenuInstance.messageToLog("Help improve FutureRestore by sharing logs: Disabled");
            }

            // Set text for version
            mainMenuInstance.authorAndVersionLabel.setText("by CoocooFroggy — v" + futureRestoreGUIVersion);

            // Shows the view
            mainMenuFrame.setVisible(true);

            // Only if they have the setting enabled, check for updates
            if (properties.getProperty("check_updates").equals("true")) {
                System.out.println("Checking for FutureRestore GUI updates in the background...");
                mainMenuInstance.messageToLog("Checking for FutureRestore GUI updates in the background...");
                alertIfNewerFRGUIAvailable(mainMenuInstance, futureRestoreGUIVersion);
            }

            // If they previously downloaded FR, set it
            String previousFRPath = properties.getProperty("previous_futurerestore");
            if (previousFRPath != null) {
                mainMenuInstance.futureRestoreFilePath = previousFRPath;
                mainMenuInstance.messageToLog("Set previous FutureRestore download, " + previousFRPath + ", to FutureRestore executable.");
                // Set name of button to FR file name
                mainMenuInstance.selectFutureRestoreBinaryExecutableButton.setText("✓ " + new File(previousFRPath).getName());
            }

            /*String homeDirectory = System.getProperty("user.home");
            String frPath = homeDirectory + "/FutureRestoreGUI/";
            File extracted = new File(frPath + "extracted/");

            // If ~/FRGUI/extracted/ exists
            if (extracted.exists()) {
                //If a file exists in there, set it
                File[] filesInExtracted = extracted.listFiles();
                if (filesInExtracted != null && filesInExtracted.length > 0) {
                    File frExecutable = filesInExtracted[0];
                    if (frExecutable.exists()) {
                        mainMenuInstance.futureRestoreFilePath = frExecutable.getAbsolutePath();
                        mainMenuInstance.messageToLog("Set previous FutureRestore download, " + frExecutable.getAbsolutePath() + ", to FutureRestore executable.");
                        // Set name of button to FR file name
                        mainMenuInstance.selectFutureRestoreBinaryExecutableButton.setText("✓ " + frExecutable.getName());
                    }
                }
            }*/

        });
    }

    /* UTILITIES */

    static void turnDark(MainMenu mainMenuInstance) {
        JPanel mainMenuView = mainMenuInstance.mainMenuView;
        JTextArea logTextArea = mainMenuInstance.logTextArea;
        JScrollPane logScrollPane = mainMenuInstance.logScrollPane;

        mainMenuView.setBackground(new Color(40, 40, 40));
        logTextArea.setBackground(new Color(20, 20, 20));
        logTextArea.setForeground(new Color(200, 200, 200));
        logScrollPane.setBorder(null);

        makeComponentsDark(mainMenuView, mainMenuInstance);
    }

    public static void makeComponentsDark(Component c, MainMenu mainMenuInstance) {

        if (c instanceof JLabel) {
            c.setForeground(new Color(200, 200, 200));
        }

        if (c instanceof JButton) {
            c.setBackground(new Color(60, 60, 60));
            c.setForeground(new Color(200, 200, 200));
            if (c == mainMenuInstance.getStartFutureRestoreButton())
                // Make start button blue
                c.setBackground(new Color(38, 85, 163));
            else if (c == mainMenuInstance.getNextButtonFiles() || c == mainMenuInstance.getNextButtonOptions()) {
                // Make next buttons blue
                c.setBackground(new Color(38, 85, 163));
            }
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

        if (c instanceof JPanel) {
            c.setBackground(new Color(40, 40, 40));
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                makeComponentsDark(child, mainMenuInstance);
            }
        }
    }

    boolean chooseBbfw() {
        // Create a file chooser
        FileChooser targetIpswFileChooser = new FileChooser();
        // Set filter
        FileChooser.ExtensionFilter fileFilter =
                new FileChooser.ExtensionFilter(
                        "Baseband Firmware (BBFW)", "*.bbfw");
        targetIpswFileChooser.getExtensionFilters().add(fileFilter);
        // Open dialogue and set the return file
        File file = targetIpswFileChooser.showOpenDialog(null);
        mainMenuFrame.requestFocus();

        if (file != null) {
            messageToLog("Set " + file.getAbsolutePath() + " to baseband firmware.");
            basebandTextField.setText("✓ " + file.getName());
            basebandFilePath = file.getAbsolutePath();
            return true;
        } else {
            System.out.println("Cancelled");
            return false;
        }
    }

    boolean chooseSep() {
        // Create a file chooser
        FileChooser targetIpswFileChooser = new FileChooser();
        // Set filter
        FileChooser.ExtensionFilter fileFilter =
                new FileChooser.ExtensionFilter(
                        "SEP (IM4P)", "*.im4p");
        targetIpswFileChooser.getExtensionFilters().add(fileFilter);
        // Open dialogue and set the return file
        File file = targetIpswFileChooser.showOpenDialog(null);
        mainMenuFrame.requestFocus();

        if (file != null) {
            messageToLog("Set " + file.getAbsolutePath() + " to SEP IM4P.");
            sepTextField.setText("✓ " + file.getName());
            sepFilePath = file.getAbsolutePath();
            return true;
        } else {
            System.out.println("Cancelled");
            return false;
        }
    }

    void runCommand(ArrayList<String> allArgs, boolean fullFR) {

        // Preview command if necessary. If returned false, then they clicked copy only, so don't run command.
        if (!previewCommand(allArgs)) return;

        // If they're running an actual restore
        if (fullFR) {
            // Set current task to starting...
            currentTaskTextField.setText("Starting FutureRestore...");

            // Disable interaction
            startFutureRestoreButton.setEnabled(false);
            stopFutureRestoreUnsafeButton.setText("Stop FutureRestore (Unsafe)");

            // Not necessary if we don't check GitHub for version as well
            // Check FutureRestore version
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(futureRestoreFilePath);
                /*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Pattern pattern = Pattern.compile("Version: (.*)");
                String s;
                Only check the first 5 lines
                for (int i = 0; i < 5; i++) {
                    s = bufferedReader.readLine();
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find());
                        version = matcher.group(1);
                }*/
            } catch (IOException ioException) {
                System.out.println("Unable to check FutureRestore version.");
                JOptionPane.showMessageDialog(mainMenuView, "Unable to run FutureRestore. Ensure you selected the correct FutureRestore executable.", "Error", JOptionPane.ERROR_MESSAGE);
                ioException.printStackTrace();
                startFutureRestoreButton.setEnabled(true);
                currentTaskTextField.setText("");
                stopFutureRestoreUnsafeButton.setText("Stop FutureRestore");
                return;
            }

            /*
            // Good idea at first but got kinda annoying every time

            // Ask if they want to check for latest
            if (version == null) {
                JOptionPane.showMessageDialog(mainMenuView, "Unable to check FutureRestore version from selected executable. Manually ensure you have the latest version.", "Warning", JOptionPane.ERROR_MESSAGE);
            } else {
                int response = JOptionPane.showConfirmDialog(mainMenuView, "Your FutureRestore's version: v" + version + ". Would you like to ensure this is the latest version on m1sta's fork?", "FutureRestore Version", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        String latestVersion = getLatestFutureRestore();
                        if (version.equals(latestVersion)) {
                            JOptionPane.showMessageDialog(mainMenuView, "You're up to date! The latest version is v" + latestVersion + ".");
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
            */
        }

        System.out.println("Starting FutureRestore...");
        messageToLog("Make sure to hit \"trust\" on your device if prompted!");

        new Thread(() -> {
            try {
                FutureRestoreWorker.runFutureRestore(futureRestoreFilePath, allArgs, mainMenuView, logTextArea, logProgressBar, currentTaskTextField, startFutureRestoreButton, stopFutureRestoreUnsafeButton);
            } catch (IOException e) {
                System.out.println("Unable to run FutureRestore.");
                startFutureRestoreButton.setEnabled(true);
                stopFutureRestoreUnsafeButton.setText("Stop FutureRestore");
                e.printStackTrace();
            }
        }).start();

    }

    int lineNumber = 1;

    void messageToLog(String string) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append("[" + lineNumber + "] " + string + "\n");
            lineNumber++;
        });
    }

// --Commented out by Inspection START (3/2/22, 10:01 PM):
//    String getLatestFrTag() throws IOException {
//        // Vars
//        Map<String, Object> newestRelease = getLatestFrGithub();
//        String newestTag = (String) newestRelease.get("tag_name");
//        System.out.println("Newest version: " + newestTag);
//
//        return newestTag;
//    }
// --Commented out by Inspection STOP (3/2/22, 10:01 PM)

    Map<String, String> getLatestFrDownload(String operatingSystem) throws IOException {
        // operatingSystem = "mac", "windows", "linux"

        Map<String, String> linkNameMap = new HashMap<>();

        Map<String, Object> newestRelease = getLatestFrGithub();
        ArrayList<Map<String, Object>> assets = (ArrayList<Map<String, Object>>) newestRelease.get("assets");
        // Get asset for our operating system
        for (Map<String, Object> asset : assets) {
            String assetName = ((String) asset.get("name"));
            // Linux can be linux or ubuntu in filename
            if (operatingSystem.equals("linux")) {
                if (assetName.toLowerCase().contains("linux") || assetName.toLowerCase().contains("ubuntu")) {
                    linkNameMap.put("link", (String) asset.get("browser_download_url"));
                    linkNameMap.put("name", assetName);
                    return linkNameMap;
                }
            }
            // All other operating systems
            else if (assetName.toLowerCase().contains(operatingSystem)) {
                linkNameMap.put("link", (String) asset.get("browser_download_url"));
                linkNameMap.put("name", assetName);
                return linkNameMap;
            }
        }
        // Pop-up saying "no binaries for your OS available"
        noFrForOSPopup("No FutureRestore asset found for your operating system. Check releases to see if there's one available.\n", "https://github.com/m1stadev/futurerestore/releases/latest/");
        return linkNameMap;
    }

    Map<String, String> getLatestFrBetaDownload(String operatingSystem, String architecture) throws IOException {
        // operatingSystem = "mac", "windows", "linux"

        Map<String, String> linkNameMap = new HashMap<>();

        String content = getRequestUrl("https://api.github.com/repos/m1stadev/futurerestore/actions/artifacts");

        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(content, Map.class);
        ArrayList<Map<String, Object>> artifacts = (ArrayList<Map<String, Object>>) result.get("artifacts");

        // Get asset for our operating system
        for (Map<String, Object> artifact : artifacts) {
            String assetName = ((String) artifact.get("name"));
            // Look for our OS, architecture, and debug binary
            if (assetName.toLowerCase().contains(operatingSystem)
                    && assetName.toLowerCase().contains(architecture)
                    && assetName.toLowerCase().contains("debug")) {
                linkNameMap.put("link", (String) artifact.get("archive_download_url"));
                linkNameMap.put("name", assetName);
                return linkNameMap;
            }
        }

        // Pop-up saying "no binaries for your OS available"
        noFrForOSPopup("No FutureRestore beta asset found for your operating system.\n" +
                "Try a release version instead, or manually download a beta for your OS.\n", "https://github.com/m1stadev/futurerestore/actions");
        return linkNameMap;
    }

    private void noFrForOSPopup(String message, String urlString) {
        Object[] choices = {"Open link", "Ok"};
        Object defaultChoice = choices[0];

        int response = JOptionPane.showOptionDialog(mainMenuView, message +
                urlString, "Download FutureRestore", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);
        if (response == JOptionPane.YES_OPTION) {
            FRUtils.openWebpage(urlString, this);
        }
    }

    public static String getRequestUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");

        // Auth for higher rate limit
        FRUtils.githubAuthorizeWithAccount(con);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    private Map<String, Object> getLatestFrGithub() throws IOException {
        String content = getRequestUrl("https://api.github.com/repos/m1stadev/futurerestore/releases");

        Gson gson = new Gson();
        ArrayList<Map<String, Object>> result = gson.fromJson(content, ArrayList.class);
        return result.get(0); // Newest release
    }

    File downloadFutureRestore(String urlString) {
        String homeDirectory = System.getProperty("user.home");
        File frGuiDir = new File(homeDirectory + "/FutureRestoreGUI");

        // Make directory to store files
        if (!frGuiDir.exists())
            frGuiDir.mkdir();

        String frguiDirPath = frGuiDir.getPath();

        // Clean the area first
        File destinationDir = new File(frguiDirPath + "/extracted");
        if (destinationDir.exists()) {
            File[] filesList = destinationDir.listFiles();
            if (filesList != null && filesList.length > 0) {
                System.out.println("More than 0 files in dir. Cleaning");
                try {
                    FileUtils.cleanDirectory(destinationDir);
                } catch (IOException e) {
                    System.err.println("Unable to delete all existing files in extracted directory. Aborting.");
                    messageToLog("Unable to delete all existing files in extracted directory. Aborting.");
                    e.printStackTrace();
                    currentTaskTextField.setText("");
                    return null;
                }
            }
        }

        File downloadedFr;
        try {
            downloadedFr = FRUtils.downloadFileWithProgress(urlString, frguiDirPath, this.getLogProgressBar());
            if (downloadedFr == null) {
                System.err.println("Unable to download FutureRestore. Aborting.");
                messageToLog("Unable to download FutureRestore. Aborting.");
                SwingUtilities.invokeLater(() -> {
                    currentTaskTextField.setText("");
                    logProgressBar.setValue(0);
                });
                return null;
            }

            SwingUtilities.invokeLater(() -> {
                currentTaskTextField.setText("");
                logProgressBar.setValue(0);
                messageToLog("FutureRestore finished downloading.");
            });
        } catch (IOException e) {
            System.err.println("Unable to download FutureRestore.");
            messageToLog("Unable to download FutureRestore.");
            e.printStackTrace();
            return null;
        }
        return downloadedFr;
    }

    File extractFutureRestore(File fileToExtract, String frguiDirPath, String operatingSystem) throws IOException {
        SwingUtilities.invokeLater(() -> {
            currentTaskTextField.setText("Decompressing FutureRestore...");
            messageToLog("Decompressing FutureRestore...");
        });

        File destinationDir = new File(frguiDirPath + "/extracted");

        String downloadedFileExtension = FilenameUtils.getExtension(fileToExtract.getName());
        switch (downloadedFileExtension) {
            case "zip": {
                Archiver archiver = ArchiverFactory.createArchiver("zip");
                archiver.extract(fileToExtract, destinationDir);
                break;
            }
            case "xz": {
                Archiver archiver = ArchiverFactory.createArchiver("tar", "xz");
                archiver.extract(fileToExtract, destinationDir);
                break;
            }
            case "exe":
            case "": {
                FileUtils.copyFileToDirectory(fileToExtract, destinationDir);
                break;
            }
            default: {
                System.out.println("Cannot decompress, unknown file format :(");
                messageToLog("Cannot decompress, unknown file format :(");
                return null;
            }
        }

        deleteFile(fileToExtract);

        // Actions artifacts (beta FR) are in a .zip then in a .tar.xz. Extract again if we need to
        File[] files = destinationDir.listFiles();
        if (files == null) return null;
        File unzippedFile = files[0];
        String unzippedExtension = FilenameUtils.getExtension(unzippedFile.getName());
        if (unzippedExtension.equals("xz") || unzippedExtension.equals("zip")) {
            // Move the archive from /FRGUI/extracted to /FRGUI
            FileUtils.moveFileToDirectory(unzippedFile, new File(frguiDirPath), false);
            // Declare this file
            File nestedArchive = new File(frguiDirPath + "/" + unzippedFile.getName());
            // Extract the new one (run this method with it) and return the extracted file
            return extractFutureRestore(nestedArchive, frguiDirPath, operatingSystem);
        }

        File futureRestoreExecutable = files[0];

        if (futureRestoreExecutable == null) {
            System.out.println("Unable to decompress " + fileToExtract);
            messageToLog("Unable to decompress " + fileToExtract);
            return null;
        }

        // Only run on MacOS and Linux
        if (operatingSystem.contains("mac") || operatingSystem.contains("linux")) {
            // Make FutureRestore executable
            Process process;
            try {
                process = Runtime.getRuntime().exec("chmod +x " + futureRestoreExecutable);
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                System.out.println("Unable to make FutureRestore executable.");
                messageToLog("Unable to make FutureRestore executable.");
                e.printStackTrace();
            }
        }

        return futureRestoreExecutable;

    }

    static void initializePreferences() {
        // Read preferences
        String homeDirectory = System.getProperty("user.home");
        File frGuiDirectory = new File(homeDirectory + "/FutureRestoreGUI/");
        if (!frGuiDirectory.exists())
            frGuiDirectory.mkdir();
        File prefsFile = new File(homeDirectory + "/FutureRestoreGUI/preferences.properties");

        // Init
        if (!prefsFile.exists()) {
            try {
                prefsFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Unable to initialize preferences.");
                e.printStackTrace();
                return;
            }
        }

        try {
            properties.load(new FileReader(prefsFile));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (properties.getProperty("upload_logs") == null)
            properties.setProperty("upload_logs", "true");
        if (properties.getProperty("discord_name") == null)
            properties.setProperty("discord_name", "None");
        if (properties.getProperty("preview_command") == null)
            properties.setProperty("preview_command", "false");
        if (properties.getProperty("check_updates") == null)
            properties.setProperty("check_updates", "true");
        if (properties.getProperty("futurerestore_beta") == null)
            properties.setProperty("futurerestore_beta", "false");
        if (properties.getProperty("theme_preference") == null)
            properties.setProperty("theme_preference", "auto");

        savePreferences();
    }

    static void savePreferences() {
        String homeDirectory = System.getProperty("user.home");
        File prefsFile = new File(homeDirectory + "/FutureRestoreGUI/preferences.properties");

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(prefsFile);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to create output stream for preferences.");
            e.printStackTrace();
            return;
        }

        try {
            properties.store(outputStream, "Preferences for FutureRestore GUI");
        } catch (IOException e) {
            System.out.println("Unable to save preferences.");
            e.printStackTrace();
            return;
        }
    }

    boolean previewCommand(ArrayList<String> allArgs) {
        // If they want to preview command
        if (properties.getProperty("preview_command").equals("true")) {
            StringBuilder commandStringBuilder = new StringBuilder();
            // Surround FutureRestore's path in quotes
            commandStringBuilder.append("\"").append(futureRestoreFilePath).append("\" ");
            for (String arg : allArgs) {
                if (!arg.startsWith("-")) {
                    // If it's an argument that doesn't start with -, (so a file), surround it in quotes.
                    commandStringBuilder.append("\"").append(arg).append("\" ");
                    continue;
                }
                commandStringBuilder.append(arg).append(" ");
            }

            // Build the preview area
            JTextArea commandPreviewTextArea = new JTextArea();
            commandPreviewTextArea.setEditable(false);
            commandPreviewTextArea.setLineWrap(true);

            String finalCommand = commandStringBuilder.toString();
            commandPreviewTextArea.setText(finalCommand);
            JScrollPane scrollPane = new JScrollPane(commandPreviewTextArea);
            scrollPane.setPreferredSize(new Dimension(300, 125));

            Object[] choices = {"Copy command only", "Copy command and run", "Only run"};
            int response = JOptionPane.showOptionDialog(mainMenuView, scrollPane, "Command Preview", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[1]);

            StringSelection stringSelection = new StringSelection(finalCommand);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch (response) {
                case 0: {
                    // Copy command only
                    clipboard.setContents(stringSelection, null);
                    messageToLog("Copied \"" + finalCommand + "\" to clipboard.");
                    // Return false, don't continue running
                    return false;
                }
                case 1: {
                    // Copy command and run
                    clipboard.setContents(stringSelection, null);
                    messageToLog("Copied \"" + finalCommand + "\" to clipboard.");
                    // Return true, continue running
                    return true;
                }
                case 2: {
                    // Run only
                    // Return true, continue running
                    return true;
                }
                case JOptionPane.CLOSED_OPTION: {
                    // If they close the popup just don't do anything
                    return false;
                }
            }
        }
        // Return true, continue running since preview command is disabled
        return true;
    }

    static void alertIfNewerFRGUIAvailable(MainMenu mainMenuInstance, String currentFRGUIVersion) {
        new Thread(() -> {
            try {
                String content = getRequestUrl("https://api.github.com/repos/CoocooFroggy/FutureRestore-GUI/releases");

                Gson gson = new Gson();
                ArrayList<Map<String, Object>> result = gson.fromJson(content, ArrayList.class);
                Map<String, Object> newestRelease = result.get(0);
                String newestTag = (String) newestRelease.get("tag_name");
                System.out.println("Newest FRGUI version: " + newestTag);

                // If user is not on latest version
                String currentFRGUITag = "v" + currentFRGUIVersion;
                if (!newestTag.equals(currentFRGUITag)) {
                    System.out.println("A newer version of FutureRestore GUI is available.");
                    mainMenuInstance.messageToLog("A newer version of FutureRestore GUI is available.");

                    // Label on top of release notes
                    JLabel label = new JLabel("A newer version of FutureRestore GUI is available.\n" +
                            "You're on version " + currentFRGUIVersion + " and the latest version is " + newestTag + ".");
                    Border padding = BorderFactory.createEmptyBorder(0, 0, 10, 10);
                    label.setBorder(padding);

                    // Fetch release notes
                    String mdReleaseBody = getLatestFrguiReleaseBody();
                    String htmlReleaseBody = "<html>" +
                            "<head>" +
                            "<style type=\"text/css\">" +
                            HTMLPresets.css +
                            "</style>" +
                            "</head>" +
                            "<div class=\"markdown-body\">"
                            + Processor.process(mdReleaseBody).replaceAll("\\n", "") +
                            "</div>" +
                            "</html>";
//                    System.out.println(htmlReleaseBody);

                    // Build the text area
                    JTextPane whatsNewTextPane = new JTextPane();
                    whatsNewTextPane.setEditable(false);
                    whatsNewTextPane.setContentType("text/html");
                    whatsNewTextPane.setText(htmlReleaseBody);
                    JScrollPane scrollPane = new JScrollPane(whatsNewTextPane);
                    scrollPane.setBorder(null);
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setPreferredSize(new Dimension(150, 300));

                    JPanel panel = new JPanel();
                    BoxLayout boxlayout = new BoxLayout(panel, BoxLayout.Y_AXIS); // Top to bottom
                    panel.setBorder(null);
                    panel.setLayout(boxlayout);
                    panel.add(label);
                    panel.add(scrollPane);

                    Object[] choices = {"Update now", "Remind me later"};
                    Object defaultChoice = choices[0];
                    int response = JOptionPane.showOptionDialog(mainMenuFrame, panel, "Update FutureRestore GUI", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);

                    if (response == JOptionPane.YES_OPTION) {
//                        FRUtils.openWebpage("https://github.com/CoocooFroggy/FutureRestore-GUI/releases");
                        boolean didSucceedUpdate = FRUtils.updateFRGUI(mainMenuInstance);
                        // If update failed fatally, enable everything again
                        if (!didSucceedUpdate) {
                            FRUtils.setEnabled(mainMenuInstance.mainMenuView, true, true);
                        }
                    }

                } else {
                    System.out.println("You're on the latest version of FutureRestore GUI.");
                    mainMenuInstance.messageToLog("You're on the latest version of FutureRestore GUI.");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getLatestFrguiReleaseBody() throws IOException {
        String content = getRequestUrl("https://api.github.com/repos/CoocooFroggy/FutureRestore-GUI/releases");

        Gson gson = new Gson();
        ArrayList<Map<String, Object>> result = gson.fromJson(content, ArrayList.class);
        Map<String, Object> newestRelease = result.get(0);
        return (String) newestRelease.get("body");
    }

    public static void deleteFile(File fileToDelete) {
        if (!fileToDelete.delete()) {
            try {
                FileUtils.forceDelete(fileToDelete);
            } catch (IOException exception) {
                System.err.println("Unable to delete " + fileToDelete.getAbsolutePath() + ".");
                exception.printStackTrace();
            }
        }
    }


    // region Getters

    public JFrame getMainMenuFrame() {
        return mainMenuFrame;
    }

    public JPanel getMainMenuView() {
        return mainMenuView;
    }

    public JProgressBar getLogProgressBar() {
        return logProgressBar;
    }

    public JTextField getCurrentTaskTextField() {
        return currentTaskTextField;
    }

    public JButton getStartFutureRestoreButton() {
        return startFutureRestoreButton;
    }

    public JButton getNextButtonFiles() {
        return nextButtonFiles;
    }

    public JButton getNextButtonOptions() {
        return nextButtonOptions;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    // endregion
}
