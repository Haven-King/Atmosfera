package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.concurrent.*;

public class AtmosphericSoundHandler {
    private static final Random RANDOM = new Random();

    private static final Map<AtmosphericSound, MusicSound> MUSIC = new HashMap<>();

    private final Collection<AtmosphericSound> sounds = new ArrayList<>();
    private final Collection<AtmosphericSound> musics = new ArrayList<>();
    private final Map<AtmosphericSound, AtmosphericSoundInstance> soundInstances = new WeakHashMap<>();
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;

    public AtmosphericSoundHandler(ClientWorld world) {
        this.executor = new ThreadPoolExecutor(1, 1,
                0, TimeUnit.MILLISECONDS,
                this.taskQueue,
                (runnable) -> {
                    Thread thread = new Thread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });

        for (AtmosphericSoundDefinition definition : Atmosfera.SOUND_DEFINITIONS.values()) {
            ImmutableCollection.Builder<AtmosphericSoundModifier> modifiers = ImmutableList.builder();

            for (AtmosphericSoundModifier.Factory factory : definition.modifiers()) {
                modifiers.add(factory.create(world));
            }

            this.sounds.add(new AtmosphericSound(definition.id(), definition.soundId(), definition.shape(), definition.size(), definition.defaultVolume(), definition.hasSubtitleByDefault(), modifiers.build()));
        }

        for (AtmosphericSoundDefinition definition : Atmosfera.MUSIC_DEFINITIONS.values()) {
            ImmutableCollection.Builder<AtmosphericSoundModifier> modifiers = ImmutableList.builder();

            for (AtmosphericSoundModifier.Factory factory : definition.modifiers()) {
                modifiers.add(factory.create(world));
            }

            this.musics.add(new AtmosphericSound(definition.id(), definition.soundId(), definition.shape(), definition.size(), definition.defaultVolume(), definition.hasSubtitleByDefault(), modifiers.build()));
        }
    }

    public void tick() {
        if (this.taskQueue.isEmpty()) {
            this.executor.execute(this::tickSounds);
        }
    }

    private void tickSounds() {
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world != null) {
            SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();

            world.atmosfera$updateEnvironmentContext();

            for (AtmosphericSound definition : this.sounds) {
                if (!this.soundInstances.containsKey(definition) || this.soundInstances.get(definition).isDone()) {
                    float volume = definition.getVolume(world);

                    // The non-zero volume prevents the events getting triggered multiple times at volumes near zero.
                    if (volume >= 0.0125 && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.AMBIENT) > 0) {
                        AtmosphericSoundInstance soundInstance = new AtmosphericSoundInstance(definition, 0.0001F);
                        this.soundInstances.put(definition, soundInstance);
                        soundManager.playNextTick(soundInstance);
                        Atmosfera.debug("volume > 0: {} - {}", definition.id(), volume);
                    }
                }
            }
        }

        this.soundInstances.values().removeIf(AtmosphericSoundInstance::isDone);
    }

    public MusicSound getMusicSound(MusicSound defaultSound) {
        MusicSound result = defaultSound;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world != null && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MUSIC) > 0 && MinecraftClient.getInstance().player != null && world.atmosfera$isEnvironmentContextInitialized()) {
            SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
            int total = Objects.requireNonNull(soundManager.get(defaultSound.getSound().getId())).getWeight();

            List<Pair<Integer, MusicSound>> sounds = new ArrayList<>();
            sounds.add(new Pair<>(total, defaultSound));

            for (AtmosphericSound definition : this.musics) {
                float volume = definition.getVolume(world);

                if (volume > 0.0125) {
                    int weight = Objects.requireNonNull(soundManager.get(definition.soundId())).getWeight();

                    sounds.add(new Pair<>(weight, MUSIC.computeIfAbsent(definition, id -> {
                        Atmosfera.debug("createIngameMusic: {}", definition.id());
                        return MusicType.createIngameMusic(new SoundEvent(definition.soundId()));
                    })));

                    total += 5 * volume;
                }
            }


            int i = total <= 0 ? 0 : RANDOM.nextInt(total);

            for (Pair<Integer, MusicSound> pair : sounds) {
                i -= pair.getLeft();

                if (i <= 0) {
                    result = pair.getRight();
                }
            }

            sounds.clear();
        }

        return result;
    }
}
