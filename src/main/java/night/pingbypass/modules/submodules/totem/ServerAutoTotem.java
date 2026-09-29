/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 */
package night.pingbypass.modules.submodules.totem;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerPopEvent;
import night.modules.impl.combat.SuicideModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.modules.PbModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.IMinecraft;
import night.utils.minecraft.InventoryUtils;

public class ServerAutoTotem
extends PbModule
implements IMinecraft {
    public ModeSetting item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", new String[]{"Totem", "Crystal", "Gapple"});
    public NumberSetting health = new NumberSetting("Health", "The health at which a totem will be prioritized.", 16, 0, 36);
    public BooleanSetting elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", Float.valueOf(20.0f), Float.valueOf(0.0f), Float.valueOf(80.0f));
    public BooleanSetting useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
    public BooleanSetting gappleSword = new BooleanSetting("Sword", "Switches to a gapple when holding a sword or axe in main hand.", true);
    public BooleanSetting gapplePickaxe = new BooleanSetting("Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", true);
    public BooleanSetting gappleTotem = new BooleanSetting("Totem", "Switches to a gapple when holding a totem in main hand.", false);
    public BooleanSetting lethalOverride = new BooleanSetting("LethalOverride", "Overrides any necessity for a totem when gappling.", false);
    public BooleanSetting version117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
    public BooleanSetting smartMine = new BooleanSetting("SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", false);
    public BooleanSetting antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
    public NumberSetting maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", Float.valueOf(12.0f), Float.valueOf(0.0f), Float.valueOf(24.0f));
    private int totemCount = 0;
    private int gappleStagedSlot = -1;

    public ServerAutoTotem() {
        super("AutoTotem");
    }

    @Override
    public void onEnable() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @Override
    public void onDisable() {
        Night.EVENT_HANDLER.unsubscribe(this);
        this.gappleStagedSlot = -1;
    }

    @Override
    public List<Setting> getSettings() {
        return List.of(this.item, this.health, this.elytraCheck, this.fallDistance, this.useGapple, this.gappleSword, this.gapplePickaxe, this.gappleTotem, this.lethalOverride, this.version117, this.smartMine, this.antiMace, this.maceRange);
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerPop(PlayerPopEvent event) {
        if (event.getPlayer() == ServerAutoTotem.mc.player && !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            this.tick();
        }
    }

    @Override
    public void tick() {
        int slot;
        if (ServerAutoTotem.mc.player == null || ServerAutoTotem.mc.level == null) {
            return;
        }
        this.totemCount = ServerAutoTotem.mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
        if (!(ServerAutoTotem.mc.gui.screen() instanceof InventoryScreen) && ServerAutoTotem.mc.gui.screen() instanceof AbstractContainerScreen) {
            return;
        }
        Item item = this.getItem();
        if (item == null) {
            return;
        }
        if (item == Items.TOTEM_OF_UNDYING && Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            if (ServerAutoTotem.mc.player.getOffhandItem().isEmpty()) {
                return;
            }
            slot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
        } else {
            if (ServerAutoTotem.mc.player.getOffhandItem().getItem() == item) {
                return;
            }
            if (this.gappleStagedSlot != -1 && item == Items.TOTEM_OF_UNDYING) {
                if (ServerAutoTotem.mc.player.getInventory().getItem(this.gappleStagedSlot).getItem() == Items.TOTEM_OF_UNDYING) {
                    slot = this.gappleStagedSlot;
                } else {
                    slot = InventoryUtils.findInventory(item);
                    if (slot == -1) {
                        slot = InventoryUtils.find(item);
                    }
                }
                this.gappleStagedSlot = -1;
            } else if (item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_APPLE) {
                slot = InventoryUtils.findHotbar(item);
                if (slot == -1) {
                    slot = InventoryUtils.findInventory(item);
                }
                if (slot != -1) {
                    this.gappleStagedSlot = slot;
                }
            } else {
                slot = InventoryUtils.findInventory(item);
                if (slot == -1) {
                    slot = InventoryUtils.find(item);
                }
            }
            if (slot == -1) {
                if (item == Items.TOTEM_OF_UNDYING) {
                    slot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
                } else {
                    return;
                }
            }
        }
        if (slot == -1) {
            return;
        }
        if (this.version117.getValue()) {
            InventoryUtils.swap("Swap", slot, 40);
        } else {
            InventoryUtils.swap("Pickup", slot, 45);
        }
    }

    private Item getItem() {
        boolean mainHandMatches;
        boolean hasTotem = this.hasItem(Items.TOTEM_OF_UNDYING);
        Item gappleItem = this.hasItem(Items.ENCHANTED_GOLDEN_APPLE) ? Items.ENCHANTED_GOLDEN_APPLE : (this.hasItem(Items.GOLDEN_APPLE) ? Items.GOLDEN_APPLE : null);
        boolean hasGapple = gappleItem != null;
        boolean holdingSword = ServerAutoTotem.mc.player.getMainHandItem().is(ItemTags.SWORDS) || ServerAutoTotem.mc.player.getMainHandItem().getItem() instanceof AxeItem;
        boolean holdingPickaxe = ServerAutoTotem.mc.player.getMainHandItem().is(ItemTags.PICKAXES);
        boolean holdingTotem = ServerAutoTotem.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
        boolean bl = mainHandMatches = this.gappleSword.getValue() && holdingSword || this.gapplePickaxe.getValue() && holdingPickaxe || this.gappleTotem.getValue() && holdingTotem;
        if (this.useGapple.getValue() && ServerAutoTotem.mc.options.keyUse.isDown() && mainHandMatches && hasGapple && (this.lethalOverride.getValue() || !this.needsTotem() || !hasTotem)) {
            return gappleItem;
        }
        if (this.hasItem(Items.TOTEM_OF_UNDYING)) {
            SpeedMineModule module;
            if (this.needsTotem()) {
                return Items.TOTEM_OF_UNDYING;
            }
            if (!(!this.item.getValue().equalsIgnoreCase("Crystal") || !this.smartMine.getValue() || (module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)).getPrimary() != null && module.getPrimary().isMining() || module.getSecondary() != null && module.getSecondary().isMining())) {
                return Items.TOTEM_OF_UNDYING;
            }
        }
        switch (this.item.getValue()) {
            case "Crystal": {
                if (!this.hasItem(Items.END_CRYSTAL)) {
                    return Items.TOTEM_OF_UNDYING;
                }
                return Items.END_CRYSTAL;
            }
            case "Gapple": {
                if (!hasGapple) {
                    return Items.TOTEM_OF_UNDYING;
                }
                return gappleItem;
            }
        }
        return Items.TOTEM_OF_UNDYING;
    }

    private boolean needsTotem() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            return false;
        }
        if (ServerAutoTotem.mc.player.getHealth() + ServerAutoTotem.mc.player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
            return true;
        }
        if (ServerAutoTotem.mc.player.fallDistance > (double)this.fallDistance.getValue().floatValue()) {
            return true;
        }
        if (this.elytraCheck.getValue() && ServerAutoTotem.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return true;
        }
        return this.antiMace.getValue() && ServerAutoTotem.mc.level.players().stream().anyMatch(entity -> entity != ServerAutoTotem.mc.player && !Night.FRIEND_MANAGER.contains(entity.getName().getString()) && ServerAutoTotem.mc.player.distanceToSqr((Entity)entity) <= (double)Mth.square((float)this.maceRange.getValue().floatValue()) && entity.fallDistance >= 1.5 && entity.getMainHandItem().getItem().equals(Items.MACE));
    }

    private boolean hasItem(Item item) {
        return InventoryUtils.find(item) != -1 || ServerAutoTotem.mc.player.getOffhandItem().getItem() == item;
    }

    public String getMetaData() {
        return String.valueOf(this.totemCount);
    }
}

