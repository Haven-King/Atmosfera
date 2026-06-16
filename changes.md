## 2.7.1

- port to 26.2

## 2.7.0

Small adjustments with large implications!

- [1.19+] fix biome tag data not resetting  
  previously entering and leaving a biome would not reset the biome tag percentage, meaning sounds from that biome could still play. This bug has existed since 2.0.0 for 1.19.
- redefine `sky_visibility`: Instead of calculating the percentage of blocks with direct sight of the sky, calculate the average sky light (as a number between 0 and 1).  
  This is to address an issue where sounds could stop completely inside dense forests. `sky_visibility` is often used to distinguish between overground and underground and since leaf blocks lower sky light by at least 1, everything below a leaf block was treated the same as being underground in that sense.
  note: the new `sky_visibility` is generally much higher in value and the Dungeons resource pack has been adjusted accordingly
- most `"max"`/`"min"`s in the Dungeons resource pack have been turned into `"range"`s so the volume scales smoothly instead of sounds stopping abruptly
- the sound events `dungeons_forest_rainy`, `dungeons_forest_snowy_daytime`, and `dungeons_forest_snowy_nighttime` have been split up into "howls" variants for consistency too
- fix tooltips for "howls" variants
- [1.18 - 1.19] fix mod requiring `fabric-api` instead of `fabric` and hence not being able to load the game (regression of 2.6.0)

## 2.6.1

- add Russian (ru_ru) translation, thanks to itsrec0very!

## 2.6.0

This is a lot.

- `max`/`min` bounded conditions have been changed to return 1 instead of the original value (like they were originally intended to).  
  this partially fixes cave sounds not playing at all
- cave sounds don't require `cave_air` anymore (as it is almost non-existent in modern versions) and rely on `altitude` instead  
  this fully fixes cave sounds not playing at all
- add back `is_submerged` boolean condition (lost some 5 years ago) which checks if the player is submerged in water or lava
- all current sounds are now blocked when the player is submerged
- add optional `sound_alias` to sound definitions, which essentially groups together sounds so they won't play when another sound with the same id or alias plays
- owl hooting sounds are now aliased to the original nighttime sounds they came from (meaning a lot less hooting)
- owls now only hoot on clear weather
- the order sounds are considered in is now shuffled, so certain sounds won't consistently 'win' over other sounds with the same or similar conditions (this also means less hooting)
- cave speleothems sounds now play with pointed dripstone nearby
- cave speleothems volume has been lowered
- many wind sounds have been lowered in volume and most howling sounds were separated for the config (though it's not possible to completely separate them)
- `dungeons_wind` ("Wind blows" which plays when almost completely surrounded with air) now requires more air around the player
- optimize checking for already playing sounds
- music is now streamed (should reduce audio lag)
- [1.21+] fix closed captions not showing
- [26.1+] fix `is_day` using the wrong clock (it now uses the dimension's default clock)

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