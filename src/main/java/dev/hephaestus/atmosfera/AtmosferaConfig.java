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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class AtmosferaConfig {
	private static final boolean IS_DEVELOPMENT_ENVIRONMENT = FabricLoader.getInstance().isDevelopmentEnvironment();

	private static final TreeMap<Identifier, Integer> VOLUME_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
	private static final TreeMap<Identifier, Boolean> SUBTITLE_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
	private static final TreeMap<Identifier, Integer> MUSIC_WEIGHTS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
	private static boolean PRINT_DEBUG_MESSAGES = false;

	static {
		read();
	}

	private static void read() {
		try {
			InputStream fi = new FileInputStream("config" + File.separator + "atmosfera.json");
			JsonParser jsonParser = new JsonParser();

			JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));

			if (json.has("volumes")) {
				for (Map.Entry<String, JsonElement> element : json.get("volumes").getAsJsonObject().entrySet()) {
					if (element.getValue().isJsonPrimitive()) {
						VOLUME_MODIFIERS.put(new Identifier(element.getKey()), element.getValue().getAsInt());
					}
				}
			}

			if (json.has("subtitles")) {
				for (Map.Entry<String, JsonElement> element : json.get("subtitles").getAsJsonObject().entrySet()) {
					if (element.getValue().isJsonPrimitive()) {
						SUBTITLE_MODIFIERS.put(new Identifier(element.getKey()), element.getValue().getAsBoolean());
					}
				}
			}

			if (json.has("debug")) {
				JsonObject debug = json.getAsJsonObject("debug");

				if (debug.has("print_debug_messages")) {
					PRINT_DEBUG_MESSAGES = debug.get("print_debug_messages").getAsBoolean();
				}
			}

			fi.close();
		} catch (FileNotFoundException e) {
			Atmosfera.LOG.info("[Atmosfera] The user config file was not found. Creating the default config...");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			for (AtmosphericSoundDefinition sound : Atmosfera.SOUND_DEFINITIONS.values()) {
				VOLUME_MODIFIERS.putIfAbsent(sound.getId(), sound.getDefaultVolume());
				SUBTITLE_MODIFIERS.putIfAbsent(sound.getId(), sound.getDefaultSubtitle());
			}

			for (AtmosphericSoundDefinition sound : Atmosfera.MUSIC_DEFINITIONS.values()) {
				VOLUME_MODIFIERS.putIfAbsent(sound.getId(), sound.getDefaultVolume());
			}

			write();
		}
	}

	private static void write() {
		File configFile = new File("config" + File.separator + "atmosfera.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));

			JsonObject jsonObject = new JsonObject();
			jsonObject.add("volumes", gson.toJsonTree(VOLUME_MODIFIERS));
			jsonObject.add("subtitles", gson.toJsonTree(SUBTITLE_MODIFIERS));

			JsonObject debug = new JsonObject();

			debug.addProperty("print_debug_messages", PRINT_DEBUG_MESSAGES);

			jsonObject.add("debug", debug);

			writer.write(gson.toJson(jsonObject));
			writer.close();
		} catch (IOException e) {
			Atmosfera.LOG.warn("[Atmosfera] Failed to save the config to the file.");
		}
	}

	public static float volumeModifier(Identifier soundId) {
		try {
			return (VOLUME_MODIFIERS.getOrDefault(
					soundId, Atmosfera.SOUND_DEFINITIONS.getOrDefault(
							soundId, Atmosfera.MUSIC_DEFINITIONS.get(soundId)).getDefaultVolume())) / 100F;
		} catch (NullPointerException e) {
			Atmosfera.LOG.warn("[Atmosfera] Unknown sound: {}", soundId);
			throw e;
		}
	}

	public static boolean subtitleModifier(Identifier soundId) {
		return SUBTITLE_MODIFIERS.getOrDefault(soundId, Atmosfera.SOUND_DEFINITIONS.get(soundId).getDefaultSubtitle());
	}

	public static int weight(Identifier musicId) {
		return MUSIC_WEIGHTS.getOrDefault(musicId, 5);
	}

	public static void refreshConfig() {
		Atmosfera.LOG.info("[Atmosfera] Refreshing the user config...");
		read();
	}

	public static Screen getScreen(Screen parent) {
		read();

		ConfigBuilder builder = ConfigBuilder.create().setTitle(new LiteralText(Atmosfera.MOD_NAME));
		builder.setParentScreen(parent);

		ConfigCategory volumesCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.volumes"));
		ConfigCategory subtitlesCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.subtitles"));

		if (IS_DEVELOPMENT_ENVIRONMENT) {
			ConfigCategory debugCategory = builder.getOrCreateCategory(new TranslatableText("config.category.atmosfera.debug"));
			debugCategory.addEntry(new BooleanToggleBuilder(
					new TranslatableText("text.cloth-config.reset_value"), new TranslatableText("config.value.atmosfera.print_debug_messages"), false)
					.setSaveConsumer(b -> PRINT_DEBUG_MESSAGES = b)
					.build()
			);
		}

		SubCategoryBuilder soundSubcategory = new SubCategoryBuilder(
				new TranslatableText("text.cloth-config.reset_value"), new TranslatableText("config.subcategory.atmosfera.ambient_sound"))
				.setExpanded(true);
		SubCategoryBuilder musicSubcategory = new SubCategoryBuilder(
				new TranslatableText("text.cloth-config.reset_value"), new TranslatableText("config.subcategory.atmosfera.music"))
				.setExpanded(true);

		builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/light_blue_stained_glass.png"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		for (Map.Entry<Identifier, Integer> sound : VOLUME_MODIFIERS.entrySet()) {
			Map<Identifier, AtmosphericSoundDefinition> soundType;

			if (Atmosfera.SOUND_DEFINITIONS.containsKey(sound.getKey())) {
				soundType = Atmosfera.SOUND_DEFINITIONS;

				// Prevents the crash caused by additional and missing elements.
				if (soundType.containsKey(sound.getKey())) {

					// Replaces the "colon" with a "dot" as the ID separator to utilize the language file.
					String soundLangID = String.join(".", sound.getKey().toString().split(":"));

					TranslatableText subtitleText = new TranslatableText("subtitle." + soundLangID);
					LiteralText tooltipText = new LiteralText(soundLangID + "\n" + I18n.translate(subtitleText.getKey()));

					soundSubcategory.add(
							entryBuilder.startIntSlider(new TranslatableText(soundLangID), sound.getValue(), 0, 200)
									.setDefaultValue(soundType.get(sound.getKey()).getDefaultVolume())
									.setTooltip(tooltipText.formatted(Formatting.GRAY))
									.setTextGetter(integer -> new LiteralText(integer + "%"))
									.setSaveConsumer(volume -> VOLUME_MODIFIERS.put(sound.getKey(), volume))
									.build()
					);
				}
			} else {
				soundType = Atmosfera.MUSIC_DEFINITIONS;
				if (soundType.containsKey(sound.getKey())) {
					String soundLangID = String.join(".", sound.getKey().toString().split(":"));

					musicSubcategory.add(
							entryBuilder.startIntSlider(new TranslatableText(soundLangID), sound.getValue(), 0, 200)
									.setDefaultValue(soundType.get(sound.getKey()).getDefaultVolume())
									.setTooltip(new LiteralText(soundLangID).formatted(Formatting.GRAY))
									.setTextGetter(integer -> new LiteralText(integer + "%"))
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

				TranslatableText subtitleText = new TranslatableText("subtitle." + soundLangID);
				LiteralText tooltipText = new LiteralText(soundLangID + "\n" + I18n.translate(subtitleText.getKey()));

				subtitlesCategory.addEntry(
						entryBuilder.startBooleanToggle(new TranslatableText(soundLangID), sound.getValue())
								.setDefaultValue(Atmosfera.SOUND_DEFINITIONS.get(sound.getKey()).getDefaultSubtitle())
								.setTooltip(tooltipText.formatted(Formatting.GRAY))
								.setSaveConsumer(subtitle -> SUBTITLE_MODIFIERS.put(sound.getKey(), subtitle))
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

	public static boolean printDebugMessages() {
		return PRINT_DEBUG_MESSAGES && IS_DEVELOPMENT_ENVIRONMENT;
	}
}
