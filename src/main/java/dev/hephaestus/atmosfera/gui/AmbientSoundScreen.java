package dev.hephaestus.atmosfera.gui;

import dev.hephaestus.atmosfera.AmbientSound;
import dev.hephaestus.atmosfera.AmbientSoundRegistry;
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
    private AmbientSoundScreen.LanguageSelectionListWidget languageSelectionList;

    public AmbientSoundScreen(Screen screen_1) {
        super(new TranslatableText("options.atmosfera.title", new Object[0]));
        this.parent = screen_1;
    }

    protected void init() {
        this.languageSelectionList = new AmbientSoundScreen.LanguageSelectionListWidget(this.minecraft);
        this.children.add(this.languageSelectionList);

        this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 38,
                310, 20, I18n.translate("gui.done"), (buttonWidget_1) -> {
                    this.minecraft.openScreen(this.parent);
                }));
        super.init();
    }

    public void render(int int_1, int int_2, float float_1) {
        this.languageSelectionList.render(int_1, int_2, float_1);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
    }

    @Environment(EnvType.CLIENT)
    class LanguageSelectionListWidget
            extends VolumeSliderListWidget<AmbientSoundScreen.LanguageSelectionListWidget.LanguageEntry> {
        public LanguageSelectionListWidget(MinecraftClient minecraftClient_1) {
            super(minecraftClient_1, AmbientSoundScreen.this.width, AmbientSoundScreen.this.height, 32,
                    AmbientSoundScreen.this.height - 65 + 4, 18);
            for (AmbientSound sound : AmbientSoundRegistry.getRegistered()) {
                AmbientSoundScreen.LanguageSelectionListWidget.LanguageEntry entry = new AmbientSoundScreen.LanguageSelectionListWidget.LanguageEntry(
                        sound);
                this.addEntry(entry);
            }
        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        public int getRowWidth() {
            return super.getRowWidth();
        }

        protected void renderBackground() {
            AmbientSoundScreen.this.renderBackground();
        }

        protected boolean isFocused() {
            return AmbientSoundScreen.this.getFocused() == this;
        }

        @Environment(EnvType.CLIENT)
        public class LanguageEntry extends
            VolumeSliderListWidget.Entry<AmbientSoundScreen.LanguageSelectionListWidget.LanguageEntry> {
            private VolumeSliderWidget slider;

            public LanguageEntry(AmbientSound sound) {
                this.slider = new VolumeSliderWidget(0, 0, sound, 8 * (LanguageSelectionListWidget.this.width / 10));
            }

            public void render(int int_1, int int_2, int int_3, int int_4, int int_5, int int_6, int int_7,
                    boolean boolean_1, float float_1) {

                slider.updateShape(LanguageSelectionListWidget.this.width / 2 - 128, int_2, 256);
                slider.render(int_6, int_7, minecraft.getLastFrameDuration());
            }

            public boolean mouseClicked(double double_1, double double_2, int int_1) {
                slider.onClick(double_1, double_2);

                return true;
            }
      }
   }
}