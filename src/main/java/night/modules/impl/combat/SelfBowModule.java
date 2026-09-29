/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.TippedArrowItem
 */
package night.modules.impl.combat;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TippedArrowItem;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.pingbypass.server.ProxyServerTickListener;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="SelfBow", description="Automatically shoots arrows at you in order to give yourself potion effects.", category=Module.Category.COMBAT)
public class SelfBowModule
extends Module {
    public BooleanSetting manual = new BooleanSetting("Manual", "Whether or not to do the self bow manually.", false);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for switching slots.", "Normal", new String[]{"None", "Normal"});
    public NumberSetting chargeTime = new NumberSetting("ChargeTime", "The amount of ticks that the module will be charging the bow for.", 4, 0, 20);
    public BooleanSetting effectCycle = new BooleanSetting("EffectCycle", "Fires multiple arrows in case of having more than one arrow type.", false);
    private boolean switched = false;
    private boolean todo = false;
    private boolean first = false;
    private int previousSlot = -1;
    private int chargeTicks = 0;
    private int bestArrow = -1;
    private final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue();

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        int arrow;
        if (SelfBowModule.mc.player == null || SelfBowModule.mc.level == null) {
            return;
        }
        int slot = InventoryUtils.findHotbar(Items.BOW);
        if (this.manual.getValue()) {
            boolean flag;
            boolean bl = flag = SelfBowModule.mc.player.isUsingItem() && SelfBowModule.mc.player.getInventory().getSelectedItem().getItem() == Items.BOW;
            if (flag && !this.todo) {
                if (this.effectCycle.getValue()) {
                    this.findArrows();
                }
                this.todo = true;
                this.first = true;
            }
            if (this.todo) {
                if (SelfBowModule.mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
                    this.todo = false;
                    SelfBowModule.mc.options.keyUse.setDown(false);
                    return;
                }
                SelfBowModule.mc.options.keyUse.setDown(true);
            }
            if (!this.todo) {
                return;
            }
        } else {
            if (this.autoSwitch.getValue().equals("None") && SelfBowModule.mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
                Night.CHAT_MANAGER.tagged("You are currently not holding a bow.", this.getName());
                this.setToggled(false);
                return;
            }
            if (!this.autoSwitch.getValue().equals("None") && slot == -1) {
                Night.CHAT_MANAGER.tagged("Could not find a bow in your hotbar.", this.getName());
                this.setToggled(false);
                return;
            }
            if (SelfBowModule.mc.player.getMainHandItem().getItem() != Items.BOW) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, this.previousSlot);
                this.switched = true;
            }
            SelfBowModule.mc.options.keyUse.setDown(true);
        }
        if (this.chargeTicks < this.chargeTime.getValue().intValue() - (this.first ? 1 : 0)) {
            ++this.chargeTicks;
            return;
        }
        if (this.effectCycle.getValue() && !this.queue.isEmpty() && (arrow = this.queue.poll().intValue()) != this.bestArrow) {
            InventoryUtils.swap("Pickup", arrow, this.bestArrow);
        }
        if (mc.getConnection() != null) {
            ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(Night.POSITION_MANAGER.getServerX(), Night.POSITION_MANAGER.getServerY(), Night.POSITION_MANAGER.getServerZ(), SelfBowModule.mc.player.getYRot(), -90.0f, Night.POSITION_MANAGER.isServerOnGround(), SelfBowModule.mc.player.horizontalCollision)));
        }
        SelfBowModule.mc.options.keyUse.setDown(false);
        SelfBowModule.mc.gameMode.releaseUsingItem((Player)SelfBowModule.mc.player);
        this.chargeTicks = 0;
        this.first = false;
        if (!this.effectCycle.getValue() || this.queue.isEmpty()) {
            if (this.manual.getValue()) {
                this.todo = false;
                SelfBowModule.mc.options.keyUse.setDown(false);
            } else {
                this.setToggled(false);
            }
        }
    }

    @Override
    public void onEnable() {
        if (!this.getNull()) {
            if (this.effectCycle.getValue() && !this.manual.getValue()) {
                this.findArrows();
            }
            this.previousSlot = SelfBowModule.mc.player.getInventory().getSelectedSlot();
        }
    }

    @Override
    public void onDisable() {
        if (SelfBowModule.mc.player == null || SelfBowModule.mc.level == null) {
            return;
        }
        if (this.switched) {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), this.previousSlot, this.previousSlot);
        }
        SelfBowModule.mc.options.keyUse.setDown(false);
    }

    private void findArrows() {
        this.bestArrow = -1;
        for (int i = 9; i < 36; ++i) {
            Item item;
            if (SelfBowModule.mc.player.getInventory().getItem(i).isEmpty() || !((item = SelfBowModule.mc.player.getInventory().getItem(i).getItem()) instanceof TippedArrowItem)) continue;
            if (this.bestArrow == -1) {
                this.bestArrow = i;
            }
            this.queue.add(i);
        }
    }

    @Override
    public String getMetaData() {
        int chargeTicks = SelfBowModule.mc.player.getInventory().getSelectedItem().getItem() == Items.BOW ? SelfBowModule.mc.player.getTicksUsingItem() : 0;
        return String.valueOf(chargeTicks);
    }
}

