# Better Coords

Fabric client mod for Minecraft **1.21.11**. Author: **ruzgar**.

- **Mod ON:** pressing F3 shows only one line in the top-left: `XYZ: x  y  z`. No other debug text, charts or the 3D crosshair gizmo.
- **Mod OFF:** the vanilla F3 screen, untouched.
- **Facing Line:** optional second line, worded exactly like vanilla:
  `Facing: south (Towards positive Z) (-0.6 / 2.7)`.
- **Glitch Mode:** replaces the numbers with shifting junk glyphs — arrows, box drawing, maths,
  Greek, Cyrillic and katakana. Each character runs on its own timer, so the line churns instead of
  flipping over all at once, and the spaces, slashes and brackets are kept so it still reads as a
  coordinate line. Applies to the facing line too.
- **Fake Coordinates:** when enabled, the spot you are standing on is displayed as the X/Y/Z you typed, and moving offsets from there.
  Example: you are at `50 100 50` and enter `1000 100 1000` → F3 shows `1000 100 1000`; walk 100 blocks in +Z → `1000 100 1100`.

## Install

1. Fabric Loader ≥ 0.19.5 for Minecraft 1.21.11.
2. Drop `better-coords-1.2.0.jar` into your `mods` folder.
3. Optional but recommended: [Mod Menu](https://modrinth.com/mod/modmenu) (+ Fabric API, which Mod Menu needs) to get the settings screen.
   Without Mod Menu the mod still works; edit `config/bettercoords.json` by hand instead.

## Settings (Mod Menu → Better Coords → configure)

| Setting | Meaning |
|---|---|
| Mod | ON = coords-only F3, OFF = vanilla F3 |
| Glitch Mode | ON = the numbers become constantly shifting junk glyphs |
| Facing Line | ON = adds the vanilla-style `Facing:` line under the coordinates |
| Fake Coordinates | OFF = real coordinates, ON = fake coordinates |
| X / Y / Z | The fake coordinates for the spot you are at when you close the screen |
| Re-anchor to where I am now | Keep the numbers, but make your *current* position count as that point again |

Changing the X/Y/Z values or turning Fake Coordinates on re-anchors to your current position automatically.

## Config file

`config/bettercoords.json`

```json
{
  "enabled": true,
  "glitchEnabled": false,
  "showFacing": false,
  "fakeCoordsEnabled": false,
  "fakeX": 0.0, "fakeY": 64.0, "fakeZ": 0.0,
  "offsetX": 0.0, "offsetY": 0.0, "offsetZ": 0.0,
  "needsCalibration": true
}
```

`offset*` is computed by the mod (`fake - real` at anchor time) and `needsCalibration: true` makes it recompute on the next frame you have a position.

## Build

```
gradlew build
```

Output: `build/libs/better-coords-1.2.0.jar`. Requires JDK 21. Uses Fabric Loom 1.17 with Yarn `1.21.11+build.6`.

`gradlew runClient` launches a dev client with Mod Menu + Fabric API added (dev only; they are not bundled or required by the built jar).
