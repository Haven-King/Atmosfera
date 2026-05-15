package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Bound;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Range;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getBound;
import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getRange;

public record PercentBlockModifier(Range range, Bound bound, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    public PercentBlockModifier(Range range, Bound bound, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) {
        var blocksBuilder = ImmutableList.<Block>builder();

        // Remove blocks that are already present in tags so that they aren't counted twice
        blocks:
        for (var block : blocks) {
            for (var tag : blockTags) {
                if (block.getDefaultState().isIn(tag)) {
                    continue blocks;
                }
            }

            blocksBuilder.add(block);
        }

        this.blocks = blocksBuilder.build();
        this.blockTags = blockTags;
        this.range = range;
        this.bound = bound;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0;

        for (var block : this.blocks) {
            modifier += context.getBlockTypePercentage(block);
        }

        for (var tag : this.blockTags) {
            modifier += context.getBlockTagPercentage(tag);
        }

        if (bound != null) return bound.apply(modifier);
        if (range != null) return range.apply(modifier);
        return modifier;
    }

    public static PercentBlockModifier create(JsonObject object) {
        var blocks = ImmutableList.<Block>builder();
        var tags = ImmutableList.<TagKey<Block>>builder();

        JsonHelper.getArray(object, "blocks").forEach(block -> {
            // Registers only the loaded IDs to avoid false triggers.
            if (block.getAsString().startsWith("#")) {
                var tagId = new Identifier(block.getAsString().substring(1));
                tags.add(TagKey.of(RegistryKeys.BLOCK, tagId));
            } else {
                var blockId = new Identifier(block.getAsString());

                if (Registries.BLOCK.containsId(blockId)) {
                    Block b = Registries.BLOCK.get(blockId);
                    blocks.add(b);
                }
            }
        });

        var range = getRange(object);
        var bound = getBound(object);

        return new PercentBlockModifier(range, bound, blocks.build(), tags.build());
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }
}
