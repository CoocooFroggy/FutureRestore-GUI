# Futurerestore GUI
![Github CI](https://img.shields.io/github/workflow/status/JohnnnnyKlayy/FutureRestore-GUI/Java%20CI%20with%20Gradle.svg)
![Github releases](https://img.shields.io/github/v/release/JohnnnnyKlayy/FutureRestore-GUI?include_prereleases.svg)
![Github issues](https://img.shields.io/github/issues/JohnnnnyKlayy/FutureRestore-GUI.svg)
![Github repo size](https://img.shields.io/github/repo-size/JohnnnnyKlayy/FutureRestore-GUI.svg)

A GUI implementation for FutureRestore written in Java 8.

![Screenshot of FutureRestore GUI in Light Theme](.github/FutureRestoreGUILight.png?raw=true "FutureRestore GUI Light")
![Screenshot of FutureRestore GUI in Dark Theme](.github/FutureRestoreGUIDark.png?raw=true "FutureRestore GUI Dark")

## Installation

Download from [releases](https://github.com/JohnnnnyKlayy/FutureRestore-GUI/releases). Java 8 required for Mac, Windows, and Universal builds. Install Java from the [official source](https://www.java.com/download/).

Mac builds work on MacOS.
Windows builds work on Windows.
Universal builds work on Mac, Windows, and Linux.

## Features
- Fancy, user-friendly interface for selecting files for FutureRestore. No more huge commands such as:
```
/Users/CoocooFroggy/Downloads/futurerestore -d -t /Users/CoocooFroggy/Downloads/353561670934855681_iPhone69\,4_d200ap_18.2-31D37_27325c8258be46e69d9ee57fa9a8fbc28b873df434e5e702a8b27999551138ae.shsh2 --latest-sep --latest-baseband /Users/CoocooFroggy/Downloads/iPhone69\,4\,iPhone20\,0_18.2_31D37_Restore.ipsw
```
- Only select BuildManifest once for both SEP and BB.
- Ensures you don't select incorrect files: The program will ensure you have a working FutureRestore build. You can only select .iPSW files for target firmware, .BBFW files for baseband, etc.
- Option to connect to GitHub and check if your version of FutureRestore is the latest version.
- **Download FutureRestore** will automatically fetch the latest FutureRestore for your operating system, extract it, and select it.
- **Exit Recovery** button to run futurerestore --exit recovery
- **Stop FutureRestore** to kill the FutureRestore process. Button dynamically changes to "Stop FutureRestore (Unsafe)" while the process is running. Pop-up to confirm killing the process if it's currently running.
- Automatically launch with **Dark or Light mode theme** (not supported on Linux).
- **Error parsing** such as iBEC, APTicket-APNonce mismatch, unable to place device in recovery mode. Will show a pop-up with some help and a link on where to get help. 
![Error Parsing Example](.github/FutureRestoreGUIiBEC.png?raw=true "FutureRestore GUI iBEC Error")
- **Automatically retry** FutureRestore only once if error received is "unable to place device in recovery mode."
- Inline **GUI progress bar** for downloading SEP, BB, Sending Filesystem, etc.
- Automatically **saves all logs** to /[Home]/FutureRestoreGUI/logs. Never worry about accidentally closing terminal, forgetting to paste your terminal to pastebin, etc.
- **Current task** text field to simply show what FutureRestore is doing.
- Log **smart autoscroll** when scrolled to the bottom.

## Usage

See [how to use FutureRestore](https://github.com/marijuanARM/futurerestore#how-to-use).

Double click to open. On Linux, make the .jar executable with `chmod -x [Drag FutureRestore GUI.jar]`

1. Download FutureRestore automatically through the **Download FutureRestore** button, or manually from [marijuanARM's fork](https://github.com/marijuanARM/futurerestore/releases).
2. Select your **blob** (SHSH2) file.
3. Select your **target firmware** (iPSW) file.
4. Choose your desired arguments. See [this table](https://github.com/marijuanARM/futurerestore#help) for an explanation of arguments.
5. Baseband and SEP (choose 1 each):
    1. If the latest Baseband and/or SEP firmware is compatible with your target version, select **Latest Baseband**/**Latest SEP**.
    2. Choose **Manual Baseband**/**Manual SEP**, and select your desired **Baseband** and **SEP** (BBFW and IM4P), along with a BuildManifest (.PList).
    3. If your device is Wi-Fi only (no cellular/calling ability), select **No Baseband**.
6. **Start FutureRestore**!

- You can take your device out of recovery mode with **Exit Recovery**, which will run `[futurerestore] --exit-recovery`
- You may kill the FutureRestore process while it is running, but it is considered unsafe. Do not press the **Stop FutureRestore** button while the button's text indicates that it is "Unsafe."

## Third-Party Assets

Download FutureRestore using the button included in the GUI, or manually from [here](https://github.com/marijuanARM/futurerestore/releases). Download target iPSW from [iPSW.me](https://ipsw.me) or [iPSW.dev](https://ipsw.dev).

## Troubleshooting

For FutureRestore related issues, refer to #futurerestore-help in the [r/jailbreak Discord server](https://discord.gg/GaCUYSDGt9).

For GUI related issues, open an issue in the GitHub [issues section](https://github.com/JohnnnnyKlayy/FutureRestore-GUI/issues).

## Contributing

Cloning the repository:
```
git clone https://github.com/JohnnnnyKlayy/FutureRestore-GUI.git
```

Pull requests are welcome. For major feature requests, please open an issue to discuss what improvements you would like to see.
