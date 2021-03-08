# Futurerestore GUI
![Github CI](https://img.shields.io/github/workflow/status/JohnnnnyKlayy/FutureRestore-GUI/Java%20CI%20with%20Gradle.svg)
![Github releases](https://img.shields.io/github/v/release/JohnnnnyKlayy/FutureRestore-GUI?include_prereleases.svg)
![Github issues](https://img.shields.io/github/issues/JohnnnnyKlayy/FutureRestore-GUI.svg)
![Github repo size](https://img.shields.io/github/repo-size/JohnnnnyKlayy/FutureRestore-GUI.svg)

A GUI implementation for FutureRestore written in Java 8.

![Screenshot of FutureRestore GUI](.github/FutureRestoreGUI2.png?raw=true "FutureRestore GUI")

## Installation

Download from [releases](https://github.com/JohnnnnyKlayy/FutureRestore-GUI/releases). Java 8 required for Mac, Windows, and Universal builds. Install Java from the [official source](https://www.java.com/download/).

Mac builds work on MacOS.
Windows builds work on Windows.
Universal builds work on Mac, Windows, and Linux.

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
