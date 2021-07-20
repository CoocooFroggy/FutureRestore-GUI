import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FRUtils {
    private static ArrayList<Component> disabledComponents = new ArrayList<>();

    public static boolean openWebpage(String uriString) {
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            System.out.println("Unable to create link for " + uriString);
            e.printStackTrace();
        }
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean updateFRGUI(MainMenu mainMenuInstance, JPanel mainMenuView, JProgressBar logProgressBar, JTextField currentTaskTextField) throws IOException, InterruptedException {
        // If FutureRestore process is running, cancel early
        if (!(FutureRestoreWorker.futureRestoreProcess == null || !FutureRestoreWorker.futureRestoreProcess.isAlive())) {
            failUpdate("Can't update when FutureRestore is running!", mainMenuInstance, null);
            return false;
        }

        // Disable the whole menu
        setMainMenuEnabled(mainMenuView, false);

        String osName = System.getProperty("os.name").toLowerCase();
        String frguiDownloadIdentifier = null;
        if (osName.contains("mac")) {
            frguiDownloadIdentifier = "Mac";
        } else if (osName.contains("win")) {
            frguiDownloadIdentifier = "Windows";
        } else if (osName.contains("linux")) {
            // Debs only work on debian
            int exitCode = Runtime.getRuntime().exec("dpkg --version").waitFor();
            if (exitCode == 0)
                frguiDownloadIdentifier = "Debian";
        }

        if (frguiDownloadIdentifier == null) {
            Object[] choices = {"Open link", "Ok"};
            Object defaultChoice = choices[0];

            int response = JOptionPane.showOptionDialog(mainMenuView, "Unable to automatically update for this operating system. Please update FutureRestore GUI manually.\n" +
                    "https://github.com/CoocooFroggy/FutureRestore-GUI/releases/latest", "Download FutureRestore GUI", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);
            if (response == JOptionPane.YES_OPTION) {
                FRUtils.openWebpage("https://github.com/CoocooFroggy/FutureRestore-GUI/releases/latest");
            }
            return false;
        } else {
            File downloadedFrgui = downloadFRGUI(mainMenuInstance, frguiDownloadIdentifier, logProgressBar, currentTaskTextField);

            if (downloadedFrgui == null) {
                // We already notify the user of error in downloadFRGUI()
                return false;
            }

            if (installFrgui(downloadedFrgui, frguiDownloadIdentifier, mainMenuInstance, currentTaskTextField)) {
                System.out.println("All done updating FRGUI. Closing now...");
                System.exit(0);
            }
            return true;
        }
    }

    public static File downloadFRGUI(MainMenu mainMenuInstance, String frguiDownloadIdentifier, JProgressBar logProgressBar, JTextField currentTaskTextField) {
        //Download synchronously
        String frguiDownloadName = null;
        String frguiDownloadUrl = null;
        try {
            System.out.println("Finding download...");
            // TODO: Debug
            URL releasesApiUrl = new URL("https://api.github.com/repos/Forge-Nius-Trio/FutureRestore-GUI-CI-Test/releases/latest");
//            URL releasesApiUrl = new URL("https://api.github.com/repos/CoocooFroggy/FutureRestore-GUI/releases/latest");
            String releasesApiResponse = IOUtils.toString(releasesApiUrl.openConnection().getInputStream(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map<String, Object> latestReleaseApi = gson.fromJson(releasesApiResponse, Map.class);

            ArrayList<Map<String, Object>> assetsApi = (ArrayList<Map<String, Object>>) latestReleaseApi.get("assets");

            for (Map<String, Object> asset : assetsApi) {
                frguiDownloadName = (String) asset.get("name");
                if (frguiDownloadName.contains(frguiDownloadIdentifier)) {
                    // Found a download for our OS
                    frguiDownloadUrl = (String) asset.get("browser_download_url");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            failUpdate("Unable to update FutureRestore GUI.", mainMenuInstance, currentTaskTextField);
            return null;
        }

        if (frguiDownloadName == null || frguiDownloadUrl == null) {
            // Never found ours O_O
            failUpdate("Unable to find FRGUI for your operating system. Please update manually.", mainMenuInstance, currentTaskTextField);
            return null;
        }

        String homeDirectory = System.getProperty("user.home");
        File frGuiDir = new File(homeDirectory + "/FutureRestoreGUI/");

        //Make directory to store files if not exists
        if (!frGuiDir.exists()) {
            frGuiDir.mkdir();
        }

        String finalFrPath = homeDirectory + "/FutureRestoreGUI/";
        String downloadedFRGUIPath = finalFrPath + frguiDownloadName;
        File downloadedFRGUI = new File(downloadedFRGUIPath);
        try {
            System.out.println("Downloading...");
            SwingUtilities.invokeLater(() -> {
                currentTaskTextField.setText("Downloading FutureRestore GUI...");
            });
            URL url = new URL(frguiDownloadUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
            long completeFileSize = httpConnection.getContentLength();

            BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
            FileOutputStream fos = new FileOutputStream(downloadedFRGUI);
            BufferedOutputStream bout = new BufferedOutputStream(
                    fos, 1024);
            byte[] data = new byte[1024];
            long downloadedFileSize = 0;
            int x;
            while ((x = in.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;

                // calculate progress
                final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100000d);

                // update progress bar
                SwingUtilities.invokeLater(() -> {
                    logProgressBar.setMaximum(100000);
                    logProgressBar.setValue(currentProgress);
                });

                bout.write(data, 0, x);
            }
            bout.close();
            in.close();
            SwingUtilities.invokeLater(() -> {
                currentTaskTextField.setText("");
                logProgressBar.setValue(0);
                mainMenuInstance.messageToLog("FutureRestore GUI finished downloading.");
            });
        } catch (IOException e) {
            System.out.println("Unable to download FutureRestore GUI.");
            mainMenuInstance.messageToLog("Unable to download FutureRestore GUI.");
            e.printStackTrace();
            return null;
        }
        return downloadedFRGUI;
    }

    public static boolean installFrgui(File downloadedFrgui, String frguiDownloadIdentifier, MainMenu mainMenuInstance, JTextField currentTaskTextField) throws IOException, InterruptedException {
        SwingUtilities.invokeLater(() -> {
            mainMenuInstance.messageToLog("Installing newly downloaded FutureRestore GUI at " + downloadedFrgui.getAbsolutePath() + ".");
            currentTaskTextField.setText("Updating FutureRestore GUI...");
        });

        switch (frguiDownloadIdentifier) {
            case "Mac": {
                // Mount downloaded DMG
                ProcessBuilder attachDmgProcessBuilder = new ProcessBuilder("/usr/bin/hdiutil", "attach", "-nobrowse", downloadedFrgui.getAbsolutePath());
                Process attachDmgProcess = attachDmgProcessBuilder.start();
                // If exit code is not 0
                if (attachDmgProcess.waitFor() != 0) {
                    failUpdate("Unable to attach downloaded FutureRestore GUI DMG.", mainMenuInstance, currentTaskTextField);
                    return false;
                }

                // Get location
                String attachDmgResponse = IOUtils.toString(attachDmgProcess.getInputStream(), StandardCharsets.UTF_8);
                Pattern attachLocationPattern = Pattern.compile("/Volumes/.*");
                Matcher attachLocationMatcher = attachLocationPattern.matcher(attachDmgResponse);
                String attachLocation = null;
                if (attachLocationMatcher.find()) {
                    attachLocation = attachLocationMatcher.group(0) + "/";
                }

                if (attachLocation == null) {
                    failUpdate("Unable to find attached location for FutureRestore GUI DMG.", mainMenuInstance, currentTaskTextField);
                    return false;
                }

                // Copy to /Applications
                File newFrguiAppContents = new File(attachLocation + "FutureRestore GUI.app/Contents/");
                FileUtils.copyDirectory(newFrguiAppContents, new File("/Applications/FutureRestore GUI.app/Contents"));
                System.out.println("Done copying FRGUI to Applications.");

                // Open the app
                ProcessBuilder openNewFrguiProcessBuilder = new ProcessBuilder("/usr/bin/open", "-n", "/Applications/FutureRestore GUI.app");
                if (openNewFrguiProcessBuilder.start().waitFor() != 0) {
                    // Non fatal
                    System.err.println("Unable to open new FutureRestore GUI, please do so manually.");
                    mainMenuInstance.messageToLog("Unable to open new FutureRestore GUI, please do so manually.");
                }

                // Eject attached DMG
                ProcessBuilder ejectDmgProcessBuilder = new ProcessBuilder("/usr/bin/hdiutil", "eject", attachLocation);
                // If exit code is not 0
                if (ejectDmgProcessBuilder.start().waitFor() != 0) {
                    // Non fatal
                    System.err.println("Unable to eject the update DMG, please do it manually.");
                    mainMenuInstance.messageToLog("Unable to eject the update DMG, please do it manually.");
                }

                break;
            }
            case "Windows": {
                // Run downloaded MSI, prompt for Admin. Also run the exe to launch the app afterwards
                ProcessBuilder updateFrguiScriptProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c start /wait C:\\Windows\\System32\\msiexec.exe /passive /package \"" + downloadedFrgui.getAbsolutePath() + "\" && \"C:\\Program Files\\FutureRestore GUI\\FutureRestore GUI.exe\"");
                // These redirect output to nothing, otherwise the process will die when JVM is killed by msiexec
                updateFrguiScriptProcessBuilder.redirectOutput(new File("NUL"));
                updateFrguiScriptProcessBuilder.redirectError(new File("NUL"));

                System.out.println("FRGUI path: " + downloadedFrgui.getAbsolutePath());
                // If exit code is not 0
                if (updateFrguiScriptProcessBuilder.start().waitFor() != 0) {
                    failUpdate("Unable to run MSI updater.", mainMenuInstance, currentTaskTextField);
                    return false;
                }

                break;
            }
            case "Debian": {
                // dpkg update the app (uninstalls old and installs new automatically)
                ProcessBuilder installDebProcessBuilder = new ProcessBuilder("/usr/bin/pkexec", "dpkg", "-i", downloadedFrgui.getAbsolutePath());
                if (installDebProcessBuilder.start().waitFor() != 0) {
                    failUpdate("Unable to update FutureRestore GUI.", mainMenuInstance, currentTaskTextField);
                    return false;
                }

                // Open the new app, don't care about output or anything
                new ProcessBuilder("/opt/futurerestore-gui/bin/FutureRestore GUI").start();

                break;
            }
            default: {
                failUpdate("Something's gone horribly wrong, this is never supposed to appear.", mainMenuInstance, currentTaskTextField);
                return false;
            }
        }
        // Delete update file
        FileUtils.deleteQuietly(downloadedFrgui);
        // Close our app
        return true;
    }

    public static void failUpdate(String message, MainMenu mainMenuInstance, JTextField currentTaskTextField) {
        System.err.println(message);
        mainMenuInstance.messageToLog(message);
        SwingUtilities.invokeLater(() -> {
            // Null means don't touch current task, such as when FR is running
            if (currentTaskTextField != null)
                currentTaskTextField.setText("");
        });
    }

    public static void setMainMenuEnabled(JPanel mainMenuView, boolean toSet) {
        // If disabling, clear list before we start adding to list
        if (!toSet)
            disabledComponents.clear();
        for (Component component : mainMenuView.getComponents()) {
            // If disabling, add the previously disabled to this list
            if (!toSet) {
                if (!component.isEnabled())
                    disabledComponents.add(component);
            }
            // Else if enabling, if the component was in the list, don't enable it
            else {
                if (disabledComponents.contains(component))
                    continue;
            }
            SwingUtilities.invokeLater(() -> {
                component.setEnabled(toSet);
            });
        }
    }
}
