# Native Android port

## Product direction

The Android application must be a native Android application, not a WebView shell around the browser editor. Reuse the original StykAnimate source wherever it is portable, including the Rust document/animation engine, codecs, drawing/evaluation logic, presets, figures, fonts, icons and other licensed assets. Build Android-specific presentation and platform integration natively.

Upstream: https://github.com/filippocalippo/StykAnimate-App

The upstream project is not a single native desktop UI that can simply be recompiled for Android. Its application interface is JavaScript and its desktop shell is Electron, while its engine is Rust. Therefore, a genuine native port requires a new Android UI and a native bridge to the Rust engine. Do not embed the web app, Electron, a browser tab, or WebView as a shortcut.

## Target architecture

- UI: Kotlin + Jetpack Compose, with Android-native navigation, dialogs, menus, accessibility, keyboard/mouse support and responsive phone/tablet layouts.
- Engine: compile the upstream native Rust crates for Android ABIs and expose a narrow C ABI/JNI interface. Prefer reusing styk-engine, styk-edit, styk-eval, styk-formats, styk-piv, styk-draw, styk-raster and styk-export rather than reimplementing document semantics.
- Rendering: native Android rendering surface backed by the upstream engine's draw-list/evaluation output. Investigate the upstream renderer and choose a compatible Android GPU backend (Vulkan/OpenGL ES) or a CPU raster fallback. Do not assume the WebGPU browser renderer can be reused unchanged.
- Files: Android Storage Access Framework for opening, importing, saving and sharing projects; support the upstream formats and preserve embedded media.
- Assets: package the upstream licensed fonts, icons, figure/preset assets and other runtime resources directly in Android resources/assets. Record provenance and retain applicable notices/licences.
- Background work: use Rust worker threads for engine tasks where safe, and Android lifecycle-aware dispatch for UI updates. Never block the Compose main thread on decoding, rendering or export.
- Export: native frame/image export first; video/audio export only after a compatible Android encoder path is implemented and tested.

## Migration phases

1. Audit and boundary: map upstream Rust crates, renderer contracts, asset locations, file formats, licensing and existing Android build. Identify platform-specific dependencies before selecting the bridge.
2. Native engine proof: build the smallest upstream native engine slice for arm64-v8a and armeabi-v7a (and x86_64 for emulator testing), expose create/open/inspect/render/save through JNI, and run upstream parity fixtures. Do not ship a fake editor screen.
3. Native canvas: implement a Compose-hosted Android rendering surface, touch/pinch/pan/selection and a frame playback loop using engine-produced draw data.
4. Core editor: add native project home, timeline, frame insertion/duplication/deletion, figure selection and transforms, undo/redo, onion skinning and playback.
5. Interoperability: add SAF import/export for .piv, .stk, .styk, .stkz, .stykmov and supported Stick Nodes formats, validating round trips against upstream tests.
6. Assets and advanced tools: integrate original figure libraries, brushes, drawing tools, effects, layers, camera controls, fonts, templates and presets.
7. Export and release: implement image sequence/GIF/video export as supported, test real devices and tablets, publish universal/multi-ABI APKs and document known gaps.

## Non-negotiable acceptance criteria

- No WebView, WebViewAssetLoader, browser-based editor page, or remote website in the runtime app.
- No WebView workaround hidden behind a native-looking shell.
- The actual upstream Rust engine and format implementations are compiled into the APK and called by the Android application.
- Android UI and platform services are native.
- Every claimed feature is backed by a working implementation and a test; no placeholder buttons.
- Preserve upstream attribution, GPL-3.0-or-later obligations and third-party notices. A distributed derivative must comply with the upstream licence.

## Important migration rule

Do not simply delete the existing WebView before the native engine and canvas are usable: that would leave the APK blank. Keep the old implementation only as a temporary development fallback, then remove its WebView dependencies and asset-bundling workflow once the native path passes its acceptance tests. The final release must be WebView-free.
