/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package night.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.NumberSetting;
import night.utils.IMinecraft;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;
import org.joml.Quaternionfc;

@RegisterModule(name="Icons", description="Renders icons for specific events.", category=Module.Category.VISUALS)
public class IconsModule
extends Module {
    public CategorySetting chorusCategory = new CategorySetting("Chorus", "The category for chorus icons.");
    public BooleanSetting chorus = new BooleanSetting("Chorus", "Enabled", "Renders an icon for chorus positions.", new CategorySetting.Visibility(this.chorusCategory), true);
    public ColorSetting chorusColor = new ColorSetting("ChorusColor", "Color", "The color for the chorus icons.", new CategorySetting.Visibility(this.chorusCategory), new ColorSetting.Color(new Color(192, 147, 212), false, false));
    public NumberSetting duration = new NumberSetting("Duration", "The duration of icon renders.", new CategorySetting.Visibility(this.chorusCategory), (Number)1500, (Number)0, (Number)5000);
    public CategorySetting pearlsCategory = new CategorySetting("Pearls", "The category for pearl icons.");
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Enabled", "Renders an icon for pearl positions.", new CategorySetting.Visibility(this.pearlsCategory), true);
    public ColorSetting pearlsColor = new ColorSetting("PearlsColor", "Color", "The color for the pearl icons.", new CategorySetting.Visibility(this.pearlsCategory), new ColorSetting.Color(new Color(30, 131, 89), false, false));
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 40, 10, 50);
    private final ArrayList<Icon> iconList = new ArrayList();
    private final Map<Icon, Integer> pearlMap = new HashMap<Icon, Integer>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (this.getNull()) {
            return;
        }
        ArrayList<Icon> arrayList = this.iconList;
        synchronized (arrayList) {
            Entity entity = event.getEntity();
            if (entity instanceof ThrownEnderpearl) {
                ThrownEnderpearl pearl = (ThrownEnderpearl)entity;
                IconsModule.mc.level.players().stream().min(Comparator.comparingDouble(p -> p.distanceTo((Entity)pearl))).ifPresent(player -> {
                    Vec3 landing = this.projectPearl(pearl.position(), player.getYRot(), player.getXRot(), player.getDeltaMovement());
                    if (landing != null) {
                        Icon icon = new Icon(this, new Vec3(landing.x, landing.y, landing.z), Icon.Type.PEARL);
                        this.pearlMap.put(icon, pearl.getId());
                        this.iconList.add(icon);
                    }
                });
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (this.getNull()) {
            return;
        }
        ArrayList<Icon> arrayList = this.iconList;
        synchronized (arrayList) {
            ClientboundSoundPacket packet;
            SoundEvent sound;
            Packet<?> packet2 = event.getPacket();
            if (packet2 instanceof ClientboundSoundPacket && ((sound = (SoundEvent)(packet = (ClientboundSoundPacket)packet2).getSound().value()) == SoundEvents.CHORUS_FRUIT_TELEPORT || sound == SoundEvents.ENDERMAN_TELEPORT)) {
                this.iconList.add(new Icon(this, new Vec3(packet.getX(), packet.getY(), packet.getZ()), Icon.Type.CHORUS));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.getNull()) {
            return;
        }
        PoseStack matrices = event.getMatrices();
        ArrayList<Icon> arrayList = this.iconList;
        synchronized (arrayList) {
            this.iconList.removeIf(icon -> icon.type == Icon.Type.CHORUS && System.currentTimeMillis() - icon.time >= (long)(this.duration.getValue().intValue() + 1200) || icon.type == Icon.Type.PEARL && !(IconsModule.mc.level.getEntity(this.pearlMap.get(icon).intValue()) instanceof ThrownEnderpearl) && icon.animation.value() == 0.0f);
            if (this.chorus.getValue()) {
                for (Icon icon2 : this.iconList.stream().filter(icon -> icon.type == Icon.Type.CHORUS).toList()) {
                    icon2.render(matrices, System.currentTimeMillis() - icon2.time >= (long)(600 + this.duration.getValue().intValue()) ? 0.0f : 1.0f, () -> Renderer2D.renderTexture(matrices, -6.0f, -6.5f, 6.0f, 6.5f, Identifier.fromNamespaceAndPath((String)"night", (String)"textures/chorus.png"), Color.WHITE), this.chorusColor.getColor());
                }
            }
            if (this.pearls.getValue()) {
                for (Icon icon2 : this.iconList.stream().filter(icon -> icon.type == Icon.Type.PEARL).toList()) {
                    icon2.render(matrices, !(IconsModule.mc.level.getEntity(this.pearlMap.get(icon2).intValue()) instanceof ThrownEnderpearl) ? 0.0f : 1.0f, () -> Renderer2D.renderTexture(matrices, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath((String)"night", (String)"textures/pearl.png"), Color.WHITE), this.pearlsColor.getColor());
                }
            }
        }
    }

    private Vec3 projectPearl(Vec3 vec3d, float yaw, float pitch, Vec3 velocity) {
        double x = vec3d.x;
        double y = vec3d.y;
        double z = vec3d.z;
        y = y + (double)IconsModule.mc.player.getEyeHeight(IconsModule.mc.player.getPose()) - 0.1000000014901161;
        float maxDistance = 0.4f;
        double motionX = -Mth.sin((double)(yaw / 180.0f * (float)Math.PI)) * Mth.cos((double)(pitch / 180.0f * (float)Math.PI)) * maxDistance;
        double motionY = -Mth.sin((double)(pitch / 180.0f * 3.141593f)) * maxDistance;
        double motionZ = Mth.cos((double)(yaw / 180.0f * (float)Math.PI)) * Mth.cos((double)(pitch / 180.0f * (float)Math.PI)) * maxDistance;
        float distance = Mth.sqrt((float)((float)(motionX * motionX + motionY * motionY + motionZ * motionZ)));
        motionX /= (double)distance;
        motionY /= (double)distance;
        motionZ /= (double)distance;
        float pow = 1.5f;
        motionX *= (double)pow;
        motionY *= (double)pow;
        motionZ *= (double)pow;
        motionX += velocity.x;
        motionY += velocity.y;
        motionZ += velocity.z;
        while (y > -65.0) {
            Vec3 lastPosition = new Vec3(x, y, z);
            if (IconsModule.mc.level.getBlockState(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY -= (double)0.03f;
            Vec3 position = new Vec3(x, y, z);
            for (Entity entity : IconsModule.mc.level.entitiesForRendering()) {
                if (entity == IconsModule.mc.player || entity instanceof Arrow || entity instanceof ThrownEnderpearl || !entity.getBoundingBox().intersects(new AABB(x - 1.0, y - 1.0, z - 1.0, x + 1.0, y + 1.0, z + 1.0))) continue;
                return entity.getBoundingBox().getCenter();
            }
            BlockHitResult possibleResult = IconsModule.mc.level.clip(new ClipContext(lastPosition, position, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)IconsModule.mc.player));
            if (possibleResult == null || possibleResult.getType() == HitResult.Type.MISS) continue;
            return possibleResult.getLocation();
        }
        return null;
    }

    private class Icon {
        private final Vec3 pos;
        private final Animation animation;
        private final Type type;
        private final long time;
        final /* synthetic */ IconsModule this$0;

        public Icon(IconsModule iconsModule, Vec3 pos, Type type) {
            IconsModule iconsModule2 = iconsModule;
            Objects.requireNonNull(iconsModule2);
            this.this$0 = iconsModule2;
            this.pos = pos;
            this.animation = new Animation(0.0f, 1.0f, 600, Easing.Method.EASE_IN_OUT_ELASTIC);
            this.type = type;
            this.time = System.currentTimeMillis();
        }

        private void render(PoseStack matrices, float target, Runnable runnable, Color color) {
            float progress = this.animation.get(target);
            float distance = (float)Math.sqrt(IMinecraft.mc.gameRenderer.mainCamera().position().distanceToSqr(this.pos.x, this.pos.y, this.pos.z));
            float scaling = 0.0018f + this.this$0.scale.getValue().floatValue() / 10000.0f * distance;
            if ((double)distance <= 4.0) {
                scaling = 0.0245f;
            }
            Vec3 vec3d = new Vec3(this.pos.x - IMinecraft.mc.gameRenderer.mainCamera().position().x, this.pos.y - IMinecraft.mc.gameRenderer.mainCamera().position().y, this.pos.z - IMinecraft.mc.gameRenderer.mainCamera().position().z);
            matrices.pushPose();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.mulPose((Quaternionfc)IMinecraft.mc.gameRenderer.mainCamera().rotation());
            matrices.scale(scaling, -scaling, scaling);
            matrices.scale(progress, progress, 1.0f);
            Renderer2D.renderCircle(matrices, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(matrices, 0.0f, 0.0f, 10.0f, color);
            runnable.run();
            matrices.popPose();
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public Animation getAnimation() {
            return this.animation;
        }

        @Generated
        public Type getType() {
            return this.type;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        public static enum Type {
            CHORUS,
            PEARL;

        }
    }
}

