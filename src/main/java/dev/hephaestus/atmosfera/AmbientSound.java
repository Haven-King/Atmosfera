package dev.hephaestus.atmosfera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class AmbientSound extends MovingSoundInstance {
    private final SoundEvent soundEvent;

    protected boolean done;
    private  float max_volume;
    private final Identifier id;
    private  float max_age;
    private  Function<Pair<World, BlockPos>, VolumeData> data_volume;
    private ClientPlayerEntity player;
    private int transitionTimer;
    private float age = 0;
    private ArrayList<Function<VolumeData, Boolean>> conditionsList = new ArrayList<>();

    // -------------------------------------------------------------------------------------------- //
    // --- Constructors --------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected AmbientSound(SoundEvent soundEvent_1) {
        super(soundEvent_1, SoundCategory.AMBIENT);
        this.soundEvent = soundEvent_1;
        this.max_volume = 1.0f;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = max_volume;
        this.field_18935 = true;
        this.looping = true;
        this.id = soundEvent_1.getId();
        this.max_age = 0;
        this.data_volume = (Pair<World, BlockPos> pair) -> VolumeData.sampleArea(pair.getLeft(), pair.getRight());

        conditionsList = new ArrayList<>();
    }

    // --- Copy constructor. Used by the AmbientSoundHandler to create new sound instances -------- //
    protected AmbientSound(AmbientSound sound) {
        super(sound.soundEvent, SoundCategory.AMBIENT);

        this.done = false;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
        this.field_18935 = true;
        this.looping = true;
        this.id = sound.id;

        this.soundEvent = sound.soundEvent;
        this.max_volume = sound.max_volume;
        this.data_volume = sound.data_volume;
        this.conditionsList = sound.conditionsList;
        this.max_age = sound.max_age;
    }

    // -------------------------------------------------------------------------------------------- //
    // --- Setters. Used by AmbientSoundRegistry to avoid having a lot of constructor variables --- //
    // --- and temporary variable is AmbientSoundRegistry.register -------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected void setMaxVolume(float volume) {
        this.max_volume = volume;
    }

    protected void setMaxAge(float age) {
        this.max_age = age * 20;
    }

    protected void setDataVolume(Function<Pair<World, BlockPos>,VolumeData> function) {
        this.data_volume = function;
    }

    protected void setPlayer(ClientPlayerEntity player) {
        this.player = player;
    }

    protected void setConditions(ArrayList<Function<VolumeData, Boolean>> condition_args) {
        this.conditionsList = condition_args;
    }

    protected void addCondition(Function<VolumeData, Boolean> condition) {
        conditionsList.add(condition);
    }

    protected void setDelay(float delay) {
        this.repeatDelay = (int)delay;
    }

    // -------------------------------------------------------------------------------------------- //
    // --- Runtime functions ---------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected boolean conditions(ClientPlayerEntity player) {
        VolumeData volumeData = this.data_volume.apply(new Pair<World, BlockPos>(player.world, player.getBlockPos()));


        boolean result = true;

        for (Function<VolumeData, Boolean> condition : conditionsList) {
            result = result && condition.apply(volumeData);

        }

        return conditionsList.size() == 0 ? false : result;
    }

    protected boolean conditions() {
        if (this.player != null) {
            return conditions(player);
        } else {
            return false;
        }
    }

    protected void play(ClientPlayerEntity player) {
        age++;

        if (!player.removed && this.transitionTimer >= 0) {
            if (conditions(player) && (age < max_age || max_age == 0)) {
                ++this.transitionTimer;
            } else {
                this.transitionTimer -= 1;
            }

            this.transitionTimer = Math.min(this.transitionTimer, 40);
            this.volume = Math.max(0.0F, Math.min((float)this.transitionTimer / 40.0F, this.max_volume));
        } else {
            this.done = true;
        }
    }

    protected void play() {
        if (this.player != null) {
            play(this.player);
        }
    }

    public boolean isDone() {
        return this.done;
    }

    public void reset() {
        this.transitionTimer = 0;
        this.done = false;
    }

    @Override
    public void tick() {

    }
}
