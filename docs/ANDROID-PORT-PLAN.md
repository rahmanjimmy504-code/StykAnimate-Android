# Android port plan

## 1. What is being ported

The target is the StykAnimate desktop application from [filippocalippo/StykAnimate-App](https://github.com/filippocalippo/StykAnimate-App), not its separately deployed website as a product. The desktop app uses Electron, JavaScript modules, and a Rust animation/format engine compiled to WebAssembly. Electron itself is not an Android runtime, so it cannot simply be copied into an APK.

The Android port will replace the Electron desktop shell with an Android application shell while retaining and adapting the actual editor, document model, codecs, and rendering engine. Editor assets will be bundled inside the APK and run locally; the app must not load the production website or depend on a remote webpage.

## 2. Target architecture

- **Android app shell:** Kotlin, Android Gradle Plugin, Android lifecycle, window handling, and app permissions only where needed.
- **Editor and engine:** bring across the upstream editor modules and Rust/WebAssembly engine, then adapt platform-specific entry points. The embedded editor is local application content, not a remote website.
- **Native bridge:** a narrow, documented Kotlin-to-JavaScript bridge for Android capabilities. Do not expose unrestricted native methods to network content.
- **File access:** Android Storage Access Framework for opening and saving projects, figures, images, and exports. Avoid broad storage permissions by default.
- **Sharing:** Android Sharesheet for user-selected project and rendered export files.
- **Persistence:** app-private storage for recovery/autosave and user-selected document locations for explicit saves.
- **Rendering:** use supported WebGPU/WebGL2 paths only where Android System WebView and device GPU support them. Detect unsupported devices and explain the limitation; do not silently claim a Canvas2D fallback.
- **Video export:** investigate Android MediaCodec integration or a compatible encoder. Desktop FFmpeg/Python packaging must not be assumed to work on Android.
- **Offline operation:** package all editor resources and required engine modules in the APK. No PWA install flow or remote app URL is required.

## 3. Porting phases

1. **Source and licence audit** — pin the upstream source revision; preserve copyright headers, GPL notices, and third-party licence files; audit Electron-only imports, filesystem calls, child processes, native helpers, and desktop update code.
2. **Android foundation** — add the Gradle project, application manifest, Kotlin activity, app icon, themes, and build variants; bundle editor resources locally and establish a strict, versioned native bridge.
3. **Editor integration** — adapt startup and platform services without replacing the editor with a separate demo; integrate the Rust/WASM engine and verify its memory and threading requirements on Android.
4. **Native workflows** — implement open/save, autosave/recovery, import, export, and Android sharing; handle back navigation, lifecycle pause/resume, rotation, keyboard, stylus, and touch gestures.
5. **Performance and compatibility** — test low-memory and mid-range devices as well as modern devices and tablets; test GPU availability, large projects, long timelines, memory pressure, and backgrounding.
6. **Release** — build a universal APK and ABI-specific APKs where useful; sign release builds, publish checksums and source, and document minimum Android/WebView requirements. Do not label a build stable until the test matrix passes.

## 4. Required acceptance tests

- App launches in airplane mode after installation.
- No editor screen is loaded from app.stykanimate.com or another remote website.
- Create, edit, undo, redo, save, close, and reopen a project.
- Open and save supported StykAnimate, Pivot Animator, and Stick Nodes formats.
- Import images and use the Android document picker.
- Export supported still-image and animation formats; verify output files can be opened by other apps.
- Share an export using the Android Sharesheet.
- Autosave and recovery survive process death and device rotation.
- Touch selection, dragging, timeline scrolling, pinch zoom, and context actions work on phones and tablets.
- No data loss when the app is backgrounded, interrupted, or denied a storage permission.
- GPU-unsupported and low-memory conditions fail gracefully with a useful explanation.

## 5. Universal APK

A universal APK is one installable package containing native libraries for all supported Android ABIs. It is larger than an ABI-specific APK. The project should publish a universal APK and may additionally publish smaller ARM64 and ARM32 variants after confirming dependency support. Android version and GPU/WebView compatibility still limit which devices can run the app; universal does not mean every Android device is guaranteed to support every rendering or export feature.

## 6. Upstream references

- [Upstream repository](https://github.com/filippocalippo/StykAnimate-App)
- [Upstream architecture](https://github.com/filippocalippo/StykAnimate-App/blob/main/docs/ARCHITECTURE.md)
- [Upstream web build notes](https://github.com/filippocalippo/StykAnimate-App/blob/main/docs/WEB.md)
- [Upstream third-party notices](https://github.com/filippocalippo/StykAnimate-App/blob/main/THIRD_PARTY_NOTICES.md)
- [Pivot Animator FAQ — creator Peter Bone](https://pivotanimator.net/FAQ.html)
- [Stick Nodes about page — developer Ralph Damiano](https://sticknodes.com/about-us/)
