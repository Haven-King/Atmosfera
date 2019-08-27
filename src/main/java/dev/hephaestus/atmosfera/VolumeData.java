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

import static java.lang.Math.round;
import static net.minecraft.util.math.MathHelper.sqrt;

public class VolumeData {
    public HashSet<BlockPos> blockPositions;
    private HashMap<Block, Integer> blockTypes;
    private HashMap<Biome, Integer> blockBiomes;
    private BlockPos origin;
    private int sky;
    public World world;

    // -------------------------------------------------------------------------------------------- //
    // --- Constructors and pseudo-constructors --------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected VolumeData(World world, BlockPos origin) {
        this.blockPositions = new HashSet<>();
        this.blockTypes = new HashMap<>();
        this.blockBiomes = new HashMap<>();
        this.origin = origin;
        this.world = world;
        this.sky = 0;
    }

    // --- Sampling pseudo-constructors. Use these whenever possible ------------------------------ //
    public static VolumeData sampleUpperHemisphere(World world, BlockPos block, int radius) {
        VolumeData result = upperHemisphere(world, block, radius);
        return result.merge(upperHemisphere(world, block, round(sqrt(radius))));
    }

    public static VolumeData sampleLowerHemisphere(World world, BlockPos block, int radius) {
        VolumeData result = lowerHemisphere(world, block, radius);
        return result.merge(lowerHemisphere(world, block, round(sqrt(radius))));
    }

    public static VolumeData sampleArea(World world, BlockPos block, int radius) {
        VolumeData upper = sampleUpperHemisphere(world, block, radius);
        return upper.merge(sampleLowerHemisphere(world, block, radius));
    }

    public static VolumeData sampleArea(World world, BlockPos block) {
        return sampleArea(world, block, 16);
    }

    // --- Much more intensive. Avoid unless absolutely necessary --------------------------------- //
    public static VolumeData withinRadius(World world, BlockPos block, int radius) {
        VolumeData result = withinHemisphere(world, block, Direction.UP, radius);
        result.merge(withinHemisphere(world, block, Direction.DOWN, radius));
        return result;
    }

    // --- Internal only -------------------------------------------------------------------------- //
    public static VolumeData withinHemisphere(World world, BlockPos block, Direction direction, int radius) {
        int floor  = -radius;
        int ceil = radius;

        if (direction == Direction.UP || direction == Direction.DOWN) {
            floor = direction == Direction.UP ? 0 : -radius;
            ceil = direction == Direction.UP ? radius : 0;
        } else {
            throw new IllegalArgumentException("direction");
        }


        VolumeData result = new VolumeData(world, block);
        for (int x = -radius; x <= radius; ++x) {
            for (int y = floor; y <= ceil; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos current = block.add(x, y, z);
                    if (block.isWithinDistance(current, radius)) {
                        result.add(current);
                    }
                }
            }
        }

        return result;
    }



    // -------------------------------------------------------------------------------------------- //
    // --- Protected getters for conditions ------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected int size() {
        return blockPositions.size();
    }

    protected BlockPos getOrigin() {
        return this.origin;
    }

    protected float percentBlockType(ArrayList<Block> blocks) {
        float result = 0.0f;
        for (Block block : blocks) {
            result += blockTypes.getOrDefault(block, 0);
        }

        return result / ((float) this.size());
    }

    protected float percentBlockBiome(ArrayList<Biome> biomes) {
        float result = 0.0f;
        for (Biome biome : biomes) {
            result += blockBiomes.getOrDefault(biome, 0);
        }

        return result / ((float) this.size());
    }

    protected float percentCanSeeSkyThroughLeaves() {
        float result = 0f;

        for (BlockPos block : this.blockPositions) {
            for ( BlockPos current = block; world.getBlockState(current).getBlock().getClass() == LeavesBlock.class || world.getBlockState(current).getBlock() == Blocks.AIR; current = current.add(0,1,0)) {
                if (world.isSkyVisible(current)) {
                    result += 1.0f;
                    break;
                }
            }
        }

        return result / ((float)this.size());
    }

    protected float percentSkyVisible() {
        return (float)this.sky/(float)this.size();
    }

    protected int distanceFromGround() {
		int result = 0;
		for (BlockPos current = origin; world.getBlockState(current).getBlock() == Blocks.AIR; current = current.add(0,-1,0)) {
			result += 1;
		}

		return result;
	}

    // -------------------------------------------------------------------------------------------- //
    // --- Private methods for data volume manipulation ------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    private VolumeData merge(VolumeData other) {
        blockPositions.addAll(other.blockPositions);
        blockTypes.putAll(other.blockTypes);
        blockBiomes.putAll(other.blockBiomes);

        return this;
    }

    private void add(BlockPos block) {
        blockPositions.add(block);
        blockTypes.merge(world.getBlockState(block).getBlock(),1, Integer::sum);
        blockBiomes.merge(world.getBiome(block), 1, Integer::sum);
        if (this.world.isSkyVisible(block)) {
            sky = sky+1;
        }
    }

    // -------------------------------------------------------------------------------------------- //
    // --- Private static methods used by the pseudo constructors --------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    private static VolumeData lowerHemisphere(World world, BlockPos block, int radius) {
        block = block.add(0,-2,0);
        float mid = sqrt(((radius * radius) / 2.0F));
        float tri = sqrt(((radius * radius) / 3.0F));
        VolumeData result = new VolumeData(world, block);

        // For efficiency purposes, we sample 17 points on hemisphere's surface + the origin
        float[][] offsets = {
                {radius,0,0},
                {0,0,radius},
                {-radius,0,0},
                {0,-radius,0},
                {0,0,-radius},

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
            result.add(block.add(offset[0], offset[1], offset[2]));
        }

        return result;
    }

    private static VolumeData upperHemisphere(World world, BlockPos block, int radius) {
        block = block.add(0,2,0);
        float mid = sqrt(((radius * radius) / 2.0F));
        float tri = sqrt(((radius * radius) / 3.0F));
        VolumeData result = new VolumeData(world, block);

        // For efficiency purposes, we sample 17 points on hemisphere's surface + the origin
        float[][] offsets = {
                {radius, 0, 0},
                {0, radius, 0},
                {-radius, 0, 0},
                {0, 0, -radius},

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
            result.add(block.add(offset[0], offset[1], offset[2]));
        }

        return result;
    }
}
