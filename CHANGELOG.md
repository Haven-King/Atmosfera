# Changelog
The following acknowledges the notable changes for each release of the **Atmosfera** project.

Versioning Format: [MAJOR.MINOR.PATCH-PRE+BUILD](https://semver.org/spec/v2.0.0.html)

Date Format: [YYYY-MM-DD](https://www.iso.org/iso-8601-date-and-time-format.html)

## 1.4.0 (Unreleased)
* Added "Rainy", "Stormy" and "Submerged" conditions.
* Added block tag support for modifiers.
* Added settings to toggle on/off individual subtitles.
* Added music support. ([#4](https://github.com/Haven-King/Atmosfera/issues/4))
* Added "ambient sound", "music" subcategories and tooltips to the configuration screen.
* Added the "Dungeons" resource pack containing 28 ambient sound and 7 music events with 78 sounds. ([#5](https://github.com/Haven-King/Atmosfera/issues/5))
* Added compatibility support for "Biome Makeover", "Ecotones", "Lakeside", "Oh The Biomes You'll Go", "Promenade", "River Redux", "Sakura Rosea", "Terrestria", "Traverse", "Vanilla Enhanced", "Vanilla+ Biomes", "Wild World" and "Woods and Mires" mods.
* Updated the "Legacy" resources to be archived. The pack is deprecated and might be removed in a future release.
* Updated the mod dependencies and metadata. ([#15](https://github.com/Haven-King/Atmosfera/issues/15))
* Fixed the playback to restore smooth loops, repeats, transitions and subtitles. ([#10](https://github.com/Haven-King/Atmosfera/issues/10))
* Fixed sound events not stopping after exiting the world.
* Fixed sound events getting triggered multiple times at volumes near zero.
* Fixed the direction detection for block based modifiers.
* Fixed data types and values of "more", "less" and "range".
* Fixed the crash caused by opening the debug screen with the profiler graph (`Shift`+`F3`). ([#14](https://github.com/Haven-King/Atmosfera/issues/14))
* Changed the daytime period to start at 0 ticks (06:00), end at 12000 ticks (18:00).
* Changed the mod resources as built-in resource packs.
* Changed the ID separators and location of subtitles to utilize the language file in the configuration screen.

## 1.3.5 (2020-10-30)
* Added pitch modifier for when the player is submerged in fluid.
* Migrated conditions and modifiers to a registry system to allow other mods to add conditions and modifiers easily.
* Moved processing off-thread. Should lead to massively improved performance.
* Fixed invalid config elements causing the game to crash. Config entries will not be added for sounds that aren't registered.
* Fixed the config applying improperly on the restart.

## 1.3.3 (2020-09-21)
* Added support for 1.16.3.
* Updated the build system and metadata.

## 1.3.2 (2020-09-06)
* Adds missing translation for daytime swamp sounds.
* Fixes sounds not loading on some machines with non-English localization.

## 1.3.1 (2020-09-06)
* Fixes crash when Mod Menu is not present.
* Fixes crash on dedicated servers.
* Reduces default volume for daytime swamp sounds.
* Now properly requires Cloth Config.

## 1.3.0 (2020-08-31)
* Atmosfera has been rewritten! The config and sound definition schemas have changed, so you'll need to make some adjustments there. Sounds now adjust to different levels, and fade in and out more smoothly. Should also have improved performance.

## 1.2.2 (2020-06-27)
* Added a "dimension" tag to sound definitions. This fixes the lag caused by using Atmosfera in the End.

## 1.2.1 (2020-04-19)
* The default soundpack is now included in the jar. Default volumes adjusted slightly.

## 1.2.0 (2020-03-25)
* Moved config screen to ModMenu.
* Note: if you update to this version you **must** use the [updated sound pack](https://hephaestus.dev/atmosfera/) as well.

## 1.1.5 (2020-03-17)
* Added support for 1.15.
* Fixed the "Reset All" button on the sounds option screen.

## 1.1.3 (2019-09-08)
* Added settings screen to adjust the volume of individual sounds (Options -> Music & Sounds -> Atmosfera).
* Added language file to Atmosfera-Sounds to display sound names on the settings screen.

## 1.1.2 (2019-08-28)
* Sounds should now transition smoothly.

## 1.1.1 (2019-08-27)
* Migrated sounds to a resource pack to allow dynamic content creation.
