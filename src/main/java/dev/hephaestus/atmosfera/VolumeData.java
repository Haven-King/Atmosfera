package dev.hephaestus.atmosfera;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

import static net.minecraft.util.math.MathHelper.sqrt;

public class VolumeData {
    public HashSet<BlockPos> blockPositions;
    private HashMap<Block, Integer> blockTypes;
    private HashMap<Biome, Integer> blockBiomes;
    private BlockPos origin;
    private World world;
    private int sky;

    private final Type type;
    private final Direction direction;
    private final int radius;

    public enum Type {
        SAMPLE_HEMISPHERE,
        WITHIN_HEMISPHERE,
        SAMPLE_SPHERE,
        WITHIN_SPHERE
    };

    // -------------------------------------------------------------------------------------------- //
    // --- Constructors and pseudo-constructors --------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    public VolumeData(Type type, @Nullable Direction direction, int radius) {
        // Counters
        this.blockPositions = new HashSet<>();
        this.blockTypes = new HashMap<>();
        this.blockBiomes = new HashMap<>();
        this.sky = 0;

        // Instance specific
        this.direction = direction;
        this.radius = radius;
        this.type = type;
    }

    public void empty() {
        this.blockPositions = new HashSet<>();
        this.blockTypes = new HashMap<>();
        this.blockBiomes = new HashMap<>();
        this.sky = 0;
    }

    // --- Internal only -------------------------------------------------------------------------- //


    public VolumeData update(World world, BlockPos origin) {
        this.empty();

        this.world = world;
        this.origin = origin;
    

        switch(this.type) {
            case SAMPLE_HEMISPHERE:
                if (direction == Direction.UP)      { this.sampleUpperHemisphere(); } else
                if (direction == Direction.DOWN)    { this.sampleLowerHemisphere(); }
                else {
                    this.sampleUpperHemisphere();
                    this.sampleLowerHemisphere();
                }
                break;
            case SAMPLE_SPHERE:
                this.sampleUpperHemisphere();
                this.sampleLowerHemisphere();
                break;
            case WITHIN_SPHERE:
            case WITHIN_HEMISPHERE:
                this.withinHemisphere();
                break;
        }

        return this;
    }


    // -------------------------------------------------------------------------------------------- //
    // --- Public getters for conditions ---------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    public BlockPos getOrigin() {
        return this.origin;
    }

    public World getWorld() {
        return this.world;
    }

    public float percentBlockType(ArrayList<Block> blocks) {
        float result = 0.0f;
        for (Block block : blocks) {
            result += blockTypes.getOrDefault(block, 0);
        }

        return result / ((float) blockPositions.size());
    }

    public float percentBlockBiome(ArrayList<Biome> biomes) {
        float result = 0.0f;
        for (Biome biome : biomes) {
            result += blockBiomes.getOrDefault(biome, 0);
        }

        return result / ((float) blockPositions.size());
    }

    public float percentSkyVisible() {
        return (float)this.sky/(float)blockPositions.size();
    }

    public int distanceFromGround() {
		int result = 0;
		for (BlockPos current = origin; this.world.getBlockState(current).getBlock() == Blocks.AIR; current = current.add(0,-1,0)) {
			result += 1;
		}

		return result;
	}

    // -------------------------------------------------------------------------------------------- //
    // --- Private methods for data volume manipulation ------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //

    private void add(World world, BlockPos block) {
        blockPositions.add(block);
        blockTypes.merge(world.getBlockState(block).getBlock(), 1, Integer::sum);
        blockBiomes.merge(world.getBiome(block), 1, Integer::sum);

        for ( BlockPos current = block; world.getBlockState(current).getBlock().getClass() == LeavesBlock.class || world.getBlockState(current).getBlock() == Blocks.AIR; current = current.add(0,1,0)) {
            if (world.isSkyVisible(current)) {
                sky += 1;
                break;
            }
        }
    }

    // -------------------------------------------------------------------------------------------- //
    // --- Private static methods used by the pseudo constructors --------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    private void sampleLowerHemisphere() {
        BlockPos block = this.origin.add(0,-2,0);
        float mid = sqrt(((radius * radius) / 2.0F));
        float tri = sqrt(((radius * radius) / 3.0F));

        // For efficiency purposes, we sample 17 points on hemisphere's surface + the origin
        float[][] offsets = {
                {this.radius,0,0},
                {0,0,this.radius},
                {-this.radius,0,0},
                {0,-this.radius,0},
                {0,0,-this.radius},

                {mid, -mid, 0},
                {-mid, -mid, 0},

                {0, -mid, mid},
                {0, -mid, -mid},

                {mid, 0, mid},
                {mid, 0, -mid},
                {-mid, 0, -mid},
                {-mid, 0, mid},

                {-tri,-tri,-tri},
                {-tri,-tri,tri},
                {tri, -tri, -tri},
                {tri, -tri, tri},

                {0,0,0}
        };

        for (float[] offset : offsets) {
            this.add(world, block.add(offset[0], offset[1], offset[2]));
        }
    }

    private void sampleUpperHemisphere() {
        BlockPos block = this.origin.add(0,2,0);
        float mid = sqrt(((this.radius * this.radius) / 2.0F));
        float tri = sqrt(((this.radius * this.radius) / 3.0F));

        // For efficiency purposes, we sample 17 points on hemisphere's surface + the origin
        float[][] offsets = {
                {this.radius, 0, 0},
                {0, this.radius, 0},
                {-this.radius, 0, 0},
                {0, 0, -this.radius},

                {mid, mid, 0},
                {-mid, mid, 0},

                {0, mid, mid},
                {0, mid, -mid},

                {mid, 0, mid},
                {mid, 0, -mid},
                {-mid, 0, -mid},
                {-mid, 0, mid},

                {tri, tri, tri},
                {tri, tri, -tri},
                {-tri,tri,tri},
                {-tri,tri,-tri},

                {0,0,0}
        };

        for (float[] offset : offsets) {
            this.add(world, block.add(offset[0], offset[1], offset[2]));
        }
    }

    private void withinHemisphere() {
        int floor  = -this.radius;
        int ceil = this.radius;

        if (direction == Direction.UP || direction == Direction.DOWN) {
            floor = direction == Direction.UP ? 0 : -this.radius;
            ceil = direction == Direction.UP ? this.radius : 0;
        } else {
            floor = -this.radius;
            ceil = this.radius;
        }

        for (int x = -radius; x <= radius; ++x) {
            for (int y = floor; y <= ceil; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos current = this.origin.add(x, y, z);
                    if (this.origin.isWithinDistance(current, radius)) {
                        this.add(world, current);
                    }
                }
            }
        }
    }
}
