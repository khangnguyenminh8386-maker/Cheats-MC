/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="SpearKill", description="Lunges at players and attacks with a spear/trident.", category=Module.Category.COMBAT)
public class SpearKillModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "Range to search for players.", Float.valueOf(12.0f), Float.valueOf(1.0f), Float.valueOf(100.0f));
    public NumberSetting wallRange = new NumberSetting("WallRange", "Range to search through walls.", Float.valueOf(4.0f), Float.valueOf(0.0f), Float.valueOf(100.0f));
    public NumberSetting hitRange = new NumberSetting("HitRange", "Range to send spear attacks.", Float.valueOf(4.2f), Float.valueOf(1.0f), Float.valueOf(100.0f));
    public NumberSetting delay = new NumberSetting("Delay", "Minimum delay in milliseconds between attacks.", 100, 0, 1000);
    public BooleanSetting attackDelay = new BooleanSetting("AttackDelay", "Uses vanilla attack cooldown before attacking.", true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "Attacks per second when AttackDelay is off.", new BooleanSetting.Visibility(this.attackDelay, false), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(20.0f));
    public ModeSetting autoSwap = new ModeSetting("AutoSwap", "Swaps to a spear before attacking.", "Normal", new String[]{"Off", "Normal", "Silent"});
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates to the target before lunging and attacking.", true);
    public BooleanSetting lunge = new BooleanSetting("Lunge", "Pushes you toward the target.", true);
    public NumberSetting lungeSpeed = new NumberSetting("LungeSpeed", "Horizontal lunge speed.", new BooleanSetting.Visibility(this.lunge, true), (Number)Float.valueOf(1.25f), (Number)Float.valueOf(0.1f), (Number)Float.valueOf(5.0f));
    public NumberSetting lungeY = new NumberSetting("LungeY", "Upward velocity added while lunging.", new BooleanSetting.Visibility(this.lunge, true), (Number)Float.valueOf(0.08f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(1.0f));
    public NumberSetting stopDistance = new NumberSetting("StopDistance", "Stops lunging inside this distance.", new BooleanSetting.Visibility(this.lunge, true), (Number)Float.valueOf(2.4f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(6.0f));
    public NumberSetting lungeDelay = new NumberSetting("LungeDelay", "Delay in milliseconds between lunges.", new BooleanSetting.Visibility(this.lunge, true), (Number)150, (Number)0, (Number)1000);
    public BooleanSetting swing = new BooleanSetting("Swing", "Swings the hand after attacking.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Renders the current SpearKill target.", true);
    public BooleanSetting renderLine = new BooleanSetting("RenderLine", "Renders a line to the current target.", new BooleanSetting.Visibility(this.render, true), true);
    public ColorSetting fillColor = new ColorSetting("FillColor", "Fill color for the target render.", new BooleanSetting.Visibility(this.render, true), new ColorSetting.Color(new Color(255, 210, 90, 45), false, false));
    public ColorSetting lineColor = new ColorSetting("LineColor", "Outline and line color for the target render.", new BooleanSetting.Visibility(this.render, true), new ColorSetting.Color(new Color(255, 210, 90, 180), false, false));
    private long lastAttackMs = 0L;
    private long lastLungeMs = 0L;
    private Player target;

    @Override
    public String getMetaData() {
        return this.target != null ? this.target.getName().getString() : "Spear";
    }

    @Override
    public void onDisable() {
        this.target = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (SpearKillModule.mc.player == null || SpearKillModule.mc.level == null || SpearKillModule.mc.player.isSpectator()) {
            return;
        }
        this.target = this.findTarget();
        if (this.target == null) {
            return;
        }
        int spearSlot = this.getSpearSlot();
        if (spearSlot == -1 && !this.isHoldingSpear()) {
            return;
        }
        boolean silentSwapped = false;
        int prevSlot = SpearKillModule.mc.player.getInventory().getSelectedSlot();
        if (!this.isHoldingSpear()) {
            if (this.autoSwap.getValue().equalsIgnoreCase("Off")) {
                return;
            }
            if (this.autoSwap.getValue().equalsIgnoreCase("Normal")) {
                InventoryUtils.switchSlot("Normal", spearSlot, prevSlot);
            } else if (this.autoSwap.getValue().equalsIgnoreCase("Silent")) {
                InventoryUtils.switchSlot("Silent", spearSlot, prevSlot);
                silentSwapped = true;
            }
        }
        if (this.rotate.getValue()) {
            float[] rot = RotationUtils.getRotations(this.target.getX(), this.target.getY() + (double)this.target.getEyeHeight() / 2.0, this.target.getZ());
            Night.ROTATION_MANAGER.legacyRotate(rot[0], rot[1], this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        double dist = SpearKillModule.mc.player.distanceTo((Entity)this.target);
        if (this.lunge.getValue() && dist > this.stopDistance.getValue().doubleValue() && System.currentTimeMillis() - this.lastLungeMs >= this.lungeDelay.getValue().longValue()) {
            this.doLunge(this.target);
            this.lastLungeMs = System.currentTimeMillis();
        }
        if (dist <= this.hitRange.getValue().doubleValue() && this.canAttack()) {
            this.doAttack(this.target);
            this.lastAttackMs = System.currentTimeMillis();
        }
        if (silentSwapped) {
            InventoryUtils.switchBack("Silent", spearSlot, prevSlot);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.render.getValue() || this.target == null || !this.target.isAlive() || this.target.isRemoved()) {
            return;
        }
        PoseStack matrices = event.getMatrices();
        Vec3 renderPos = EntityUtils.getRenderPos((Entity)this.target, event.getTickDelta());
        AABB box = this.target.getBoundingBox().move(renderPos.x - this.target.getX(), renderPos.y - this.target.getY(), renderPos.z - this.target.getZ());
        Renderer3D.renderBox(matrices, box, this.fillColor.getColor());
        Renderer3D.renderBoxOutline(matrices, box, this.lineColor.getColor());
        if (this.renderLine.getValue()) {
            Vec3 eyePos = EntityUtils.getRenderPos((Entity)SpearKillModule.mc.player, event.getTickDelta()).add(0.0, (double)SpearKillModule.mc.player.getEyeHeight(SpearKillModule.mc.player.getPose()), 0.0);
            Renderer3D.renderLine(matrices, eyePos, box.getCenter(), this.lineColor.getColor());
        }
    }

    private Player findTarget() {
        return SpearKillModule.mc.level.players().stream().filter(this::isValidTarget).min(Comparator.comparingDouble(p -> SpearKillModule.mc.player.distanceToSqr((Entity)p))).orElse(null);
    }

    private boolean isValidTarget(Player player) {
        if (player == SpearKillModule.mc.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (EntityUtils.isGhost((Entity)player)) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        double dist = SpearKillModule.mc.player.distanceTo((Entity)player);
        if (dist > this.range.getValue().doubleValue()) {
            return false;
        }
        return SpearKillModule.mc.player.hasLineOfSight((Entity)player) || dist <= this.wallRange.getValue().doubleValue();
    }

    private boolean canAttack() {
        if (System.currentTimeMillis() - this.lastAttackMs < this.delay.getValue().longValue()) {
            return false;
        }
        if (this.attackDelay.getValue()) {
            return SpearKillModule.mc.player.getAttackStrengthScale(0.5f) >= 1.0f;
        }
        return System.currentTimeMillis() - this.lastAttackMs >= (long)(1000.0f / Math.max(this.attackSpeed.getValue().floatValue(), 0.1f));
    }

    private void doLunge(Player player) {
        Vec3 delta = player.position().subtract(SpearKillModule.mc.player.position());
        Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.length() < 1.0E-4) {
            return;
        }
        Vec3 dir = horizontal.normalize();
        double vy = Math.max(SpearKillModule.mc.player.getDeltaMovement().y, this.lungeY.getValue().doubleValue());
        SpearKillModule.mc.player.setDeltaMovement(dir.x * this.lungeSpeed.getValue().doubleValue(), vy, dir.z * this.lungeSpeed.getValue().doubleValue());
    }

    private void doAttack(Player player) {
        SpearKillModule.mc.gameMode.attack((Player)SpearKillModule.mc.player, (Entity)player);
        SpearKillModule.mc.player.resetAttackStrengthTicker();
        if (this.swing.getValue()) {
            SpearKillModule.mc.player.swing(InteractionHand.MAIN_HAND);
        } else {
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
    }

    private boolean isHoldingSpear() {
        if (SpearKillModule.mc.player == null) {
            return false;
        }
        ItemStack held = SpearKillModule.mc.player.getMainHandItem();
        return held.is(Items.TRIDENT) || held.getItem() instanceof TridentItem;
    }

    private int getSpearSlot() {
        if (SpearKillModule.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = SpearKillModule.mc.player.getInventory().getItem(i);
            if (!stack.is(Items.TRIDENT) && !(stack.getItem() instanceof TridentItem)) continue;
            return i;
        }
        return -1;
    }
}

