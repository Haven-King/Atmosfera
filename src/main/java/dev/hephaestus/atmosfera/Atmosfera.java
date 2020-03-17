package dev.hephaestus.atmosfera;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class Atmosfera implements ClientModInitializer {
	public static HashMap<String, Float> configs;
	public static final AmbientSoundHandler handler = new AmbientSoundHandler(MinecraftClient.getInstance());

	public static void readConfig() {
		configs = new HashMap<>();

		try {
			InputStream fi = new FileInputStream(new File("config" + File.separator + "atmosfera.json"));
			JsonParser jsonParser = new JsonParser();

			JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));
			for (Map.Entry<String,JsonElement> element: json.entrySet()) {
				if (element.getValue().isJsonPrimitive()) {
					configs.put(element.getKey(), element.getValue().getAsFloat());
				}
			}

			fi.close();
		} catch (FileNotFoundException e) {
			System.out.println("Atmosfera - no user config file found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeConfig() {
		File configFile = new File("config" + File.separator + "atmosfera.json");
		Gson gson = new Gson();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
			writer.write(gson.toJson(configs));
			writer.close();
		} catch (IOException e) {
			System.out.println("Atmosfera - failed to save config to file");
		}
	}

	public static void clearConfig() {
		File configFile = new File("config" + File.separator + "atmosfera.json");
		boolean success = configFile.delete();
		if (success) {
			System.out.println("Atmosfera - successfully deleted user config file: " + configFile.getAbsolutePath());
		} else {
			System.out.println("Atmosfera - failed to delete user config file: " + configFile.getAbsolutePath());
		}

		something();
	}

	public static void something() {
		AmbientSoundRegistry.removeRegistered();
		readConfig();

		ResourceManager manager = MinecraftClient.getInstance().getResourceManager();

		Collection<Identifier> resources = manager.findResources("sounds/definitions", (string) -> string.endsWith(".json"));

		for (Identifier r : resources) {
			try {
				JsonParser JsonParser = new JsonParser();
				JsonObject json = (JsonObject)JsonParser.parse(new InputStreamReader(manager.getResource(r).getInputStream()));

				AmbientSoundRegistry.register(json);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onInitializeClient() {		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
		
			@Override
			public void apply(ResourceManager manager) {
					something();

					System.out.println("Atmosfera - Successfully added " + AmbientSoundRegistry.getRegistered().size() + " sound events");
			}

			@Override
			public Collection<Identifier> getFabricDependencies() {
				return Collections.emptyList();
			}
		
			@Override
			public Identifier getFabricId() {
				return new Identifier("atmosfera:sound_json");
			}
		});
	}
}
