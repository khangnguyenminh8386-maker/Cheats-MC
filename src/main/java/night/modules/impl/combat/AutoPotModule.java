/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.minecraft.InventoryUtils;
import night.utils.system.Timer;

@RegisterModule(name="AutoPot", description="Throws a splash potion under/above you to keep whitelisted potions' effects topped up.", category=Module.Category.COMBAT)
public class AutoPotModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "WhiteList = keep these potions' effects topped up. BlackList = keep every potion's effects topped up except these.", "WhiteList", new String[]{"WhiteList", "BlackList"});
    public WhitelistSetting whitelist = new WhitelistSetting("Whitelist", "Potions this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.POTIONS);
    public NumberSetting amplifier = new NumberSetting("Amplifier", "Minimum amplifier level required before an effect counts as \"already have it\".", 1, 0, 4);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates to look straight up/down before throwing (nami: real rotation, waits for it to land before throwing).", false);
    public ModeSetting throwMode = new ModeSetting("Throw", "Where the potion lands relative to you.", new BooleanSetting.Visibility(this.rotate, true), "Under", new String[]{"Above", "Under"});
    public ModeSetting swapMode = new ModeSetting("Swap", "How the potion slot gets selected before throwing.", "Silent", new String[]{"Normal", "Silent"});
    public BooleanSetting whenNoTarget = new BooleanSetting("NoTarget", "Only throws while no enemy is nearby.", false);
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only throws while phased into a block.", false);
    public BooleanSetting selfToggle = new BooleanSetting("SelfToggle", "Turns the module off once every whitelisted effect is already topped up.", true);
    public NumberSetting range = new NumberSetting("Range", "How close an enemy has to be for NoTarget to see them.", new BooleanSetting.Visibility(this.whenNoTarget, true), (Number)32, (Number)4, (Number)64);
    private final Timer throwTimer = new Timer();
    private static final long THROW_DELAY_MS = 5000L;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        float pitch;
        if (AutoPotModule.mc.player == null || AutoPotModule.mc.level == null) {
            return;
        }
        Potion missing = this.findMissingPotion();
        if (missing == null) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (this.whenNoTarget.getValue() && this.hasNearbyEnemy()) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (this.onlyPhased.getValue() && !this.isPhased()) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        int potInvSlot = this.findPot(missing);
        if (potInvSlot == -1) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (!this.throwTimer.hasTimeElapsed(5000L)) {
            return;
        }
        int potSlot = potInvSlot < 9 ? potInvSlot : -1;
        int previousSlot = AutoPotModule.mc.player.getInventory().getSelectedSlot();
        if (potSlot == -1) {
            InventoryUtils.swap("Pickup", potInvSlot, InventoryUtils.HOTBAR_START);
            potSlot = InventoryUtils.HOTBAR_START;
        }
        float f = pitch = this.throwMode.getValue().equalsIgnoreCase("Above") ? -90.0f : 90.0f;
        if (this.rotate.getValue()) {
            float[] target = new float[]{AutoPotModule.mc.player.getYRot(), pitch};
            Night.ROTATION_MANAGER.legacyRotate(target, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        this.throwTimer.reset();
        InventoryUtils.switchSlot(this.swapMode.getValue(), potSlot, previousSlot);
        AutoPotModule.mc.gameMode.useItem((Player)AutoPotModule.mc.player, InteractionHand.MAIN_HAND);
        InventoryUtils.switchBack(this.swapMode.getValue(), potSlot, previousSlot);
    }

    private Potion findMissingPotion() {
        int requiredAmp = this.amplifier.getValue().intValue();
        for (Potion potionType : this.whitelist.getWhitelistedPotions()) {
            boolean missingAny = false;
            for (MobEffectInstance want : potionType.getEffects()) {
                boolean have = false;
                for (MobEffectInstance active : AutoPotModule.mc.player.getActiveEffects()) {
                    if (active.getEffect() != want.getEffect() || active.getAmplifier() < requiredAmp) continue;
                    have = true;
                    break;
                }
                if (have) continue;
                missingAny = true;
                break;
            }
            if (!missingAny) continue;
            return potionType;
        }
        return null;
    }

    private int findPot(Potion targetPotion) {
        for (int i = 0; i < 36; ++i) {
            PotionContents contents;
            ItemStack stack = AutoPotModule.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != Items.SPLASH_POTION || (contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS)) == null || contents.potion().isEmpty() || ((Holder)contents.potion().get()).value() != targetPotion) continue;
            return i;
        }
        return -1;
    }

    private boolean hasNearbyEnemy() {
        double rangeSq = Mth.square((double)this.range.getValue().doubleValue());
        for (Player player : AutoPotModule.mc.level.players()) {
            if (player == AutoPotModule.mc.player || Night.FRIEND_MANAGER.contains(player.getName().getString()) || !(AutoPotModule.mc.player.distanceToSqr((Entity)player) <= rangeSq)) continue;
            return true;
        }
        return false;
    }

    private boolean isPhased() {
        AABB box = AutoPotModule.mc.player.getBoundingBox();
        int minX = Mth.floor((double)box.minX);
        int maxX = Mth.ceil((double)box.maxX);
        int minY = Mth.floor((double)box.minY);
        int maxY = Mth.ceil((double)box.maxY);
        int minZ = Mth.floor((double)box.minZ);
        int maxZ = Mth.ceil((double)box.maxZ);
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = AutoPotModule.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)AutoPotModule.mc.level, pos);
                    if (shape.isEmpty() || !shape.bounds().move(pos).intersects(box)) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

