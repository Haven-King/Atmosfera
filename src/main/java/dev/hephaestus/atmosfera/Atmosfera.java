package dev.hephaestus.atmosfera;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.util.AtmosphericSoundSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class Atmosfera implements ClientModInitializer {
	public static final String MODID = "atmosfera";
	public static final String MOD_NAME = "Atmosfera";
	public static final Logger LOG = LogManager.getLogger(MOD_NAME);

	public static HashMap<Identifier, AtmosphericSoundDefinition> SOUND_DEFINITIONS = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new AtmosphericSoundSerializer());
		LOG.info("Atmosfera initialized");
	}

	public static Identifier id(String... path) {
		return new Identifier(MODID, String.join(".", path));
	}
}
