package dev.hephaestus.atmosfera;

import com.google.gson.*;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AtmosferaConfig {
	private static final TreeMap<Identifier, Integer> VOLUME_MODIFIERS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));
	private static final TreeMap<Identifier, Integer> MUSIC_WEIGHTS = new TreeMap<>(Comparator.comparing(id -> I18n.translate(id.toString())));

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

			fi.close();
		} catch (FileNotFoundException e) {
			Atmosfera.LOG.info("Atmosfera - no user config file found. Creating default config file.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			for (AtmosphericSoundDefinition sound : Atmosfera.SOUND_DEFINITIONS.values()) {
				VOLUME_MODIFIERS.putIfAbsent(sound.getId(), sound.getDefaultVolume());
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

			writer.write(gson.toJson(jsonObject));
			writer.close();
		} catch (IOException e) {
			Atmosfera.LOG.warn("Atmosfera - failed to save config to file");
		}
	}

	public static double modifier(Identifier soundId) {
		try {
			return ((double) VOLUME_MODIFIERS.getOrDefault(soundId, Atmosfera.SOUND_DEFINITIONS.getOrDefault(soundId, Atmosfera.MUSIC_DEFINITIONS.get(soundId)).getDefaultVolume())) / 100D;
		} catch (NullPointerException e) {
			Atmosfera.LOG.warn("Unknown sound: {}", soundId);
			throw e;
		}
	}

	public static int weight(Identifier musicId) {
		return MUSIC_WEIGHTS.getOrDefault(musicId, 5);
	}

	public static Screen getScreen(Screen parent) {
		read();

		ConfigBuilder builder = ConfigBuilder.create().setTitle(new LiteralText("Atmosfera"));
		builder.setParentScreen(parent);

		Map<String, ConfigCategory> categories = new HashMap<>();

		builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/light_blue_stained_glass.png"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		for (Map.Entry<Identifier, Integer> sound : VOLUME_MODIFIERS.entrySet()) {
			Map<Identifier, AtmosphericSoundDefinition> map;

			map = Atmosfera.SOUND_DEFINITIONS.containsKey(sound.getKey())
					? Atmosfera.SOUND_DEFINITIONS
					: Atmosfera.MUSIC_DEFINITIONS;

			if (map.containsKey(sound.getKey())) {
				ConfigCategory category = categories.computeIfAbsent(sound.getKey().getNamespace(), name -> {
					return builder.getOrCreateCategory(new TranslatableText("category." + name + ".volumes"));
				});

				category.addEntry(
						entryBuilder.startIntSlider(new TranslatableText(sound.getKey().toString()), sound.getValue(), 0, 200)
								.setDefaultValue(map.get(sound.getKey()).getDefaultVolume())
								.setTextGetter(integer -> new LiteralText(integer + "%"))
								.setSaveConsumer(volume -> VOLUME_MODIFIERS.put(sound.getKey(), volume))
								.build()
				);
			}
		}

		builder.setSavingRunnable(AtmosferaConfig::write);

		return builder.build();
	}
}
