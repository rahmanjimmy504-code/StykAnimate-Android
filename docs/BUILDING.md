# Building the Android app

The Android project packages a locally built copy of the upstream editor into the APK. It does not load the public StykAnimate website at runtime.

## Automated build

Run the **Build Android APK** workflow from the Actions tab. The workflow checks out this repository and the upstream `filippocalippo/StykAnimate-App` source, builds its Rust/WebAssembly engine and static editor bundle, copies the bundle into Android app assets, and runs the Android Gradle build. The debug APK is uploaded as a workflow artifact.

The workflow currently tracks the upstream repository's `main` branch. Before a stable release, pin the upstream checkout to a reviewed commit SHA and record that SHA in the release metadata.

## Local build

Requirements: JDK 17, Android SDK (platform 35 and build tools), Node.js 22, npm, Rust stable with `wasm32-unknown-unknown`, and Gradle 8.9 or compatible.

1. Clone this repository and the upstream StykAnimate repository side by side (or clone upstream into an `upstream/` directory).
2. In `upstream/`, run `npm ci`, `node scripts/engine-build.mjs`, then `npm run web:build`.
3. Copy the contents of `upstream/build/web/` into `app/src/main/assets/stykanimate/`.
4. Run `gradle :app:assembleDebug` from this repository.
5. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

## Current limitations

This is the initial Android packaging foundation, not a verified release. The first build still needs validation on physical Android devices. In particular, upstream documents GPU requirements and has not verified its browser build on physical phones/tablets. Android file-picker/save integration, robust lifecycle recovery, and Android-native video encoding are subsequent porting tasks. Do not treat a successful Gradle build alone as proof that the editor works correctly on a device.
