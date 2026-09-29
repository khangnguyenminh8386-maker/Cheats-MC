/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package night.modules.impl.visuals;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import night.events.SubscribeEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.LimbAnimatorAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;

@RegisterModule(name="PopChams", description="Renders chams on a frozen snapshot of the entity's pose the moment they pop a totem.", category=Module.Category.VISUALS)
public class PopChamsModule
extends Module {
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the pop chams fade.", 1500, 0, 5000);
    public NumberSetting yOffset = new NumberSetting("YOffset", "The vertical float/rise offset for pop chams.", 0, -5, 5);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the pop chams.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private static final EquipmentSlot[] COPIED_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Map<RemotePlayer, Long> ghosts = new ConcurrentHashMap<RemotePlayer, Long>();
    private int nextId = -90000;

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent event) {
        if (!this.isToggled() || PopChamsModule.mc.level == null || event.getPlayer() == PopChamsModule.mc.player) {
            return;
        }
        Player player = event.getPlayer();
        mc.execute(() -> {
            if (PopChamsModule.mc.level == null) {
                return;
            }
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            RemotePlayer ghost = new RemotePlayer(PopChamsModule.mc.level, profile);
            ghost.setId(this.nextId--);
            ghost.setPos(player.getX(), player.getY(), player.getZ());
            ghost.xo = player.getX();
            ghost.yo = player.getY();
            ghost.zo = player.getZ();
            ghost.xOld = player.getX();
            ghost.yOld = player.getY();
            ghost.zOld = player.getZ();
            ghost.setYRot(player.getYRot());
            ghost.setXRot(player.getXRot());
            ghost.yRotO = player.getYRot();
            ghost.xRotO = player.getXRot();
            ghost.setYHeadRot(player.getYHeadRot());
            ghost.yHeadRotO = player.getYHeadRot();
            ghost.yBodyRot = player.yBodyRot;
            ghost.yBodyRotO = player.yBodyRot;
            ghost.setPose(player.getPose());
            ghost.setShiftKeyDown(player.isShiftKeyDown());
            ghost.setSwimming(player.isSwimming());
            ghost.setSprinting(player.isSprinting());
            ghost.refreshDimensions();
            ghost.walkAnimation.setSpeed(player.walkAnimation.speed());
            ((LimbAnimatorAccessor)ghost.walkAnimation).setPos(player.walkAnimation.position());
            ghost.swinging = player.swinging;
            ghost.swingTime = player.swingTime;
            ghost.swingingArm = player.swingingArm;
            ghost.attackAnim = player.attackAnim;
            ghost.oAttackAnim = player.oAttackAnim;
            ghost.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            ghost.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            for (EquipmentSlot slot : COPIED_SLOTS) {
                ghost.setItemSlot(slot, player.getItemBySlot(slot).copy());
            }
            PopChamsModule.mc.level.addEntity((Entity)ghost);
            this.ghosts.put(ghost, System.currentTimeMillis());
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        long now = System.currentTimeMillis();
        this.ghosts.entrySet().removeIf(entry -> {
            if (now - (Long)entry.getValue() <= (long)this.duration.getValue().intValue()) {
                return false;
            }
            this.despawn((RemotePlayer)entry.getKey());
            return true;
        });
    }

    @Override
    public void onDisable() {
        this.ghosts.keySet().forEach(this::despawn);
        this.ghosts.clear();
    }

    private void despawn(RemotePlayer ghost) {
        if (PopChamsModule.mc.level != null) {
            PopChamsModule.mc.level.removeEntity(ghost.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public boolean isGhost(Entity entity) {
        return this.ghosts.containsKey(entity);
    }

    public boolean shouldFill() {
        return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
    }

    public boolean shouldOutline() {
        return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
    }

    public Color getFillColor(RemotePlayer ghost) {
        return this.withFade(this.fillColor.getColor(), ghost);
    }

    public Color getOutlineColor(RemotePlayer ghost) {
        return this.withFade(this.outlineColor.getColor(), ghost);
    }

    public float getYOffset(RemotePlayer ghost) {
        if (this.yOffset.getValue().intValue() == 0) {
            return 0.0f;
        }
        Long startTime = this.ghosts.get(ghost);
        if (startTime == null) {
            return 0.0f;
        }
        int dur = this.duration.getValue().intValue();
        if (dur <= 0) {
            return 0.0f;
        }
        float progress = Easing.toDelta(startTime, dur);
        return this.yOffset.getValue().floatValue() * progress;
    }

    private Color withFade(Color color, RemotePlayer ghost) {
        Long startTime = this.ghosts.get(ghost);
        if (startTime == null) {
            return color;
        }
        float ease = 1.0f - Easing.toDelta(startTime, this.duration.getValue().intValue());
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * ease));
    }
}

