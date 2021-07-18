package dev.hephaestus.atmosfera.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(BossBarHud.class)
public interface BossBarHudAccessor {
    @Accessor
    Map<UUID, ClientBossBar> getBossBars();
}
