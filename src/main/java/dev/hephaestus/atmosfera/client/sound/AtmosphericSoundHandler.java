package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableList;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.mixin.SoundManagerAccessor;
import dev.hephaestus.atmosfera.mixin.SoundSystemAccessor;
import dev.hephaestus.atmosfera.util.NopLock;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AtmosphericSoundHandler {
    public static final Lock TICKING_SOUNDS_LOCK = FabricLoader.getInstance().isModLoaded("rsls") ? new ReentrantLock() : new NopLock();

    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<Identifier, Music> MUSIC_CACHE = new HashMap<>();

    private ImmutableList<AtmosphericSound> sounds;
    private ImmutableList<AtmosphericSound> musics;

    private final ClientLevel level;

    public AtmosphericSoundHandler(ClientLevel level) {
        this.level = level;
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        this.sounds = getSoundsFromDefinitions(Atmosfera.SOUND_DEFINITIONS, level);
        this.musics = getSoundsFromDefinitions(Atmosfera.MUSIC_DEFINITIONS, level);
    }

    private static ImmutableList<AtmosphericSound> getSoundsFromDefinitions(Map<Identifier, AtmosphericSoundDefinition> definitions, ClientLevel level) {
        var sounds = ImmutableList.<AtmosphericSound>builder();

        for (var definition : definitions.values()) {
            var modifiers = ImmutableList.<AtmosphericSoundModifier>builder();

            for (var factory : definition.modifierFactories()) {
                modifiers.add(factory.create(level));
            }

            sounds.add(new AtmosphericSound(definition.id(), definition.soundId(), definition.shape(), definition.size(), modifiers.build()));
        }

        return sounds.build();
    }

    public void tick() {
        level.atmosfera$updateEnvironmentContext();

        var client = Minecraft.getInstance();
        if (client.options.getFinalSoundSourceVolume(SoundSource.AMBIENT) == 0)
            return;

        var soundManager = client.getSoundManager();
        var tickingSounds = ((SoundSystemAccessor) ((SoundManagerAccessor) soundManager).getSoundEngine()).getTickingSounds();

        for (var sound : sounds) {
            TICKING_SOUNDS_LOCK.lock();
            try {
                // don't play sound if it's already playing
                if (tickingSounds.stream()
                        .filter(s -> s instanceof AtmosphericSoundInstance)
                        .map(AtmosphericSoundInstance.class::cast)
                        .anyMatch(s -> sound.soundId().equals(s.getIdentifier())))
                    continue;
            } finally {
                TICKING_SOUNDS_LOCK.unlock();
            }

            float volume = sound.getVolume(level);

            // The non-zero volume prevents the events getting triggered multiple times at volumes near zero.
            if (volume >= 0.0125) {
                soundManager.queueTickingSound(new AtmosphericSoundInstance(sound));
                Atmosfera.debug("volume > 0: {} - {}", sound.id(), volume);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public Music getMusicSound(Music original) {
        var client = Minecraft.getInstance();
        if (!level.atmosfera$isEnvironmentContextInitialized() || client.options.getFinalSoundSourceVolume(SoundSource.MUSIC) == 0)
            return original;

        var soundManager = client.getSoundManager();
        float originalWeight = soundManager.getSoundEvent(original.sound().value().location()).getWeight(); // TODO soundManager.get() returns null with Music Control...?!

        List<Tuple<Float, Music>> candidates = new ArrayList<>();
        float total = 0;

        candidates.add(new Tuple<>(originalWeight, original));
        total += originalWeight;

        for (var music : musics) {
            float volume = music.getVolume(level);

            if (volume >= 0.0125) {
                float weight = AtmosferaConfig.customMusicWeightScale() * soundManager.getSoundEvent(music.soundId()).getWeight();
                var musicSound = MUSIC_CACHE.computeIfAbsent(music.soundId(), id -> Musics.createGameMusic(Holder.direct(SoundEvent.createVariableRangeEvent(id))));

                candidates.add(new Tuple<>(weight, musicSound));
                total += weight;
            }
        }

        float i = total <= 0 ? 0 : RANDOM.nextFloat() * total;

        for (Tuple<Float, Music> pair : candidates) {
            i -= pair.getA();

            if (i < 0)
                return pair.getB();
        }

        // due to float imprecision, i might not have fallen below 0, count this towards the last element
        return candidates.getLast().getB();
    }
}
