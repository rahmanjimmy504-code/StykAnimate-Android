# Native Android port

## Product direction

The Android application is a native Android port, not a WebView shell around the browser editor. Reuse the original StykAnimate source wherever it is portable, including the Rust document/animation engine, codecs, drawing/evaluation logic, presets, figures, fonts, icons and other licensed assets. Build Android-specific presentation and platform integration natively.

Upstream source is pinned in CI to commit 60d8f5f895192230681d89996ef7f763b12de829 of https://github.com/filippocalippo/StykAnimate-App.

The upstream project is not a single native desktop UI that can simply be recompiled for Android. Its application interface is JavaScript and its desktop shell is Electron, while its engine is Rust. Therefore, the Android port has a new Kotlin UI and a native JNI bridge to the original Rust engine. Do not embed the web app, Electron, a browser tab, or WebView.

## Current native prototype

The native-android branch contains:
- Kotlin Android activity and Android document picker.
- Rust cdylib loaded directly by Kotlin through JNI.
- Upstream Engine document opening and native frame evaluation.
- Upstream CPU rasterizer producing RGBA frame pixels.
- Frame stepping and timeline scrubbing in the native Android UI.
- CI build targets for arm64-v8a, armeabi-v7a and x86_64.
- Original upstream assets, licence and third-party notices copied into the APK.

This is a proof of engine integration and a native preview, not a complete animation editor. Editing, save, undo/redo, figure manipulation, onion skins, full timeline, native GPU acceleration and export remain to be implemented and tested.

## Target architecture

- UI: Kotlin Android UI (move to Jetpack Compose as the workspace grows), with native navigation, dialogs, menus, accessibility, keyboard/mouse support and responsive phone/tablet layouts.
- Engine: compile upstream native Rust crates for Android ABIs and expose a narrow C ABI/JNI interface. Reuse styk-engine, styk-edit, styk-eval, styk-formats, styk-piv, styk-draw, styk-raster and styk-export rather than reimplementing document semantics.
- Rendering: upstream styk-raster CPU renderer is the first backend. Investigate a compatible Android GPU backend (Vulkan/OpenGL ES) after CPU output passes parity tests. Do not assume browser WebGPU can be reused unchanged.
- Files: Android Storage Access Framework for opening, importing, saving and sharing projects; preserve embedded media and supported formats.
- Assets: package upstream licensed fonts, icons, figure/preset assets and other runtime resources directly in APK assets. Record provenance and retain applicable notices/licences.
- Background work: perform decoding, evaluation and rendering away from the Android main thread; respect Android lifecycle and memory limits.
- Export: native frame/image export first; video/audio export only after a compatible Android encoder path is implemented and tested.

## Migration phases

1. Native engine proof: cross-compile upstream Rust crates, open real Pivot/StykAnimate fixtures, evaluate and render through JNI, and compare output with upstream references.
2. Native canvas: replace the preview-only ImageView with an interactive Android canvas/surface, pan/zoom, touch and stylus input, selection and hit-testing.
3. Core editor: integrate upstream editing/session commands, figure transforms, frame operations, undo/redo, onion skins, playback and autosave.
4. Native workspace: implement project home, timeline/layers, figure builder, drawing tools, effects, camera, properties, localization and responsive tablet layouts.
5. Interoperability: robust SAF open/save and round-trip tests for .piv, .stk, .styk, .stkz, .stykmov and supported Stick Nodes formats.
6. Assets and export: integrate original figure libraries, templates, brushes, fonts and presets; add PNG/JPEG/GIF/APNG/video exports where Android supports them.
7. Release: real-device testing, memory/performance profiling, universal APK plus ABI-specific packages, signing and reproducible source builds.

## Acceptance criteria

- No WebView, WebViewAssetLoader, browser editor page, Electron shell or remote app URL in the runtime application.
- The Android application calls the actual upstream Rust engine and format codecs compiled into the APK.
- Native Android UI and platform APIs handle all presentation, file access and lifecycle behavior.
- Opening, rendering, editing and saving real upstream projects are verified on physical ARM64 devices before claiming full editor parity.
- The universal APK includes the supported ABIs and gracefully reports unsupported hardware/features.
- Every claimed feature is backed by a working implementation and a test; no placeholder buttons.
- Preserve upstream attribution, GPL-3.0-or-later obligations and third-party notices.
