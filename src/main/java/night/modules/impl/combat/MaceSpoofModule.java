/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Items
 */
package night.modules.impl.combat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import night.events.SubscribeEvent;
import night.events.impl.AttackEntityEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="MaceSpoof", description="Spoof fall height for mace damage.", category=Module.Category.COMBAT)
public class MaceSpoofModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Mode of spoofing.", "Vanilla", new String[]{"Vanilla", "NCP", "Swap"});
    public BooleanSetting noCrystal = new BooleanSetting("NoCrystal", "Don't apply on crystal attack.", new ModeSetting.Visibility(this.mode, "Swap"), true);
    public BooleanSetting inventorySwap = new BooleanSetting("InventorySwap", "Use inventory swap (vs hotbar swap).", new ModeSetting.Visibility(this.mode, "Swap"), false);
    public BooleanSetting onlyGround = new BooleanSetting("OnlyGround", "Only spoof when on ground or flying.", new ModeSetting.Visibility(this.mode, "Vanilla"), true);
    public NumberSetting height = new NumberSetting("Height", "Fake fall height.", new ModeSetting.Visibility(this.mode, "Vanilla"), (Number)25.0, (Number)1.0, (Number)2000.0);
    private boolean attacking = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (this.attacking) {
            return;
        }
        if (MaceSpoofModule.mc.player == null || MaceSpoofModule.mc.level == null || mc.getConnection() == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            if (!MaceSpoofModule.mc.player.getMainHandItem().is(Items.MACE)) {
                return;
            }
            Entity target = event.getTarget();
            if (target == null || target instanceof EndCrystal) {
                return;
            }
            if (this.onlyGround.getValue() && !MaceSpoofModule.mc.player.onGround() && !MaceSpoofModule.mc.player.getAbilities().flying) {
                return;
            }
            if (MaceSpoofModule.mc.player.isInLava() || MaceSpoofModule.mc.player.isUnderWater() || MaceSpoofModule.mc.player.isInWater()) {
                return;
            }
            for (int i = 0; i < 4; ++i) {
                this.sendFakeY(0.0);
            }
            this.sendFakeY(this.height.getValue().doubleValue());
            this.sendFakeY(0.0);
        } else if (this.mode.getValue().equalsIgnoreCase("Swap")) {
            Entity target = event.getTarget();
            if (this.noCrystal.getValue() && target instanceof EndCrystal) {
                return;
            }
            int maceSlot = this.getMaceSlot();
            if (maceSlot == -1) {
                return;
            }
            try {
                this.attacking = true;
                this.doSpoof(target, maceSlot);
            }
            finally {
                this.attacking = false;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet;
        if (MaceSpoofModule.mc.player == null || MaceSpoofModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("NCP") && (packet = event.getPacket()) instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket)packet;
            ((PlayerMoveC2SPacketAccessor)movePacket).setOnGround(false);
        }
    }

    private void doSpoof(Entity target, int maceSlot) {
        int oldSlot = MaceSpoofModule.mc.player.getInventory().getSelectedSlot();
        if (this.inventorySwap.getValue()) {
            InventoryUtils.click(maceSlot, oldSlot, ContainerInput.SWAP);
            MaceSpoofModule.mc.gameMode.attack((Player)MaceSpoofModule.mc.player, target);
            MaceSpoofModule.mc.player.resetAttackStrengthTicker();
            InventoryUtils.click(maceSlot, oldSlot, ContainerInput.SWAP);
        } else {
            InventoryUtils.switchSlot("Silent", maceSlot, oldSlot);
            MaceSpoofModule.mc.gameMode.attack((Player)MaceSpoofModule.mc.player, target);
            MaceSpoofModule.mc.player.resetAttackStrengthTicker();
            InventoryUtils.switchBack("Silent", maceSlot, oldSlot);
        }
    }

    private int getMaceSlot() {
        if (MaceSpoofModule.mc.player == null) {
            return -1;
        }
        if (this.inventorySwap.getValue()) {
            for (int i = 9; i <= 35; ++i) {
                if (!MaceSpoofModule.mc.player.getInventory().getItem(i).is(Items.MACE)) continue;
                return i;
            }
        } else {
            for (int i = 0; i < 9; ++i) {
                if (!MaceSpoofModule.mc.player.getInventory().getItem(i).is(Items.MACE)) continue;
                return i;
            }
        }
        return -1;
    }

    private void sendFakeY(double offset) {
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(MaceSpoofModule.mc.player.getX(), MaceSpoofModule.mc.player.getY() + offset, MaceSpoofModule.mc.player.getZ(), false, MaceSpoofModule.mc.player.horizontalCollision));
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

