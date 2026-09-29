/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 */
package night.utils.minecraft;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentUtils {
    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ItemEnchantments enchantments = (ItemEnchantments)stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            return 0;
        }
        for (Object2IntMap.Entry entry : enchantments.entrySet()) {
            Optional keyOptional = ((Holder)entry.getKey()).unwrapKey();
            if (!keyOptional.isPresent() || !((ResourceKey)keyOptional.get()).equals(enchantmentKey)) continue;
            return entry.getIntValue();
        }
        return 0;
    }
}

