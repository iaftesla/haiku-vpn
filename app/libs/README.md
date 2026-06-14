# sing-box-core Android Library Setup (libbox.aar)

The **Haiku VPN** app integrates the advanced `sing-box` core utilizing Go mobile library bindings.
To compile the project successfully, you must place the compiled library file `libbox.aar` in this folder.

## How to Build `libbox.aar` from Source

If you wish to build the binary yourself, follow these steps:

1. **Prerequisites**:
   - Install **Go (Golang)** version 1.21 or later.
   - Install **Android NDK** (version r25c or higher recommended) and set the `ANDROID_NDK_HOME` environment variable.
   - Install `gomobile` tools:
     ```bash
     go install golang.org/x/mobile/cmd/gomobile@latest
     gomobile init
     ```

2. **Clone and Build**:
   - Clone the official `sing-box` repository:
     ```bash
     git clone https://github.com/SagerNet/sing-box.git
     cd sing-box
     ```
   - Build the `libbox` target for Android:
     ```bash
     gomobile bind -target=android -androidapi 26 -v -ldflags="-s -w" ./experimental/libbox
     ```

3. **Install**:
   - Locate the generated `libbox.aar` and `libbox-sources.jar` in your build directory.
   - Copy both files and paste them into this directory:
     `C:\Users\dlapo\.gemini\antigravity\scratch\haiku_vpn\app\libs/`

Once the files are present, you can sync Gradle and compile/run the application in Android Studio.
