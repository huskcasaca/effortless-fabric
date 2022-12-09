package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.entity.player.EffortlessDataProvider;
import dev.huskcasaca.effortless.buildmodifier.ReplaceMode;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import dev.huskcasaca.effortless.entity.player.ReachSettings;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerTagMixin implements EffortlessDataProvider {

    @Unique
    private ModeSettings modeSettings = null;
    @Unique
    private ModifierSettings modifierSettings = null;
    @Unique
    private ReachSettings reachSettings = null;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readTag(CompoundTag tag, CallbackInfo info) {
//        readModeSettings(tag.getCompound("EffortlessMode"));
//        readModifierSettings(tag.getCompound("EffortlessModifier"));
        if (tag.contains("Effortless")) {
            readSettings(tag.getCompound("Effortless"));
        } else {
            reachSettings = new ReachSettings();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeTag(CompoundTag tag, CallbackInfo info) {
//        CompoundTag modeTag = new CompoundTag();
//        writeModeSettings(modeTag);
//        tag.put("EffortlessMode", modeTag);
//
//        CompoundTag modifierTag = new CompoundTag();
//        writeModifierSettings(modifierTag);
//        tag.put("EffortlessModifier", modifierTag);

        CompoundTag tag1 = new CompoundTag();
        writeSettings(tag1);
        tag.put("Effortless", tag1);
    }

    @Unique
    private void readSettings(CompoundTag tag) {
        reachSettings = new ReachSettings(
                tag.getInt("maxReachDistance"),
                tag.getInt("maxBlockPlacePerAxis"),
                tag.getInt("maxBlockPlaceAtOnce"),
                tag.getBoolean("canBreakFar"),
                tag.getBoolean("enableUndoRedo"),
                tag.getInt("undoStackSize")
        );
    }

    @Unique
    private void writeSettings(CompoundTag tag) {
        if (reachSettings == null) reachSettings = new ReachSettings();

        tag.putInt("maxReachDistance", reachSettings.maxReachDistance());
        tag.putInt("maxBlockPlacePerAxis", reachSettings.maxBlockPlacePerAxis());
        tag.putInt("maxBlockPlaceAtOnce", reachSettings.maxBlockPlaceAtOnce());
        tag.putBoolean("canBreakFar", reachSettings.canBreakFar());
        tag.putBoolean("enableUndoRedo", reachSettings.enableUndoRedo());
        tag.putInt("undoStackSize", reachSettings.undoStackSize());

        //TODO add mode settings
    }


    @Unique
    private void readModeSettings(CompoundTag tag) {
        modeSettings = new ModeSettings(
                BuildMode.values()[tag.getInt("buildMode")],
                false
        );
    }

    @Unique
    private void writeModeSettings(CompoundTag tag) {
        if (modeSettings == null) modeSettings = new ModeSettings();
        tag.putInt("buildMode", modeSettings.buildMode().ordinal());
    }

    @Unique
    public void writeModifierSettings(CompoundTag tag) {
        if (modifierSettings == null) modifierSettings = new ModifierSettings();

        //ARRAY
        var arraySettings = modifierSettings.arraySettings();
        if (arraySettings == null) arraySettings = new Array.ArraySettings();
        tag.putBoolean("arrayEnabled", arraySettings.enabled());
        tag.putInt("arrayOffsetX", arraySettings.offset().getX());
        tag.putInt("arrayOffsetY", arraySettings.offset().getY());
        tag.putInt("arrayOffsetZ", arraySettings.offset().getZ());
        tag.putInt("arrayCount", arraySettings.count());

        //MIRROR
        var mirrorSettings = modifierSettings.mirrorSettings();
        if (mirrorSettings == null) mirrorSettings = new Mirror.MirrorSettings();
        tag.putBoolean("mirrorEnabled", mirrorSettings.enabled());
        tag.putDouble("mirrorPosX", mirrorSettings.position().x);
        tag.putDouble("mirrorPosY", mirrorSettings.position().y);
        tag.putDouble("mirrorPosZ", mirrorSettings.position().z);
        tag.putBoolean("mirrorX", mirrorSettings.mirrorX());
        tag.putBoolean("mirrorY", mirrorSettings.mirrorY());
        tag.putBoolean("mirrorZ", mirrorSettings.mirrorZ());
        tag.putInt("mirrorRadius", mirrorSettings.radius());
        tag.putBoolean("mirrorDrawLines", mirrorSettings.drawLines());
        tag.putBoolean("mirrorDrawPlanes", mirrorSettings.drawPlanes());

        //RADIAL MIRROR
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        if (radialMirrorSettings == null) radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
        tag.putBoolean("radialMirrorEnabled", radialMirrorSettings.enabled());
        tag.putDouble("radialMirrorPosX", radialMirrorSettings.position().x);
        tag.putDouble("radialMirrorPosY", radialMirrorSettings.position().y);
        tag.putDouble("radialMirrorPosZ", radialMirrorSettings.position().z);
        tag.putInt("radialMirrorSlices", radialMirrorSettings.slices());
        tag.putBoolean("radialMirrorAlternate", radialMirrorSettings.alternate());
        tag.putInt("radialMirrorRadius", radialMirrorSettings.radius());
        tag.putBoolean("radialMirrorDrawLines", radialMirrorSettings.drawLines());
        tag.putBoolean("radialMirrorDrawPlanes", radialMirrorSettings.drawPlanes());

        tag.putBoolean("enableQuickReplace", modifierSettings.enableQuickReplace()); // dont save quickreplace

    }

    @Unique
    public void readModifierSettings(CompoundTag tag) {

        //ARRAY
        boolean arrayEnabled = tag.getBoolean("arrayEnabled");
        var arrayOffset = new BlockPos(
                tag.getInt("arrayOffsetX"),
                tag.getInt("arrayOffsetY"),
                tag.getInt("arrayOffsetZ"));
        int arrayCount = tag.getInt("arrayCount");
        var arraySettings = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);

        //MIRROR
        boolean mirrorEnabled = tag.getBoolean("mirrorEnabled");
        var mirrorPosition = new Vec3(
                tag.getDouble("mirrorPosX"),
                tag.getDouble("mirrorPosY"),
                tag.getDouble("mirrorPosZ"));
        boolean mirrorX = tag.getBoolean("mirrorX");
        boolean mirrorY = tag.getBoolean("mirrorY");
        boolean mirrorZ = tag.getBoolean("mirrorZ");
        int mirrorRadius = tag.getInt("mirrorRadius");
        boolean mirrorDrawLines = tag.getBoolean("mirrorDrawLines");
        boolean mirrorDrawPlanes = tag.getBoolean("mirrorDrawPlanes");
        var mirrorSettings = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius, mirrorDrawLines, mirrorDrawPlanes);

        //boolean enableQuickReplace = compound.getBoolean("enableQuickReplace"); //dont load quickreplace

        //RADIAL MIRROR
        boolean radialMirrorEnabled = tag.getBoolean("radialMirrorEnabled");
        var radialMirrorPosition = new Vec3(
                tag.getDouble("radialMirrorPosX"),
                tag.getDouble("radialMirrorPosY"),
                tag.getDouble("radialMirrorPosZ"));
        int radialMirrorSlices = tag.getInt("radialMirrorSlices");
        boolean radialMirrorAlternate = tag.getBoolean("radialMirrorAlternate");
        int radialMirrorRadius = tag.getInt("radialMirrorRadius");
        boolean radialMirrorDrawLines = tag.getBoolean("radialMirrorDrawLines");
        boolean radialMirrorDrawPlanes = tag.getBoolean("radialMirrorDrawPlanes");
        var radialMirrorSettings = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition,
                radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);

        modifierSettings = new ModifierSettings(arraySettings, mirrorSettings, radialMirrorSettings, ReplaceMode.DISABLED);
    }


    @Override
    public ModeSettings getModeSettings() {
        if (modeSettings == null) modeSettings = new ModeSettings();
        return modeSettings;
    }

    @Override
    public void setModeSettings(ModeSettings modeSettings) {
        this.modeSettings = modeSettings;
    }

    @Override
    public ModifierSettings getModifierSettings() {
        if (modifierSettings == null) modifierSettings = new ModifierSettings();
        return modifierSettings;
    }

    @Override
    public void setModifierSettings(ModifierSettings modifierSettings) {
        this.modifierSettings = modifierSettings;
    }

    @Override
    public ReachSettings getReachSettings() {
        if (reachSettings == null) reachSettings = new ReachSettings();
        return reachSettings;
    }

    @Override
    public void setReachSettings(ReachSettings reachSettings) {
        this.reachSettings = reachSettings;

    }
}
