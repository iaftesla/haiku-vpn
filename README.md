# Haiku Android VPN

A production-ready, lightweight, and highly secure Android VPN application named **Haiku**, built around the core philosophy of **Japanese Minimalism (Wabi-Sabi)** and running on the advanced **sing-box-core** (VLESS Reality protocol).

---

## 🎨 Visual & Interaction Design (Wabi-Sabi)
1. **Color Palette**:
   - **Background**: Off-white / Cream (`#FDFBF7`)
   - **Accents**: Deep Ink Charcoal / Sumizome (`#1C1C1C`)
   - **Active/Success State**: Subtle Moss Green (`#8A9A86`)
   - **Warning/Progress State**: Muted Sakura Pink (`#E8C5C8`)
2. **Abstract Enso Connection Button**: 
   - Uses Jetpack Compose Canvas API to draw a hand-drawn zen circle (Enso).
   - Simulates "ink expanding in water" with custom concentric circle animations (`animateColorAsState` & `InfiniteTransition`) during connection.
   - Gentle breathing pulse effect once a secure connection is established.
3. **Poetic Haiku Display**:
   - Instead of complex connection logs, the main screen displays dynamically updating 3-line Japanese Haiku poems about mist, wind, freedom, and journeys matching the current connection state.

---

## 🛠️ Architecture & Core Integration
- **Platform VpnService Wrapper**: Extends Android's `VpnService` to capture system traffic, configure DNS (`8.8.8.8`), and route it to sing-box via a secure virtual network adapter (`172.19.0.1/30`).
- **sing-box Core**: Integrates Go library bindings (`libbox.aar`) running on a memory-optimized `gvisor` userspace network stack.
- **VLESS Reality Parser**: Resolves standard URI formats (`vless://uuid@host:port?security=reality&pbk=...#Name`) into configuration parameters.
- **CameraX Barcode Scanning**: Full-screen QR code scanner integrated with Google ML Kit to parse incoming Reality nodes directly in the settings drawer.
- **MVVM Pattern**: Uses `StateFlow` and Coroutine scopes to cleanly separate UI states from core network execution.

---

## 📁 File Structure
- `settings.gradle.kts` / `build.gradle.kts` - Gradle Version Catalog project configurations.
- `app/build.gradle.kts` - App compilation configuration linking local `.aar` library.
- `app/src/main/AndroidManifest.xml` - FGS system declaration with `specialUse` permission metadata.
- `app/src/main/java/com/haiku/vpn/`
  - `MainActivity.kt` - Navigation routing and Android VpnService permission prompt controller.
  - `core/`
    - `RealityConfig.kt` - VLESS Reality configuration class and URL parser.
    - `SingBoxConfigGenerator.kt` - Programmatic JSON configuration generator for sing-box-core.
    - `HaikuVpnService.kt` - Android VpnService class and sing-box instance manager.
  - `ui/`
    - `VpnViewModel.kt` - MVVM controller (states, shared preferences, latency checker, poetry provider).
    - `theme/Theme.kt` - Custom typography (Serif/Sans-Serif blend) and color system.
    - `components/EnsoCanvas.kt` - Canvas drawing code for the dynamic Enso button.
    - `screens/`
      - `MainScreen.kt` - Poetry display, speed stats, and Enso button container.
      - `ServerListScreen.kt` - Latency tests and node lists.
      - `SettingsScreen.kt` - Kill Switch, clipboard importer, and MLKit CameraX scanner.

---

## 🚀 How to Run in Android Studio
1. Open Android Studio, select **File > Open**, and choose the `haiku_vpn` directory.
2. Compile and place your `libbox.aar` file inside `app/libs/` (see instructions in [app/libs/README.md](file:///C:/Users/dlapo/.gemini/antigravity/scratch/haiku_vpn/app/libs/README.md)).
3. Connect your Android device or start an emulator.
4. Click **Run** in Android Studio to install the application.
