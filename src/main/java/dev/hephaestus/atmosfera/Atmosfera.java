package dev.hephaestus.atmosfera;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
		File configFile = new File("config/atmosfera.json");
		JsonParser jsonParser = new JsonParser();
		try {
			JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(new FileInputStream(configFile)));
			for (Map.Entry<String,JsonElement> element: json.entrySet()) {
				if (element.getValue().isJsonPrimitive()) {
					configs.put(element.getKey(), element.getValue().getAsFloat());
				}
			}
		} catch (FileNotFoundException e) {
		}
	}

	public static void writeConfig() {
		File configFile = new File("config/atmosfera.json");
		Gson gson = new Gson();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
			writer.write(gson.toJson(configs));
			writer.close();
		} catch (IOException e) {
			System.out.println("Atmosfera - failed to save config to file");
		}
	}

	@Override
	public void onInitializeClient() {		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
		
			@Override
			public void apply(ResourceManager manager) {
					AmbientSoundRegistry.removeRegistered();
					readConfig();

					Collection<Identifier> resources = manager.findResources("sounds/definitions", (string) -> {
						return string.endsWith(".json");
					});
					
					for (Identifier r : resources) {
						try {
						JsonParser JsonParser = new JsonParser();
						JsonObject json = (JsonObject)JsonParser.parse(new InputStreamReader(manager.getResource(r).getInputStream()));

						AmbientSoundRegistry.register(json);

						} catch (IOException e) {
							e.printStackTrace();
						}
					}

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
