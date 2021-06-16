import com.google.gson.Gson;
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

            // TODO: installFutureRestore
            System.out.println("All done");
            return true;
        }
    }

    public static File downloadFRGUI(MainMenu mainMenuInstance, String frguiDownloadIdentifier, JProgressBar logProgressBar, JTextField currentTaskTextField) {
        //Download synchronously
        String frguiDownloadName = null;
        String frguiDownloadUrl = null;
        try {
            System.out.println("Finding download...");
            URL releasesApiUrl = new URL("https://api.github.com/repos/CoocooFroggy/FutureRestore-GUI/releases/latest");
            String releasesApiResponse = IOUtils.toString(releasesApiUrl.openConnection().getInputStream(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map<String, Object> latestReleaseApi = gson.fromJson(releasesApiResponse, Map.class);

            ArrayList<Map<String, Object>> assetsApi = (ArrayList<Map<String, Object>>) latestReleaseApi.get("assets");

            for (Map<String, Object> asset: assetsApi) {
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
                SwingUtilities.invokeLater(() -> logProgressBar.setValue(currentProgress));

                bout.write(data, 0, x);
            }
            bout.close();
            in.close();
            SwingUtilities.invokeLater(() -> {
                currentTaskTextField.setText("");
                logProgressBar.setValue(0);
                mainMenuInstance.appendToLog("FutureRestore GUI finished downloading.");
            });
        } catch (IOException e) {
            System.out.println("Unable to download FutureRestore GUI.");
            mainMenuInstance.appendToLog("Unable to download FutureRestore GUI.");
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
                ProcessBuilder attachDmgProcessBuilder = new ProcessBuilder("/usr/bin/hdiutil", "attach", downloadedFrgui.getAbsolutePath());
                Process attachDmgProcess = attachDmgProcessBuilder.start();
                // If exit code is not 0
                if (attachDmgProcess.waitFor() != 0) {
                    //TODO: Unable to attach
                }

                // Get location
                String attachDmgResponse = IOUtils.toString(attachDmgProcess.getInputStream(), StandardCharsets.UTF_8);
                Pattern attachLocationPattern = Pattern.compile("\\/Volumes\\/.*");
                Matcher attachLocationMatcher = attachLocationPattern.matcher(attachDmgResponse);
                String attachLocation = null;
                if (attachLocationMatcher.find()) {
                    attachLocation = attachLocationMatcher.group(0);
                }

                if (attachLocation == null) {
                    // TODO: Unable to find attached location for FRGUI DMG
                }

                // Open it and bring it to foreground
                ProcessBuilder openVolumeProcessBuilder = new ProcessBuilder("/usr/bin/open", attachLocation);
                if (openVolumeProcessBuilder.start().waitFor() != 0) {
                    //TODO: Unable to bring to foreground
                }
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
