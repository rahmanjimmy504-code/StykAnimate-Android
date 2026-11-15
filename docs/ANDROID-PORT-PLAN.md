# Native Android port plan

## Product direction

This project is a native Android port of [filippocalippo/StykAnimate-App](https://github.com/filippocalippo/StykAnimate-App), not a website wrapper. The upstream desktop application uses Electron and JavaScript for its interface, but its animation/document engine is Rust. Android will get a new native Kotlin interface and call the actual upstream Rust implementation through JNI.

## Target architecture

- **Android shell and editor UI:** Kotlin, Android lifecycle, native widgets/Compose, accessibility, touch, stylus, keyboard and responsive phone/tablet layouts.
- **Engine bridge:** a small Rust cdylib exposing a versioned JNI API. Keep platform-specific Android code out of upstream engine crates wherever possible.
- **Document and animation engine:** upstream styk-engine, styk-edit, styk-eval, styk-formats, styk-piv, styk-draw and related crates.
- **Rendering:** upstream styk-raster CPU renderer for the first working native stage; investigate a Vulkan/OpenGL ES backend after CPU output passes parity tests. Do not assume browser WebGPU is an Android-native renderer.
- **Assets:** package upstream figures, presets, icons, fonts and resource files directly, retaining provenance and all applicable licences.
- **File access:** Android Storage Access Framework for open/save/import/export and Android Sharesheet for sharing.
- **Video export:** investigate Android MediaCodec; desktop FFmpeg/Python/native helper packaging is not assumed to work on Android.

## Implemented prototype slice

The native-android branch currently contains:
- JNI cdylib bridge with one native engine session per Android UI owner.
- Upstream Engine::open document decoding and publication.
- Upstream FrameBuilder evaluation and styk-raster rendering to RGBA pixels.
- Kotlin Android document picker for supported project extensions.
- A native Android preview screen with frame stepping and timeline scrubbing.
- CI cross-compilation targets for arm64-v8a, armeabi-v7a and x86_64.
- Packaging of original upstream assets and licence/third-party notices.

This is only the first proof-of-integration. It does not yet provide native editing commands, save, undo/redo, figure manipulation, onion skins, full timeline editing or export.

## Phases

1. **Native engine proof:** cross-compile the actual upstream engine, open real Pivot/StykAnimate fixtures, evaluate and render frames through JNI, and compare output with upstream reference output.
2. **Native canvas:** replace the preview-only image stage with an interactive custom Android canvas/surface, pan/zoom, touch and stylus input, selection and hit-testing.
3. **Editing core:** integrate upstream editing/session commands, figure transforms, frame operations, undo/redo, onion skins, playback and autosave.
4. **Native workspace:** implement project home, timeline/layers, figure builder, drawing tools, effects, camera, properties, localization and responsive tablet layouts.
5. **File interoperability:** robust SAF open/save and round-trip tests for .piv, .stk, .styk, .stkz, .stykmov and supported Stick Nodes formats.
6. **Assets and export:** integrate original figure libraries, templates, brushes, fonts and presets; add PNG/JPEG/GIF/APNG/video exports where Android supports them.
7. **Release:** real-device testing, memory/performance profiling, universal APK plus ABI-specific packages, signing and reproducible source builds.

## Acceptance criteria

- The runtime application contains no WebView, WebViewAssetLoader, browser editor page, Electron shell or remote app URL.
- The Android application calls the actual upstream Rust engine and format codecs compiled into the APK.
- Native Android UI and platform APIs handle all presentation, file access and lifecycle behavior.
- Opening and rendering real upstream projects succeeds on physical ARM64 devices.
- Project editing, saving and round trips are tested before claiming full editor parity.
- The universal APK includes every supported ABI and gracefully reports unsupported hardware/features.
- All upstream GPL and third-party licence obligations are preserved.
