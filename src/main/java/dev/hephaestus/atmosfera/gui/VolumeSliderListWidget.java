package dev.hephaestus.atmosfera.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(EnvType.CLIENT)
public abstract class VolumeSliderListWidget<E extends EntryListWidget.Entry<E>> extends EntryListWidget<E> {
   public VolumeSliderListWidget(MinecraftClient minecraftClient_1, int int_1, int int_2, int int_3, int int_4,
         int int_5) {
      super(minecraftClient_1, int_1, int_2, int_3, int_4, int_5);
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry<E extends VolumeSliderListWidget.Entry<E>> extends EntryListWidget.Entry<E> {

   }
}
