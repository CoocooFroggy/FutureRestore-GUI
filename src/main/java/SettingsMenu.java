import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

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
                // Plain text shouldn't trigger this event
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

        // Changing theme pref
        ActionListener listener = e -> {
            JRadioButton buttonPressed = (JRadioButton) e.getSource();

            // Apparently you can't use switch statements on non primitives
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
        settingsMenuInstance.previewCommandCheckBox.setSelected(MainMenu.properties.getProperty("preview_command").equals("true"));

        // GUI update check box
        settingsMenuInstance.GUIUpdatesCheckBox.setSelected(MainMenu.properties.getProperty("check_updates").equals("true"));

        // FR beta update check box
        settingsMenuInstance.futureRestoreBetaCheckBox.setSelected(MainMenu.properties.getProperty("futurerestore_beta").equals("true"));

        // Theme prefs radio buttons
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

}
