package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public record PercentBlockModifier(float min, float max, ImmutableCollection<Block> blocks, ImmutableCollection<Tag<Block>> blockTags) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    public PercentBlockModifier(float min, float max, ImmutableCollection<Block> blocks, ImmutableCollection<Tag<Block>> blockTags) {
        ImmutableCollection.Builder<Block> blocksBuilder = ImmutableList.builder();

        // Remove blocks that are already present in tags so that they aren't counted twice
        blocks:
        for (Block block : blocks) {
            for (Tag<Block> tag : blockTags) {
                if (tag.contains(block)) {
                    continue blocks;
                }
            }

            blocksBuilder.add(block);
        }

        this.blocks = blocksBuilder.build();
        this.blockTags = blockTags;
        this.min = min;
        this.max = max;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0F;

        for (Block block : this.blocks) {
            modifier += context.getBlockTypePercentage(block);
        }

        for (Tag<Block> tag : this.blockTags) {
            modifier += context.getBlockTagPercentage(tag);
        }

        return modifier >= this.min
                ? (modifier - this.min) * (1.0F / (this.max - this.min))
                : 0;
    }

    public static PercentBlockModifier create(JsonObject object) {
        ImmutableCollection.Builder<Block> blocks = ImmutableList.builder();
        ImmutableCollection.Builder<Tag<Block>> tags = ImmutableList.builder();

        JsonHelper.getArray(object, "blocks").forEach(block -> {


            // Registers only the loaded IDs to avoid false triggers.
            if (block.getAsString().startsWith("#")) {
                tags.add(TagRegistry.block(new Identifier(block.getAsString().substring(1))));
            } else {
                Identifier blockId = new Identifier(block.getAsString());

                if (Registry.BLOCK.containsId(blockId)) {
                    Block b = Registry.BLOCK.get(blockId);
                    blocks.add(b);
                }
            }
        });

        float min = 0, max = 1;

        if (object.has("range")) {
            JsonArray array = object.getAsJsonArray("range");
            min = array.get(0).getAsFloat();
            max = array.get(1).getAsFloat();
        }

        return new PercentBlockModifier(min, max, blocks.build(), tags.build());
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }
}
