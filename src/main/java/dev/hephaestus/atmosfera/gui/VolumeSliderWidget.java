package dev.hephaestus.atmosfera.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.hephaestus.atmosfera.AmbientSound;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.resource.language.I18n;

@Environment(EnvType.CLIENT)
public class VolumeSliderWidget extends SliderWidget {
   private final AmbientSound sound;

   public VolumeSliderWidget(int x, int y, AmbientSound sound, int width) {
        super(x, y, width, 20, (double)sound.getMaxVolume()/2);
        this.sound = sound;
        this.updateMessage();
    }

    protected void renderBg(MinecraftClient minecraftClient_1, int int_1, int int_2) {
        minecraftClient_1.getTextureManager().bindTexture(WIDGETS_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int int_3 = (this.isHovered() ? 2 : 1) * 20;
        this.blit(this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + int_3, 4, 20);
        this.blit(this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + int_3, 4, 20);
    }

    protected void updateMessage() {
        String string_1 = (float)this.value == (float)this.getYImage(false) ? I18n.translate("options.off") : (int)((float)this.value * 100.0F) + "%";
        this.setMessage(sound.getName().asString() + ": " + string_1);
    }

    protected void applyValue() {
        this.sound.setMaxVolume((float)this.value*2f);
    }

    protected void updateShape(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }
}
