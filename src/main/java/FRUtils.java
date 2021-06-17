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
            installFrgui(downloadedFrgui, frguiDownloadIdentifier);
            System.out.println("All done");
            System.exit(0);
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
            //TODO: Catch errors
        }

        if (frguiDownloadName == null || frguiDownloadUrl == null) {
            // Never found ours O_O
            //TODO: Unable to find FRGUI for your operating system. Do it manually.
            System.out.println("No FRGUI found for your OS :(");
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
        // TODO: Return downloaded file so we can install it
        return downloadedFRGUI;
    }

    public static boolean installFrgui(File downloadedFrgui, String frguiDownloadIdentifier) throws IOException, InterruptedException {
        switch (frguiDownloadIdentifier) {
            case "Mac": {
                // Mount downloaded DMG
                ProcessBuilder attachDmgProcessBuilder = new ProcessBuilder("/usr/bin/hdiutil", "attach", "-nobrowse", downloadedFrgui.getAbsolutePath());
                Process attachDmgProcess = attachDmgProcessBuilder.start();
                // If exit code is not 0
                if (attachDmgProcess.waitFor() != 0) {
                    //TODO: Unable to attach
                    return false;
                }

                // Get location
                String attachDmgResponse = IOUtils.toString(attachDmgProcess.getInputStream(), StandardCharsets.UTF_8);
                Pattern attachLocationPattern = Pattern.compile("\\/Volumes\\/.*");
                Matcher attachLocationMatcher = attachLocationPattern.matcher(attachDmgResponse);
                String attachLocation = null;
                if (attachLocationMatcher.find()) {
                    attachLocation = attachLocationMatcher.group(0) + "/";
                }

                if (attachLocation == null) {
                    // TODO: Unable to find attached location for FRGUI DMG
                    return false;
                }

                // Copy to /Applications
                File newFrguiAppContents = new File(attachLocation + "FutureRestore GUI.app/Contents/");
                FileUtils.copyDirectory(newFrguiAppContents, new File("/Applications/FutureRestore GUI.app/Contents"));
                System.out.println("Done copying FRGUI to Applications.");

                // Open the app
                ProcessBuilder openVolumeProcessBuilder = new ProcessBuilder("/usr/bin/open", "/Applications/FutureRestore GUI.app");
                if (openVolumeProcessBuilder.start().waitFor() != 0) {
//                    TODO: Unable to open new FRGUI
                    System.out.println("Unable to open new FRGUI.");
                    return false;
                }

                // Eject attatched DMG
                ProcessBuilder ejectDmgProcessBuilder = new ProcessBuilder("/usr/bin/hdiutil", "eject", attachLocation);
                Process ejectDmgProcess = ejectDmgProcessBuilder.start();
                // If exit code is not 0
                if (ejectDmgProcess.waitFor() != 0) {
                    //TODO: Unable to eject, please do it manually
                    System.out.println("Unable to eject the DMG, please do it manually");
                    return false;
                }

                // Open the DMG and bring it to foreground, so it can be installed manually
                /*ProcessBuilder openVolumeProcessBuilder = new ProcessBuilder("/usr/bin/open", attachLocation);
                if (openVolumeProcessBuilder.start().waitFor() != 0) {
//                    TODO: Unable to bring to foreground
                }*/
                return true;
            }
            case "Windows": {
                return true;
            }
            case "Debian": {
                return true;
            }
            default: {
                // TODO: Not supposed to appear
                return false;
            }
        }
    }
}
