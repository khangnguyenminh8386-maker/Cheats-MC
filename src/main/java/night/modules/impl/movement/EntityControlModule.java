/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package night.modules.impl.movement;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.MovementUtils;
import org.joml.Vector2d;

@RegisterModule(name="EntityControl", description="Allows you to control and fly with ridden entities.", category=Module.Category.MOVEMENT)
public class EntityControlModule
extends Module {
    public static EntityControlModule INSTANCE;
    public BooleanSetting fly = new BooleanSetting("Fly", "Allows you to fly with your vehicle.", true);
    public NumberSetting speed = new NumberSetting("Speed", "Horizontal flying speed.", 5.0, 0.1, 50.0);
    public NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", "Vertical flying speed.", 6.0, 0.0, 20.0);
    public NumberSetting fallSpeed = new NumberSetting("FallSpeed", "Glide down speed when no vertical key is pressed.", 0.1, 0.0, 50.0);
    public BooleanSetting control = new BooleanSetting("Control", "Steer entities even without a saddle.", true);
    public BooleanSetting noSync = new BooleanSetting("NoSync", "Cancels server vehicle sync packets to prevent rubberbanding.", false);
    private Entity controlledVehicle;

    public EntityControlModule() {
        INSTANCE = this;
    }

    public static EntityControlModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        this.releaseVehicleGravity();
    }

    private void releaseVehicleGravity() {
        if (this.controlledVehicle != null) {
            this.controlledVehicle.setNoGravity(false);
            this.controlledVehicle = null;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        double velY;
        if (this.getNull() || !this.fly.getValue()) {
            this.releaseVehicleGravity();
            return;
        }
        Entity vehicle = EntityControlModule.mc.player.getVehicle();
        if (vehicle == null) {
            this.releaseVehicleGravity();
            return;
        }
        if (this.controlledVehicle != null && this.controlledVehicle != vehicle) {
            this.releaseVehicleGravity();
        }
        this.controlledVehicle = vehicle;
        vehicle.setNoGravity(true);
        vehicle.setYRot(EntityControlModule.mc.player.getYRot());
        Vector2d motion = MovementUtils.forward(this.speed.getValue().doubleValue());
        double velX = motion.x;
        double velZ = motion.y;
        if (EntityControlModule.mc.gui.screen() instanceof ChatScreen || EntityControlModule.mc.gui.screen() != null) {
            velY = -this.fallSpeed.getValue().doubleValue() / 20.0;
        } else {
            boolean sprint;
            boolean jump = EntityControlModule.mc.options.keyJump.isDown();
            boolean bl = sprint = EntityControlModule.mc.options.keySprint.isDown() || EntityControlModule.mc.options.keyShift.isDown();
            velY = jump ? (sprint ? -this.fallSpeed.getValue().doubleValue() / 20.0 : this.verticalSpeed.getValue().doubleValue() / 20.0) : (sprint ? -this.verticalSpeed.getValue().doubleValue() / 20.0 : -this.fallSpeed.getValue().doubleValue() / 20.0);
        }
        vehicle.setDeltaMovement(new Vec3(velX, velY, velZ));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (this.getNull()) {
            return;
        }
        if (event.getPacket() instanceof ClientboundMoveVehiclePacket && this.noSync.getValue()) {
            event.cancel();
        }
    }
}

