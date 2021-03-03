import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenu {
    private JButton selectBlobFileButton;
    private JButton selectTargetIPSWFileButton;
    private JCheckBox updateUCheckBox;
    private JCheckBox waitWCheckBox;
    private JCheckBox debugDCheckBox;
    private JRadioButton latestSEPRadioButton;
    private JRadioButton manualSEPRadioButton;
    private JRadioButton latestBasebandRadioButton;
    private JRadioButton manualBasebandRadioButton;
    private JRadioButton noBasebandRadioButton;
    private JPanel mainMenuView;
    private JButton getCommandButton;
    private JButton selectFutureRestoreBinaryExecutableButton;
    private JButton selectBuildManifestButton;

    private String futureRestoreFilePath;
    private String blobName;
    private String blobFilePath;
    private String targetIpswName;
    private String targetIpswPath;
    private String sepFilePath;
    private String basebandFilePath;
    private String buildManifestPath;
    private String buildManifestName;
    private String sepState = "latest";
    private String bbState = "latest";

    private boolean optionUpdateState = false;
    private boolean optionWaitState = false;
    private boolean optionDebugState = true;

    public MainMenu() {
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
                    System.out.println("Chose " + file.getAbsolutePath());
                    futureRestoreFilePath = file.getAbsolutePath();
                    //Set name of button to blob file name
                    selectFutureRestoreBinaryExecutableButton.setText(file.getName());
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
                    System.out.println("Chose " + file.getAbsolutePath());
                    blobFilePath = file.getAbsolutePath();
                    blobName = file.getName();
                    //Set name of button to blob file name
                    selectBlobFileButton.setText(file.getName());
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
                    System.out.println("Chose " + file.getAbsolutePath());
                    targetIpswPath = file.getAbsolutePath();
                    targetIpswName = file.getName();
                    //Set name of button to ipsw file name
                    selectTargetIPSWFileButton.setText(file.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }
        });

        ActionListener sepRadioListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (latestSEPRadioButton.isSelected()) {
                    sepState = "latest";
                    if (bbState.equals("latest") || bbState.equals("none"))
                        selectBuildManifestButton.setEnabled(false);
                } else if (manualSEPRadioButton.isSelected()) {
                    if (chooseSep()) {
                        sepState = "manual";
                        selectBuildManifestButton.setEnabled(true);
                    } else {
                        sepState = "latest";
                        latestSEPRadioButton.setSelected(true);
                        manualSEPRadioButton.setSelected(false);
                        if (bbState.equals("latest") || bbState.equals("none"))
                            selectBuildManifestButton.setEnabled(false);
                    }
                }
            }
        };
        latestSEPRadioButton.addActionListener(sepRadioListener);
        manualSEPRadioButton.addActionListener(sepRadioListener);

        selectBuildManifestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Build manifest
                //Create a file chooser
                final JFileChooser buildManifestFileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("BuildManifest (plist)", "plist");
                buildManifestFileChooser.setFileFilter(filter);
                //In response to a button click:
                int returnVal = buildManifestFileChooser.showOpenDialog(mainMenuView);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = buildManifestFileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                    System.out.println("Chose " + file.getAbsolutePath());
                    buildManifestPath = file.getAbsolutePath();
                    buildManifestName = file.getName();
                    //Set name of button to ipsw file name
                    selectBuildManifestButton.setText(file.getName());
                } else {
                    System.out.println("Cancelled");
                }
            }
        });

        ActionListener basebandRadioListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (latestBasebandRadioButton.isSelected()) {
                    bbState = "latest";
                    if (sepState.equals("latest"))
                        selectBuildManifestButton.setEnabled(false);
                } else if (manualBasebandRadioButton.isSelected()) {
                    if (chooseBbfw()) {
                        bbState = "manual";
                        selectBuildManifestButton.setEnabled(true);
                    } else {
                        bbState = "latest";
                        latestBasebandRadioButton.setSelected(true);
                        manualBasebandRadioButton.setSelected(false);
                        noBasebandRadioButton.setSelected(false);
                        if (sepState.equals("latest"))
                            selectBuildManifestButton.setEnabled(false);
                    }
                } else if (noBasebandRadioButton.isSelected()) {
                    bbState = "none";
                    if (sepState.equals("latest"))
                        selectBuildManifestButton.setEnabled(false);
                }
            }
        };
        latestBasebandRadioButton.addActionListener(basebandRadioListener);
        manualBasebandRadioButton.addActionListener(basebandRadioListener);
        noBasebandRadioButton.addActionListener(basebandRadioListener);

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

        getCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Disable interaction
                mainMenuView.setEnabled(false);
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
                    String s = null;
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
                stringBuilder.append("./futurerestore-194");

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

                //Show them their final command!
                JOptionPane.showMessageDialog(mainMenuView, "Final command copied to clipboard! Paste it into terminal.", "Command", JOptionPane.PLAIN_MESSAGE);
                mainMenuView.setEnabled(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainMenu");
        frame.setContentPane(new MainMenu().mainMenuView);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //Centers it on screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
            System.out.println("Chose " + file.getAbsolutePath());
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
            System.out.println("Chose " + file.getAbsolutePath());
            sepFilePath = file.getAbsolutePath();
            return true;
        } else {
            System.out.println("Cancelled");
            return false;
        }
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        label1.setText("Blob");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainMenuView.add(label1, gbc);
        selectBlobFileButton = new JButton();
        selectBlobFileButton.setText("Select Blob File...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectBlobFileButton, gbc);
        selectTargetIPSWFileButton = new JButton();
        selectTargetIPSWFileButton.setText("Select Target IPSW File...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectTargetIPSWFileButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Target IPSW");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainMenuView.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Options");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridheight = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainMenuView.add(label3, gbc);
        updateUCheckBox = new JCheckBox();
        updateUCheckBox.setText("Update (-u)");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(updateUCheckBox, gbc);
        waitWCheckBox = new JCheckBox();
        waitWCheckBox.setText("Wait (-w)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(waitWCheckBox, gbc);
        debugDCheckBox = new JCheckBox();
        debugDCheckBox.setSelected(true);
        debugDCheckBox.setText("Debug (-d)");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(debugDCheckBox, gbc);
        latestSEPRadioButton = new JRadioButton();
        latestSEPRadioButton.setSelected(true);
        latestSEPRadioButton.setText("Latest SEP");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(latestSEPRadioButton, gbc);
        manualSEPRadioButton = new JRadioButton();
        manualSEPRadioButton.setText("Manual SEP");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(manualSEPRadioButton, gbc);
        latestBasebandRadioButton = new JRadioButton();
        latestBasebandRadioButton.setSelected(true);
        latestBasebandRadioButton.setText("Latest Baseband");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(latestBasebandRadioButton, gbc);
        manualBasebandRadioButton = new JRadioButton();
        manualBasebandRadioButton.setText("Manual Baseband");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(manualBasebandRadioButton, gbc);
        noBasebandRadioButton = new JRadioButton();
        noBasebandRadioButton.setText("No Baseband");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainMenuView.add(noBasebandRadioButton, gbc);
        getCommandButton = new JButton();
        getCommandButton.setText("Get Command");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(getCommandButton, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("FutureRestore");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainMenuView.add(label4, gbc);
        selectFutureRestoreBinaryExecutableButton = new JButton();
        selectFutureRestoreBinaryExecutableButton.setText("Select FutureRestore Binary/Executable...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectFutureRestoreBinaryExecutableButton, gbc);
        selectBuildManifestButton = new JButton();
        selectBuildManifestButton.setEnabled(false);
        selectBuildManifestButton.setText("Select BuildManifest...");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainMenuView.add(selectBuildManifestButton, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(latestSEPRadioButton);
        buttonGroup.add(manualSEPRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(latestBasebandRadioButton);
        buttonGroup.add(manualBasebandRadioButton);
        buttonGroup.add(noBasebandRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainMenuView;
    }

}
