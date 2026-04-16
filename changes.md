## 2.5.7

- [1.20.1, 1.21.1] revert use of classtweaker; fixes Sinytra Connector compatibility

## 2.5.6

- port to 26.1

## 2.5.5

- fix a threading issue caused by Raise Sound Limit Simplified
- fix sounds with differing definition id and sound id playing at the same time (did not affect the "Dungeons" pack)

## 2.5.4

Another 4+ year old bug!

- fix player refence of environmental context not being updated

If the player died, this would cause the mod to believe it would still be at the death location, so atmospheric sounds would never change unless you change dimensions or restart the game.

## 2.5.3

Another day/night fix!

- [1.20+] compatibility with Nostalgic Tweaks' round robin relighting (it caused the `sky_visibility` modifier to not work at night)

## 2.5.2

You know what? We didn't have any fixes for fundamentally broken features this year yet, so here you go!

- fix `is_daytime` modifier believing it is always day (you can hear owls and crickets now!)
- fix sounds never replaying after they get muted once
- (re)loading Atmospheric Resource Packs does not require reloading the world anymore

## 2.5.1

- add Traditional Chinese (zh_tw) translation, thanks to StarsShine11904!

## 2.5.0

- port to 1.21.11
- Atmospheric Resource Packs: replace `"dimension_effects"` modifier with `"dimension"`, which does exactly what you expect  
  (for backwards compatibility, `"dimension_effects"` is an alias to `"dimension"`, even if they're not exactly the same)

## 2.4.0

This update addresses many issues with Atmospheric Resource Packs:

- added [documentation](https://github.com/Haven-King/Atmosfera/blob/main/documentation.md) for how to create your own Atmospheric Resource Packs!
- fix `"min"` and `"max"` not doing anything for many modifiers like `"percent_biome"` and bounded modifiers (the irony...)
- add `"range"` to all bounded modifiers
- fix `"default_volume"` and `"default_subtitle"` not being read or used
- some modifiers had alternative type names which have now been removed
- the internal "Dungeons" Atmospheric Resource Packs has been updated to use newer blocks, biomes and tags, including common "c" tags, which should improve modded biome detection
- config logic has partially been rewritten

## 2.3.1

- adjust thread pool for environment context update

the pool works like it did before 2.1.0 except it only uses 2 threads
this also fixes an issue where small and medium sphere update tasks would run less often than intended

- run environment context update at most every second

saves some wattage, probably has little to no effect on FPS/TPS

## 2.3.0

- port to 1.21.9
- fix another 2 bugs in how music was chosen - these should be the last!

the bugs resulted in custom music being less likely to play than intended and some custom tracks being skipped

- add a "Custom Music Weight Scale" option, to tune the likelihood of playing custom music

this is by default set to 250% as custom music was very rare to play, since there's so little of it