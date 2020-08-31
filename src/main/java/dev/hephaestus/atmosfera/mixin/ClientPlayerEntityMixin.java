package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.util.AtmosphericSoundHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@Environment(EnvType.CLIENT)
public class ClientPlayerEntityMixin {
	@Shadow @Final protected MinecraftClient client;

	@Unique AtmosphericSoundHandler soundHandler = new AtmosphericSoundHandler((ClientPlayerEntity) (Object) this);

	@Inject(method = "tick", at = @At("TAIL"))
	private void updateSoundContext(CallbackInfo ci) {
		this.soundHandler.tick();
	}
}
