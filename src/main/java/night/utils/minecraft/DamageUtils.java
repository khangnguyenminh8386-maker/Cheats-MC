/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.util.Mth
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.damagesource.CombatRules
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.utils.IMinecraft;
import night.utils.minecraft.EntityUtils;

public class DamageUtils
implements IMinecraft {
    private static final Map<String, Integer> PROTECTION_MAP = new HashMap<String, Integer>(){
        {
            this.put("protection", 1);
            this.put("blast_protection", 2);
            this.put("projectile_protection", 1);
            this.put("feather_falling", 1);
            this.put("fire_protection", 1);
        }
    };

    public static float getCrystalDamage(Entity entity, AABB box, EndCrystal crystal, boolean ignoreTerrain) {
        if (crystal == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, box, crystal.position(), 6.0f, null, ignoreTerrain);
    }

    public static float getCrystalDamage(Entity entity, AABB box, BlockPos position, BlockPos exception, boolean ignoreTerrain) {
        if (position == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, box, new Vec3((double)position.getX() + 0.5, (double)(position.getY() + 1), (double)position.getZ() + 0.5), 6.0f, exception, ignoreTerrain);
    }

    public static float getDamage(Entity entity, AABB box, Vec3 vec3d, float power, BlockPos exception, boolean ignoreTerrain) {
        Player player;
        if (DamageUtils.mc.level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        if (!(entity instanceof RemotePlayer) && entity instanceof Player && EntityUtils.getGameMode(player = (Player)entity) == GameType.CREATIVE) {
            return 0.0f;
        }
        float diameter = power * 2.0f;
        AABB targetBox = box != null ? box : entity.getBoundingBox();
        Vec3 feet = new Vec3(targetBox.getCenter().x, targetBox.minY, targetBox.getCenter().z);
        double distance = Math.sqrt(feet.distanceToSqr(vec3d)) / (double)diameter;
        if (distance > 1.0) {
            return 0.0f;
        }
        boolean actualIgnoreTerrain = entity == DamageUtils.mc.player ? false : ignoreTerrain;
        double exposure = (1.0 - distance) * (double)DamageUtils.getExposure(vec3d, targetBox, exception, actualIgnoreTerrain);
        float damage = (int)((exposure * exposure + exposure) / 2.0 * 7.0 * (double)diameter + 1.0);
        if (damage <= 0.0f) {
            return 0.0f;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            damage = DamageUtils.mc.level.getDifficulty() == Difficulty.EASY ? Math.min(damage / 2.0f + 1.0f, damage) : (DamageUtils.mc.level.getDifficulty() == Difficulty.HARD ? damage * 3.0f / 2.0f : damage);
            damage = CombatRules.getDamageAfterAbsorb((LivingEntity)livingEntity, (float)damage, (DamageSource)DamageUtils.mc.level.damageSources().explosion(null), (float)livingEntity.getArmorValue(), (float)((float)livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
            damage = (float)((double)damage * (livingEntity.hasEffect(MobEffects.RESISTANCE) ? 1.0 - (double)(livingEntity.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 0.2 : 1.0));
            damage = CombatRules.getDamageAfterMagicAbsorb((float)damage, (float)DamageUtils.getProtectionAmount(List.of(livingEntity.getItemBySlot(EquipmentSlot.FEET), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.HEAD))));
        }
        return Math.max(damage, 0.0f);
    }

    public static int getProtectionAmount(Iterable<ItemStack> armor) {
        int x = 0;
        for (ItemStack stack : armor) {
            x += DamageUtils.getProtectionAmount(stack);
        }
        return x;
    }

    public static int getProtectionAmount(ItemStack armor) {
        int x = 0;
        ItemEnchantments enchantments = armor.getEnchantments();
        for (Holder enchantment : enchantments.keySet()) {
            String id = enchantment.getRegisteredName().replace("minecraft:", "");
            if (!PROTECTION_MAP.containsKey(id)) continue;
            x += enchantments.getLevel(enchantment) * PROTECTION_MAP.get(id);
            break;
        }
        return x;
    }

    private static float getExposure(Vec3 source, AABB box, BlockPos exception, boolean ignoreTerrain) {
        int hitCount = 0;
        int count = 0;
        for (double k = 0.0; k <= 1.0; k += 0.4545454446934474) {
            for (double l = 0.0; l <= 1.0; l += 0.21739130885479366) {
                for (double m = 0.0; m <= 1.0; m += 0.4545454446934474) {
                    Vec3 vec3d = new Vec3(Mth.lerp((double)k, (double)box.minX, (double)box.maxX) + 0.045454555306552624, Mth.lerp((double)l, (double)box.minY, (double)box.maxY), Mth.lerp((double)m, (double)box.minZ, (double)box.maxZ) + 0.045454555306552624);
                    if (DamageUtils.raycast(vec3d, source, exception, ignoreTerrain) == HitResult.Type.MISS) {
                        ++hitCount;
                    }
                    ++count;
                }
            }
        }
        return (float)hitCount / (float)count;
    }

    private static HitResult.Type raycast(Vec3 start, Vec3 end, BlockPos exception, boolean ignoreTerrain) {
        if (DamageUtils.mc.level == null) {
            return HitResult.Type.MISS;
        }
        return (HitResult.Type)BlockGetter.traverseBlocks((Vec3)start, (Vec3)end, null, (innerContext, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals((Object)exception)) {
                blockState = Blocks.AIR.defaultBlockState();
            } else {
                blockState = DamageUtils.mc.level.getBlockState(blockPos);
                if (blockState.getBlock().getExplosionResistance() < 600.0f && ignoreTerrain) {
                    blockState = Blocks.AIR.defaultBlockState();
                }
            }
            BlockHitResult hitResult = blockState.getCollisionShape((BlockGetter)DamageUtils.mc.level, blockPos).clip(start, end, blockPos);
            return hitResult == null ? null : hitResult.getType();
        }, innerContext -> HitResult.Type.MISS);
    }
}

