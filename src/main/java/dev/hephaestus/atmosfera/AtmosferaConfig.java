package dev.hephaestus.atmosfera;

import com.google.gson.*;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AtmosferaConfig {
    private final HashMap<Identifier, Integer> configs = new HashMap<>();

    public AtmosferaConfig() {
        try {
            InputStream fi = new FileInputStream(new File("config" + File.separator + "atmosfera.json"));
            JsonParser jsonParser = new JsonParser();

            JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));
            for (Map.Entry<String, JsonElement> element: json.entrySet()) {
                if (element.getValue().isJsonPrimitive()) {
                    configs.put(new Identifier(element.getKey()), element.getValue().getAsInt());
                }
            }

            fi.close();
        } catch (FileNotFoundException e) {
            System.out.println("Atmosfera - no user config file found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(Identifier id) {
        return configs.containsKey(id);
    }

    public int get(Identifier id) {
        return configs.get(id);
    }

    public void set(Identifier id, int volume) {
        configs.put(id, volume);
    }

    public void writeConfig() {
        File configFile = new File("config" + File.separator + "atmosfera.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(gson.toJson(configs));
            writer.close();
        } catch (IOException e) {
            System.out.println("Atmosfera - failed to save config to file");
        }
    }

    public static ConfigBuilder getConfigScreen() {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(new LiteralText("Atmosfera"));
        builder.setParentScreen(MinecraftClient.getInstance().currentScreen);

        ConfigCategory volumesCategory = builder.getOrCreateCategory(new TranslatableText("category.atmosfera.volumes"));

        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/light_blue_stained_glass.png"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        for (AmbientSound sound : Atmosfera.REGISTRY.getRegistered()) {
            volumesCategory.addEntry(
                    entryBuilder.startIntSlider(new TranslatableText(sound.getLangID()), sound.max_volume, 0, 100)
                            .setDefaultValue(sound.default_volume)
                            .setTextGetter(integer -> new LiteralText(integer + "%"))
                            .setSaveConsumer((volume) -> {
                                Atmosfera.CONFIG.set(sound.id, volume);
                                Atmosfera.REGISTRY.update(sound.id);
                                Atmosfera.HANDLER.update(sound.id);
                            })
                            .build());
        }

        builder.setSavingRunnable(Atmosfera.CONFIG::writeConfig);

        return builder;
    }


}