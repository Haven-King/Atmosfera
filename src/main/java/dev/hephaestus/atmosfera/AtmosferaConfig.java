/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hephaestus.atmosfera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class AtmosferaConfig {
	public static Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("atmosfera.json");

	private static final TreeMap<Identifier, Integer> VOLUME_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.get(id.toString())));
	private static final TreeMap<Identifier, Boolean> SUBTITLE_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.get(id.toString())));
	private static boolean printDebugMessages = false;
	private static boolean enableCustomMusic = true;
	private static float customMusicWeightScale = 2.5f;

	static {
		if (!Files.exists(CONFIG_PATH)) {
			write();
		} else {
			try {
				read();
			} catch (Exception e) {
				Atmosfera.error("failed to read config! overwriting with default config...", e);
				write();
			}
		}
	}

	public static void read() throws IOException {
		if (!Files.exists(CONFIG_PATH))
			return;

		String jsonString = Files.readString(CONFIG_PATH);

		JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

		if (json.has("general")) {
			JsonObject general = json.getAsJsonObject("general");

			if (general.has("enable_custom_music")) {
				enableCustomMusic = general.get("enable_custom_music").getAsBoolean();
			}

			if (general.has("custom_music_weight_scale")) {
				customMusicWeightScale = general.get("custom_music_weight_scale").getAsFloat();
			}
		}

		if (json.has("volumes")) {
			for (var entry : json.get("volumes").getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					VOLUME_MODIFIERS.put(Identifier.parse(entry.getKey()), entry.getValue().getAsInt());
				}
			}
		}

		if (json.has("subtitles")) {
			for (var entry : json.get("subtitles").getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					SUBTITLE_MODIFIERS.put(Identifier.parse(entry.getKey()), entry.getValue().getAsBoolean());
				}
			}
		}

		if (json.has("debug")) {
			JsonObject debug = json.getAsJsonObject("debug");

			if (debug.has("print_debug_messages")) {
				printDebugMessages = debug.get("print_debug_messages").getAsBoolean();
			}
		}
	}

	// resource reloader callback
	public static void loadedSoundDefinitions() {
		for (AtmosphericSoundDefinition sound : Atmosfera.SOUND_DEFINITIONS.values()) {
			VOLUME_MODIFIERS.putIfAbsent(sound.id(), sound.defaultVolume());
			SUBTITLE_MODIFIERS.putIfAbsent(sound.id(), sound.hasSubtitleByDefault());
		}

		for (AtmosphericSoundDefinition sound : Atmosfera.MUSIC_DEFINITIONS.values()) {
			VOLUME_MODIFIERS.putIfAbsent(sound.id(), sound.defaultVolume());
		}

		write();
	}

	public static String serialize() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonObject general = new JsonObject();
		general.addProperty("enable_custom_music", enableCustomMusic);
		general.addProperty("custom_music_weight_scale", customMusicWeightScale);

		JsonObject debug = new JsonObject();
		debug.addProperty("print_debug_messages", printDebugMessages);

		JsonObject config = new JsonObject();
		config.add("general", general);
		config.add("volumes", gson.toJsonTree(VOLUME_MODIFIERS));
		config.add("subtitles", gson.toJsonTree(SUBTITLE_MODIFIERS));
		config.add("debug", debug);

		return gson.toJson(config);
	}

	public static void write() {
		try {
			Files.writeString(CONFIG_PATH, serialize());
		} catch (Exception e) {
			Atmosfera.error("could not write config file!", e);
		}
	}

	public static float volumeModifier(Identifier soundId) {
		return VOLUME_MODIFIERS.getOrDefault(soundId, 100) / 100F;
	}

	public static boolean showSubtitle(Identifier soundId) {
		return SUBTITLE_MODIFIERS.getOrDefault(soundId, true);
	}

	public static Screen getScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create().setTitle(Component.literal(Atmosfera.MOD_NAME));
		builder.setParentScreen(parent);
		builder.setDefaultBackgroundTexture(Identifier.parse("minecraft:textures/block/light_blue_stained_glass.png"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder()
				.setResetButtonKey(Component.translatable("text.cloth-config.reset_value"));

		ConfigCategory generalCategory = builder.getOrCreateCategory(Component.translatable("config.category.atmosfera.general"));
		ConfigCategory volumesCategory = builder.getOrCreateCategory(Component.translatable("config.category.atmosfera.volumes"));
		ConfigCategory subtitlesCategory = builder.getOrCreateCategory(Component.translatable("config.category.atmosfera.subtitles"));

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ConfigCategory debugCategory = builder.getOrCreateCategory(Component.translatable("config.category.atmosfera.debug"));
			debugCategory.addEntry(entryBuilder
					.startBooleanToggle(Component.translatable("config.value.atmosfera.print_debug_messages"), printDebugMessages)
					.setSaveConsumer(b -> printDebugMessages = b)
					.setDefaultValue(false)
					.build()
			);
		}

		SubCategoryBuilder soundSubcategory = entryBuilder
				.startSubCategory(Component.translatable("config.subcategory.atmosfera.ambient_sound"))
				.setExpanded(true);
		SubCategoryBuilder musicSubcategory = entryBuilder
				.startSubCategory(Component.translatable("config.subcategory.atmosfera.music"))
				.setExpanded(true);

		generalCategory.addEntry(
				entryBuilder.startBooleanToggle(Component.translatable("config.value.atmosfera.enable_custom_music"), enableCustomMusic)
						.setTooltip(Component.translatable("config.value.atmosfera.enable_custom_music.@Tooltip"))
						.setSaveConsumer(b -> enableCustomMusic = b)
						.setDefaultValue(true)
						.build()
		);

		generalCategory.addEntry(
				entryBuilder.startLongSlider(Component.translatable("config.value.atmosfera.custom_music_weight_scale"), (long)(customMusicWeightScale * 100), 1, 1000)
						.setSaveConsumer(v -> customMusicWeightScale = v / 100f)
						.setTextGetter(v -> Component.literal(v + "%"))
						.setDefaultValue(250)
						.build()
		);

		generalCategory.addEntry(
				entryBuilder.startTextDescription(Component.translatable("config.value.atmosfera.custom_music_weight_scale_explanation"))
						.setTooltip(Component.translatable("config.value.atmosfera.custom_music_weight_scale_explanation.@Tooltip"))
						.build()
		);

		for (Map.Entry<Identifier, Integer> sound : VOLUME_MODIFIERS.entrySet()) {
			Map<Identifier, AtmosphericSoundDefinition> soundType;

			if (Atmosfera.SOUND_DEFINITIONS.containsKey(sound.getKey())) {
				soundType = Atmosfera.SOUND_DEFINITIONS;

				// Prevents the crash caused by additional and missing elements.
				if (soundType.containsKey(sound.getKey())) {

					// Replaces the "colon" with a "dot" as the ID separator to utilize the language file.
					String soundLangID = String.join(".", sound.getKey().toString().split(":"));

					MutableComponent tooltip = Component.literal(soundLangID + "\n");
					tooltip.append(Component.translatable("subtitle." + soundLangID));
					tooltip.append("\n");
					tooltip.append(Component.translatable("config.value.atmosfera.sound_tip.@Tooltip"));

					soundSubcategory.add(
							entryBuilder.startIntSlider(Component.translatable(soundLangID), sound.getValue(), 0, 200)
									.setDefaultValue(soundType.get(sound.getKey()).defaultVolume())
									.setTooltip(tooltip.withStyle(ChatFormatting.GRAY))
									.setTextGetter(integer -> Component.literal(integer + "%"))
									.setSaveConsumer(volume -> VOLUME_MODIFIERS.put(sound.getKey(), volume))
									.build()
					);
				}
			} else {
				soundType = Atmosfera.MUSIC_DEFINITIONS;
				if (soundType.containsKey(sound.getKey())) {
					String soundLangID = String.join(".", sound.getKey().toString().split(":"));

					MutableComponent tooltip = Component.literal(soundLangID);
					tooltip.append("\n");
					tooltip.append(Component.translatable("config.value.atmosfera.sound_tip.@Tooltip"));

					musicSubcategory.add(
							entryBuilder.startIntSlider(Component.translatable(soundLangID), sound.getValue(), 0, 200)
									.setDefaultValue(soundType.get(sound.getKey()).defaultVolume())
									.setTooltip(tooltip.withStyle(ChatFormatting.GRAY))
									.setTextGetter(integer -> Component.literal(integer + "%"))
									.setSaveConsumer(volume -> VOLUME_MODIFIERS.put(sound.getKey(), volume))
									.build()
					);
				}
			}
		}

		volumesCategory.addEntry(soundSubcategory.build());
		volumesCategory.addEntry(musicSubcategory.build());

		for (Map.Entry<Identifier, Boolean> sound : SUBTITLE_MODIFIERS.entrySet()) {
			if (Atmosfera.SOUND_DEFINITIONS.containsKey(sound.getKey())) {
				String soundLangID = String.join(".", sound.getKey().toString().split(":"));

				MutableComponent tooltipText = Component.literal(soundLangID + "\n");
				tooltipText.append(Component.translatable("subtitle." + soundLangID));

				subtitlesCategory.addEntry(
						entryBuilder.startBooleanToggle(Component.translatable(soundLangID), sound.getValue())
								.setDefaultValue(Atmosfera.SOUND_DEFINITIONS.get(sound.getKey()).hasSubtitleByDefault())
								.setTooltip(tooltipText.withStyle(ChatFormatting.GRAY))
								.setSaveConsumer(subtitle -> SUBTITLE_MODIFIERS.put(sound.getKey(), subtitle))
								.build()
				);
			}
		}

		if (soundSubcategory.size() + musicSubcategory.size() == 0) {
			subtitlesCategory.removeCategory();
			volumesCategory.addEntry(
					entryBuilder.startTextDescription(Component.translatable("config.atmosfera.resource_pack_warning").withStyle(ChatFormatting.RED))
							.build()
			);
		}

		builder.setSavingRunnable(AtmosferaConfig::write);

		return builder.build();
	}

	public static boolean printDebugMessages() {
		return printDebugMessages;
	}

	public static boolean enableCustomMusic() {
		return enableCustomMusic;
	}

	public static float customMusicWeightScale() {
		return customMusicWeightScale;
	}
}
