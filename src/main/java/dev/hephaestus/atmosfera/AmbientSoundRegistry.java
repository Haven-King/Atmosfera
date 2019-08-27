package dev.hephaestus.atmosfera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dev.hephaestus.atmosfera.AmbientSound;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Environment(EnvType.CLIENT)
public class AmbientSoundRegistry {
    private static HashMap<Identifier,AmbientSound> registeredSounds = new HashMap<>();

    public static Collection<AmbientSound> getRegistered() {
        return registeredSounds.values();
    }

    public static void removeRegistered() {
        registeredSounds = new HashMap<>();
    }

    public static void register(JsonObject json) {
        Identifier identifier;
        if (json.get("sound") == null) {
            System.out.println("Atmosfera - no sound provided!");
            throw new IllegalArgumentException();
        } else {
            identifier = new Identifier(json.get("sound").getAsString());
        }

        System.out.println("Atmosfera - Registering " + identifier);

        SoundEvent soundEvent;
        if (Registry.SOUND_EVENT.containsId(identifier)) {
            soundEvent = Registry.SOUND_EVENT.get(identifier);
        } else {
            soundEvent = Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
        }
        
        AmbientSound sound = new AmbientSound(soundEvent);

        if (json.get("volume") == null) {
            sound.setMaxVolume(1.0f);
        } else {
            sound.setMaxVolume(json.get("volume").getAsFloat());
        }

        if (json.get("max_age") == null) {
            sound.setMaxAge(0.0f);
        } else {
            sound.setMaxAge(json.get("max_age").getAsFloat());
        }

        if (json.get("repeat_delay") == null) {
            sound.setDelay(0.0f);
        } else {
            sound.setDelay(json.get("repeat_delay").getAsFloat());
        }

        if (json.get("data_volume") == null) {
            sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleArea(pair.getLeft(), pair.getRight()));
        } else {
            JsonObject dv = json.get("data_volume").getAsJsonObject();

            String type;
            int radius;
            
            if (dv.get("type") == null) {
                type = "sample";
            } else {
                type = dv.get("type").getAsString();
            }

            if (dv.get("radius") == null) {
                radius = 16;
            } else {
                radius = dv.get("radius").getAsInt();
            }

            if (dv.get("direction") == null) {
                switch(type) {
                    case "sample":
                        sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleArea(pair.getLeft(), pair.getRight(), radius));
                        break;

                    case "radius":
                        sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.withinRadius(pair.getLeft(), pair.getRight(), radius));
                        break;

                    default:
                        System.out.println("Atmosfera - Invalid data_volume type in " + identifier);
                }
            } else {
                switch(dv.get("direction").getAsString()) {
                    case "up":
                        switch(type) {
                            case "sample":
                                sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleUpperHemisphere(pair.getLeft(), pair.getRight(), radius));
                                break;
                            case "radius":
                                sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleUpperHemisphere(pair.getLeft(), pair.getRight(), radius));
                                break;
                            default:
                                System.out.println("Atmosfera - Invalid data_volume type in " + identifier);
                        }
                        break;

                    case "down":
                        switch(type) {
                            case "sample":
                                sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleLowerHemisphere(pair.getLeft(), pair.getRight(), radius));
                                break;
                            case "radius":
                                sound.setDataVolume((Pair<World, BlockPos> pair) -> VolumeData.sampleLowerHemisphere(pair.getLeft(), pair.getRight(), radius));
                                break;
                            default:
                                System.out.println("Atmosfera - Invalid data_volume type in " + identifier);
                        }
                        break;

                    default:
                        System.out.println("Atmosfera - Invalid data_volume direction in " + identifier);
                }
            }
        }

        for (Map.Entry<String,JsonElement> element : json.get("conditions").getAsJsonObject().entrySet()) {
            switch(element.getKey()) {
                case "percent_block":
                    for ( JsonElement entry : element.getValue().getAsJsonArray()) {
                        ArrayList<Block> blocks = new ArrayList<>();

                        for (JsonElement block : entry.getAsJsonObject().getAsJsonArray("blocks") ) {
                            Block b = Registry.BLOCK.get(new Identifier(block.getAsString()));
                            if (b == null) {
                                System.out.println("Atmosfera - No such block: " + block.getAsString());
                            } else {
                                blocks.add(b);
                            }
                        }

                        for (Map.Entry<String,JsonElement> pair : entry.getAsJsonObject().entrySet()) {
                            switch(pair.getKey()) {
                                case "more":
                                    sound.addCondition((VolumeData x) -> x.percentBlockType(blocks) > pair.getValue().getAsFloat());
                                    break;

                                case "less":
                                    sound.addCondition((VolumeData x) -> x.percentBlockType(blocks) < pair.getValue().getAsFloat());
                                    break;

                                case "blocks":
                                    break;

                                default:
                                    System.out.println("Atmosfera - Invalid configuration in " + identifier + " for percent_block:\n\tNo trait " + pair.getKey());
                            }
                        }
                    }
                    break;

                case "percent_biome":
                    for ( JsonElement entry : element.getValue().getAsJsonArray()) {
                        ArrayList<Biome> biomes = new ArrayList<>();

                        for (JsonElement biome : entry.getAsJsonObject().getAsJsonArray("biomes") ) {
                            Biome b = Registry.BIOME.get(new Identifier(biome.getAsString()));
                            if (b == null) {
                                System.out.println("Atmosfera - No such biome: " + biome.getAsString());
                            } else {
                                biomes.add(b);
                            }
                        }

                        for (Map.Entry<String,JsonElement> pair : entry.getAsJsonObject().entrySet()) {
                            switch(pair.getKey()) {
                                case "more":
                                    sound.addCondition((VolumeData x) -> x.percentBlockBiome(biomes) > pair.getValue().getAsFloat());
                                    break;

                                case "less":
                                    sound.addCondition((VolumeData x) -> x.percentBlockBiome(biomes) < pair.getValue().getAsFloat());
                                    break;

                                case "biomes":
                                    break;

                                default:
                                    System.out.println("Atmosfera - Invalid configuration in " + identifier + " for percent_biome:\n\tNo trait " + pair.getKey());
                            }
                        }
                    }
                    break;

                case "height":
                    for (Map.Entry<String,JsonElement> pair : element.getValue().getAsJsonObject().entrySet()) {
                        switch(pair.getKey()) {
                            case "more":
                                sound.addCondition((VolumeData x) -> x.getOrigin().getY() > pair.getValue().getAsInt());
                                break;
                            case "less":
                                sound.addCondition((VolumeData x) -> x.getOrigin().getY() < pair.getValue().getAsInt());
                                break;
                            default:
                                System.out.println("Atmosfera - Invalid configuration in " + identifier + " for height:\n\tNo condition " + pair.getKey());
                        }
                    }
                    break;

                case "distance_from_ground":
                    for (Map.Entry<String,JsonElement> pair : element.getValue().getAsJsonObject().entrySet()) {
                        switch(pair.getKey()) {
                            case "more":
                                sound.addCondition((VolumeData x) -> x.distanceFromGround() > pair.getValue().getAsFloat());
                                break;
                            case "less":
                                sound.addCondition((VolumeData x) -> x.distanceFromGround() < pair.getValue().getAsFloat());
                                break;
                            default:
                                System.out.println("Atmosfera - Invalid configuration in " + identifier + " for distance_from_ground:\n\tNo condition " + pair.getKey());
                        }
                    }
                    break;

                case "percent_sky_visible":
                    for (Map.Entry<String,JsonElement> pair : element.getValue().getAsJsonObject().entrySet()) {
                        switch(pair.getKey()) {
                            case "more":
                                sound.addCondition((VolumeData x) -> x.percentCanSeeSkyThroughLeaves() > pair.getValue().getAsFloat());
                                break;
                            case "less":
                                sound.addCondition((VolumeData x) -> x.distanceFromGround() < pair.getValue().getAsFloat());
                                break;
                            default:
                                System.out.println("Atmosfera - Invalid configuration in " + identifier + " for percent_sky_visible:\n\tNo condition " + pair.getKey());
                        }
                    }
                    break;

                case "is_daytime":
                    if (element.getValue().getAsBoolean()) {
                        sound.addCondition((VolumeData x) -> x.world.getTimeOfDay() > 450 && x.world.getTimeOfDay() < 11616 );
                    } else {
                        sound.addCondition((VolumeData x) -> x.world.getTimeOfDay() < 450 || x.world.getTimeOfDay() > 11616 );
                    }
                    break;
            }
        }
        registeredSounds.put(identifier, sound);
    }
}