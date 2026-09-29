/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import java.awt.Color;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.AttackEntityEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.ParticleManager;

@RegisterModule(name="Particles", description="Custom particle effects for totem pops and entity hits.", category=Module.Category.VISUALS)
public class ParticlesModule
extends Module {
    private final ParticleManager particleManager = new ParticleManager();
    public CategorySetting totemCategory = new CategorySetting("Totem", "Settings for totem pop particle effects.");
    public BooleanSetting totem = new BooleanSetting("Totem", "Enabled", "Enables totem particle customization.", new CategorySetting.Visibility(this.totemCategory), true);
    public ModeSetting totemMode = new ModeSetting("TotemMode", "Mode", "Particle mode for totem pops.", new CategorySetting.Visibility(this.totemCategory), "Vanilla", new String[]{"Vanilla", "Custom"});
    public NumberSetting totemNumber = new NumberSetting("TotemNumber", "Number", "Amount of particles per totem pop.", new CategorySetting.Visibility(this.totemCategory), 15, 1, 100);
    public ColorSetting vanillaColorA = new ColorSetting("VanillaColorA", "Color A", "Primary particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, "Vanilla"), new ColorSetting.Color(new Color(255, 255, 255), false, false));
    public ColorSetting vanillaColorB = new ColorSetting("VanillaColorB", "Color B", "Fade particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, "Vanilla"), new ColorSetting.Color(new Color(255, 255, 255), false, false));
    public ColorSetting customColorA = new ColorSetting("CustomColorA", "Color A", "Primary particle color.", new ModeSetting.Visibility(this.totemMode, "Custom"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting customColorB = new ColorSetting("CustomColorB", "Color B", "Secondary particle color.", new ModeSetting.Visibility(this.totemMode, "Custom"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public BooleanSetting totemGlow = new BooleanSetting("TotemGlow", "Glow", "Adds a glow effect to totem particles.", new ModeSetting.Visibility(this.totemMode, "Custom"), true);
    public BooleanSetting totemPhysics = new BooleanSetting("TotemPhysics", "Physics", "Enables gravity and bouncing for totem particles.", new ModeSetting.Visibility(this.totemMode, "Custom"), true);
    public NumberSetting totemVelocityX = new NumberSetting("TotemVelocityX", "X", "X velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public NumberSetting totemVelocityY = new NumberSetting("TotemVelocityY", "Y", "Y velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public NumberSetting totemVelocityZ = new NumberSetting("TotemVelocityZ", "Z", "Z velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public CategorySetting hitCategory = new CategorySetting("Hit", "Settings for hit particle effects.");
    public BooleanSetting hit = new BooleanSetting("Hit", "Enabled", "Enables hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
    public NumberSetting hitNumber = new NumberSetting("HitNumber", "Number", "Amount of particles per hit.", new CategorySetting.Visibility(this.hitCategory), 15, 1, 100);
    public ColorSetting hitColorA = new ColorSetting("HitColorA", "Color A", "Primary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting hitColorB = new ColorSetting("HitColorB", "Color B", "Secondary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public BooleanSetting hitGlow = new BooleanSetting("HitGlow", "Glow", "Adds a glow effect to hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
    public BooleanSetting hitPhysics = new BooleanSetting("HitPhysics", "Physics", "Enables gravity and bouncing for hit particles.", new CategorySetting.Visibility(this.hitCategory), false);

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent event) {
        if (event.getPlayer() == null || !this.totem.getValue()) {
            return;
        }
        if (!this.totemMode.getValue().equalsIgnoreCase("Custom")) {
            return;
        }
        Vec3 pos = event.getPlayer().getBoundingBox().getCenter();
        Vec3 velocity = new Vec3(this.totemVelocityX.getValue().doubleValue(), this.totemVelocityY.getValue().doubleValue(), this.totemVelocityZ.getValue().doubleValue());
        this.spawnTotemParticles(pos, velocity);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.getTarget() == null || !this.hit.getValue()) {
            return;
        }
        Vec3 pos = event.getTarget().getBoundingBox().getCenter();
        this.spawnHitParticles(pos);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (ParticlesModule.mc.level == null || ParticlesModule.mc.player == null) {
            this.particleManager.clear();
            return;
        }
        this.particleManager.tick();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.particleManager.isEmpty()) {
            return;
        }
        this.particleManager.render(event.getMatrices(), event.getTickDelta());
    }

    private void spawnTotemParticles(Vec3 pos, Vec3 velocityMultiplier) {
        int color = this.customColorA.getColor().getRGB();
        int shadowColor = this.customColorB.getColor().getRGB();
        boolean glow = this.totemGlow.getValue();
        boolean physics = this.totemPhysics.getValue();
        int count = this.totemNumber.getValue().intValue();
        for (int i = 0; i < count; ++i) {
            Vec3 motion = ParticleManager.randomMotion(0.5 * (0.75 + Math.random() * 0.25));
            motion = new Vec3(motion.x * velocityMultiplier.x, motion.y * velocityMultiplier.y, motion.z * velocityMultiplier.z);
            if (physics) {
                this.particleManager.addBouncy(pos, motion, 1.5, color, shadowColor, glow);
                continue;
            }
            this.particleManager.addFriction(pos, motion, 0.9, 1.5, color, shadowColor, glow);
        }
    }

    private void spawnHitParticles(Vec3 pos) {
        int color = this.hitColorA.getColor().getRGB();
        int shadowColor = this.hitColorB.getColor().getRGB();
        boolean glow = this.hitGlow.getValue();
        boolean physics = this.hitPhysics.getValue();
        int count = this.hitNumber.getValue().intValue();
        for (int i = 0; i < count; ++i) {
            Vec3 motion = ParticleManager.randomMotion(0.5 * (0.75 + Math.random() * 0.25));
            if (physics) {
                this.particleManager.addBouncy(pos, motion, 1.0, color, shadowColor, glow);
                continue;
            }
            this.particleManager.addFriction(pos, motion, 0.9, 1.0, color, shadowColor, glow);
        }
    }

    @Generated
    public ParticleManager getParticleManager() {
        return this.particleManager;
    }
}

