# StykAnimate for Android

An Android port of [StykAnimate](https://github.com/filippocalippo/StykAnimate-App), the open-source stick-figure animation editor. This project aims to bring the actual StykAnimate editor and animation engine to Android as an installable APK—not publish a website or a home-screen shortcut.

> **Status: port in progress.** The Android application, native integrations, and device testing are not yet complete. No APK is available until a build has been implemented and verified.

## Project goals

- Package the StykAnimate editor and its Rust/WebAssembly animation engine inside an Android application.
- Replace the Electron desktop shell with an Android-native application shell.
- Provide Android-native file opening and saving, autosave/recovery, sharing, and export integration.
- Work offline after installation; projects remain on the device unless the user explicitly shares or exports them.
- Support phones and tablets, portrait and landscape layouts, touch, stylus where available, and external keyboards.
- Preserve supported StykAnimate, Pivot Animator, and Stick Nodes project compatibility.
- Produce a signed universal APK and, where practical, smaller architecture-specific APKs.

Desktop-only features such as installed Windows codecs must not be assumed to work on Android. GPU support, video encoding, memory use, and long-animation performance must be tested on real devices. See [the Android port plan](docs/ANDROID-PORT-PLAN.md).

## Upstream project

The starting point is **StykAnimate** by **Filippo Calippo**, available at [filippocalippo/StykAnimate-App](https://github.com/filippocalippo/StykAnimate-App). The upstream project is licensed under **GNU GPL version 3 or later**. This derivative must preserve applicable GPL terms, notices, and corresponding-source obligations. Read the upstream [LICENSE](https://github.com/filippocalippo/StykAnimate-App/blob/main/LICENSE) and [third-party notices](https://github.com/filippocalippo/StykAnimate-App/blob/main/THIRD_PARTY_NOTICES.md).

## People and projects we acknowledge

- **Filippo Calippo** — creator and maintainer of the original StykAnimate project; the upstream codebase is the foundation of this port.
- **Peter Bone** — creator of Pivot Animator, an important influence on the stick-figure animation workflow and the .piv/.stk ecosystem.
- **Ralph Damiano** — developer of Stick Nodes; its mobile stick-figure animation experience helped demonstrate how this kind of workflow can work on phones and tablets.
- **The StykAnimate, Pivot Animator, and Stick Nodes communities** — animators and contributors whose work, feedback, tutorials, and creativity helped shape this kind of software.

These acknowledgements do not imply endorsement, partnership, or permission to use third-party trademarks or artwork.

## Copyright and trademarks

Copyright in the original StykAnimate source code remains with its respective copyright holder(s), including the upstream author and contributors. Copyright in third-party libraries, fonts, artwork, and other bundled material remains with their respective owners and is governed by their own licences. The GNU General Public License is published by the Free Software Foundation.

**We do not claim ownership of the upstream StykAnimate code or third-party materials.** New Android-specific contributions must not remove or replace existing copyright notices.

Pivot Animator and Stick Nodes are independent products. Their names are used only to identify compatibility and historical inspiration. This project is not affiliated with, sponsored by, or endorsed by Peter Bone, Pivot Animator, Ralph Damiano, ForTheLoss Games, or their respective rights holders.

## Build status

The Android port is being prepared. Build instructions and APK download links will be added only after the Android project and CI pipeline have been implemented and verified.

## Licence

The intended licence is **GNU GPL-3.0-or-later**, consistent with upstream StykAnimate. See the upstream [licence text](https://github.com/filippocalippo/StykAnimate-App/blob/main/LICENSE). Third-party components retain their own licences; see [THIRD_PARTY_NOTICES.md](https://github.com/filippocalippo/StykAnimate-App/blob/main/THIRD_PARTY_NOTICES.md).
