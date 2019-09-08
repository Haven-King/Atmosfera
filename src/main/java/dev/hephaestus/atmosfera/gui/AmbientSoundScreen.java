package dev.hephaestus.atmosfera.gui;

import dev.hephaestus.atmosfera.AmbientSound;
import dev.hephaestus.atmosfera.AmbientSoundRegistry;
import dev.hephaestus.atmosfera.Atmosfera;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class AmbientSoundScreen extends Screen {
    protected final Screen parent;
    private AmbientSoundScreen.VolumeControlListWidget volumeControlList;

    public AmbientSoundScreen(Screen screen_1) {
        super(new TranslatableText("options.atmosfera.title", new Object[0]));
        this.parent = screen_1;
    }

    protected void init() {
        this.volumeControlList = new AmbientSoundScreen.VolumeControlListWidget(this.minecraft);
        this.children.add(this.volumeControlList);

        this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 38,
            150, 20, I18n.translate("gui.done"), (buttonWidget_1) -> {
                this.minecraft.openScreen(this.parent);
            }
        ));

        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 38,
            150, 20, I18n.translate("options.atmosfera.reset"), (buttonWidget_1) -> {
                for (AmbientSoundScreen.VolumeControlListWidget.VolumeEntry entry : volumeControlList.children()) {
                    entry.mouseClicked(100000, 0, 0);
                }
            }
        ));

        super.init();
    }

    public void render(int int_1, int int_2, float float_1) {
        this.volumeControlList.render(int_1, int_2, float_1);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
    }

    @Environment(EnvType.CLIENT)
    class VolumeControlListWidget
            extends VolumeSliderListWidget<AmbientSoundScreen.VolumeControlListWidget.VolumeEntry> {
        public VolumeControlListWidget(MinecraftClient minecraftClient_1) {
            super(minecraftClient_1, AmbientSoundScreen.this.width, AmbientSoundScreen.this.height, 32,
                    AmbientSoundScreen.this.height - 65 + 4, 18);
            for (AmbientSound sound : AmbientSoundRegistry.getRegistered()) {
                AmbientSoundScreen.VolumeControlListWidget.VolumeEntry entry = new AmbientSoundScreen.VolumeControlListWidget.VolumeEntry(
                        sound);
                this.addEntry(entry);
            }
        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        protected void renderBackground() {
            AmbientSoundScreen.this.renderBackground();
        }

        protected boolean isFocused() {
            return AmbientSoundScreen.this.getFocused() == this;
        }

        @Environment(EnvType.CLIENT)
        public class VolumeEntry extends
            VolumeSliderListWidget.Entry<AmbientSoundScreen.VolumeControlListWidget.VolumeEntry> {
            private VolumeSliderWidget slider;
            private ButtonWidget button;
            private AmbientSound sound;

            public VolumeEntry(AmbientSound sound) {
                this.sound = sound;
                this.slider = new VolumeSliderWidget(0, 0, sound, 8 * (VolumeControlListWidget.this.width / 10));
            }

            public void render(int int_1, int int_2, int int_3, int int_4, int int_5, int int_6, int int_7,
                    boolean boolean_1, float float_1) {

                slider.updateShape(VolumeControlListWidget.this.width / 2 - 128, int_2, 236);
                slider.render(int_6, int_7, float_1);
                button = new ButtonWidget(VolumeControlListWidget.this.width / 2 - 128 + 236, int_2, 20, 20, "X", (buttonWidget_1) -> {
                    this.sound.resetVolume();
                    this.slider = new VolumeSliderWidget(0, 0, this.sound, 8 * (VolumeControlListWidget.this.width / 10));
                    Atmosfera.handler.removeAll();
                    Atmosfera.configs.remove(sound.getAtmosferaId());
                });
                button.render(int_6, int_7, float_1);
            }

            public boolean mouseClicked(double double_1, double double_2, int int_1) {
                if (double_1 < (slider.getWidth() + slider.x)) {
                    slider.onClick(double_1, double_2);
                } else {
                    button.onClick(double_1, double_2);
                }

                return true;
            }
      }
   }
}