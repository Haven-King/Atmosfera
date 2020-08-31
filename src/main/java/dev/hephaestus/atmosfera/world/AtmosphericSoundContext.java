package dev.hephaestus.atmosfera.world;

import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Math.sqrt;

public class AtmosphericSoundContext {
	private static final HashMap<Size, AtmosphericSoundContext> CONTEXTS = new HashMap<>();
	public static final HashMap<Direction, HashMap<Size, HashSet<int[]>>> OFFSETS = new HashMap<>();

	static {
		for (Size size : Size.values()) {
//			double mid = sqrt(((size.radius * size.radius) / 2.0F));
//			double tri = sqrt(((size.radius * size.radius) / 3.0F));

			BlockPos origin = new BlockPos(0, 0, 0);

			int radius = size.radius;
			for (int x = 0; x <= radius + 1; ++x) {
				for (int y = -radius; y <= 0; ++y) {
					for (int z = 0; z <= radius + 1; ++z) {
						double distance = origin.getSquaredDistance(x, y, z, true);
						if (distance <= (radius + 1) * (radius + 1) /*&& distance >= (radius) * (radius)*/) {
							OFFSETS.computeIfAbsent(Direction.DOWN, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {x, y, z}
							);

							OFFSETS.computeIfAbsent(Direction.UP, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {x, -y + 1, z}
							);

							OFFSETS.computeIfAbsent(Direction.DOWN, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {x, y, -z}
							);

							OFFSETS.computeIfAbsent(Direction.UP, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {x, -y + 1, -z}
							);

							OFFSETS.computeIfAbsent(Direction.DOWN, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {-x, y, z}
							);

							OFFSETS.computeIfAbsent(Direction.UP, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {-x, -y + 1, z}
							);

							OFFSETS.computeIfAbsent(Direction.DOWN, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {-x, y, -z}
							);

							OFFSETS.computeIfAbsent(Direction.UP, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
									new int[] {-x, -y + 1, -z}
							);
						}
					}
				}
			}

//			OFFSETS.computeIfAbsent(Direction.DOWN, key -> new HashMap<>()).put(size, new double[][] {
//					{size.radius,0,0},
//					{0,0,size.radius},
//					{-size.radius,0,0},
//					{0,-size.radius,0},
//					{0,0,-size.radius},
//
//					{mid, -mid, 0},
//					{-mid, -mid, 0},
//
//					{0, -mid, mid},
//					{0, -mid, -mid},
//
//					{mid, 0, mid},
//					{mid, 0, -mid},
//					{-mid, 0, -mid},
//					{-mid, 0, mid},
//
//					{-tri,-tri,-tri},
//					{-tri,-tri,tri},
//					{tri, -tri, -tri},
//					{tri, -tri, tri},
//
//					{0,0,0}
//			});
//
//			OFFSETS.computeIfAbsent(Direction.UP, key -> new HashMap<>()).put(size, new double[][] {
//					{size.radius, 1, 0},
//					{0, size.radius + 1, 0},
//					{-size.radius, 1, 0},
//					{0, 1, -size.radius},
//
//					{mid, mid + 1, 0},
//					{-mid, mid + 1, 0},
//
//					{0, mid + 1, mid},
//					{0, mid + 1, -mid},
//
//					{mid, 1, mid},
//					{mid, 1, -mid},
//					{-mid, 1, -mid},
//					{-mid, 1, mid},
//
//					{tri, tri + 1, tri},
//					{tri, tri + 1, -tri},
//					{-tri,tri + 1,tri},
//					{-tri,tri + 1,-tri},
//
//					{0,1,0}
//			});
		}

		clear();
	}

	public static AtmosphericSoundContext getContext(Size size) {
		return CONTEXTS.get(size);
	}

	public static void updateContext(ClientPlayerEntity playerEntity) {
		if (playerEntity != null) {
			for (AtmosphericSoundContext context : CONTEXTS.values()) {
				context.update(playerEntity);
			}
		}
	}

	public static void clear() {
		for (Size size : Size.values()) {
			CONTEXTS.put(size, new AtmosphericSoundContext(size));
		}
	}

	private final Section up;
	private final Section down;

	private int distanceFromGround = 0;
	private int playerHeight = -1;
	private boolean isDay = false;
	private Identifier dimension;
	public Collection<BlockPos> blocks = new HashSet<>();

	private AtmosphericSoundContext(Size size) {
		this.up = new Section(Direction.UP, size);
		this.down = new Section(Direction.DOWN, size);
	}

	private void update(ClientPlayerEntity playerEntity) {
		ClientWorld world = (ClientWorld) playerEntity.world;
		BlockPos pos = playerEntity.getBlockPos();

		world.getProfiler().push("atmosfera.update");
		if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			this.blocks = new HashSet<>();
			this.up.update(world, pos);
			this.down.update(world, pos);
			this.dimension = world.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getId(world.getDimension());

			this.distanceFromGround = 0;
			BlockPos.Mutable mut = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());

			this.playerHeight = pos.getY();
			this.isDay = playerEntity.world.getTimeOfDay() > 450 && playerEntity.world.getTimeOfDay() < 11616;

			while (world.getBlockState(mut).isAir() && mut.getY() > 0) {
				this.distanceFromGround += 1;
				mut.move(0, -1, 0);
			}
		}
		world.getProfiler().pop();
	}

	public float percentBlockType(Collection<Block> blocks) {
		int count = 0;
		for (Block block : blocks) {
			count += this.up.blockTypes.getOrDefault(block, 0);
			count += this.down.blockTypes.getOrDefault(block, 0);
		}

		int denom = this.up.blockCount + this.down.blockCount;
		return denom == 0 ? 0 : ((float) count) / ((float) (denom));
	}

	public float percentBlockType(Collection<Block> blocks, Direction direction) {
		int count = 0;

		Section section = direction == Direction.UP ? this.up : this.down;
		for (Block block : blocks) {
			count += section.blockTypes.getOrDefault(block, 0);
		}

		return ((float) count) / ((float) section.blockCount);
	}

	public float percentBiomeType(Collection<Identifier> biomes) {
		int count = 0;
		for (Identifier biome : biomes) {
			count += this.up.biomeTypes.getOrDefault(biome, 0);
			count += this.down.biomeTypes.getOrDefault(biome, 0);
		}

		return ((float) count) / ((float) (this.up.blockCount + this.down.blockCount));

	}

	public float percentBiomeType(Collection<Identifier> biomes, Direction direction) {
		int count = 0;

		Section section = direction == Direction.UP ? this.up : this.down;
		for (Identifier biome : biomes) {
			count += section.biomeTypes.getOrDefault(biome, 0);
		}

		return ((float) count) / ((float) section.blockCount);
	}

	public float percentSkyVisible() {
		return (this.percentSkyVisible(Direction.UP) + this.percentSkyVisible(Direction.DOWN)) / 2F;
	}

	public float percentSkyVisible(Direction direction) {
		Section section = direction == Direction.UP ? this.up : this.down;
		return (float) section.numberSkyVisible / (float) section.blockCount;
	}

	public int getDistanceFromGround() {
		return this.distanceFromGround;
	}

	public int getPlayerHeight() {
		return this.playerHeight;
	}

	public Identifier getDimension() {
		return this.dimension;
	}

	public boolean isDaytime() {
		return this.isDay;
	}

	private static class Section {
		private final Size size;
		private final Direction direction;

		int blockCount = 0;
		int numberSkyVisible = 0;
		HashMap<Block, Integer> blockTypes = new HashMap<>();
		HashMap<Identifier, Integer> biomeTypes = new HashMap<>();

		private Section(Direction direction, Size size) {
			this.size = size;
			this.direction = direction;
		}

		private void clear() {
			this.blockCount = 0;
			this.numberSkyVisible = 0;
			this.blockTypes = new HashMap<>();
			this.biomeTypes = new HashMap<>();
		}

		private void add(ClientWorld world, BlockPos pos) {
			this.blockTypes.merge(world.getBlockState(pos).getBlock(), 1, Integer::sum);
			this.biomeTypes.merge(world.getRegistryManager().get(Registry.BIOME_KEY).getId(world.getBiome(pos)), 1, Integer::sum);
			this.numberSkyVisible += world.isSkyVisible(pos) ? 1 : 0;
			this.blockCount++;
		}

		private void update(ClientWorld world, BlockPos center) {
			this.clear();

			BlockPos.Mutable mut = new BlockPos.Mutable();

			for (int[] a : OFFSETS.get(this.direction).get(this.size)) {
				mut.set(center.getX() + a[0], center.getY() + a[1], center.getZ() + a[2]);
				this.add(world, mut);
			}
		}
	}

	public enum Direction {
		UP, DOWN
	}

	public enum Size {
		SMALL(4),
		MEDIUM(8),
		LARGE(16);

		public final int radius;
		Size(int radius) {
			this.radius = radius;
		}
	}
}
