package dev.hephaestus.atmosfera.mixin;

import net.minecraft.client.gui.screen.options.SoundOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.hephaestus.atmosfera.gui.AmbientSoundScreen;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin extends Screen {
    private Screen parent;
    protected SoundOptionsScreenMixin(Text text_1, Screen parent) {
        super(text_1);
        this.parent = parent;
    }
    
    @Inject(at=@At("RETURN"), method="init")
    private void addOptionsScreen(CallbackInfo info) {
        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height / 6 - 12 + 24 * 5, 150, 20, I18n.translate("options.atmosfera.title"), (buttonWidget_1) -> {
            assert this.minecraft != null;
            this.minecraft.openScreen(new AmbientSoundScreen(this));
        }));
    }
}