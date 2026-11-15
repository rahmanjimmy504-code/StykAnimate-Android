# Android UI parity contract

## Non-negotiable direction

The Android application must reproduce the existing StykAnimate editor's visual language, hierarchy, labels, icons, panel behaviour and editing workflow. It is not a redesign and must not become a generic mobile animation app. It remains a native Android implementation: no WebView, browser shell, Electron runtime or remote editor page.

The upstream implementation is the source of truth. Before implementing each screen, compare against the pinned upstream revision's UI modules, CSS tokens, icons, fonts and touch-interface documentation. When upstream changes, record the source revision used for the comparison.

## Workspace shell to reproduce

The native editor should follow the same shell structure described by upstream `docs/ARCHITECTURE.md`:

- **Top band:** StykAnimate mark/menu, open project tabs, workspace switch, Undo, Redo, Save and Export. Preserve the upstream folding order when horizontal space becomes tight.
- **Stage header:** scene/tool context and options, messages, view toggles, zoom and Fit.
- **Tool rail:** workspace-specific tools, same ordering and grouping, including variant menus and add menus.
- **Stage/canvas:** the animation scene with selection handles and overlays; canvas gestures must never be confused with panel gestures.
- **Dock panels:** Properties, VFX, Library and Build Structure, with the same tabs, visibility, fold, float/dock and sheet behaviour where practical.
- **Timeline island:** Simple and Advanced timeline modes, frame rate, zoom, playback transport, ruler and playhead. Preserve the original distinction between the Pivot-style frame strip and the advanced layer/keyframe timeline.

## Phone and tablet behaviour

Use the upstream touch interface as the behavioural reference, not an independently invented layout.

- On phones, retain the editor's compact shell rules: folded side panels open as sheets, Properties opens as a sheet, and the timeline can collapse to a header and open in a sheet.
- On tablets, retain the full stage, rail, docks and timeline arrangement whenever space allows.
- Do not rewrite a user's saved workspace layout just because a smaller screen temporarily requires a compact presentation.
- Support finger and stylus input, pointer/mouse input, keyboard shortcuts and accessibility actions.
- Preserve safe areas, system bars, rotation and Android lifecycle state.

## Visual parity checklist

For every screen and workspace, verify against the upstream website/desktop/touch UI:

- Exact hierarchy, control names, iconography and grouping.
- Shared light/dark theme tokens, typography, spacing, borders, radii and control states.
- Project tabs, workspace switching and top-band folding.
- Stage header options and view toggles.
- Tool rail order, active/disabled states, variants and context menus.
- Dock tabs, sheets, floating/docking affordances and panel visibility.
- Simple and Advanced timeline controls, ruler, playhead and transport.
- Dialogs, menus, settings, Home, tutorials, import/export and error states.
- Responsive behaviour at narrow phone, wide phone, portrait tablet and landscape tablet sizes.

A screenshot comparison is required for each target size. A screen is not considered visually complete because it merely uses similar colours.

## Functional parity checklist

The UI is only complete when its controls call working engine/platform operations. No decorative or placeholder controls may be presented as finished.

- Open, create, save, Save As, autosave and recovery.
- Supported Pivot, Stick Nodes and native StykAnimate import/export, with round-trip fixtures.
- Selection, transforms, rig/joint manipulation, IK and figure builder.
- Drawing, shapes, text, images, layers and object ordering.
- Simple and Advanced timelines, frame/key operations, easing and playback.
- Undo/redo, onion skins, motion paths, camera and effects.
- Properties, Library, VFX and workspace preferences.
- Image, animation and video export to formats actually supported by the Android implementation.
- Offline use, accessibility, localisation and project privacy.

## Test gates

1. **Reference capture:** save upstream reference screenshots and identify the source revision.
2. **Native shell:** compare top band, stage header, rail, docks and timeline on phone/tablet screenshots.
3. **Engine parity:** run the same fixture and operation in upstream and Android, comparing document state and rendered pixels where deterministic.
4. **Interaction tests:** exercise touch, stylus, mouse, keyboard, dialogs, panel changes and lifecycle recreation.
5. **File round trips:** open, edit, save, reopen and compare real sample projects.
6. **Device gates:** verify physical ARM64 devices, memory pressure, rotation, background/foreground and offline operation.

## Current implementation status

The current Android branch is an engine-preview prototype with a minimal native screen. It does not yet meet this UI or functional parity contract. The contract is a target and test plan, not a claim that the listed features already work.
