package dev.hephaestus.atmosfera.conditions;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PercentBlock extends BoundedCondition {
    ArrayList<Block> blocks = new ArrayList<>();

    public PercentBlock(JsonObject conditions) {
        super(conditions);

        for (JsonElement block : conditions.getAsJsonObject().getAsJsonArray("blocks") ) {
            Block b = Registry.BLOCK.get(new Identifier(block.getAsString()));
            if (b == null) { System.out.println("Atmosfera - No such block: " + block.getAsString()); }
            else { blocks.add(b); }
        }
    }

    @Override
    protected float getValue(VolumeData volume) {
        return volume.percentBlockType(blocks);
    }
}