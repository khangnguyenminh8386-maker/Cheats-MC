/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.system.Timer;

@RegisterModule(name="PearlChase", description="Automatically throws an Ender Pearl to chase enemy pearl throws with trajectory prediction and obstacle avoidance.", category=Module.Category.COMBAT)
public class PearlChaseModule
extends Module {
    public ModeSetting target = new ModeSetting("Target", "Priority used to select which enemy to chase.", "Nearest", new String[]{"Lowest", "Furthest", "Nearest"});
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore naked players, even if wearing only elytra.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "Rotation mode used before throwing the pearl.", "Silent", new String[]{"None", "Normal", "Silent"});
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to ender pearls.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting offset = new NumberSetting("Offset", "Distance offset relative to target landing position along flight path (positive = ahead, negative = behind).", 0.0, -10.0, 10.0);
    public NumberSetting distance = new NumberSetting("Distance", "Minimum horizontal travel distance that enemy will move for pearl to be chased.", 10.0, 0.0, 50.0);
    public ModeSetting arcMode = new ModeSetting("ArcMode", "Trajectory selection mode: Smart (low if clear, high if blocked), Low (fast direct), High (maximum lob).", "Smart", new String[]{"Smart", "Low", "High"});
    public BooleanSetting avoidObstacles = new BooleanSetting("AvoidObstacles", "Calculates alternate high arcs or yaw offsets if direct path is blocked by terrain.", true);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "Maximum range to enemy player throwing pearl.", 64.0, 8.0, 128.0);
    public NumberSetting delay = new NumberSetting("Delay", "Delay in ticks before throwing the pearl.", 0, 0, 20);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Allows throwing pearl while eating.", true);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off module if no ender pearls are found in inventory.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Renders predicted landing position and pearl trajectory line.", true);
    public NumberSetting renderDuration = new NumberSetting("RenderTime", "Duration in seconds to render trajectory after throwing.", 3.0, 0.5, 10.0);
    private final Set<Integer> processedPearls = new HashSet<Integer>();
    private PendingThrow pendingThrow = null;
    private final Timer renderTimer = new Timer();
    private Vec3 renderLandingPos = null;
    private List<Vec3> renderPath = null;

    @Override
    public void onEnable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    @Override
    public void onDisable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        TrajectorySolution solution;
        double dz;
        Vec3 enemyLandingPos;
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof ThrownEnderpearl)) {
            return;
        }
        ThrownEnderpearl pearl = (ThrownEnderpearl)entity;
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        Player owner = null;
        Entity entity2 = pearl.getOwner();
        if (entity2 instanceof Player) {
            Player p;
            owner = p = (Player)entity2;
        } else {
            double minD = 16.0;
            for (Player p : PearlChaseModule.mc.level.players()) {
                double d;
                if (!this.validTarget(p) || !((d = p.distanceToSqr(pearl.position())) < minD)) continue;
                minD = d;
                owner = p;
            }
        }
        if (owner == null || !this.validTarget(owner)) {
            return;
        }
        Player bestTarget = this.getBestTarget();
        if (bestTarget != null && owner != bestTarget) {
            return;
        }
        if (!this.processedPearls.add(pearl.getId())) {
            return;
        }
        if (this.processedPearls.size() > 100) {
            this.processedPearls.clear();
        }
        if ((enemyLandingPos = this.predictEnemyLanding(pearl, owner)) == null) {
            return;
        }
        double dx = enemyLandingPos.x - pearl.getX();
        double horizontalDistance = Math.sqrt(dx * dx + (dz = enemyLandingPos.z - pearl.getZ()) * dz);
        if (horizontalDistance < this.distance.getValue().doubleValue()) {
            return;
        }
        Vec3 flightDir = new Vec3(dx, 0.0, dz);
        flightDir = horizontalDistance > 1.0E-4 ? flightDir.scale(1.0 / horizontalDistance) : Vec3.ZERO;
        Vec3 targetPos = enemyLandingPos.add(flightDir.scale(this.offset.getValue().doubleValue()));
        BlockHitResult groundHit = PearlChaseModule.mc.level.clip(new ClipContext(targetPos.add(0.0, 2.0, 0.0), targetPos.subtract(0.0, 10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
        if (groundHit.getType() != HitResult.Type.MISS) {
            targetPos = groundHit.getLocation();
        }
        if ((solution = this.solveTrajectory(targetPos)) == null) {
            return;
        }
        int delayTicks = this.delay.getValue().intValue();
        if (delayTicks <= 0) {
            this.executeThrow(solution.yaw(), solution.pitch(), targetPos, solution.path());
        } else {
            this.pendingThrow = new PendingThrow(targetPos, solution.yaw(), solution.pitch(), delayTicks, solution.path());
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        if (this.pendingThrow != null) {
            if (!this.whileEating.getValue() && EntityUtils.isEating()) {
                return;
            }
            int remaining = this.pendingThrow.delayTicks() - 1;
            if (remaining <= 0) {
                TrajectorySolution solution = this.solveTrajectory(this.pendingThrow.targetPos());
                if (solution != null) {
                    this.executeThrow(solution.yaw(), solution.pitch(), this.pendingThrow.targetPos(), solution.path());
                } else {
                    this.executeThrow(this.pendingThrow.yaw(), this.pendingThrow.pitch(), this.pendingThrow.targetPos(), this.pendingThrow.path());
                }
                this.pendingThrow = null;
            } else {
                this.pendingThrow = new PendingThrow(this.pendingThrow.targetPos(), this.pendingThrow.yaw(), this.pendingThrow.pitch(), remaining, this.pendingThrow.path());
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        long maxDuration;
        if (!this.render.getValue() || this.renderLandingPos == null || PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        long elapsed = this.renderTimer.timeElapsed();
        if (elapsed > (maxDuration = (long)(this.renderDuration.getValue().doubleValue() * 1000.0))) {
            this.renderLandingPos = null;
            this.renderPath = null;
            return;
        }
        float alphaRatio = 1.0f - (float)elapsed / (float)maxDuration;
        int alphaFill = (int)(60.0f * alphaRatio);
        int alphaOutline = (int)(220.0f * alphaRatio);
        if (alphaFill <= 0 && alphaOutline <= 0) {
            return;
        }
        Color fillColor = new Color(0, 230, 255, alphaFill);
        Color outlineColor = new Color(0, 230, 255, alphaOutline);
        Color pathColor = new Color(80, 255, 180, alphaOutline);
        AABB box = new AABB(this.renderLandingPos.x - 0.35, this.renderLandingPos.y, this.renderLandingPos.z - 0.35, this.renderLandingPos.x + 0.35, this.renderLandingPos.y + 0.7, this.renderLandingPos.z + 0.35);
        Renderer3D.renderBox(event.getMatrices(), box, fillColor);
        Renderer3D.renderBoxOutline(event.getMatrices(), box, outlineColor);
        if (this.renderPath != null && this.renderPath.size() > 1) {
            for (int i = 0; i < this.renderPath.size() - 1; ++i) {
                Renderer3D.renderLine(event.getMatrices(), this.renderPath.get(i), this.renderPath.get(i + 1), pathColor);
            }
        }
    }

    private void executeThrow(float yaw, float pitch, Vec3 targetPos, List<Vec3> path) {
        if (this.switchMode.getValue().equalsIgnoreCase("None") && PearlChaseModule.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("You are currently not holding an Ender Pearl.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        int slot = InventoryUtils.find(Items.ENDER_PEARL, 0, this.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.switchMode.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = PearlChaseModule.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("No Ender Pearls could be found in your inventory.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.packetRotate(yaw, pitch, true);
        } else if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(new float[]{yaw, pitch}, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            PearlChaseModule.mc.player.setYRot(yaw);
            PearlChaseModule.mc.player.setXRot(pitch);
        }
        InventoryUtils.switchSlot(this.switchMode.getValue(), slot, previousSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.switchMode.getValue(), slot, previousSlot);
        this.renderLandingPos = targetPos;
        this.renderPath = path;
        this.renderTimer.reset();
    }

    private Vec3 predictEnemyLanding(ThrownEnderpearl pearl, Player owner) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-4 && owner != null) {
            vel = PearlChaseModule.getPearlInitialVelocity(owner.getYRot(), owner.getXRot(), owner.getDeltaMovement(), owner.onGround());
        }
        for (int t = 0; t < 300; ++t) {
            Vec3 nextPos = pos.add(vel);
            BlockHitResult hit = PearlChaseModule.mc.level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)pearl));
            if (hit.getType() != HitResult.Type.MISS) {
                return hit.getLocation();
            }
            pos = nextPos;
            vel = vel.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        return pos;
    }

    private TrajectorySolution solveTrajectory(Vec3 targetPos) {
        float[] yawOffsets;
        float[] fArray;
        float baseYaw;
        Vec3 eyePos = PearlChaseModule.mc.player.getEyePosition();
        Vec3 diff = targetPos.subtract(eyePos);
        float bestYaw = baseYaw = (float)Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float bestPitch = 0.0f;
        double bestScore = Double.MAX_VALUE;
        if (this.avoidObstacles.getValue()) {
            float[] fArray2 = new float[7];
            fArray2[0] = 0.0f;
            fArray2[1] = -1.0f;
            fArray2[2] = 1.0f;
            fArray2[3] = -2.5f;
            fArray2[4] = 2.5f;
            fArray2[5] = -4.0f;
            fArray = fArray2;
            fArray2[6] = 4.0f;
        } else {
            float[] fArray3 = new float[1];
            fArray = fArray3;
            fArray3[0] = 0.0f;
        }
        for (float yOff : yawOffsets = fArray) {
            float testYaw = baseYaw + yOff;
            for (float p = -85.0f; p <= 85.0f; p += 1.0f) {
                SimulationResult sim = this.simulatePearl(eyePos, testYaw, p, targetPos, false);
                double score = sim.distToTarget();
                if (sim.hitEarlyObstacle()) {
                    score += 500.0;
                }
                if (this.arcMode.getValue().equalsIgnoreCase("Low")) {
                    score += (double)Math.max(0.0f, -p) * 0.1;
                } else if (this.arcMode.getValue().equalsIgnoreCase("High")) {
                    score += Math.max(0.0, (double)p + 50.0) * 0.2;
                } else if (this.arcMode.getValue().equalsIgnoreCase("Smart") && !sim.hitEarlyObstacle()) {
                    score += (double)Math.max(0.0f, -p) * 0.02;
                }
                if (!(score < bestScore)) continue;
                bestScore = score;
                bestPitch = p;
                bestYaw = testYaw;
            }
            if (bestScore < 1.0) break;
        }
        float refinedPitch = bestPitch;
        for (float p = bestPitch - 1.5f; p <= bestPitch + 1.5f; p += 0.1f) {
            SimulationResult sim = this.simulatePearl(eyePos, bestYaw, p, targetPos, false);
            double score = sim.distToTarget();
            if (sim.hitEarlyObstacle()) {
                score += 500.0;
            }
            if (!(score < bestScore)) continue;
            bestScore = score;
            refinedPitch = p;
        }
        SimulationResult finalSim = this.simulatePearl(eyePos, bestYaw, refinedPitch, targetPos, true);
        return new TrajectorySolution(bestYaw, refinedPitch, finalSim.path());
    }

    private SimulationResult simulatePearl(Vec3 startPos, float yaw, float pitch, Vec3 targetPos, boolean recordPath) {
        ArrayList<Vec3> path;
        Vec3 pVel = PearlChaseModule.mc.player != null ? PearlChaseModule.mc.player.getDeltaMovement() : Vec3.ZERO;
        boolean onGround = PearlChaseModule.mc.player == null || PearlChaseModule.mc.player.onGround();
        Vec3 vel = PearlChaseModule.getPearlInitialVelocity(yaw, pitch, pVel, onGround);
        Vec3 currentPos = startPos;
        ArrayList<Vec3> arrayList = path = recordPath ? new ArrayList<Vec3>() : null;
        if (recordPath) {
            path.add(currentPos);
        }
        Vec3 landing = null;
        boolean hitEarly = false;
        double totalTargetDist = targetPos.distanceTo(startPos);
        for (int t = 0; t < 250; ++t) {
            Vec3 nextPos = currentPos.add(vel);
            BlockHitResult hit = PearlChaseModule.mc.level.clip(new ClipContext(currentPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                landing = hit.getLocation();
                if (recordPath) {
                    path.add(landing);
                }
                double distToLanding = landing.distanceTo(startPos);
                double distToTgt = landing.distanceTo(targetPos);
                if (!(distToLanding < totalTargetDist * 0.7) || !(distToTgt > 3.0)) break;
                hitEarly = true;
                break;
            }
            currentPos = nextPos;
            if (recordPath) {
                path.add(currentPos);
            }
            vel = vel.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        if (landing == null) {
            landing = currentPos;
        }
        double distToTarget = landing.distanceTo(targetPos);
        return new SimulationResult(landing, distToTarget, hitEarly, path != null ? path.size() : 0, path);
    }

    private static Vec3 getLookVector(float yaw, float pitch) {
        float f = -Mth.sin((double)(yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)));
        float g = -Mth.sin((double)(pitch * ((float)Math.PI / 180)));
        float h = Mth.cos((double)(yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)));
        return new Vec3((double)f, (double)g, (double)h).normalize();
    }

    private static Vec3 getPearlInitialVelocity(float yaw, float pitch, Vec3 playerVelocity, boolean onGround) {
        Vec3 dir = PearlChaseModule.getLookVector(yaw, pitch);
        Vec3 addVel = new Vec3(playerVelocity.x, onGround ? 0.0 : playerVelocity.y, playerVelocity.z);
        return dir.scale(1.5).add(addVel);
    }

    private boolean validTarget(Player player) {
        if (player == null || player == PearlChaseModule.mc.player) {
            return false;
        }
        if (player.isDeadOrDying()) {
            return false;
        }
        if (PearlChaseModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue())) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        return !this.ignoreNaked.getValue() || !EntityUtils.isNaked(player);
    }

    private Player getBestTarget() {
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return null;
        }
        Player best = null;
        double bestMetric = 0.0;
        for (Player p : PearlChaseModule.mc.level.players()) {
            if (!this.validTarget(p)) continue;
            double metric = switch (this.target.getValue()) {
                case "Lowest" -> p.getHealth() + p.getAbsorptionAmount();
                case "Furthest" -> -PearlChaseModule.mc.player.distanceToSqr((Entity)p);
                default -> PearlChaseModule.mc.player.distanceToSqr((Entity)p);
            };
            if (best != null && !(metric < bestMetric)) continue;
            best = p;
            bestMetric = metric;
        }
        return best;
    }

    private record PendingThrow(Vec3 targetPos, float yaw, float pitch, int delayTicks, List<Vec3> path) {
    }

    private record TrajectorySolution(float yaw, float pitch, List<Vec3> path) {
    }

    private record SimulationResult(Vec3 landingPos, double distToTarget, boolean hitEarlyObstacle, int ticks, List<Vec3> path) {
    }
}

