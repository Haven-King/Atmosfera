package dev.hephaestus.atmosfera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;


@Environment(EnvType.CLIENT)
public class Atmosfera implements ClientModInitializer {
	public static final AmbientSoundHandler HANDLER = new AmbientSoundHandler(MinecraftClient.getInstance());
	public static final AtmosferaConfig CONFIG = new AtmosferaConfig();
	public static final AmbientSoundRegistry REGISTRY = new AmbientSoundRegistry();

	@Override
	public void onInitializeClient() {
		System.out.println("Atmosfera initialized");
	}
}
