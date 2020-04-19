package dev.hephaestus.atmosfera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.conditions.Daytime;
import dev.hephaestus.atmosfera.conditions.DistanceFromGround;
import dev.hephaestus.atmosfera.conditions.Height;
import dev.hephaestus.atmosfera.conditions.PercentBiome;
import dev.hephaestus.atmosfera.conditions.PercentBlock;
import dev.hephaestus.atmosfera.conditions.SkyVisible;
import dev.hephaestus.atmosfera.conditions.SoundCondition;

@Environment(EnvType.CLIENT)
public class AmbientSound extends MovingSoundInstance {
    private final SoundEvent soundEvent;

    protected boolean done;
    private TranslatableText name;
    public final Identifier id;
    private int transitionTimer;
    private ArrayList<SoundCondition> conditionsList = new ArrayList<>();

    public int max_volume;
    public int default_volume;

    public VolumeData data_volume;

    // -------------------------------------------------------------------------------------------- //
    // --- Constructors --------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //

    // --- Initializes an AmbientSound from the definition. Not playable.
    protected AmbientSound(SoundEvent soundEvent_1, JsonObject json) {
        super(soundEvent_1, SoundCategory.AMBIENT);
        this.soundEvent = soundEvent_1;
        this.id = this.soundEvent.getId();

        String[] split = this.id.toString().split(":");
        this.name = new TranslatableText( split[0] + '.' + split[1]);

        this.default_volume = json.get("volume") == null ? 50 : MathHelper.clamp(json.get("volume").getAsInt(), 0, 100);
        this.max_volume = Atmosfera.CONFIG.contains(this.id) ? Atmosfera.CONFIG.get(this.id) : this.default_volume;

        if (json.get("data_volume") != null) {
            JsonObject dv = json.get("data_volume").getAsJsonObject();
            Direction direction = dv.get("direction") == null ? null : dv.get("direction").getAsString().equals("up")
                ? Direction.UP
                : Direction.DOWN;
            int radius = dv.get("radius") == null ? 16 : dv.get("radius").getAsInt();
            VolumeData.Type type = dv.get("type") == null ? VolumeData.Type.SAMPLE_SPHERE : direction != null
                ? dv.get("type").getAsString().equals("radius") ? VolumeData.Type.WITHIN_HEMISPHERE : VolumeData.Type.SAMPLE_HEMISPHERE
                : dv.get("type").getAsString().equals("radius") ? VolumeData.Type.WITHIN_HEMISPHERE : VolumeData.Type.SAMPLE_HEMISPHERE;

                this.data_volume = new VolumeData(type, direction, radius);
        } else {
            this.data_volume = new VolumeData(VolumeData.Type.SAMPLE_SPHERE, null, 16);
        }

        JsonObject conditions = json.get("conditions").getAsJsonObject();

        if (conditions.get("percent_block") != null) {
            for ( JsonElement entry : conditions.get("percent_block").getAsJsonArray()) {
                conditionsList.add(new PercentBlock(entry.getAsJsonObject()));
            }
        }

        if (conditions.get("percent_biome") != null) {
            for ( JsonElement entry : conditions.get("percent_biome").getAsJsonArray()) {
                conditionsList.add(new PercentBiome(entry.getAsJsonObject()));
            }
        }

        if (conditions.get("height") != null) {
            conditionsList.add(new Height(conditions.get("height").getAsJsonObject()));
        }

        if (conditions.get("distance_from_ground") != null) {
            conditionsList.add(new DistanceFromGround(conditions.get("distance_from_ground").getAsJsonObject()));
        }

        if (conditions.get("percent_sky_visible") != null) {
            conditionsList.add(new SkyVisible(conditions.get("percent_sky_visible").getAsJsonObject()));
        }

        if (conditions.get("is_daytime") != null) {
            conditionsList.add(new Daytime(conditions, conditions.get("is_daytime").getAsBoolean()));
        }
    }

    // --- Copy constructor. Used by the AmbientSoundHandler to create new sound instances -------- //
    protected AmbientSound(AmbientSound sound) {
        super(sound.soundEvent, SoundCategory.AMBIENT);

        this.done = false;
        this.repeat = true;
        this.volume = 0.0f;
        this.field_18935 = true;
        this.looping = true;

        this.soundEvent = sound.soundEvent;
        this.max_volume = sound.max_volume;
        this.default_volume = sound.default_volume;
        this.data_volume = sound.data_volume;
        this.conditionsList = sound.conditionsList;
        this.transitionTimer = sound.transitionTimer;
        this.id = sound.id;
        this.name = sound.name;
    }
    
    // -------------------------------------------------------------------------------------------- //
    // --- Runtime functions ---------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------- //
    protected boolean shouldPlay() {
        try {
            assert MinecraftClient.getInstance().player != null;
            data_volume.update(MinecraftClient.getInstance().player.world,MinecraftClient.getInstance().player.getBlockPos());
        } catch (NullPointerException e) {
            System.out.println("Atmosfera - Invalid configuration for sound " + this.getName().asString());
            return false;
        }

        boolean result = true;

        for (SoundCondition condition : conditionsList) {
            result = result && condition.apply(this.data_volume);

        }

        return conditionsList.size() != 0 && result;
    }

    public String getLangID() {
        return String.join(".", this.id.toString().split(":"));
    }

    protected void play() {
        assert MinecraftClient.getInstance().player != null;
        if (!MinecraftClient.getInstance().player.removed && this.transitionTimer >= 0) {
            if (shouldPlay()) {
                ++this.transitionTimer;
            } else {
                this.transitionTimer -= 2;
            }

            this.transitionTimer = Math.min(this.transitionTimer, 40);
            this.volume = Math.max(0.0F, Math.min((float)this.transitionTimer / 40.0F, ((float)this.max_volume) / 100f));
        } else {
            this.done = true;
        }
    }

    public boolean isDone() {
        return this.done;
    }

    @Override
    public void tick() {

    }

    public TranslatableText getName() {
        return this.name;
    }
}
