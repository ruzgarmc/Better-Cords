# Better Coords

Fabric client mod for Minecraft **1.21.11**. Author: **ruzgar**.

- **Mod ON:** pressing F3 shows only one line in the top-left: `XYZ: x  y  z`. No other debug text, charts or the 3D crosshair gizmo.
- **Mod OFF:** the vanilla F3 screen, untouched.
- **Facing Line:** optional second line, worded exactly like vanilla:
  `Facing: south (Towards positive Z) (-0.6 / 2.7)`.
- **Glitch:** replaces the numbers with shifting junk glyphs — arrows, box drawing, maths,
  Greek, Cyrillic and katakana. Each character runs on its own timer, so the line churns instead of
  flipping over all at once, and the spaces, slashes and brackets are kept so it still reads as a
  coordinate line. Applies to the facing line too.
- **Fake Coordinates:** when enabled, the spot you are standing on is displayed as the X/Y/Z you typed, and moving offsets from there.
  Example: you are at `50 100 50` and enter `1000 100 1000` → F3 shows `1000 100 1000`; walk 100 blocks in +Z → `1000 100 1100`.
- **Toggle keys:** bind a key for the mod and a key for fake coordinates, straight from the mod's own
  settings screen. Pressing one flips the setting and says so above the hotbar.

## Install

1. Fabric Loader ≥ 0.19.5 for Minecraft 1.21.11.
2. Drop `better-coords-1.3.0.jar` into your `mods` folder.
3. Optional but recommended: [Mod Menu](https://modrinth.com/mod/modmenu) (+ Fabric API, which Mod Menu needs) to get the settings screen.
   Without Mod Menu the mod still works; edit `config/bettercoords.json` by hand instead.

## Settings (Mod Menu → Better Coords → configure)

| Setting | Meaning |
|---|---|
| Mod | ON = coords-only F3, OFF = vanilla F3 |
| Glitch | ON = the numbers become constantly shifting junk glyphs |
| Facing Line | ON = adds the vanilla-style `Facing:` line under the coordinates |
| Fake Coords | OFF = real coordinates, ON = fake coordinates |
| Keys → Mod | Key that toggles the mod in game |
| Keys → Fake coords | Key that toggles fake coordinates in game |
| X / Y / Z | The fake coordinates for the spot you are at when you close the screen |
| Re-anchor to where I am now | Keep the numbers, but make your *current* position count as that point again |

Click a key button, then press the key you want; Escape clears it. The keys only fire while no screen
is open, and the press is passed on to the game, so a key you also use for something else still works.
Turning fake coordinates on with the key anchors them to wherever you are standing at that moment.

## Config file

`config/bettercoords.json`

```json
{
  "enabled": true,
  "glitchEnabled": false,
  "showFacing": false,
  "toggleModKey": -1,
  "toggleFakeCoordsKey": -1,
  "fakeCoordsEnabled": false,
  "fakeX": 0.0, "fakeY": 64.0, "fakeZ": 0.0,
  "offsetX": 0.0, "offsetY": 0.0, "offsetZ": 0.0,
  "needsCalibration": true
}
```

`toggleModKey` / `toggleFakeCoordsKey` are GLFW key codes, `-1` meaning unbound. `offset*` is computed
by the mod (`fake - real` at anchor time) and `needsCalibration: true` makes it recompute on the next
frame you have a position.

## Build

```
gradlew build
```

Output: `build/libs/better-coords-1.3.0.jar`. Requires JDK 21. Uses Fabric Loom 1.17 with Yarn `1.21.11+build.6`.

`gradlew runClient` launches a dev client with Mod Menu + Fabric API added (dev only; they are not bundled or required by the built jar).
