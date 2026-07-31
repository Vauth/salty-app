<div align="center">

# Salty App

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Salty App Logo" width="150"/>
</p>

![Android](https://img.shields.io/badge/Android-212121?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-212121?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-212121?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/License_MIT-212121?style=for-the-badge)

*Exchange sensitive information securely by encrypting messages with a shared "Salt" (Key). Built with a strict focus on privacy and modern design, it utilizes AES-256-GCM encryption and stores all history locally on your device.*

</div>

<br>

## 🔎 Features
*   **Secure Encryption:** Industry-standard AES-256 with PBKDF2 (600,000 iterations).
*   **Modern Design:** Sleek Material Design 3 interface with an expressive dark mode.
*   **Offline Privacy:** Zero analytics, zero cloud sync[cite: 1]. Your data stays completely on your phone.
*   **Intuitive Workflow:** Simple, easily navigable tabs for Encoding, Decoding, and History management.

<br>

## 📸 Screenshots
<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="Screenshot 1" width="30%">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Screenshot 3" width="30%">
</div>

<br>

## 🛠 Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** MVVM
*   **Local Data:** Room Database
*   **Asynchronous Programming:** Coroutines & Flow

<br>

## 🚀 Quick Start
Get the latest version of the app through your preferred privacy-focused marketplace.

<a href="https://f-droid.org/packages/com.vauth.salty">
  <img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="72" align="middle">
</a>
<a href="https://apt.izzysoft.de/packages/com.vauth.salty">
  <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroidButtonGreyBorder_nofont.png" alt="Get it at IzzyOnDroid" height="49" align="middle">
</a>

<br>

## ⚙️ Building
* #### Clone Repository:
```bash
git clone https://github.com/vauth/salty-app.git
cd salty-app
   ```
* #### Build and Install:
```bash
gradle :app:assembleRelease --no-daemon
```
*(Note: If you are using Windows, use `gradlew.bat` instead of `./gradlew`)*

<br>

## 🤝 Contributing
Contributions are welcome! Feel free to submit a pull request or report an issue.

<br>

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
