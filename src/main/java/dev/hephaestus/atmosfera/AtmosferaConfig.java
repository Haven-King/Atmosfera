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

package dev.hephaestus.atmosfera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class AtmosferaConfig {
	public static Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("atmosfera.json");

	private static final TreeMap<Identifier, Integer> VOLUME_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
	private static final TreeMap<Identifier, Boolean> SUBTITLE_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
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
					VOLUME_MODIFIERS.put(new Identifier(entry.getKey()), entry.getValue().getAsInt());
				}
			}
		}

		if (json.has("subtitles")) {
			for (var entry : json.get("subtitles").getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					SUBTITLE_MODIFIERS.put(new Identifier(entry.getKey()), entry.getValue().getAsBoolean());
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
		ConfigBuilder builder = ConfigBuilder.create().setTitle(new LiteralText(Atmosfera.MOD_NAME));
		builder.setParentScreen(parent);
		builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/light_blue_stained_glass.png"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder()
				.setResetButtonKey(new TranslatableText("text.cloth-config.reset_value"));

		ConfigCategory generalCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.general"));
		ConfigCategory volumesCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.volumes"));
		ConfigCategory subtitlesCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.subtitles"));

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ConfigCategory debugCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.debug"));
			debugCategory.addEntry(entryBuilder
					.startBooleanToggle(new TranslatableText("config.value.atmosfera.print_debug_messages"), printDebugMessages)
					.setSaveConsumer(b -> printDebugMessages = b)
					.setDefaultValue(false)
					.build()
			);
		}

		SubCategoryBuilder soundSubcategory = entryBuilder
				.startSubCategory(new TranslatableText("config.subcategory.atmosfera.ambient_sound"))
				.setExpanded(true);
		SubCategoryBuilder musicSubcategory = entryBuilder
				.startSubCategory(new TranslatableText("config.subcategory.atmosfera.music"))
				.setExpanded(true);

		generalCategory.addEntry(
				entryBuilder.startBooleanToggle(new TranslatableText("config.value.atmosfera.enable_custom_music"), enableCustomMusic)
						.setTooltip(new TranslatableText("config.value.atmosfera.enable_custom_music.@Tooltip"))
						.setSaveConsumer(b -> enableCustomMusic = b)
						.setDefaultValue(true)
						.build()
		);

		generalCategory.addEntry(
				entryBuilder.startLongSlider(new TranslatableText("config.value.atmosfera.custom_music_weight_scale"), (long)(customMusicWeightScale * 100), 1, 1000)
						.setSaveConsumer(v -> customMusicWeightScale = v / 100f)
						.setTextGetter(v -> new LiteralText(v + "%"))
						.setDefaultValue(250)
						.build()
		);

		generalCategory.addEntry(
				entryBuilder.startTextDescription(new TranslatableText("config.value.atmosfera.custom_music_weight_scale_explanation"))
						.setTooltip(new TranslatableText("config.value.atmosfera.custom_music_weight_scale_explanation.@Tooltip"))
						.build()
		);

		for (Map.Entry<Identifier, Integer> entry : VOLUME_MODIFIERS.entrySet()) {
			Identifier soundId = entry.getKey();
			int volume = entry.getValue();
			String translationKey = getTranslationKey(soundId);

			SubCategoryBuilder subcategory;
			MutableText tooltip;
			int defaultVolume;

			if (Atmosfera.SOUND_DEFINITIONS.containsKey(soundId)) {
				defaultVolume = Atmosfera.SOUND_DEFINITIONS.get(soundId).defaultVolume();
				subcategory = soundSubcategory;

				tooltip = new LiteralText(translationKey + "\n")
						.append(new TranslatableText(getSubtitleTranslationKey(translationKey)))
						.append("\n")
						.append(new TranslatableText("config.value.atmosfera.sound_tip.@Tooltip"));
			} else if (Atmosfera.MUSIC_DEFINITIONS.containsKey(soundId)) {
				defaultVolume = Atmosfera.MUSIC_DEFINITIONS.get(soundId).defaultVolume();
				subcategory = musicSubcategory;

				tooltip = new LiteralText(translationKey)
						.append("\n")
						.append(new TranslatableText("config.value.atmosfera.sound_tip.@Tooltip"));
			} else {
				continue;
			}

			subcategory.add(getVolumeSlider(entryBuilder, translationKey, volume, defaultVolume, tooltip, soundId));
		}

		volumesCategory.addEntry(soundSubcategory.build());
		volumesCategory.addEntry(musicSubcategory.build());

		for (Map.Entry<Identifier, Boolean> entry : SUBTITLE_MODIFIERS.entrySet()) {
			Identifier soundId = entry.getKey();
			boolean value = entry.getValue();
			String translationKey = getTranslationKey(soundId);

			if (Atmosfera.SOUND_DEFINITIONS.containsKey(soundId)) {
				boolean defaultValue = Atmosfera.SOUND_DEFINITIONS.get(soundId).hasSubtitleByDefault();

				MutableText tooltip = new LiteralText(translationKey + "\n")
						.append(new TranslatableText(getSubtitleTranslationKey(translationKey)));

				subtitlesCategory.addEntry(
						entryBuilder.startBooleanToggle(new TranslatableText(translationKey), value)
								.setDefaultValue(defaultValue)
								.setTooltip(tooltip.formatted(Formatting.GRAY))
								.setSaveConsumer(subtitle -> SUBTITLE_MODIFIERS.put(soundId, subtitle))
								.build()
				);
			}
		}

		if (soundSubcategory.size() + musicSubcategory.size() == 0) {
			subtitlesCategory.removeCategory();
			volumesCategory.addEntry(
					entryBuilder.startTextDescription(new TranslatableText("config.atmosfera.resource_pack_warning").formatted(Formatting.RED))
							.build()
			);
		}

		builder.setSavingRunnable(AtmosferaConfig::write);

		return builder.build();
	}

	private static IntegerSliderEntry getVolumeSlider(ConfigEntryBuilder entryBuilder, String translationKey, int volume, int defaultVolume, MutableText tooltip, Identifier soundId) {
		return entryBuilder.startIntSlider(new TranslatableText(translationKey), volume, 0, 200)
				.setDefaultValue(defaultVolume)
				.setTooltip(tooltip.formatted(Formatting.GRAY))
				.setTextGetter(integer -> new LiteralText(integer + "%"))
				.setSaveConsumer(v -> VOLUME_MODIFIERS.put(soundId, v))
				.build();
	}

	private static String getTranslationKey(Identifier soundId) {
		return String.join(".", soundId.toString().split(":"));
	}

	private static String getSubtitleTranslationKey(String translationKey) {
		// a bit of a hack
		if (translationKey.endsWith("_howls"))
			translationKey = translationKey.substring(0, translationKey.length() - "_howls".length());

		return "subtitle." + translationKey;
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
