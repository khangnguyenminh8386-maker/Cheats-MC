/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.movement;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;


import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="Velocity", description="Modifies the amount of knockback that you receive.", category=Module.Category.MOVEMENT)
public class VelocityModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve the knockback modification.", "Normal", new String[]{"Normal", "Walls", "Cancel", "Grim"});
    public NumberSetting horizontal = new NumberSetting("Horizontal", "The amount of horizontal knockback that you will receive.", new ModeSetting.Visibility(this.mode, "Normal", "Walls"), (Number)0, (Number)0, (Number)100);
    public NumberSetting vertical = new NumberSetting("Vertical", "The amount of vertical knockback that you will receive.", new ModeSetting.Visibility(this.mode, "Normal", "Walls"), (Number)0, (Number)0, (Number)100);
    public BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", "Only reduces knockback while phased AND on the ground.", new ModeSetting.Visibility(this.mode, "Walls"), false);
    public BooleanSetting explosions = new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true);
    public BooleanSetting pause = new BooleanSetting("Pause", "Pauses the velocity for a certain duration whenever you get rubberbanded.", new ModeSetting.Visibility(this.mode, "Cancel", "Grim"), true);
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only reduces knockback while phased (Grim mode).", new ModeSetting.Visibility(this.mode, "Grim"), false);
    public BooleanSetting onlyWhenHeadCovered = new BooleanSetting("OnlyCoveredHead", "Only reduces knockback when a block is above your head (Grim mode).", new ModeSetting.Visibility(this.mode, "Grim"), false);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "Pause velocity if falling from more than this distance in blocks (0 to disable).", 10.0, 0.0, 50.0);
    public BooleanSetting conceal = new BooleanSetting("Conceal", "Ignores fake 0-0-0 velocity packets sent after a setback.", false);
    public CategorySetting antiPushCategory = new CategorySetting("AntiPush", "Prevents certain things from pushing you.");
    public BooleanSetting antiPush = new BooleanSetting("AntiPush", "Entities", "Prevents other entities from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), true);
    public BooleanSetting antiLiquidPush = new BooleanSetting("AntiLiquidPush", "Liquids", "Prevents liquids from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
    public BooleanSetting antiBlockPush = new BooleanSetting("AntiBlockPush", "Blocks", "Prevents you from being pushed outside of blocks.", new CategorySetting.Visibility(this.antiPushCategory), true);
    public BooleanSetting antiFishingRod = new BooleanSetting("AntiFishingRod", "FishingRods", "Prevents fishing rods from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
    private boolean cancel;
    private boolean pendingConcealment = false;
    private boolean pendingVelocity = false;

    @Override
    public void onEnable() {
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    @Override
    public void onDisable() {
        if (!this.pendingVelocity) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (VelocityModule.mc.player == null) {
            return;
        }
        if (this.pendingVelocity && this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
        this.cancel = false;
    }

    private void sendRotationFix() {
        float yaw = Night.ROTATION_MANAGER.getServerYaw();
        float pitch = Night.ROTATION_MANAGER.getServerPitch();
        float f = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
        float f2 = Mth.clamp((float)(pitch + f), (float)-90.0f, (float)90.0f);
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(VelocityModule.mc.player.getX(), VelocityModule.mc.player.getY(), VelocityModule.mc.player.getZ(), yaw, f2, VelocityModule.mc.player.onGround(), VelocityModule.mc.player.horizontalCollision));
    }

    @SubscribeEvent
   public void onPacketReceive(PacketReceiveEvent event) {
      if (mc.player != null) {
         if (!(this.fallDistance.getValue().doubleValue() > 0.0) || !(mc.player.fallDistance >= this.fallDistance.getValue().floatValue())) {
            if (event.getPacket() instanceof ClientboundPlayerPositionPacket && this.conceal.getValue()) {
               this.pendingConcealment = true;
            }

            if (event.getPacket() instanceof ClientboundSetEntityMotionPacket packet) {
               if (packet.id() != mc.player.getId()) {
                  return;
               }

               if (this.pendingConcealment && packet.movement().x == 0.0 && packet.movement().y == 0.0 && packet.movement().z == 0.0) {
                  this.pendingConcealment = false;
                  return;
               }

               switch (this.mode.getValue()) {
                  case "Normal":
                     this.scaleVelocity(event, packet);
                     break;
                  case "Walls":
                     if (!this.isPhased(mc.player) || this.onlyOnGround.getValue() && !mc.player.onGround()) {
                        return;
                     }

                     this.scaleVelocity(event, packet);
                     break;
                  case "Cancel":
                     if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                        return;
                     }

                     event.setCancelled(true);
                     break;
                  case "Grim":
                     if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                        return;
                     }

                     if (this.onlyPhased.getValue() && !this.isPhased(mc.player)) {
                        return;
                     }

                     if (this.onlyWhenHeadCovered.getValue()) {
                        BlockPos target = mc.player.blockPosition().above(2);
                        if (mc.player.isVisuallyCrawling()) {
                           target = mc.player.blockPosition().above(1);
                        }

                        if (mc.level.getBlockState(target).isAir()) {
                           return;
                        }
                     }

                     this.scaleVelocity(event, packet);
                     this.pendingVelocity = true;
               }
            }

            if (event.getPacket() instanceof ClientboundExplodePacket packet && this.explosions.getValue()) {
               switch (this.mode.getValue()) {
                  case "Normal":
                     this.scaleExplosion(packet);
                     break;
                  case "Walls":
                     if (this.isPhased(mc.player)) {
                        this.scaleExplosion(packet);
                     }
                     break;
                  case "Cancel":
                     if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                        return;
                     }

                     event.setCancelled(true);
                     break;
                  case "Grim":
                     if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                        return;
                     }

                     if (this.onlyPhased.getValue() && !this.isPhased(mc.player)) {
                        return;
                     }

                     if (this.onlyWhenHeadCovered.getValue()) {
                        BlockPos target = mc.player.blockPosition().above(2);
                        if (mc.player.isVisuallyCrawling()) {
                           target = mc.player.blockPosition().above(1);
                        }

                        if (mc.level.getBlockState(target).isAir()) {
                           return;
                        }
                     }

                     this.scaleExplosion(packet);
                     this.pendingVelocity = true;
               }

               if (event.isCancelled()) {
                  mc.executeBlocking(
                     () -> {
                        Vec3 vec3d = packet.center();
                        mc.getSoundManager()
                           .play(
                              new SimpleSoundInstance(
                                 packet.explosionSound().value(),
                                 SoundSource.BLOCKS,
                                 4.0F,
                                 (1.0F + (mc.level.getRandom().nextFloat() - mc.level.getRandom().nextFloat()) * 0.2F) * 0.7F,
                                 mc.level.getRandom(),
                                 vec3d.x,
                                 vec3d.y,
                                 vec3d.z
                              )
                           );
                        mc.level.addParticle(packet.explosionParticle(), vec3d.x, vec3d.y, vec3d.z, 1.0, 0.0, 0.0);
                     }
                  );
               }
            }
         }
      }
   }

    private void scaleVelocity(PacketReceiveEvent event, ClientboundSetEntityMotionPacket packet) {
        Vec3 velocity = VelocityModule.mc.player.getDeltaMovement();
        Vec3 target = packet.movement();
        double x = (target.x - velocity.x) * (this.horizontal.getValue().doubleValue() / 100.0) + velocity.x;
        double y = (target.y - velocity.y) * (this.vertical.getValue().doubleValue() / 100.0) + velocity.y;
        double z = (target.z - velocity.z) * (this.horizontal.getValue().doubleValue() / 100.0) + velocity.z;
        event.setCancelled(true);
        VelocityModule.mc.player.lerpMotion(new Vec3(x, y, z));
    }

    private void scaleExplosion(ClientboundExplodePacket packet) {
        if (packet.playerKnockback().isPresent()) {
            ((Vec3dAccessor)packet.playerKnockback().get()).setX((float)(((Vec3)packet.playerKnockback().get()).x * (this.horizontal.getValue().doubleValue() / 100.0)));
        }
        if (packet.playerKnockback().isPresent()) {
            ((Vec3dAccessor)packet.playerKnockback().get()).setY((float)(((Vec3)packet.playerKnockback().get()).y * (this.vertical.getValue().doubleValue() / 100.0)));
        }
        if (packet.playerKnockback().isPresent()) {
            ((Vec3dAccessor)packet.playerKnockback().get()).setZ((float)(((Vec3)packet.playerKnockback().get()).z * (this.horizontal.getValue().doubleValue() / 100.0)));
        }
    }

    private boolean isPhased(Entity entity) {
        if (entity == null || VelocityModule.mc.level == null) {
            return false;
        }
        AABB box = entity.getBoundingBox();
        int minX = Mth.floor((double)box.minX);
        int maxX = Mth.ceil((double)box.maxX);
        int minY = Mth.floor((double)box.minY);
        int maxY = Mth.ceil((double)box.maxY);
        int minZ = Mth.floor((double)box.minZ);
        int maxZ = Mth.ceil((double)box.maxZ);
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = VelocityModule.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)VelocityModule.mc.level, pos);
                    if (shape.isEmpty() || !shape.bounds().move(pos).intersects(box)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getMetaData() {
        if (this.mode.getValue().equalsIgnoreCase("Cancel")) {
            return "0%, 0%";
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            return "Grim";
        }
        return this.horizontal.getValue().intValue() + "%, " + this.vertical.getValue().intValue() + "%";
    }

    private static /* synthetic */ void lambda$onPacketReceive$0(ClientboundExplodePacket packet) {
        Vec3 vec3d = packet.center();
        mc.getSoundManager().play((SoundInstance)new SimpleSoundInstance((SoundEvent)packet.explosionSound().value(), SoundSource.BLOCKS, 4.0f, (1.0f + (VelocityModule.mc.level.getRandom().nextFloat() - VelocityModule.mc.level.getRandom().nextFloat()) * 0.2f) * 0.7f, VelocityModule.mc.level.getRandom(), vec3d.x, vec3d.y, vec3d.z));
        VelocityModule.mc.level.addParticle(packet.explosionParticle(), vec3d.x, vec3d.y, vec3d.z, 1.0, 0.0, 0.0);
    }
}

