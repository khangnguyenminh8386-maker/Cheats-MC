/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.AttackEntityEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.pingbypass.server.ProxyServerTickListener;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;

@RegisterModule(name="Criticals", description="Changes player movement for always landing critical hits.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class CriticalsModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve critical hits.", "Packet", new String[]{"Packet", "Grim", "GrimNew"});
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only crits when you are phased into blocks.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    public BooleanSetting onlyStandingStill = new BooleanSetting("OnlyStandingStill", "Only crits when you are standing still.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    public BooleanSetting onlyWhenHeadCovered = new BooleanSetting("HeadCovered", "Only crits when there is a block covering your head.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    private long lastCritTime = 0L;

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        LivingEntity living;
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (CriticalsModule.mc.player == null || CriticalsModule.mc.level == null) {
            return;
        }
        if (CriticalsModule.mc.player.isFallFlying() || CriticalsModule.mc.player.isInWater() || CriticalsModule.mc.player.isInLava() || CriticalsModule.mc.player.isSuppressingSlidingDownLadder() || CriticalsModule.mc.player.hasEffect(MobEffects.BLINDNESS)) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity) || !(living = (LivingEntity)target).isAlive() || target instanceof EndCrystal || target instanceof ItemFrame) {
            return;
        }
        if (CriticalsModule.mc.player.isHandsBusy()) {
            this.ridingAttack(target);
            return;
        }
        this.onGroundAttack();
        CriticalsModule.mc.player.crit(target);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (event.getPacket() instanceof ServerboundAttackPacket) {
            long now = System.currentTimeMillis();
            if (now - this.lastCritTime < 50L) {
                return;
            }
            if (CriticalsModule.mc.player == null || CriticalsModule.mc.level == null) {
                return;
            }
            if (CriticalsModule.mc.player.isFallFlying() || CriticalsModule.mc.player.isInWater() || CriticalsModule.mc.player.isInLava() || CriticalsModule.mc.player.isSuppressingSlidingDownLadder() || CriticalsModule.mc.player.hasEffect(MobEffects.BLINDNESS)) {
                return;
            }
            this.onGroundAttack();
        }
    }

    private void ridingAttack(Entity target) {
        if (this.mode.getValue().equalsIgnoreCase("Packet")) {
            for (int i = 0; i < 5; ++i) {
                mc.getConnection().send((Packet)new ServerboundAttackPacket(target.getId()));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    private void onGroundAttack() {
        this.lastCritTime = System.currentTimeMillis();
        double x = CriticalsModule.mc.player.getX();
        double y = CriticalsModule.mc.player.getY();
        double z = CriticalsModule.mc.player.getZ();
        ProxyServerTickListener.allowSend(() -> {
            switch (this.mode.getValue()) {
                case "Packet": {
                    this.packetCrit(x, y, z);
                    break;
                }
                case "Grim": {
                    this.grimCrit(x, y, z);
                    break;
                }
                case "GrimNew": {
                    this.grimNewCrit(x, y, z);
                }
            }
        });
        ((ClientPlayerEntityAccessor)CriticalsModule.mc.player).setLastOnGround(false);
    }

    private void packetCrit(double x, double y, double z) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(x, y, z, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private void grimCrit(double x, double y, double z) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        if (this.onlyPhased.getValue() && !this.isPhased((Entity)CriticalsModule.mc.player)) {
            return;
        }
        if (this.onlyStandingStill.getValue() && (Math.abs(CriticalsModule.mc.player.getDeltaMovement().x) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().y) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().z) > 0.01)) {
            return;
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            BlockPos target;
            BlockPos pos = CriticalsModule.mc.player.blockPosition();
            BlockPos blockPos = target = CriticalsModule.mc.player.isVisuallyCrawling() ? pos.above(1) : pos.above(2);
            if (CriticalsModule.mc.level.getBlockState(target).isAir()) {
                return;
            }
        }
        float yaw = Night.ROTATION_MANAGER.getServerYaw();
        float pitch = Night.ROTATION_MANAGER.getServerPitch();
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625, z, yaw, pitch, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625013579, z, yaw, pitch, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y + 1.3579E-6, z, yaw, pitch, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private void grimNewCrit(double x, double y, double z) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        if (!(!this.onlyPhased.getValue() || this.isPhased((Entity)CriticalsModule.mc.player) && this.eyesPhased((Player)CriticalsModule.mc.player))) {
            return;
        }
        if (this.onlyStandingStill.getValue() && (Math.abs(CriticalsModule.mc.player.getDeltaMovement().x) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().y) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().z) > 0.01)) {
            return;
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            BlockPos target;
            BlockPos pos = CriticalsModule.mc.player.blockPosition();
            BlockPos blockPos = target = CriticalsModule.mc.player.isVisuallyCrawling() ? pos.above(1) : pos.above(2);
            if (CriticalsModule.mc.level.getBlockState(target).isAir()) {
                return;
            }
        }
        float yaw = Night.ROTATION_MANAGER.getServerYaw();
        float pitch = Night.ROTATION_MANAGER.getServerPitch();
        float f = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
        float f2 = Mth.clamp((float)(pitch + f), (float)-90.0f, (float)90.0f);
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y + 0.0626, z, yaw, f2, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y + 0.0455, z, yaw, f2, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private boolean isPhased(Entity e) {
        if (e == null || CriticalsModule.mc.level == null) {
            return false;
        }
        AABB box = e.getBoundingBox();
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
                    VoxelShape shape = CriticalsModule.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)CriticalsModule.mc.level, pos);
                    if (shape.isEmpty() || !shape.bounds().move(pos).intersects(box)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean eyesPhased(Player player) {
        Vec3 eyePos = player.getEyePosition();
        BlockPos pos = BlockPos.containing((Position)eyePos);
        BlockState state = CriticalsModule.mc.level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape((BlockGetter)CriticalsModule.mc.level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        for (AABB box : shape.toAabbs()) {
            if (!box.move(pos).contains(eyePos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

