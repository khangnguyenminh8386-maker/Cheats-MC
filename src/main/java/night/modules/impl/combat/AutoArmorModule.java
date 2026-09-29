/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.equipment.Equippable
 */
package night.modules.impl.combat;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="AutoArmor", description="Automatically equips the best armor.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class AutoArmorModule
extends Module {
    public ModeSetting health = new ModeSetting("Health", "The health priority to apply.", "Highest", new String[]{"Highest", "Lowest", "Any"});
    public BooleanSetting elytraPriority = new BooleanSetting("ElytraPriority", "Prioritizes elytra over armor pieces.", true);
    public BooleanSetting preserve = new BooleanSetting("Preserve", "Preserve low health armor to avoid it from breaking.", false);
    public NumberSetting preserveHealth = new NumberSetting("PreserveHealth", "The minimum health of armor to preserve it.", new BooleanSetting.Visibility(this.preserve, true), (Number)Float.valueOf(20.0f), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(50.0f));
    public NumberSetting safeRange = new NumberSetting("SafeRange", "Range to check for nearby players before re-equipping preserved armor.", new BooleanSetting.Visibility(this.preserve, true), (Number)0, (Number)0, (Number)10);
    public BooleanSetting elytra = new BooleanSetting("Elytra", "Equips an elytra instead of a chestplate.", false);
    public BindSetting elytraBind = new BindSetting("ElytraBind", "Keybind that toggles Elytra on/off.", 0).disableHoldModes();
    public BooleanSetting smartElytra = new BooleanSetting("SmartElytra", "Chooses when to enable elytra in a more convenient way.", false);
    public ModeSetting headMode = new ModeSetting("HeadMode", "Which protection enchant Head armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting chestMode = new ModeSetting("ChestMode", "Which protection enchant Chest armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting legsMode = new ModeSetting("LegsMode", "Which protection enchant Legs armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting feetMode = new ModeSetting("FeetMode", "Which protection enchant Feet armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    private int ticks = 0;

    @SubscribeEvent
    public void onElytraBind(KeyInputEvent event) {
        if (this.shouldRunOnProxy() || this.getNull() || AutoArmorModule.mc.gui != null && AutoArmorModule.mc.gui.screen() != null) {
            return;
        }
        if (this.elytraBind.getValue() != 0 && event.getKey() == this.elytraBind.getValue() && this.elytraBind.getMode().equals("Bind")) {
            this.elytra.setValue(!this.elytra.getValue());
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull() || !this.smartElytra.getValue()) {
            return;
        }
        if (!(event.getKey() != 32 || this.elytra.getValue() || AutoArmorModule.mc.player.onGround() || EntityUtils.isInWeb((Entity)AutoArmorModule.mc.player))) {
            this.elytra.setValue(true);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (AutoArmorModule.mc.player == null || AutoArmorModule.mc.level == null) {
            return;
        }
        if (this.smartElytra.getValue() && this.elytra.getValue() && (AutoArmorModule.mc.player.onGround() && !(AutoArmorModule.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) || EntityUtils.isInWeb((Entity)AutoArmorModule.mc.player))) {
            this.elytra.setValue(false);
        }
        if (this.ticks <= 0) {
            if (InventoryUtils.inInventoryScreen()) {
                return;
            }
            this.update(EquipmentSlot.HEAD, 5);
            if (!this.elytra.getValue() || !this.elytraPriority.getValue() || AutoArmorModule.mc.player.getInventory().getItem(38).getItem() != Items.ELYTRA) {
                this.update(EquipmentSlot.CHEST, 6);
            }
            this.update(EquipmentSlot.LEGS, 7);
            this.update(EquipmentSlot.FEET, 8);
        }
        --this.ticks;
    }

    private boolean isTierDiamondOrNetherite(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        return name.contains("diamond") || name.contains("netherite");
    }

    private boolean isValidCombatArmor(ItemStack stack, String enchantId) {
        if (!this.isArmor(stack)) {
            return false;
        }
        return this.isTierDiamondOrNetherite(stack) || this.getEnchantLevel(stack, enchantId) > 0;
    }

    private void update(EquipmentSlot type, int x) {
        int best;
        boolean flag;
        int elytraSlot = this.findElytra();
        boolean bl = flag = this.elytra.getValue() && type == EquipmentSlot.CHEST;
        int slot = type == EquipmentSlot.HEAD ? 39 : (type == EquipmentSlot.CHEST ? 38 : (type == EquipmentSlot.LEGS ? 37 : 36));
        int armor = flag ? elytraSlot : this.findArmor(type);
        int n = best = flag ? this.compareElytra(38, armor) : this.compare(slot, armor, true, type);
        if (armor != -1 && best != slot) {
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, x, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, InventoryUtils.indexToSlot(armor), 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, x, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            this.ticks = 2 + Night.SERVER_MANAGER.getPingDelay();
        }
    }

    private int compare(int x, int y, boolean swap, EquipmentSlot type) {
        if (y == -1) {
            return x;
        }
        if (!this.isArmor(AutoArmorModule.mc.player.getInventory().getItem(x))) {
            return y;
        }
        String enchantId = switch (type) {
            case EquipmentSlot.HEAD -> this.headMode.getValue();
            case EquipmentSlot.CHEST -> this.chestMode.getValue();
            case EquipmentSlot.LEGS -> this.legsMode.getValue();
            default -> this.feetMode.getValue();
        };
        enchantId = enchantId.equalsIgnoreCase("Blast") ? "blast_protection" : "protection";
        ItemStack stackX = AutoArmorModule.mc.player.getInventory().getItem(x);
        ItemStack stackY = AutoArmorModule.mc.player.getInventory().getItem(y);
        if (this.getEnchantLevel(stackX, enchantId) < this.getEnchantLevel(stackY, enchantId)) {
            return y;
        }
        if (this.preserve.getValue()) {
            boolean safe = this.isSafe();
            if (safe) {
                if (this.getDurability(y) < this.preserveHealth.getValue().floatValue() && this.getDurability(x) >= this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(stackY, enchantId)) {
                    return y;
                }
            } else if (this.getDurability(x) < this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(stackY, enchantId)) {
                return this.getDurability(x) < this.getDurability(y) ? y : x;
            }
        }
        if (!swap) {
            if (this.health.getValue().equals("Highest") && this.getDurability(x) < this.getDurability(y)) {
                return y;
            }
            if (this.health.getValue().equals("Lowest") && this.getDurability(x) > this.getDurability(y)) {
                return y;
            }
        }
        return x;
    }

    private boolean isSafe() {
        if (this.safeRange.getValue().doubleValue() <= 0.0) {
            return false;
        }
        if (AutoArmorModule.mc.level == null || AutoArmorModule.mc.player == null) {
            return false;
        }
        double rangeSq = this.safeRange.getValue().doubleValue() * this.safeRange.getValue().doubleValue();
        for (Player other : AutoArmorModule.mc.level.players()) {
            if (other == AutoArmorModule.mc.player || other.isSpectator() || !other.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(other.getName().getString()) || !(AutoArmorModule.mc.player.distanceToSqr((Entity)other) <= rangeSq)) continue;
            return false;
        }
        return true;
    }

    private int compareElytra(int x, int y) {
        if (y == -1) {
            return x;
        }
        if (AutoArmorModule.mc.player.getInventory().getItem(x).getItem() != Items.ELYTRA) {
            return y;
        }
        if (this.health.getValue().equals("Highest") && this.getDurability(x) < this.getDurability(y)) {
            return y;
        }
        if (this.health.getValue().equals("Lowest") && this.getDurability(x) > this.getDurability(y)) {
            return y;
        }
        return x;
    }

    private int findArmor(EquipmentSlot type) {
        int slot = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack stack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (!this.isArmor(stack) || !this.getSlotType(stack).equals((Object)type)) continue;
            slot = this.compare(i, slot, false, type);
        }
        return slot;
    }

    private int findElytra() {
        int slot = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack stack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (stack.getItem() != Items.ELYTRA) continue;
            slot = this.compareElytra(i, slot);
        }
        return slot;
    }

    private int getEnchantLevel(ItemStack stack, String enchantId) {
        for (Holder enchantment : stack.getEnchantments().keySet()) {
            if (!enchantment.getRegisteredName().replace("minecraft:", "").equals(enchantId)) continue;
            return stack.getEnchantments().getLevel(enchantment);
        }
        return 0;
    }

    private float getDurability(int slot) {
        ItemStack stack = AutoArmorModule.mc.player.getInventory().getItem(slot);
        return (float)(stack.getMaxDamage() - stack.getDamageValue()) * 100.0f / (float)stack.getMaxDamage();
    }

    private EquipmentSlot getSlotType(ItemStack itemStack) {
        if (itemStack.has(DataComponents.GLIDER)) {
            return EquipmentSlot.CHEST;
        }
        return ((Equippable)itemStack.get(DataComponents.EQUIPPABLE)).slot();
    }

    private boolean isArmor(ItemStack itemStack) {
        if (itemStack.getItem() == Items.ELYTRA) {
            return false;
        }
        boolean grantsArmor = ((ItemAttributeModifiers)itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, (Object)ItemAttributeModifiers.EMPTY)).modifiers().stream().anyMatch(entry -> entry.attribute().is(Attributes.ARMOR));
        if (!grantsArmor) {
            return false;
        }
        Equippable equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}

