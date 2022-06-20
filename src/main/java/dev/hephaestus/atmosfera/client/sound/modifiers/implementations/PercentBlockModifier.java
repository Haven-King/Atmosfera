package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public record PercentBlockModifier(float lowerVolumeSlider, float upperVolumeSlider, float min, float max, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    public PercentBlockModifier(float lowerVolumeSlider, float upperVolumeSlider, float min, float max, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) {
        ImmutableCollection.Builder<Block> blocksBuilder = ImmutableList.builder();

        // Remove blocks that are already present in tags so that they aren't counted twice
        blocks:
        for (Block block : blocks) {
            for (TagKey<Block> tag : blockTags) {
                if (block.getDefaultState().isIn(tag)) {
                    continue blocks;
                }
            }

            blocksBuilder.add(block);
        }

        this.blocks = blocksBuilder.build();
        this.blockTags = blockTags;
        this.lowerVolumeSlider = lowerVolumeSlider;
        this.upperVolumeSlider = upperVolumeSlider;
        this.min = min;
        this.max = max;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0F;

        for (Block block : this.blocks) {
            modifier += context.getBlockTypePercentage(block);
        }

        for (TagKey<Block> tag : this.blockTags) {
            modifier += context.getBlockTagPercentage(tag);
        }

        return modifier >= this.lowerVolumeSlider && modifier >= this.min && modifier <= this.max
                ? (modifier - this.lowerVolumeSlider) * (1.0F / (this.upperVolumeSlider - this.lowerVolumeSlider))
                : 0;
    }

    public static PercentBlockModifier create(JsonObject object) {
        ImmutableCollection.Builder<Block> blocks = ImmutableList.builder();
        ImmutableCollection.Builder<TagKey<Block>> tags = ImmutableList.builder();

        JsonHelper.getArray(object, "blocks").forEach(block -> {
            // Registers only the loaded IDs to avoid false triggers.
            if (block.getAsString().startsWith("#")) {
                Identifier tagId = new Identifier(block.getAsString().substring(1));
                TagKey<Block> tagKey = TagKey.of(Registry.BLOCK_KEY, tagId);
                tags.add(tagKey);
            } else {
                Identifier blockId = new Identifier(block.getAsString());

                if (Registry.BLOCK.containsId(blockId)) {
                    Block b = Registry.BLOCK.get(blockId);
                    blocks.add(b);
                }
            }
        });

        float lowerVolumeSlider = 0, upperVolumeSlider = 1;

        if (object.has("range")) {
            JsonArray array = object.getAsJsonArray("range");
            lowerVolumeSlider = array.get(0).getAsFloat();
            upperVolumeSlider = array.get(1).getAsFloat();
        }

        float min = object.has("min") ? object.get("min").getAsFloat() : -Float.MAX_VALUE;
        float max = object.has("max") ? object.get("max").getAsFloat() : Float.MAX_VALUE;

        return new PercentBlockModifier(lowerVolumeSlider, upperVolumeSlider, min, max, blocks.build(), tags.build());
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }
}
