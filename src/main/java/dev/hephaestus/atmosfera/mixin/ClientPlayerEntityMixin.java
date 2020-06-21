package dev.hephaestus.atmosfera.mixin;

import com.google.common.collect.Lists;
import dev.hephaestus.atmosfera.Atmosfera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Final
    @Shadow
    private final List<ClientPlayerTickable> tickables = Lists.newArrayList();

    @Inject(at=@At("RETURN"),method="<init>*")
    public void initializeHandler(MinecraftClient minecraftClient, ClientWorld clientWorld, ClientPlayNetworkHandler clientPlayNetworkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean bl, boolean bl2, CallbackInfo ci) {
        tickables.add(Atmosfera.HANDLER);
    }
}
