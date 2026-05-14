package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Bound;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Range;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getBound;
import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getRange;

public record PercentBlockModifier(Range range, Bound bound, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    public PercentBlockModifier(Range range, Bound bound, ImmutableCollection<Block> blocks, ImmutableCollection<TagKey<Block>> blockTags) {
        var blocksBuilder = ImmutableList.<Block>builder();

        // Remove blocks that are already present in tags so that they aren't counted twice
        blocks:
        for (var block : blocks) {
            for (var tag : blockTags) {
                if (block.defaultBlockState().is(tag)) {
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

        GsonHelper.getAsJsonArray(object, "blocks").forEach(block -> {
            // Registers only the loaded IDs to avoid false triggers.
            if (block.getAsString().startsWith("#")) {
                var tagId = Identifier.parse(block.getAsString().substring(1));
                tags.add(TagKey.create(Registries.BLOCK, tagId));
            } else {
                var blockId = Identifier.parse(block.getAsString());

                if (BuiltInRegistries.BLOCK.containsKey(blockId)) {
                    Block b = BuiltInRegistries.BLOCK.getValue(blockId);
                    blocks.add(b);
                }
            }
        });

        var range = getRange(object);
        var bound = getBound(object);

        return new PercentBlockModifier(range, bound, blocks.build(), tags.build());
    }

    @Override
    public AtmosphericSoundModifier create(Level level) {
        return this;
    }
}
