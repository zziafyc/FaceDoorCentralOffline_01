# FaceDoorCentralOffline

A face recognition application designed for door access control. It includes user registration, group management, identity verification, and animated UI components.

## Features
- **User Registration & Authentication**: Enroll new users with facial data.
- **Group Management**: Organize users into groups for batch operations.
- **Face Identification**: Real‑time recognition to grant or deny access.
- **Voice Verification**: Complementary audio authentication.
- **Admin Dashboard**: Central view for registering users, managing groups, and querying records.
- **Animation**: Uses Lottie JSON animations for smooth UI effects.

## Installation

### Prerequisites
- **Android Studio** (Arctic Fox or later)
- **JDK 8** (or later)
- **Android SDK** and platform tools (included with Android Studio)

### Build the Project
1. Clone this repository.
2. Open the project in Android Studio.
3. Sync the Gradle files (click **File → Sync Project with Gradle Files**).
4. Ensure your development environment meets the minimum SDK requirements (the app targets API 19 and above).

### Run the App
1. Connect an Android device or start an emulator.
2. Enable **USB Debugging** on the device (if using a physical device).
3. Click the **Run** button in Android Studio (or select **Run → Run ‘app’** from the menu).
4. Grant the requested permissions (camera, storage, etc.) when prompted.

## Usage

- **Launcher**: The app includes two launcher activities (`LauncherActivity2` and `LauncherActivity`). The first one (`LauncherActivity2`) is set as the default home screen.
- **Permissions**: The app requests numerous permissions (camera, storage, internet, etc.) to support face capture, asset storage, and cloud services. Ensure these are accepted during installation or runtime.
- **Database**: User and group data are stored locally in an SQLite database (`test.db`). The schema includes `user` and `group_info` tables.
- **Animations**: JSON assets (`0_begin.json`, `2_listening_loop.json`) are used with Lottie for animated overlays. Ensure your version of Android Studio includes the Lottie plugin if you plan to edit them.
- **Testing**: An instrumented test (`ExampleInstrumentedTest`) is provided to verify basic context handling.

## Notes
- This repository is intended for Android development exploration and may require additional setup (e.g., API keys, cloud configuration) for full functionality in production environments.
- The project uses standard Android development practices and includes a modular structure (`app` module). No external libraries beyond the standard Android SDK and Butter Knife (as seen in the adapter) are evident in the provided files.
