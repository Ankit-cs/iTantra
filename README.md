# iTantra

> **Smart India Hackathon 2026 | Problem Statement PS-26173**
> Indian Multilingual TTS & STT Aided Neural Transceiver Radio Access for Low Bitrate Links

[![Platform: Android/iOS](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-green.svg)](https://reactnative.dev)
[![Framework: Expo](https://img.shields.io/badge/Framework-Expo-black.svg)](https://expo.dev)
[![Status: Work in Progress](https://img.shields.io/badge/Status-Work%20in%20Progress-orange.svg)]()

---

## What is iTantra Multi?

**iTantra Multi** is an offline, AI-powered multilingual voice communication module built with React Native and Expo. It is designed to work in disaster zones, tactical field operations, and rural areas without GSM infrastructure. 

Currently, this repository holds the progressive implementation of the core communication capabilities. It uses device-to-device **Wi-Fi Direct (P2P)** networking instead of streaming over the internet. Voice input is processed entirely on-device via **Speech-to-Text (STT)**, and responses can be read back using **Text-to-Speech (TTS)**.

### Core capabilities

| Capability | Status | Implementation Details |
| --- | --- | --- |
| Offline Speech-to-Text (STT) | Implemented | `react-native-vosk` for on-device voice recognition |
| Text-to-Speech (TTS) | Implemented | `expo-speech` for localized text synthesis |
| Peer-to-Peer Networking (P2P) | Implemented | `react-native-wifi-p2p` for Wi-Fi Direct connections |

---

## Technology Stack

| Layer | Technology | Notes |
| --- | --- | --- |
| Framework | Expo / React Native | `create-expo-app` starter |
| Language | TypeScript | Strictly typed |
| STT | `react-native-vosk` | Offline acoustic models |
| TTS | `expo-speech` | System-level TTS engine |
| P2P | `react-native-wifi-p2p` | Wi-Fi Direct API wrapper |
| Styling | NativeWind / TailwindCSS | Utility-first styling |

---

## Project Structure

```
multi/
├── app/                      # Expo Router screens and navigation
├── assets/                   # Static assets (images, fonts)
├── components/               # Reusable React components
├── lib/                      # Core business logic and hardware services
│   ├── p2pService.ts         # Wi-Fi Direct Peer-to-Peer implementation
│   ├── sttService.ts         # Vosk offline Speech-to-Text implementation
│   └── ttsService.ts         # Expo Speech Text-to-Speech implementation
├── package.json              # Project dependencies
└── README.md                 # Project documentation
```

---

## Core Modules

### 1. Offline Speech-to-Text (`sttService.ts`)
Integrated offline Speech-to-Text recognition using the `react-native-vosk` library. It initializes the Vosk API, loads a local acoustic model, and continuously listens for user input without requiring an internet connection.

### 2. Text-to-Speech (`ttsService.ts`)
Integrated Text-to-Speech using `expo-speech`. It enables the application to read out text strings out loud to the user. Supports language specification and stopping the current synthesis playback.

### 3. Peer-to-Peer Communication (`p2pService.ts`)
Implemented device-to-device offline communication via Wi-Fi Direct using the `react-native-wifi-p2p` library.
* Initializes the P2P module and requests required location/nearby-devices permissions on Android.
* Allows discovering nearby peers.
* Connects to devices via MAC address.
* Sends string payloads over the active Wi-Fi Direct link.
* Listens for incoming messages in real-time.

---

## Building & Running

### Prerequisites
* Node.js and npm/yarn installed.
* Standard React Native setup for Android (Android Studio) / iOS (Xcode).

### Get started

1. Install dependencies

   ```bash
   npm install
   ```

2. Start the app

   ```bash
   npx expo start
   ```

*(Note: Because this project uses native modules like `react-native-vosk` and `react-native-wifi-p2p`, you cannot run this within the standard Expo Go app. You must build a custom development client or a standalone native build for Android/iOS).*

---

## License

Application code: **Apache 2.0**.
