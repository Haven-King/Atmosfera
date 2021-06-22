/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hephaestus.atmosfera.world;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundModifierRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AtmosphericSoundContext {
	private static final HashMap<Size, AtmosphericSoundContext> CONTEXTS = new HashMap<>();
	public static final HashMap<Direction, HashMap<Size, HashSet<int[]>>> OFFSETS = new HashMap<>();

	static {
		for (Size size : Size.values()) {
			BlockPos origin = new BlockPos(0, 0, 0);

			int radius = size.radius;
			for (int x = 0; x <= radius + 1; ++x) {
				for (int y = -radius; y <= 0; ++y) {
					for (int z = 0; z <= radius + 1; ++z) {
						double distance = origin.getSquaredDistance(x, y, z, true);
						if (distance <= (radius + 1) * (radius + 1)) {
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

				Atmosfera.debug("[Atmosfera] percentSkyVisible: Radius = " + context.up.size.radius
						+ " - Sphere = " + context.percentSkyVisible()
						+ " - Down = " + context.percentSkyVisible(Direction.DOWN)
						+ " - Up = " + context.percentSkyVisible(Direction.UP));
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

	private PlayerEntity player;
	private int distanceFromGround = 0;
	private int playerHeight = -1;
	private boolean isDay = false;
	private boolean isRainy = false;
	private boolean isStormy = false;
	private boolean isSubmerged = false;
	private Identifier dimension;
	public Collection<BlockPos> blocks = new HashSet<>();

	private AtmosphericSoundContext(Size size) {
		this.up = new Section(Direction.UP, size);
		this.down = new Section(Direction.DOWN, size);
	}

	private void update(ClientPlayerEntity playerEntity) {
		this.player = playerEntity;
		ClientWorld world = (ClientWorld) playerEntity.world;
		BlockPos pos = playerEntity.getBlockPos();

		if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			this.blocks = new HashSet<>();
			this.up.update(world, pos);
			this.down.update(world, pos);

			this.dimension = world.getDimension().getSkyProperties();

			this.distanceFromGround = 0;
			BlockPos.Mutable mut = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());

			this.playerHeight = pos.getY();
			this.isDay = playerEntity.world.getTimeOfDay() > 0 && playerEntity.world.getTimeOfDay() < 12000; // https://minecraft.fandom.com/wiki/Daylight_cycle
			this.isRainy = playerEntity.world.isRaining();
			this.isStormy = playerEntity.world.isThundering();
			this.isSubmerged = playerEntity.isSubmergedIn(FluidTags.LAVA) || playerEntity.isSubmergedIn(FluidTags.WATER);

			while (world.getBlockState(mut).isAir() && mut.getY() > 0) {
				this.distanceFromGround += 1;
				mut.move(0, -1, 0);
			}
		}
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

	public float percentBlockTag(Set<Tag<Block>> blockTags) {
		int count = 0;
		for (Tag<Block> blockTag : blockTags) {
			count += this.up.blockTags.getOrDefault(blockTag, 0);
			count += this.down.blockTags.getOrDefault(blockTag, 0);
		}

		int denom = this.up.blockCount + this.down.blockCount;
		return denom == 0 ? 0 : ((float) count) / ((float) (denom));
	}

	public float percentBlockTag(Set<Tag<Block>> blockTags, Direction direction) {
		int count = 0;
		Section section = direction == Direction.UP ? this.up : this.down;
		for (Tag<Block> blockTag : blockTags) {
			count += section.blockTags.getOrDefault(blockTag, 0);
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

	public boolean isRainy() {
		return this.isRainy;
	}

	public boolean isStormy() {
		return this.isStormy;
	}

	public boolean isSubmerged() {
		return this.isSubmerged;
	}

	public PlayerEntity getPlayer() {
		return this.player;
	}

	private static class Section {
		private final Size size;
		private final Direction direction;

		int blockCount = 0;
		int numberSkyVisible = 0;
		HashMap<Block, Integer> blockTypes = new HashMap<>();
		HashMap<Tag<Block>, Integer> blockTags = new HashMap<>();
		HashMap<Identifier, Integer> biomeTypes = new HashMap<>();

		private Section(Direction direction, Size size) {
			this.size = size;
			this.direction = direction;
		}

		private void clear() {
			this.blockCount = 0;
			this.numberSkyVisible = 0;
			this.blockTypes = new HashMap<>();
			this.blockTags = new HashMap<>();
			this.biomeTypes = new HashMap<>();
		}

		private void add(ClientWorld world, BlockPos pos) {
			Block block = world.getBlockState(pos).getBlock();
			this.blockTypes.merge(block, 1, Integer::sum);

			for (Tag<Block> blockTag : AtmosphericSoundModifierRegistry.USED_BLOCK_TAGS) {
				if (blockTag.contains(block)) {
					this.blockTags.merge(blockTag, 1, Integer::sum);
				}
			}

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
