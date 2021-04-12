# Futurerestore GUI
![Github CI](https://img.shields.io/github/workflow/status/CoocooFroggy/FutureRestore-GUI/Java%20CI%20with%20Gradle.svg)
![Github releases](https://img.shields.io/github/v/release/CoocooFroggy/FutureRestore-GUI?include_prereleases.svg)
![Github issues](https://img.shields.io/github/issues/CoocooFroggy/FutureRestore-GUI.svg)
![Github repo size](https://img.shields.io/github/repo-size/CoocooFroggy/FutureRestore-GUI.svg)

A GUI implementation for FutureRestore written in Java.

![Screenshot of FutureRestore GUI in Light Theme](.github/Light.png?raw=true "FutureRestore GUI Light")
![Screenshot of FutureRestore GUI in Dark Theme](.github/Dark.png?raw=true "FutureRestore GUI Dark")

## Installation

Download from [releases](https://github.com/CoocooFroggy/FutureRestore-GUI/releases). No Java download required (it's bundled).

On Mac, right click the Mac app and click "Open" to open.

On Windows, double click the Windows MSI to install the App. Launch it from the Start Menu or the Desktop shortcut.

Linux (64 bit, amd64 + x86_64):  
- On Debian based Linux systems, such as Ubuntu and Mint, double click the DEB to install it. Launch it from your application library.  
- On RPM based Linux systems, such as Red Hat, Fedora, and CentOS, double click the file to install it. Launch it from your application library.  
- On Linux systems which support none of these, download the Linux-Universal build, and run the runFRGUI.sh script in terminal to launch the GUI.

## Features
- Fancy, user-friendly interface for selecting files for FutureRestore. No more huge commands such as:
```
/Users/CoocooFroggy/Downloads/futurerestore -d -t /Users/CoocooFroggy/Downloads/353561670934855681_iPhone69\,4_d200ap_18.2-31D37_27325c8258be46e69d9ee57fa9a8fbc28b873df434e5e702a8b27999551138ae.shsh2 --latest-sep --latest-baseband /Users/CoocooFroggy/Downloads/iPhone69\,4\,iPhone20\,0_18.2_31D37_Restore.ipsw
```
- Only select BuildManifest once for both SEP and BB.
- Ensures you don't select incorrect files: The program will ensure you have a working FutureRestore build. You can only select .iPSW files for target firmware, .BBFW files for baseband, etc.
- Option to connect to GitHub and check if your version of FutureRestore is the latest version.
- **Download FutureRestore** will automatically fetch the latest FutureRestore for your operating system, extract it, and select it.
- **Exit Recovery** button to run `futurerestore --exit recovery`
- **Stop FutureRestore** to kill the FutureRestore process. Button dynamically changes to "Stop FutureRestore (Unsafe)" while the process is running. Pop-up to confirm killing the process if it's currently running.
- Automatically launch with **Dark or Light mode theme** (not supported on Linux).
- **Error parsing** such as iBEC, APTicket-APNonce mismatch, unable to place device in recovery mode. Will show a pop-up with some help and a link on where to get help. 
![Error Parsing Example](.github/FutureRestoreGUIiBEC.png?raw=true "FutureRestore GUI iBEC Error")
- **Automatically retry** FutureRestore only once if error received is "unable to place device in recovery mode."
- Inline **GUI progress bar** for downloading SEP, BB, Sending Filesystem, etc.
- Automatically **saves all logs** to `/[Home]/FutureRestoreGUI/logs`. Never worry about accidentally closing terminal, forgetting to paste your terminal to pastebin, etc.
- **Current task** text field to simply show what FutureRestore is doing.
- Log **smart autoscroll** when scrolled to the bottom.

## Settings
- **Share logs**: Shares logs automatically to help develop FutureRestore.
- **Preview command**: Preview the final FutureRestore command. You can then choose to copy and/or run the command.
- **GUI update**: Automatically checks for updates for this program on launch.

## Usage

See [how to use FutureRestore](https://github.com/marijuanARM/futurerestore#how-to-use).

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

For GUI related issues, open an issue in the GitHub [issues section](https://github.com/CoocooFroggy/FutureRestore-GUI/issues).

## Contributing

#### Cloning the repository:
```
git clone https://github.com/CoocooFroggy/FutureRestore-GUI.git
```

#### Building:
Build a .jar with `gradle shadowjar`. Requires Java 11 or later.

Package to a Windows .msi, Mac .app, or Linux .deb, .rpm, app-image with JPackage from Java 14 or later (continuous integration releases use Java 15).

Pull requests are welcome. For major feature requests, please open an issue to discuss what improvements you would like to see.
