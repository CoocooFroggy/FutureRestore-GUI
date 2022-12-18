# FutureRestore GUI
![Github CI](https://img.shields.io/github/actions/workflow/status/CoocooFroggy/FutureRestore-GUI/build.yml?branch=master)
![Github releases](https://img.shields.io/github/v/release/CoocooFroggy/FutureRestore-GUI?include_prereleases.svg)
![Github issues](https://img.shields.io/github/issues/CoocooFroggy/FutureRestore-GUI.svg)
![GitHub all releases](https://img.shields.io/github/downloads/CoocooFroggy/FutureRestore-GUI/total)

A modern GUI for FutureRestore, with added features to make the process easier.

![Screenshot of FutureRestore GUI in Light Theme](.github/Light.png?raw=true "FutureRestore GUI Light")
![Screenshot of FutureRestore GUI in Dark Theme](.github/Dark.png?raw=true "FutureRestore GUI Dark")

## Installation

Download from [releases](https://github.com/CoocooFroggy/FutureRestore-GUI/releases). No Java installation required (it's bundled).

- Mac: Mount the DMG with a double click. Drag into the Applications folder. You may need to right-click and press "Open" your first launch.
- Windows: Double-click the Windows MSI to install the App. Launch it from the Start Menu or the Desktop shortcut.  
- Linux:  
  - On Debian based Linux systems, such as Ubuntu and Mint, double-click the .deb file to install it. Launch it from your application library.  
  - On other Linux systems, download the Linux-Universal build, and run the `run.sh` script in terminal to launch the GUI.

#### Package managers

- Mac: `brew install futurerestore-gui`
- Windows: `winget install futurerestore-gui`

## Features

- Fancy, user-friendly interface for selecting files for FutureRestore. No more huge commands such as:
```
/Users/CoocooFroggy/Downloads/futurerestore -d -t /Users/CoocooFroggy/Downloads/353561670934855681_iPhone69\,4_d200ap_18.2-31D37_27325c8258be46e69d9ee57fa9a8fbc28b873df434e5e702a8b27999551138ae.shsh2 --use-pwndfu --set-nonce=0x1111111111111111 --custom-latest 15.3.1 --latest-sep --latest-baseband /Users/CoocooFroggy/Downloads/iPhone69\,4\,iPhone20\,0_18.2_31D37_Restore.ipsw
```
- Only select BuildManifest once for both SEP and BB.
- Ensures you don't select incorrect files: The program will ensure you have a working FutureRestore build. You can only select .iPSW files for target firmware, .BBFW files for baseband, etc.
- **Download FutureRestore** will automatically fetch the latest FutureRestore for your operating system, extract it, and select it.
- **Exit Recovery** button to run `futurerestore --exit-recovery`
- **Stop FutureRestore** to kill the FutureRestore process. Button dynamically changes to "Stop FutureRestore (Unsafe)" while the process is running. Pop-up to confirm killing the process if it's currently running.
- Automatically launch with **Dark or Light mode theme**.
- **Error parsing** such as iBEC, APTicket – APNonce mismatch, unable to place device in recovery mode. Will show a pop-up with some help and a link on where to get help. 
![Error Parsing Example](.github/AP%20Nonce%20Error.png?raw=true "FutureRestore GUI AP Nonce Error")
- **Automatically retry** FutureRestore only once if error received is "unable to place device in recovery mode."
- Inline **GUI progress bar** for downloading SEP, BB, Sending Filesystem, etc.
- Automatically **saves all logs** to `/[Home]/FutureRestoreGUI/logs`. Never worry about accidentally closing terminal, forgetting to paste your terminal to pastebin, etc.
- **Current task** text field to simply show what FutureRestore is doing.
- Log **smart autoscroll** when scrolled to the bottom.
- Optionally share logs automatically to help improve FutureRestore
- If you like terminal, you can use this to simply **generate the command and copy it** with a few clicks
- **Automatic dark mode** supported for macOS, Windows 10, and some Linux distros
- New FutureRestore features (with a supported FutureRestore build)
  - **Pwndfu** restore and **onboard** blobs
  - Set generator from blob
  - Custom firmware version for --latest 
- Automatic GUI updates

## Settings

- **Share logs**: Shares logs automatically to help develop FutureRestore.
- **Preview command**: Preview the final FutureRestore command. You can then choose to copy and/or run the command.
- **GUI updates**: Automatically checks for updates for this program on launch.
- **FutureRestore Beta**: The Download FutureRestore button will download the latest beta asset of FutureRestore.
- **GUI themes**: Choose between system theme, force light, or force dark theme.

## Usage

See [how to use FutureRestore](https://ios.cfw.guide/futurerestore).

1. Download FutureRestore automatically through the **Download FutureRestore** button, or manually from [Cryptic & m1sta's fork](https://github.com/futurerestore/futurerestore/releases).
2. Select your **blob** (SHSH2) file.
3. Select your **target firmware** (iPSW) file.
4. Choose your desired arguments. See [this table](https://github.com/futurerestore/futurerestore#help) for an explanation of arguments.
5. Baseband and SEP (choose 1 each):
    1. If the latest Baseband and/or SEP firmware is compatible with your target version, select **Latest Baseband**/**Latest SEP**.
    2. Otherwise, choose **Manual Baseband**/**Manual SEP**, and select your desired **Baseband** and **SEP** (BBFW and IM4P), along with a BuildManifest (.PList).
    3. If your device is Wi-Fi only (no cellular/calling ability), select **No Baseband**. If it does not have a Secure Enclave, select **No SEP**.
6. **Start FutureRestore**!

- You can take your device out of recovery mode with **Exit Recovery**, which will set `auto-boot` to true and reboot the phone.
- You may kill the FutureRestore process while it is running, but it is considered unsafe. It is strongly recommended to not press the **Stop FutureRestore** button while the button's text indicates that it is "unsafe," unless you know what you are doing.

## Third-Party Assets

Download FutureRestore using the button included in the GUI, or manually from [here](https://github.com/futurerestore/futurerestore/releases). Download target iPSW from [iPSW.me](https://ipsw.me) or [iPSW.dev](https://ipsw.dev). 

## Troubleshooting

For FutureRestore related issues, send a message in #support in the [official FutureRestore support server](https://discord.gg/96wCSnwYVX).

For GUI related issues, open an issue in the GitHub [issues section](https://github.com/CoocooFroggy/FutureRestore-GUI/issues).

## Contributing

#### Cloning the repository:
```
git clone https://github.com/CoocooFroggy/FutureRestore-GUI.git
```

#### Building:
Build a .jar with `gradle shadowjar`. Requires Java 11 or later. Class `Main`, method `main` is the entry point of this program. Please do not touch Not an actual Number.

Package to a Windows .msi, Mac .app, or Linux .deb, .rpm, app-image with JPackage from Java 14 or later (continuous integration releases use Java 17).

Pull requests are welcome. For feature requests, please open an issue to discuss what improvements you would like to see.
