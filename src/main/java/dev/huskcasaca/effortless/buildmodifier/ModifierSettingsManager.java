package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessDataProvider;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import dev.huskcasaca.effortless.helper.ReachHelper;
import dev.huskcasaca.effortless.network.ModifierSettingsMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

//@Mod.EventBusSubscriber
public class ModifierSettingsManager {

    //Retrieves the build settings of a player through the modifierCapability capability
    //Never returns null
    public static ModifierSettings getModifierSettings(Player player) {
        return ((EffortlessDataProvider) player).getModifierSettings();
    }

    public static void setModifierSettings(Player player, ModifierSettings modifierSettings) {
        if (player == null) {
            Effortless.log("Cannot set build modifier settings, player is null");
            return;
        }
        ((EffortlessDataProvider) player).setModifierSettings(modifierSettings);

    }

    public static String getSanitizeMessage(ModifierSettings modifierSettings, Player player) {
        int maxReach = ReachHelper.getMaxReach(player);
        String error = "";

        //Array settings
        var arraySettings = modifierSettings.arraySettings();
        if (arraySettings.count() < 0) {
            error += "Array count may not be negative. It has been reset to 0. \n";
        }

        if (arraySettings.reach() > maxReach) {
            error += "Array exceeds your maximum reach of " + maxReach + ". Array count has been reset to 0. \n";
        }

        //Mirror settings
        var mirrorSettings = modifierSettings.mirrorSettings();
        if (mirrorSettings.radius() < 1) {
            error += "Mirror size has to be at least 1. This has been corrected. \n";
        }
        if (mirrorSettings.reach() > maxReach) {
            error += "Mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". \n";
        }

        //Radial mirror settings
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        if (radialMirrorSettings.slices() < 2) {
            error += "Radial mirror needs to have at least 2 slices. Slices has been set to 2. \n";
        }

        if (radialMirrorSettings.radius() < 1) {
            error += "Radial mirror radius has to be at least 1. This has been corrected. \n";
        }
        if (radialMirrorSettings.reach() > maxReach) {
            error += "Radial mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". \n";
        }

        return error;
    }

    // TODO: 17/9/22
    public static ModifierSettings sanitize(ModifierSettings modifierSettings, Player player) {
        int maxReach = ReachHelper.getMaxReach(player);

        //Array settings
        var arraySettings = modifierSettings.arraySettings();
        int count = arraySettings.count();
        if (count < 0) {
            count = 0;
        }

        if (arraySettings.reach() > maxReach) {
            count = 0;
        }
        arraySettings = new Array.ArraySettings(
                arraySettings.enabled(),
                arraySettings.offset(),
                count
        );

        //Mirror settings
        var mirrorSettings = modifierSettings.mirrorSettings();
        int radius = mirrorSettings.radius();
        if (radius < 1) {
            radius = 1;
        }
        if (mirrorSettings.reach() > maxReach) {
            radius = maxReach / 2;
        }
        mirrorSettings = new Mirror.MirrorSettings(
                mirrorSettings.enabled(),
                mirrorSettings.position(),
                mirrorSettings.mirrorX(),
                mirrorSettings.mirrorY(),
                mirrorSettings.mirrorZ(),
                radius,
                mirrorSettings.drawLines(),
                mirrorSettings.drawPlanes()
        );

        //Radial mirror settings
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        int slices = radialMirrorSettings.slices();
        if (slices < 2) {
            slices = 2;
        }
        int radius1 = radialMirrorSettings.radius();
        if (radius1 < 1) {
            radius1 = 1;
        }
        if (radialMirrorSettings.reach() > maxReach) {
            radius1 = maxReach / 2;
        }
        radialMirrorSettings = new RadialMirror.RadialMirrorSettings(
                radialMirrorSettings.enabled(),
                radialMirrorSettings.position(),
                slices,
                radialMirrorSettings.alternate(),
                radius1,
                radialMirrorSettings.drawLines(),
                radialMirrorSettings.drawPlanes()
        );

        //Other
        boolean quickReplace = modifierSettings.quickReplace;

        return new ModifierSettings(
                arraySettings,
                mirrorSettings,
                radialMirrorSettings,
                quickReplace
        );
    }

    public static void handleNewPlayer(Player player) {
        //Makes sure player has modifier settings (if it doesn't it will create it)
        getModifierSettings(player);

        //Only on server
        if (!player.level.isClientSide) {
            //Send to client
            ModifierSettingsMessage msg = new ModifierSettingsMessage(getModifierSettings(player));
            PacketHandler.sendToClient(msg, (ServerPlayer) player);
        }
    }

    public record ModifierSettings(
            Array.ArraySettings arraySettings,
            Mirror.MirrorSettings mirrorSettings,
            RadialMirror.RadialMirrorSettings radialMirrorSettings,
            boolean quickReplace
    ) {

        public ModifierSettings() {
            this(new Array.ArraySettings(), new Mirror.MirrorSettings(), new RadialMirror.RadialMirrorSettings(), false);
        }

        public ModifierSettings(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings,
                                RadialMirror.RadialMirrorSettings radialMirrorSettings) {
            this(arraySettings, mirrorSettings, radialMirrorSettings, false);
        }

//        public void setReachUpgrade(int reachUpgrade) {
//            this.reachUpgrade = reachUpgrade;
//            //Set mirror radius to max
//            int reach = 10;
//            switch (reachUpgrade) {
//                case 0:
//                    reach = BuildConfig.reach.maxReachLevel0;
//                    break;
//                case 1:
//                    reach = BuildConfig.reach.maxReachLevel1;
//                    break;
//                case 2:
//                    reach = BuildConfig.reach.maxReachLevel2;
//                    break;
//                case 3:
//                    reach = BuildConfig.reach.maxReachLevel3;
//                    break;
//            }
//
//            if (this.mirrorSettings != null)
//                this.mirrorSettings.radius = reach / 2;
//            if (this.radialMirrorSettings != null)
//                this.radialMirrorSettings.radius = reach / 2;
//        }
    }
}

