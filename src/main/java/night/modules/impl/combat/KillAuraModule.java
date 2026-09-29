/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ShulkerBullet
 *  net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.MaceItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Entry
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.AutoCrystalModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EnchantmentUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.GrimUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="KillAura", description="Automatically attacks optimal targets using Nami's combat engine.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class KillAuraModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The reach distance to entities.", 3.0, 1.0, 6.0);
    public BooleanSetting stanceAbuse = new BooleanSetting("StanceAbuse", "Abuses Grim stance eye positions to optimize reach.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Attack cooldown delay multiplier.", 0.92, 0.0, 1.0);
    public ModeSetting swap = new ModeSetting("Swap", "Weapon switch mode.", "Require", new String[]{"None", "Require", "Normal", "Silent"});
    public BooleanSetting onlyGapHold = new BooleanSetting("OnlyGapHold", "Only attacks using silent swap when holding a golden apple in main hand.", new ModeSetting.Visibility(this.swap, "Silent"), false);
    public BooleanSetting onlyTotemHold = new BooleanSetting("OnlyTotemHold", "Only attacks using silent swap when holding a totem in main hand.", new ModeSetting.Visibility(this.swap, "Silent"), false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses attacking while eating or using items.", false);
    public ModeSetting tpsMode = new ModeSetting("TPS", "TPS synchronization mode for attack cooldown.", "Average", new String[]{"None", "Average"});
    public BooleanSetting multiTask = new BooleanSetting("Multitask", "Allows attacking while eating or using items.", true);
    public ModeSetting stopSprinting = new ModeSetting("Sprinting", "Sprint management during attacks.", "None", new String[]{"None", "Packet"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Rotation mode.", "Normal", new String[]{"None", "Normal", "Hold", "Silent"});
    public BooleanSetting swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", new String[]{"Box", "Circle", "Both"});
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
    public BooleanSetting players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public BooleanSetting friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Target hostile mobs (Zombies, Skeletons, Spiders, Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting neutrals = new BooleanSetting("Neutrals", "Target neutral mobs (Endermen, Zombie Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting projectiles = new BooleanSetting("Projectiles", "Target shulker bullets & fireballs.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public ColorSetting boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
    public Entity target = null;
    private Entity lastTarget = null;
    private long lastTargetTime = 0L;
    private static final long FADE_TIME_MS = 300L;
    private long lastAttackTime = 0L;
    private int originalSlot = -1;
    private int silentHeldWeaponSlot = -1;
    private int silentHeldReturnSlot = -1;

    public Entity getTarget() {
        return this.target;
    }

    private void releaseSilentHold() {
        if (this.silentHeldWeaponSlot == -1) {
            return;
        }
        InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        if (KillAuraModule.mc.player != null) {
            InventoryUtils.switchBack("Silent", this.silentHeldWeaponSlot, this.silentHeldReturnSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        if (this.silentHeldWeaponSlot != -1) {
            InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
        this.originalSlot = -1;
        this.target = null;
        this.lastTarget = null;
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.originalSlot = -1;
        this.lastAttackTime = 0L;
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    @Override
    public void onDisable() {
        if (this.originalSlot != -1 && KillAuraModule.mc.player != null) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, KillAuraModule.mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        this.releaseSilentHold();
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
   public void onPlayerUpdate(PlayerUpdateEvent event) {
      if (this.shouldRunOnProxy()) {
         this.releaseSilentHold();
      } else if (mc.player != null && mc.level != null && !mc.player.isDeadOrDying()) {
         AutoCrystalModule ac = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
         if (ac != null && ac.isToggled() && ac.getTarget() != null) {
            this.releaseSilentHold();
         } else {
            boolean eating = mc.options.keyUse.isDown() || mc.player.isUsingItem() || mc.player.getUseItemRemainingTicks() > 0;
            if (this.pauseOnEat.getValue() && eating) {
               this.target = null;
               this.releaseSilentHold();
            } else if (!this.multiTask.getValue() && eating) {
               this.target = null;
               this.releaseSilentHold();
            } else {
               ItemStack stack = mc.player.getMainHandItem();
               if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                  boolean gap = this.onlyGapHold.getValue();
                  boolean totem = this.onlyTotemHold.getValue();
                  if (gap || totem) {
                     boolean isGap = stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
                     boolean isTotem = stack.is(Items.TOTEM_OF_UNDYING);
                     boolean allowed = gap && isGap || totem && isTotem;
                     if (!allowed) {
                        this.target = null;
                        this.releaseSilentHold();
                        return;
                     }
                  }
               }

               Entity optimal = this.findOptimalTarget();
               if (optimal != null && (!this.swap.getValue().equalsIgnoreCase("Require") || isItemAWeapon(stack))) {
                  this.target = optimal;
                  boolean skipCooldown = false;
                  if (!(this.target instanceof ShulkerBullet) && !(this.target instanceof LargeFireball)) {
                     float attackDamage = this.calculatePlayerAttackDamage();
                     if (this.target instanceof LivingEntity living && living.getMaxHealth() <= attackDamage) {
                        skipCooldown = true;
                     }
                  } else {
                     skipCooldown = true;
                  }

                  int weaponSlot = this.getBestWeaponSlot();
                  double cooldownMs = this.getCooldownMs(weaponSlot);
                  long now = System.currentTimeMillis();
                  boolean cooldownReady = skipCooldown || now - this.lastAttackTime >= cooldownMs;

                  double preRotateMs = switch (this.rotate.getValue()) {
                     case "Hold" -> 1000.0;
                     case "Normal" -> 100.0;
                     default -> 0.0;
                  };
                  if (skipCooldown || cooldownReady || cooldownMs - (now - this.lastAttackTime) <= preRotateMs) {
                     Vec3 eyePos = mc.player.getEyePosition(1.0F);
                     if (this.stanceAbuse.getValue()) {
                        double foundDist = Double.MAX_VALUE;

                        for (Vec3 v : GrimUtils.getPossibleEyePositions(mc.player)) {
                           Vec3 closest = RotationUtils.getClampClosestPoint(v, this.target.getBoundingBox());
                           double dist = v.distanceTo(closest);
                           if (dist < foundDist) {
                              foundDist = dist;
                              eyePos = v;
                           }
                        }
                     }

                     Vec3 aimPoint = this.target.getBoundingBox().getCenter();
                     if (this.target instanceof LivingEntity living && this.target.getBoundingBox().getYsize() >= 0.8) {
                        aimPoint = new Vec3(this.target.getX(), this.target.getY() + living.getEyeHeight() * 0.75, this.target.getZ());
                     }

                     float pYRot = RotationUtils.getYRotToVec(eyePos, aimPoint);
                     float pXRot = RotationUtils.getXRotToVec(eyePos, aimPoint);
                     boolean insideBox = this.target.getBoundingBox().contains(eyePos);
                     if (this.getTargetDistance(eyePos, this.target) > this.range.getValue().doubleValue()) {
                        this.target = null;
                        this.releaseSilentHold();
                        return;
                     }

                     boolean canAttack = this.rotate.getValue().equalsIgnoreCase("None") || insideBox;
                     if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                           Night.ROTATION_MANAGER.silentRotate(pYRot, pXRot);
                           canAttack = true;
                        } else {
                           Night.ROTATION_MANAGER.legacyRotate(pYRot, pXRot, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                           EntityHitResult serverCheck = RotationUtils.raycastTarget(
                              eyePos,
                              this.target,
                              this.range.getValue().doubleValue(),
                              Night.ROTATION_MANAGER.getServerYaw(),
                              Night.ROTATION_MANAGER.getServerPitch()
                           );
                           canAttack = serverCheck != null || insideBox;
                        }
                     }

                     if (!canAttack) {
                        return;
                     }
                  }

                  if (cooldownReady) {
                     boolean stoppedSprint = false;
                     if (this.stopSprinting.getValue().equalsIgnoreCase("Packet") && mc.player.isSprinting() && !mc.player.isShiftKeyDown()) {
                        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, Action.STOP_SPRINTING));
                        stoppedSprint = true;
                     }

                     int prevSlot = mc.player.getInventory().getSelectedSlot();
                     if (this.swap.getValue().equalsIgnoreCase("Normal") && weaponSlot != -1) {
                        if (this.originalSlot == -1 && prevSlot != weaponSlot) {
                           this.originalSlot = prevSlot;
                        }

                        InventoryUtils.switchSlot("Normal", weaponSlot, prevSlot);
                     } else if (this.swap.getValue().equalsIgnoreCase("Silent") && weaponSlot != -1 && this.silentHeldWeaponSlot != weaponSlot) {
                        this.releaseSilentHold();
                        InventoryUtils.switchSlot("Silent", weaponSlot, prevSlot);
                        InventoryUtils.markLongHold(weaponSlot);
                        this.silentHeldWeaponSlot = weaponSlot;
                        this.silentHeldReturnSlot = prevSlot;
                     }

                     mc.getConnection().send(new ServerboundAttackPacket(this.target.getId()));
                     mc.player.resetAttackStrengthTicker();
                     if (this.swing.getValue()) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                     }

                     if (stoppedSprint) {
                        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, Action.START_SPRINTING));
                     }

                     this.lastAttackTime = System.currentTimeMillis();
                  }
               } else {
                  this.target = null;
                  this.releaseSilentHold();
               }
            }
         }
      } else {
         this.releaseSilentHold();
      }
   }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof ServerboundSetCarriedItemPacket)) {
            return;
        }
        if (KillAuraModule.mc.player == null || KillAuraModule.mc.level == null) {
            return;
        }
        mc.execute(() -> {
            this.lastAttackTime = System.currentTimeMillis();
        });
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.render.getValue() || KillAuraModule.mc.player == null || KillAuraModule.mc.player.isDeadOrDying()) {
            return;
        }
        Entity renderEntity = this.target;
        float alphaMult = 1.0f;
        if (renderEntity != null) {
            this.lastTarget = renderEntity;
            this.lastTargetTime = System.currentTimeMillis();
        } else if (this.lastTarget != null) {
            long elapsed = System.currentTimeMillis() - this.lastTargetTime;
            if (elapsed < 300L) {
                renderEntity = this.lastTarget;
                alphaMult = 1.0f - (float)elapsed / 300.0f;
            } else {
                this.lastTarget = null;
            }
        }
        if (renderEntity == null) {
            return;
        }
        Color renderColor = ColorUtils.getColor(this.boxColor.getColor(), (int)((float)this.boxColor.getColor().getAlpha() * alphaMult));
        if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Vec3 vec3d = EntityUtils.getRenderPos(renderEntity, event.getTickDelta());
            AABB box = renderEntity.getBoundingBox().move(vec3d.x - renderEntity.getX(), vec3d.y - renderEntity.getY(), vec3d.z - renderEntity.getZ());
            Renderer3D.renderBox(event.getMatrices(), box, renderColor);
            Renderer3D.renderBoxOutline(event.getMatrices(), box, renderColor);
        }
        if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderTargetCircle(event.getMatrices(), renderEntity, event.getTickDelta(), renderColor);
        }
    }

    public double getTargetDistance(Vec3 eyePos, Entity e) {
        if (KillAuraModule.mc.player == null || e == null) {
            return Double.MAX_VALUE;
        }
        if (this.stanceAbuse.getValue()) {
            double best = Double.MAX_VALUE;
            for (Vec3 v : GrimUtils.getPossibleEyePositions((Player)KillAuraModule.mc.player)) {
                double d = v.distanceTo(RotationUtils.getClampClosestPoint(v, e.getBoundingBox()));
                if (!(d < best)) continue;
                best = d;
            }
            return best;
        }
        return eyePos.distanceTo(RotationUtils.getClampClosestPoint(eyePos, e.getBoundingBox()));
    }

    private Entity findOptimalTarget() {
        if (KillAuraModule.mc.player == null || KillAuraModule.mc.level == null) {
            return null;
        }
        ArrayList<Entity> candidates = new ArrayList<Entity>();
        Vec3 eyePos = KillAuraModule.mc.player.getEyePosition(1.0f);
        double maxReach = this.range.getValue().doubleValue();
        for (Entity e2 : KillAuraModule.mc.level.entitiesForRendering()) {
            double dist;
            if (e2 == KillAuraModule.mc.player || !e2.isAlive() || EntityUtils.isGhost(e2) || (dist = this.getTargetDistance(eyePos, e2)) > maxReach) continue;
            if (e2 instanceof Player) {
                Player p = (Player)e2;
                if (!this.players.getValue() || !this.friends.getValue() && Night.FRIEND_MANAGER.contains(p.getName().getString()) || this.ignoreNaked.getValue() && EntityUtils.isNaked(p)) continue;
                candidates.add(e2);
                continue;
            }
            if (this.projectiles.getValue() && (e2 instanceof ShulkerBullet || e2 instanceof LargeFireball)) {
                candidates.add(e2);
                continue;
            }
            if (EntityUtils.isNeutral(e2)) {
                if (!this.neutrals.getValue() && !EntityUtils.isAngry(e2)) continue;
                candidates.add(e2);
                continue;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(e2)) {
                candidates.add(e2);
                continue;
            }
            if (!this.animals.getValue() || !EntityUtils.isAnimal(e2)) continue;
            candidates.add(e2);
        }
        List<Entity> playerCandidates = candidates.stream().filter(e -> e instanceof Player).sorted(Comparator.comparingDouble(e -> this.getTargetDistance(eyePos, (Entity)e))).toList();
        if (!playerCandidates.isEmpty()) {
            return playerCandidates.get(0);
        }
        List<Entity> creeperCandidates = candidates.stream().filter(e -> e instanceof Creeper && this.getTargetDistance(eyePos, (Entity)e) <= 3.0).sorted(Comparator.comparingDouble(e -> this.getTargetDistance(eyePos, (Entity)e))).toList();
        if (!creeperCandidates.isEmpty()) {
            return creeperCandidates.get(0);
        }
        List<Entity> projCandidates = candidates.stream().filter(e -> e instanceof ShulkerBullet || e instanceof LargeFireball).sorted(Comparator.comparingDouble(e -> this.getTargetDistance(eyePos, (Entity)e))).toList();
        if (!projCandidates.isEmpty()) {
            return projCandidates.get(0);
        }
        return candidates.stream().min(Comparator.comparingDouble(e -> this.getTargetDistance(eyePos, (Entity)e))).orElse(null);
    }

    private float getTps() {
        if (this.tpsMode.getValue().equalsIgnoreCase("None")) {
            return 20.0f;
        }
        return Night.SERVER_MANAGER.getTickRate();
    }

    private double getCooldownMs(int weaponSlot) {
        ItemAttributeModifiers modifiers;
        double baseAttackSpeed;
        if (KillAuraModule.mc.player == null) {
            return 500.0;
        }
        int useSlot = weaponSlot == -1 ? KillAuraModule.mc.player.getInventory().getSelectedSlot() : weaponSlot;
        ItemStack stack = KillAuraModule.mc.player.getInventory().getItem(useSlot);
        double attackSpeed = baseAttackSpeed = KillAuraModule.mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS) && (modifiers = (ItemAttributeModifiers)stack.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (!entry.attribute().is(Attributes.ATTACK_SPEED)) continue;
                attackSpeed += entry.modifier().amount();
            }
        }
        double cooldownTicks = 1.0 / Math.max(0.1, attackSpeed) * 20.0;
        double cooldownMs = cooldownTicks * 50.0 * this.delay.getValue().doubleValue();
        if (this.tpsMode.getValue().equalsIgnoreCase("Average")) {
            float tps = Math.max(1.0f, Night.SERVER_MANAGER.getTickRate());
            cooldownMs *= (double)(20.0f / tps);
        }
        return cooldownMs;
    }

    public int getBestWeaponSlot() {
        if (KillAuraModule.mc.player == null) {
            return -1;
        }
        int bestSlot = -1;
        float bestDamage = -1.0f;
        boolean prioritizeMace = !KillAuraModule.mc.player.getAbilities().flying && !KillAuraModule.mc.player.onGround();
        for (int slot = 0; slot < 9; ++slot) {
            int bane;
            int smite;
            int sharpness;
            ItemAttributeModifiers modifiers;
            ItemStack held = KillAuraModule.mc.player.getInventory().getItem(slot);
            if (held.isEmpty()) continue;
            boolean isSword = held.is(ItemTags.SWORDS);
            boolean isAxe = held.is(ItemTags.AXES);
            boolean isTrident = held.getItem() instanceof TridentItem;
            boolean isMace = held.getItem() instanceof MaceItem;
            if (!isSword && !isAxe && !isTrident && !isMace) continue;
            if (isMace && prioritizeMace) {
                return slot;
            }
            float attackDamage = 0.0f;
            if (held.has(DataComponents.ATTRIBUTE_MODIFIERS) && (modifiers = (ItemAttributeModifiers)held.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
                for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                    if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) continue;
                    attackDamage += (float)entry.modifier().amount();
                }
            }
            if (isSword) {
                attackDamage += 5.0f;
            }
            if (!((attackDamage += (float)(sharpness = EnchantmentUtils.getEnchantmentLevel(held, (ResourceKey<Enchantment>)Enchantments.SHARPNESS)) * 1.25f + (float)(smite = EnchantmentUtils.getEnchantmentLevel(held, (ResourceKey<Enchantment>)Enchantments.SMITE)) * 2.5f + (float)(bane = EnchantmentUtils.getEnchantmentLevel(held, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) * 2.5f) > bestDamage)) continue;
            bestDamage = attackDamage;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private float calculatePlayerAttackDamage() {
        MobEffectInstance weakness;
        MobEffectInstance strength;
        if (KillAuraModule.mc.player == null) {
            return 1.0f;
        }
        float attackDamage = 1.0f;
        if (KillAuraModule.mc.player.hasEffect(MobEffects.STRENGTH) && (strength = KillAuraModule.mc.player.getEffect(MobEffects.STRENGTH)) != null) {
            attackDamage += 3.0f * (float)(strength.getAmplifier() + 1);
        }
        if (KillAuraModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (weakness = KillAuraModule.mc.player.getEffect(MobEffects.WEAKNESS)) != null) {
            attackDamage -= 4.0f * (float)(weakness.getAmplifier() + 1);
        }
        return Math.max(attackDamage, 0.0f);
    }

    public static boolean isItemAWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.getItem() instanceof TridentItem || stack.getItem() instanceof MaceItem;
    }

    @Override
    public String getMetaData() {
        return this.target == null ? "None" : this.target.getName().getString();
    }
}

