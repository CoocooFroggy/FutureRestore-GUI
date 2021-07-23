import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

public class SettingsMenu {
    JPanel settingsMenuView;
    private JCheckBox shareLogsCheckBox;
    private JTextArea discordTextArea;
    private JCheckBox previewCommandCheckBox;
    private JCheckBox GUIUpdatesCheckBox;
    private JRadioButton autoRadioButton;
    private JRadioButton lightRadioButton;
    private JRadioButton darkRadioButton;
    private JCheckBox futureRestoreBetaCheckBox;

    public SettingsMenu() {
        shareLogsCheckBox.addActionListener(e -> {
            // Set prefs to true or false depending on what they check / uncheck
            if (shareLogsCheckBox.isSelected()) {
                MainMenu.properties.setProperty("upload_logs", "true");
                discordTextArea.setEnabled(true);
            } else {
                MainMenu.properties.setProperty("upload_logs", "false");
                discordTextArea.setEnabled(false);
            }

            MainMenu.savePreferences();

        });

        discordTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                MainMenu.properties.setProperty("discord_name", discordTextArea.getText());
                MainMenu.savePreferences();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                MainMenu.properties.setProperty("discord_name", discordTextArea.getText());
                MainMenu.savePreferences();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                //Plain text shouldn't trigger this event
            }
        });

        previewCommandCheckBox.addActionListener(e -> {
            if (previewCommandCheckBox.isSelected()) {
                MainMenu.properties.setProperty("preview_command", "true");
                MainMenu.savePreferences();
            } else {
                MainMenu.properties.setProperty("preview_command", "false");
                MainMenu.savePreferences();
            }
        });

        GUIUpdatesCheckBox.addActionListener(e -> {
            if (GUIUpdatesCheckBox.isSelected()) {
                MainMenu.properties.setProperty("check_updates", "true");
                MainMenu.savePreferences();
            } else {
                MainMenu.properties.setProperty("check_updates", "false");
                MainMenu.savePreferences();
            }
        });

        futureRestoreBetaCheckBox.addActionListener(e -> {
            if (futureRestoreBetaCheckBox.isSelected()) {
                MainMenu.properties.setProperty("futurerestore_beta", "true");
                MainMenu.savePreferences();
            } else {
                MainMenu.properties.setProperty("futurerestore_beta", "false");
                MainMenu.savePreferences();
            }
        });

        //Changing theme pref
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton buttonPressed = (JRadioButton) e.getSource();

                //I'm sorry, tried to get a switch statement to work here but nothing was constant it seemed
                if (buttonPressed.equals(autoRadioButton)) {
                    MainMenu.properties.setProperty("theme_preference", "auto");
                    MainMenu.savePreferences();
                } else if (buttonPressed.equals(lightRadioButton)) {
                    MainMenu.properties.setProperty("theme_preference", "light");
                    MainMenu.savePreferences();
                } else if (buttonPressed.equals(darkRadioButton)) {
                    MainMenu.properties.setProperty("theme_preference", "dark");
                    MainMenu.savePreferences();
                }

            }
        };
        autoRadioButton.addActionListener(listener);
        lightRadioButton.addActionListener(listener);
        darkRadioButton.addActionListener(listener);
    }

    static void initializeSettingsMenu(SettingsMenu settingsMenuInstance) {
        // Share logs check box
        if (MainMenu.properties.getProperty("upload_logs").equals("true")) {
            settingsMenuInstance.shareLogsCheckBox.setSelected(true);
            settingsMenuInstance.discordTextArea.setEnabled(true);
        } else {
            settingsMenuInstance.shareLogsCheckBox.setSelected(false);
            settingsMenuInstance.discordTextArea.setEnabled(false);
        }

        // Discord text area
        settingsMenuInstance.discordTextArea.setText(MainMenu.properties.getProperty("discord_name"));

        // Preview command check box
        if (MainMenu.properties.getProperty("preview_command").equals("true"))
            settingsMenuInstance.previewCommandCheckBox.setSelected(true);
        else
            settingsMenuInstance.previewCommandCheckBox.setSelected(false);

        // GUI update check box
        if (MainMenu.properties.getProperty("check_updates").equals("true"))
            settingsMenuInstance.GUIUpdatesCheckBox.setSelected(true);
        else
            settingsMenuInstance.GUIUpdatesCheckBox.setSelected(false);

        // FR beta update check box
        if (MainMenu.properties.getProperty("futurerestore_beta").equals("true"))
            settingsMenuInstance.futureRestoreBetaCheckBox.setSelected(true);
        else
            settingsMenuInstance.futureRestoreBetaCheckBox.setSelected(false);

        //Theme prefs radio buttons
        switch (MainMenu.properties.getProperty("theme_preference")) {
            case "auto":
                settingsMenuInstance.autoRadioButton.setSelected(true);
                break;
            case "light":
                settingsMenuInstance.lightRadioButton.setSelected(true);
                break;
            case "dark":
                settingsMenuInstance.darkRadioButton.setSelected(true);
                break;
        }
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
        settingsMenuView = new JPanel();
        settingsMenuView.setLayout(new GridBagLayout());
        shareLogsCheckBox = new JCheckBox();
        shareLogsCheckBox.setText("Share logs");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(shareLogsCheckBox, gbc);
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        settingsMenuView.add(spacer1, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("(Optional) Let us contact you about your logs by providing your Discord.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        settingsMenuView.add(spacer2, gbc);
        previewCommandCheckBox = new JCheckBox();
        previewCommandCheckBox.setText("Preview command");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(previewCommandCheckBox, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("See a preview of the final command before it runs.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label2, gbc);
        discordTextArea = new JTextArea();
        discordTextArea.setLineWrap(true);
        discordTextArea.setRows(1);
        discordTextArea.setText("None#0000");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(discordTextArea, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Automatically share logs to help make FutureRestore better.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label3, gbc);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 20, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Settings");
        label4.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 0, 0);
        settingsMenuView.add(label4, gbc);
        final JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator2, gbc);
        final JSeparator separator3 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator3, gbc);
        final JSeparator separator4 = new JSeparator();
        separator4.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator4, gbc);
        GUIUpdatesCheckBox = new JCheckBox();
        GUIUpdatesCheckBox.setSelected(false);
        GUIUpdatesCheckBox.setText("GUI updates");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(GUIUpdatesCheckBox, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Automatically check for updates for FutureRestore GUI.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label5, gbc);
        final JSeparator separator5 = new JSeparator();
        separator5.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator5, gbc);
        autoRadioButton = new JRadioButton();
        autoRadioButton.setText("Auto");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(autoRadioButton, gbc);
        lightRadioButton = new JRadioButton();
        lightRadioButton.setText("Light");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        settingsMenuView.add(lightRadioButton, gbc);
        darkRadioButton = new JRadioButton();
        darkRadioButton.setText("Dark");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        settingsMenuView.add(darkRadioButton, gbc);
        final JSeparator separator6 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator6, gbc);
        final JSeparator separator7 = new JSeparator();
        separator7.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator7, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Set the theme of the GUI. Requires a restart to take effect.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label6, gbc);
        futureRestoreBetaCheckBox = new JCheckBox();
        futureRestoreBetaCheckBox.setText("FutureRestore Beta");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 0);
        settingsMenuView.add(futureRestoreBetaCheckBox, gbc);
        final JSeparator separator8 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator8, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("\"Download FutureRestore\" will use the latest beta of FutureRestore.");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 10);
        settingsMenuView.add(label7, gbc);
        final JSeparator separator9 = new JSeparator();
        separator9.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        settingsMenuView.add(separator9, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(autoRadioButton);
        buttonGroup.add(lightRadioButton);
        buttonGroup.add(darkRadioButton);
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
        return settingsMenuView;
    }

}
