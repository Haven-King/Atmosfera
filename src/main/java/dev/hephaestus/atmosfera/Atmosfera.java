package dev.hephaestus.atmosfera;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class Atmosfera implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
		
			@Override
			public void apply(ResourceManager manager) {
					AmbientSoundRegistry.removeRegistered();

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
