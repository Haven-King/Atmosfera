# Atmosfera Resource Pack Documentation

To get an idea of how to build Atmosfera Resource Packs, it's easiest to look at the included "dungeons" resource pack.  
It can be found under [this link](src/main/resources/resourcepacks/dungeons), or by opening the mod jar with any zip program and navigating into the `resourcepacks` directory.

Atmosfera Resource Packs are laid out like this:

```
assets/
  <namespace>/
    sounds.json
    sounds/
      ambient/
        definitions/
      music/
        definitions/
    lang/
      en_us.json
pack.mcmeta
pack.png
```

## sounds.json (Vanilla)

To be able to use sounds and music, they first have to be registered using the vanilla `sounds.json`. Here's a quick example:

```
{
  "dungeons_wind": {
    "sounds": [
      "atmosfera:ambient/wind/wind_rumble_loop_1_1",
      "atmosfera:ambient/wind/wind_rumble_loop_1_2"
    ],
    "subtitle": "subtitle.atmosfera.dungeons_wind"
  },
  "dungeons_swamp_soggy_cave": {
    "sounds": [
      {
        "name": "atmosfera:music/swamp/swamp_soggier_cave_ost",
        "stream": true
      }
    ]
  }
}
```

This means `dungeons_wind` is an ambient sound that plays one of  
`assets/atmosfera/sounds/ambient/wind/wind_rumble_loop_1_1.ogg`
`assets/atmosfera/sounds/ambient/wind/wind_rumble_loop_1_2.ogg`  
and uses a subtitle with translation key `subtitle.atmosfera.dungeons_wind` (defined in `en_us.json` or any other language code).  
Similarly `dungeons_swamp_soggy_cave` is music that plays `assets/atmosfera/sounds/music/swamp/swamp_soggier_cave_ost.ogg`.  
(The `"stream": true` means the sound file is streamed instead of loaded in its entirety, which is recommended for music but probably not a great idea for sound effects since there can only be a very limited number streamed sounds at once.)

For more details about `sounds.json`, see the [Minecraft wiki](https://minecraft.wiki/w/Sounds.json).

## Sound Definitions

`sounds/ambient/definitions` and `sounds/music/definitions` are the meat of Atmosfera Resource Packs.  
Each json file inside these directories defines either an ambient sound or a music track that plays under certain environmental conditions.  
Here's an example of what that might look like:

```
{
  "sound": "atmosfera:dungeons_wind",
  "default_volume": 100,
  "default_subtitle": true,
  "shape": "sphere",
  "size": "large",
  "modifiers": [
    {
      "type": "percent_block",
      "blocks": [
        "minecraft:air"
      ],
      "range": [
        0.7,
        1
      ]
    },
    {
      "type": "sky_visibility",
      "min": 0.5
    }
  ]
}
```
This reads roughly as "the `atmosfera:dungeons_wind` sound can play when the average sky light (as a number between 0 and 1) in a large sphere around the player is at least 0.5 and at least 70% of it is air, maxing the volume when it reaches 100%."

`"sound"` is the sound to be played, defined in `sounds.json`.

`"default_volume"` and `"default_subtitle"` (both optional) define the default Atmosfera config values for the volume (in %) and whether the subtitles are enabled.  

`"shape"` and `"size"` define the environment, aka the space around the player where the input data is being pulled from.  
`"shape"` allows: `"sphere"` (all around the player), `"upper_hemisphere"` (top half of the sphere), `"lower_hemisphere"` (bottom half of the sphere).  
`"size"` allows: `"large"` (radius 16), `"medium"` (radius 8), `"small"` (radius 4).

`"modifiers"` define the environmental conditions under which the sound should play.  
(See next section.)

The name of a definition file doesn't really matter as long as it's unique. It is good practice to use the same name as the sound it plays, e.g. the example above should be called `dungeons_wind.json`.

There's another one:
```
{
  "sound": "atmosfera:dungeons_forest_nighttime_owl",
  "sound_alias": "atmosfera:dungeons_forest_nighttime",
  ...
```

By default, only one instance of a sound with a given `"sound"` id can play.  
When there are multiple definitions with the same or similar conditions that means they would all play at the same time.  
To prevent this from happening, you can give your definition a "pretend sound id", `"sound_alias"`, which essentially groups the sounds together.  
In this example, if `"dungeons_forest_nighttime"` is playing, `"dungeons_forest_nighttime_owl"` cannot and vice versa.


### Modifiers (aka Conditions)

Modifiers look for example like this:
```
{
  "type": "sky_visibility",
  "min": 0.5
}
```
This reads as "the average sky light in the environment (as a number between 0 and 1) is at least 0.5".

Modifiers have a `"type"` and may accept different parameters.

Think of modifiers as functions, taking a certain input and outputting a number - usually between 0 and 1.  
When there are multiple modifiers, their outputs are multiplied.  
This final output is then used as a volume for ambient sounds (but not for music).  
If the final output is close to 0, the sound will not play.  
Since multiplying by 0 gives 0, modifiers are basically chained like a logical "and"; The sound will only play if all modifiers output non-0 values.

The different types of modifiers are:

#### Boolean Conditions

Parameters:

- `"value"`: boolean (optional)  
  `"value": false` will invert the condition.

Types:

- `"is_daytime"`  
  Outputs 1 if the current time is between 0 and 13000, else 0.  
  This corresponds to the time between players/villagers waking up and the `/time set night` time, roughly were mobs are starting to spawn.

- `"is_rainy"`  
  Outputs 1 if it rains, else 0.

- `"is_stormy"`  
  Outputs 1 if it is thundering, else 0.  

<details><summary>Examples</summary>

```
{
  "type": "is_rainy"
}
```
"Play only when it rains."

```
{
  "type": "is_daytime",
  "value": false
}
```
"Play only at night."

```
{
  "type": "is_daytime"
},
{
  "type": "is_rainy",
  "value": false
},
{
  "type": "is_stormy",
  "value": false
}
```
"Play only when the sun shines."
</details>

#### Bounded Conditions

Parameters:

- `"min"`: number (optional)  
- `"max"`: number (optional)  

When `min < x ≤ max` output 1, else 0. One or both can be defined.  
Cannot be used together with `"range"`.

<details><summary>Details</summary>

Input to output graphs:
```
min and max defined:     only min defined:     only max defined:
                                                                
1      ______            1      _________      1 ___________   
            |                                              |   
0 _____     |____        0 _____               0           |____
      |     |                  |                           |
    min     max              min                           max
```

A `"min"` of 0 on a `"percent_block"` would mean "there is at least one such block".

</details>

- `"range": [lower, upper]"`: numbers (optional)  
  Output linearly grows from 0 to 1 on the interval from `[lower, upper]`.  
  `lower` and `upper` can be swapped for growth from 1 to 0.  

<details><summary>Details</summary>

```
lower < upper:       upper < lower:

          ____ 1     1 ____
         /|               |\
        / |               | \
       /  |               |  \
      /   |               |   \
0 ___/    |               |    \____ 0
     |    |               |     |        
 lower    upper       upper     lower    
```

Calculates `(input - lower) / (upper - lower)` and clamps it between 0 and 1.  
 
Works similar to a `"min"` or a `"max"`, except it gives control over the output (volume). It is useful for rare events which would otherwise have a low volume, e.g. `"range": [0.1, 0.3]"` would max the volume when the input reaches 0.3.

</details>

Types:

- `"altitude"`  
  Input: The number of blocks from the player to the ground.  

- `"elevation"`  
  Input: The y-value of the player.

- `"sky_visibility"`  
  Input: The average sky light in the environment as a number between 0 and 1.  
  (More precisely it is the average of the per-block sky light divided by 15.)

#### Type: "percent_block"

Parameters:

- All [Bounded Conditions](#bounded-conditions) parameters

- `"blocks"`: array of block or block tags   
  e.g. `"blocks": ["minecraft:cave_air", "#minecraft:mineable/pickaxe"]`

Input: The percentage of matching blocks in the environment

#### Type: "percent_biome"

Parameters:

- All [Bounded Conditions](#bounded-conditions) parameters

- `"biomes"`: array of biome or biome tags (or biome categories in 1.18.2)  
  e.g. `biomes: ["minecraft:desert", "#minecraft:is_taiga"]`

Input: The percentage of matching biomes in the environment

#### Type: "dimension" ("dimension_effects" before 2.5.0)

Parameters:

- `"id"`: id of dimension type  
  In vanilla this is one of `"minecraft:overworld"`, `"minecraft:the_nether"`, `"minecraft:the_end"`, or `"minecraft:overworld_caves"`

Outputs 1 if the current dimension type match the given `id`, otherwise 0.

#### Type: "riding"

Parameters:

- `"value"`: entity id or array of entity ids  
  e.g. `"value": "minecraft:horse"`, or `"value": ["minecraft:cherry_boat", "minecraft:cherry_chest_boat"]`

Outputs 1 if currently riding a matching entity, otherwise 0.

#### Type: "boss_bar"

Parameters (one or the other):

- `"text"`: string

- `"matches"`: RegEx string

Outputs 1 if a boss bar's translation key (or name, if there's no translation key) contains the `"text"` string or matches the `"matches"` RegEx string, otherwise 0.

Common boss bars include:  
`"entity.minecraft.ender_dragon"`, `"entity.minecraft.wither"`, `"event.minecraft.raid"`

<details><summary>Example</summary>

```
{
  "type": "boss_bar",
  "text": "ender_dragon"
}
```
"Play in the ender dragon fight."

</details>