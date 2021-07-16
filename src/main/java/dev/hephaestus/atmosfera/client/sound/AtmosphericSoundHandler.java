package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableMultimap;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.concurrent.*;

public class AtmosphericSoundHandler {
    private static final Random RANDOM = new Random();

    private static final Map<Identifier, MusicSound> MUSIC = new HashMap<>();

    private final Collection<AtmosphericSound> sounds = new ArrayList<>();
    private final Collection<AtmosphericSound> musics = new ArrayList<>();
    private final Map<Identifier, AtmosphericSoundInstance> soundInstances = new WeakHashMap<>();
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
            ImmutableMultimap.Builder<String, AtmosphericSoundModifier> modifiers = ImmutableMultimap.builder();


            for (Map.Entry<String, AtmosphericSoundModifier.Factory> entry : definition.modifiers().entries()) {
                modifiers.put(entry.getKey(), entry.getValue().create(world));
            }

            this.sounds.add(new AtmosphericSound(definition.id(), definition.soundEvent(), definition.shape(), definition.size(), definition.defaultVolume(), definition.hasSubtitleByDefault(), modifiers.build()));
        }

        for (AtmosphericSoundDefinition definition : Atmosfera.MUSIC_DEFINITIONS.values()) {
            ImmutableMultimap.Builder<String, AtmosphericSoundModifier> modifiers = ImmutableMultimap.builder();


            for (Map.Entry<String, AtmosphericSoundModifier.Factory> entry : definition.modifiers().entries()) {
                modifiers.put(entry.getKey(), entry.getValue().create(world));
            }

            this.musics.add(new AtmosphericSound(definition.id(), definition.soundEvent(), definition.shape(), definition.size(), definition.defaultVolume(), definition.hasSubtitleByDefault(), modifiers.build()));
        }
    }

    public void tick() {
        if (this.taskQueue.isEmpty()) {
            this.executor.execute(this::tickSounds);
        }
    }

    private void tickSounds() {
        EnvironmentContext.update();

        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();

        for (AtmosphericSound definition : this.sounds) {
            Identifier id = definition.id();
            if (!this.soundInstances.containsKey(id) || this.soundInstances.get(id).isDone()) {
                float volume = definition.getVolume();

                // The non-zero volume prevents the events getting triggered multiple times at volumes near zero.
                if (volume >= 0.0125 && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.AMBIENT) > 0) {
                    AtmosphericSoundInstance soundInstance = new AtmosphericSoundInstance(definition, 0.0001F);
                    this.soundInstances.put(id, soundInstance);
                    soundManager.playNextTick(soundInstance);

                    Atmosfera.debug("[Atmosfera] volume > 0: " + definition.id() + " - " + volume);
                }
            }
        }
    }

    public MusicSound getMusicSound(MusicSound defaultSound) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        int total = Objects.requireNonNull(soundManager.get(defaultSound.getSound().getId())).getWeight();

        List<Pair<Integer, MusicSound>> sounds = new ArrayList<>();
        sounds.add(new Pair<>(total, defaultSound));

        for (AtmosphericSound definition : this.musics) {
            float volume = definition.getVolume();

            if (volume > 0.0125 && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MUSIC) > 0) {
                int weight = Objects.requireNonNull(soundManager.get(definition.soundEvent().getId())).getWeight();

                sounds.add(new Pair<>(weight, MUSIC.computeIfAbsent(definition.id(), id -> {
                    Atmosfera.debug("[Atmosfera] createIngameMusic: " + definition.id());
                    return MusicType.createIngameMusic(definition.soundEvent());
                })));

                total += 5 * volume;
            }
        }

        MusicSound result = defaultSound;

        int i = total <= 0 ? 0 : RANDOM.nextInt(total);

        for (Pair<Integer, MusicSound> pair : sounds) {
            i -= pair.getLeft();

            if (i <= 0) {
                result = pair.getRight();
            }
        }

        sounds.clear();

        return result;
    }
}
