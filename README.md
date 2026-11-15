# StykAnimate for Android

A native Android port of [StykAnimate](https://github.com/filippocalippo/StykAnimate-App). The goal is to reuse the original Rust animation engine, document codecs, rendering/evaluation logic and licensed assets while replacing the desktop/browser presentation layer with a genuinely native Android interface.

> **Status: native port in progress.** The native-android branch contains the first JNI bridge and a basic native project-preview screen. It is an engineering prototype, not a complete animation editor or a verified release APK yet.

## Native architecture

- **Android UI:** Kotlin and Android platform widgets; the final editor will use a fully native, responsive interface for phones and tablets.
- **Engine:** upstream Rust crates compiled into Android shared libraries and called through JNI. The current bridge uses styk-engine, styk-raster, styk-render's platform-neutral view types and the upstream frame builder.
- **Rendering:** upstream CPU rasterizer for the first native milestone. A hardware-accelerated Android renderer can be added after parity and device testing.
- **Files:** Android document picker; initial preview prototype accepts .piv, .stk, .styk, .stkz, .stykmov and .json.
- **Assets:** original upstream assets, fonts, licence and third-party notices are packaged locally in the APK.
- **Offline:** no web page or remote website is used by the native application.

## Build

The GitHub Actions workflow checks out this repository and the upstream StykAnimate source, compiles the Rust bridge for arm64-v8a, armeabi-v7a and x86_64, packages the upstream assets, and builds a universal debug APK.

The upstream checkout is expected at upstream/ while building. The Rust bridge manifest is native/android-bridge/Cargo.toml.

## Upstream and licensing

The starting point is **StykAnimate** by **Filippo Calippo**, available at [filippocalippo/StykAnimate-App](https://github.com/filippocalippo/StykAnimate-App). The upstream project is licensed under **GNU GPL version 3 or later**. This derivative must preserve applicable GPL terms, notices, and corresponding-source obligations. Read the upstream [LICENSE](https://github.com/filippocalippo/StykAnimate-App/blob/main/LICENSE) and [third-party notices](https://github.com/filippocalippo/StykAnimate-App/blob/main/THIRD_PARTY_NOTICES.md).

## Acknowledgements

- **Filippo Calippo** — creator and maintainer of the original StykAnimate project; its source code is the foundation of this port.
- **Peter Bone** — creator of Pivot Animator, an important influence on the stick-figure animation workflow and the .piv / .stk ecosystem.
- **Ralph Damiano** — developer of Stick Nodes; its mobile stick-figure animation experience helped demonstrate how this workflow can work on phones and tablets.
- **The StykAnimate, Pivot Animator and Stick Nodes communities** — animators and contributors whose work and feedback helped shape this category of software.

These acknowledgements do not imply endorsement, partnership or permission to use third-party trademarks or artwork. Copyright in upstream and third-party material remains with its respective owners. New Android-specific contributions must not remove or replace existing copyright notices.

## Licence

The intended licence is **GNU GPL-3.0-or-later**, consistent with upstream StykAnimate. Third-party components retain their own licences; see the upstream third-party notices.
