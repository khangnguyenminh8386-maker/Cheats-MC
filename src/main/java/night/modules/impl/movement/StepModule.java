/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 */
package night.modules.impl.movement;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.UpdateMovementEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="Step", description="Gives you the ability to instantly climb over a customizable amount of blocks.", category=Module.Category.MOVEMENT)
public class StepModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode that will be used to climb over blocks.", "Vanilla", new String[]{"Vanilla", "NCP"});
    public NumberSetting height = new NumberSetting("Height", "The maximum height at which blocks can be climbed over.", Float.valueOf(2.0f), Float.valueOf(0.0f), Float.valueOf(12.0f));
    public BooleanSetting useTimer = new BooleanSetting("UseTimer", "Uses timer to slow you down while climbing.", new ModeSetting.Visibility(this.mode, "NCP"), true);
    private boolean resetTimer = false;

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.resetTimer) {
            Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
            this.resetTimer = false;
        }
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent event) {
        if (StepModule.mc.player == null || StepModule.mc.level == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("NCP")) {
            return;
        }
        double stepHeight = StepModule.mc.player.getY() - StepModule.mc.player.yo;
        if (stepHeight <= 0.75 || stepHeight > this.height.getValue().doubleValue()) {
            return;
        }
        double[] offsets = this.getOffset(stepHeight);
        if (offsets != null && offsets.length > 1) {
            if (this.useTimer.getValue()) {
                Night.WORLD_MANAGER.setTimerMultiplier(1.0f / (float)offsets.length);
                this.resetTimer = true;
            }
            for (double offset : offsets) {
                mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(StepModule.mc.player.xo, StepModule.mc.player.yo + offset, StepModule.mc.player.zo, false, StepModule.mc.player.horizontalCollision));
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    public double[] getOffset(double height) {
        double[] dArray;
        switch ((int)(height * 10000.0)) {
            case 7500: 
            case 10000: {
                double[] dArray2 = new double[2];
                dArray2[0] = 0.42;
                dArray = dArray2;
                dArray2[1] = 0.753;
                break;
            }
            case 8125: 
            case 8750: {
                double[] dArray3 = new double[2];
                dArray3[0] = 0.39;
                dArray = dArray3;
                dArray3[1] = 0.7;
                break;
            }
            case 15000: {
                double[] dArray4 = new double[6];
                dArray4[0] = 0.42;
                dArray4[1] = 0.75;
                dArray4[2] = 1.0;
                dArray4[3] = 1.16;
                dArray4[4] = 1.23;
                dArray = dArray4;
                dArray4[5] = 1.2;
                break;
            }
            case 20000: {
                double[] dArray5 = new double[8];
                dArray5[0] = 0.42;
                dArray5[1] = 0.78;
                dArray5[2] = 0.63;
                dArray5[3] = 0.51;
                dArray5[4] = 0.9;
                dArray5[5] = 1.21;
                dArray5[6] = 1.45;
                dArray = dArray5;
                dArray5[7] = 1.43;
                break;
            }
            case 250000: {
                double[] dArray6 = new double[10];
                dArray6[0] = 0.425;
                dArray6[1] = 0.821;
                dArray6[2] = 0.699;
                dArray6[3] = 0.599;
                dArray6[4] = 1.022;
                dArray6[5] = 1.372;
                dArray6[6] = 1.652;
                dArray6[7] = 1.869;
                dArray6[8] = 2.019;
                dArray = dArray6;
                dArray6[9] = 1.907;
                break;
            }
            default: {
                dArray = null;
            }
        }
        return dArray;
    }
}

