package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.mixin.SoundManagerAccessor;
import dev.hephaestus.atmosfera.mixin.SoundSystemAccessor;
import dev.hephaestus.atmosfera.util.NopLock;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AtmosphericSoundHandler {
    public static final Lock TICKING_SOUNDS_LOCK = FabricLoader.getInstance().isModLoaded("rsls") ? new ReentrantLock() : new NopLock();

    private static final Random RANDOM = Random.create();
    private static final Map<Identifier, MusicSound> MUSIC_CACHE = new HashMap<>();

    private ImmutableList<AtmosphericSound> sounds;
    private ImmutableList<AtmosphericSound> musics;

    private final ClientWorld world;

    public AtmosphericSoundHandler(ClientWorld world) {
        this.world = world;
        reloadDefinitions();
    }

    public void reloadDefinitions() {
        this.sounds = getSoundsFromDefinitions(Atmosfera.SOUND_DEFINITIONS, world);
        this.musics = getSoundsFromDefinitions(Atmosfera.MUSIC_DEFINITIONS, world);
    }

    private static ImmutableList<AtmosphericSound> getSoundsFromDefinitions(Map<Identifier, AtmosphericSoundDefinition> definitions, ClientWorld world) {
        var sounds = ImmutableList.<AtmosphericSound>builder();

        for (var definition : definitions.values()) {
            var modifiers = ImmutableList.<AtmosphericSoundModifier>builder();

            for (var factory : definition.modifierFactories()) {
                modifiers.add(factory.create(world));
            }

            sounds.add(new AtmosphericSound(definition.id(), definition.soundId(), definition.soundIdAlias(), definition.shape(), definition.size(), modifiers.build()));
        }

        return sounds.build();
    }

    public void tick() {
        world.atmosfera$updateEnvironmentContext();

        var client = MinecraftClient.getInstance();
        var soundManager = client.getSoundManager();
        var tickingSounds = ((SoundSystemAccessor) ((SoundManagerAccessor) soundManager).getSoundSystem()).getTickingSounds();

        Set<Identifier> playingSounds = new HashSet<>();

        TICKING_SOUNDS_LOCK.lock();
        try {
            for (var s : tickingSounds)
                if (s instanceof AtmosphericSoundInstance a) {
                    playingSounds.add(a.getDefinition().getAliasedSoundId());
                }
        } finally {
            TICKING_SOUNDS_LOCK.unlock();
        }

        var shuffledSounds = new ArrayList<>(sounds);
        Collections.shuffle(shuffledSounds);

        for (var sound : shuffledSounds) {
            // don't play sound if it's already playing
            if (playingSounds.contains(sound.getAliasedSoundId()))
                continue;

            float volume = sound.getVolume(world);

            // The non-zero volume prevents the events getting triggered multiple times at volumes near zero.
            if (volume >= 0.0125) {
                soundManager.playNextTick(new AtmosphericSoundInstance(sound));
                playingSounds.add(sound.getAliasedSoundId());
                Atmosfera.debug("volume > 0: {} - {}", sound.id(), volume);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public MusicSound getMusicSound(MusicSound original) {
        var client = MinecraftClient.getInstance();
        if (!world.atmosfera$isEnvironmentContextInitialized() || client.options.getSoundVolume(SoundCategory.MUSIC) == 0)
            return original;

        var soundManager = client.getSoundManager();
        float originalWeight = soundManager.get(original.sound().value().id()).getWeight(); // TODO soundManager.get() returns null with Music Control...?!

        List<Pair<Float, MusicSound>> candidates = new ArrayList<>();
        float total = 0;

        candidates.add(new Pair<>(originalWeight, original));
        total += originalWeight;

        for (var music : musics) {
            float volume = music.getVolume(world);

            if (volume >= 0.0125) {
                float weight = AtmosferaConfig.customMusicWeightScale() * soundManager.get(music.soundId()).getWeight();
                var musicSound = MUSIC_CACHE.computeIfAbsent(music.soundId(), id -> MusicType.createIngameMusic(RegistryEntry.of(SoundEvent.of(id))));

                candidates.add(new Pair<>(weight, musicSound));
                total += weight;
            }
        }

        float i = total <= 0 ? 0 : RANDOM.nextFloat() * total;

        for (Pair<Float, MusicSound> pair : candidates) {
            i -= pair.getLeft();

            if (i < 0)
                return pair.getRight();
        }

        // due to float imprecision, i might not have fallen below 0, count this towards the last element
        return candidates.getLast().getRight();
    }
}
