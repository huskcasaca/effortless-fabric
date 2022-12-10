# Effortless Fabric
[Effortless Fabric](https://www.curseforge.com/minecraft/mc-mods/effortless-fabric) is a fabric version of [Effortless Building](https://www.curseforge.com/minecraft/mc-mods/effortless-building).
This mod implements most features from Effortless Building, but removed randomizer bag and reach upgrade items to make it a pure vanilla compat one.

## Building

Hold ALT key to switch build modes in the radial panel. There are buttons for undo/redo, modifier settings and replace modes on the left. The options for each build mode (like a filled vs hollow wall) are on the right.

### Build Modes

- **Disable**: Place in vanilla way
- **Single**: Place single block
- **Line**: Place lines of blocks in any 3 axis
- **Wall**: Place walls with locked x or z axis
- **Floor**: Place floor with locked y axis
- **Diagonal Line**: Place freeform lines of blocks
- **Diagonal Wall**: Place walls at any angle
- **Slope Floor**: Place slopes at any angle
- **Cube**: Place cubes with 3 clicks
- **Circle**: Place blocks in a circle (ellipse)
- **Cylinder**: Place a cylindrical shape like a tower
- **Sphere**: Place a spheroid made of cubes

### Build Modifiers

- **Mirror**: Place blocks and their states in mirror, works with even and uneven builds.
- **Array**: Copies builds and block states in a certain direction a certain number of times.
- **Radial Mirror**: Places blocks in a circle around a certain point. The circle can be divided into slices, and each slide will copy your block placements.

### Replace Modes

- **Disable**: Placing blocks does not replace any existing blocks
- **Normal**: Placing blocks replaces the existing blocks except the first one
- **Quick**: Placing blocks replaces the existing blocks including the first one

## Todo
* [x] Undo/Redo
* [x] Configure screen
* [x] Commands
* [x] Magnet (pick up distant items)
* [x] Rewrite attack/use logic
* [x] Rewrite shader
* [x] Rewrite S2C/C2S logic
* [x] Wall blocks (torch) support
* [ ] Performance optimize (for large amount of blocks)
* [ ] Item randomizer
* [ ] Makkit support
* [ ] Tweakeroo support

## Changelog
### 1.5.1
* Fix undo/redo in creative mode with no matching item in inventory
### 1.5.0
* Add build info in gui
* Rearrange modifier settings entries
* Fix block preview issue in place with large coordinates
### 1.4.2
* Fix network packet issue
### 1.4.1
* Fix player dimension change issue
### 1.4.0
* Add vanilla keybindings
* Build modifier settings is no longer save to player data
* Fix some mixin missing issue on server side
* Fix contact info in mod description file
### 1.3.0
* Rearrange radial menu buttons
* Fix wall items placing issue
* Fix no hitbox item breaking issue
### 1.2.1
* Add message when closing radial menu
* Fix player attach action
* Fix new player settings issue
### 1.2.0
* Fix magnet texture issue
* Fix modifier settings
### 1.1.1
* Add cloth config and modmenu
* Fix access widener issue
### 1.1.0
* Add item magnet
### 1.0.1
* Fix left ctrl not working
### 1.0.0
* Initial release

## Credits
* **[Requioss](https://www.curseforge.com/members/requioss)**, the author of [Effortless Building](https://www.curseforge.com/minecraft/mc-mods/effortless-building) 

## License

Effortless Fabric is licensed under LGPLv3.
