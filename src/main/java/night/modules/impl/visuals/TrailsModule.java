/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.Fireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.ServerConnectEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="Trails", description="Renders smooth trails behind projectiles and throwables.", category=Module.Category.VISUALS)
public class TrailsModule
extends Module {
    public CategorySetting targets = new CategorySetting("Targets", "The projectiles to render trails for.");
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Renders trails behind ender pearls.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting arrows = new BooleanSetting("Arrows", "Renders trails behind arrows.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting witherSkull = new BooleanSetting("WitherSkull", "Renders trails behind wither skulls.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting egg = new BooleanSetting("Egg", "Renders trails behind eggs.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting snowball = new BooleanSetting("Snowball", "Renders trails behind snowballs.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting fireCharge = new BooleanSetting("FireCharge", "Renders trails behind fire charges.", new CategorySetting.Visibility(this.targets), true);
    public NumberSetting fade = new NumberSetting("Fade", "The duration in milliseconds before a trail segment fades away.", 1000, 100, 5000);
    public ColorSetting color = new ColorSetting("Color", "The color used for the trails.", ColorUtils.getDefaultOutlineColor());
    private final Map<UUID, List<TrailPoint>> trails = new ConcurrentHashMap<UUID, List<TrailPoint>>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (TrailsModule.mc.level == null || TrailsModule.mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Entity entity : TrailsModule.mc.level.entitiesForRendering()) {
            List points;
            if (!this.isValidTarget(entity) || !entity.isAlive()) continue;
            UUID uuid = entity.getUUID();
            Vec3 pos = entity.position().add(0.0, (double)entity.getBbHeight() * 0.5, 0.0);
            List list = points = this.trails.computeIfAbsent(uuid, k -> new ArrayList());
            synchronized (list) {
                if (points.isEmpty()) {
                    Projectile proj;
                    Entity entity2;
                    if (entity instanceof Projectile && (entity2 = (proj = (Projectile)entity).getOwner()) instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)entity2;
                        points.add(new TrailPoint(living.getEyePosition().subtract(0.0, 0.1, 0.0), now));
                    } else if ((double)entity.distanceTo((Entity)TrailsModule.mc.player) <= 3.5 && TrailsModule.mc.player.isAlive()) {
                        points.add(new TrailPoint(TrailsModule.mc.player.getEyePosition().subtract(0.0, 0.1, 0.0), now));
                    }
                }
                if (points.isEmpty() || ((TrailPoint)points.get(points.size() - 1)).pos().distanceToSqr(pos) > 1.0E-4) {
                    points.add(new TrailPoint(pos, now));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (TrailsModule.mc.level == null || TrailsModule.mc.player == null || this.trails.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long maxAge = this.fade.getValue().longValue();
        PoseStack matrices = event.getMatrices();
        Color baseColor = this.color.getColor();
        HashMap<UUID, Entity> liveEntities = new HashMap<UUID, Entity>();
        for (Entity entity : TrailsModule.mc.level.entitiesForRendering()) {
            if (!entity.isAlive()) continue;
            liveEntities.put(entity.getUUID(), entity);
        }
        Iterator<Map.Entry<UUID, List<TrailPoint>>> iterator = this.trails.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<TrailPoint>> entry = iterator.next();
            UUID uuid = entry.getKey();
            List<TrailPoint> points = entry.getValue();
            Entity liveEntity = (Entity)liveEntities.get(uuid);
            List<TrailPoint> list = points;
            synchronized (list) {
                points.removeIf(p -> now - p.time() > maxAge);
                if (points.isEmpty() && liveEntity == null) {
                    iterator.remove();
                    continue;
                }
                if (points.size() < 1 && liveEntity == null) {
                    continue;
                }
                for (int i = 0; i < points.size() - 1; ++i) {
                    TrailPoint p1 = points.get(i);
                    TrailPoint p2 = points.get(i + 1);
                    float alphaRatio1 = maxAge > 0L ? Mth.clamp((float)(1.0f - (float)(now - p1.time()) / (float)maxAge), (float)0.0f, (float)1.0f) : 1.0f;
                    float alphaRatio2 = maxAge > 0L ? Mth.clamp((float)(1.0f - (float)(now - p2.time()) / (float)maxAge), (float)0.0f, (float)1.0f) : 1.0f;
                    Color c1 = ColorUtils.getColor(baseColor, Math.round((float)baseColor.getAlpha() * alphaRatio1));
                    Color c2 = ColorUtils.getColor(baseColor, Math.round((float)baseColor.getAlpha() * alphaRatio2));
                    Renderer3D.renderLine(matrices, p1.pos(), p2.pos(), c1, c2);
                }
                if (liveEntity != null && !points.isEmpty()) {
                    TrailPoint lastPoint = points.get(points.size() - 1);
                    Vec3 lerpedHead = EntityUtils.getRenderPos(liveEntity, event.getTickDelta()).add(0.0, (double)liveEntity.getBbHeight() * 0.5, 0.0);
                    float alphaLast = maxAge > 0L ? Mth.clamp((float)(1.0f - (float)(now - lastPoint.time()) / (float)maxAge), (float)0.0f, (float)1.0f) : 1.0f;
                    Color cLast = ColorUtils.getColor(baseColor, Math.round((float)baseColor.getAlpha() * alphaLast));
                    Color cHead = ColorUtils.getColor(baseColor, baseColor.getAlpha());
                    Renderer3D.renderLine(matrices, lastPoint.pos(), lerpedHead, cLast, cHead);
                }
            }
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (this.pearls.getValue() && (entity instanceof ThrownEnderpearl || entity.getType() == EntityTypes.ENDER_PEARL)) {
            return true;
        }
        if (this.arrows.getValue() && (entity instanceof AbstractArrow || entity.getType() == EntityTypes.ARROW || entity.getType() == EntityTypes.SPECTRAL_ARROW)) {
            return true;
        }
        if (this.witherSkull.getValue() && (entity instanceof WitherSkull || entity.getType() == EntityTypes.WITHER_SKULL)) {
            return true;
        }
        if (this.egg.getValue() && (entity instanceof ThrownEgg || entity.getType() == EntityTypes.EGG)) {
            return true;
        }
        if (this.snowball.getValue() && (entity instanceof Snowball || entity.getType() == EntityTypes.SNOWBALL)) {
            return true;
        }
        return this.fireCharge.getValue() && (entity instanceof SmallFireball || entity instanceof LargeFireball || entity instanceof Fireball || entity instanceof DragonFireball || entity.getType() == EntityTypes.SMALL_FIREBALL || entity.getType() == EntityTypes.FIREBALL || entity.getType() == EntityTypes.DRAGON_FIREBALL);
    }

    @Override
    public void onDisable() {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent event) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent event) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent event) {
        this.trails.clear();
    }

    public record TrailPoint(Vec3 pos, long time) {
    }
}

