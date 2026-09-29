/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 */
package night.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientRotationEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.pingbypass.server.ProxyServerTickListener;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="SelfFill", description="Automatically places a block in the spot that you were previously in.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class SelfFillModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting jumpMode = new ModeSetting("JumpMode", "The mode that will be used for jumping.", "Normal", new String[]{"Normal", "Packet"});
    public ModeSetting burrow = new ModeSetting("Burrow", "Teleports you inside of the block that was placed.", new ModeSetting.Visibility(this.jumpMode, "Packet"), "None", new String[]{"None", "Bypass"});
    public BooleanSetting obsidianOnly = new BooleanSetting("ObsidianOnly", "Only places using obsidian.", false);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Whether or not to rotate when placing the block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Whether or not to destroy crystals that obstruct the block's placement.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private BlockPos lastPosition = null;
    private boolean jumped = false;
    private boolean rotatedBypass = false;
    private int ticks;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        Direction direction;
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.lastPosition == null) {
            this.setToggled(false);
            return;
        }
        if (this.jumpMode.getValue().equalsIgnoreCase("Normal")) {
            if (!this.jumped) {
                SelfFillModule.mc.player.jumpFromGround();
                this.jumped = true;
                this.ticks = 0;
                return;
            }
            if (this.ticks++ < 3) {
                return;
            }
        }
        if ((direction = WorldUtils.getDirection(this.lastPosition, this.strictDirection.getValue())) == null) {
            this.setToggled(false);
            return;
        }
        if (this.jumpMode.getValue().equalsIgnoreCase("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
            if (!this.rotatedBypass) {
                this.rotatedBypass = true;
                this.ticks = 0;
                return;
            }
            if (this.ticks++ < 3) {
                return;
            }
        }
        if (this.autoSwitch.getValue().equalsIgnoreCase("None") && (!(SelfFillModule.mc.player.getMainHandItem().getItem() instanceof BlockItem) || this.obsidianOnly.getValue() && SelfFillModule.mc.player.getMainHandItem().getItem() != Items.OBSIDIAN)) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any " + (this.obsidianOnly.getValue() ? "valid " : "") + "blocks.", this.getName());
            this.setToggled(false);
            return;
        }
        if (!SelfFillModule.mc.level.getBlockState(this.lastPosition).canBeReplaced()) {
            return;
        }
        if (!SelfFillModule.mc.level.getEntities((Entity)null, new AABB(this.lastPosition), e -> e != SelfFillModule.mc.player && !(e instanceof ExperienceOrb) && !(e instanceof ItemEntity) && !(e instanceof EndCrystal)).isEmpty()) {
            return;
        }
        int slot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = SelfFillModule.mc.player.getInventory().getSelectedSlot();
        if (this.obsidianOnly.getValue()) {
            slot = InventoryUtils.find(Items.OBSIDIAN, 0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        }
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1) {
            Night.CHAT_MANAGER.tagged("There are currently no " + (this.obsidianOnly.getValue() ? "valid " : "") + "blocks in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        if (this.jumpMode.getValue().equals("Packet")) {
            ProxyServerTickListener.allowSend(() -> {
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY() + 0.4, SelfFillModule.mc.player.getZ(), false, SelfFillModule.mc.player.horizontalCollision));
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY() + 0.75, SelfFillModule.mc.player.getZ(), false, SelfFillModule.mc.player.horizontalCollision));
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY() + 1.01, SelfFillModule.mc.player.getZ(), false, SelfFillModule.mc.player.horizontalCollision));
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY() + (this.jumpMode.getValue().equalsIgnoreCase("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass") ? 0.99999992 : 1.15), SelfFillModule.mc.player.getZ(), false, SelfFillModule.mc.player.horizontalCollision));
            });
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
        boolean placed = WorldUtils.placeBlock(this.lastPosition, direction, InteractionHand.MAIN_HAND, (!this.jumpMode.getValue().equalsIgnoreCase("Packet") || !this.burrow.getValue().equalsIgnoreCase("Bypass")) && this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        if (!placed) {
            return;
        }
        if (this.jumpMode.getValue().equals("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
            ProxyServerTickListener.allowSend(() -> {
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY() + 1.15, SelfFillModule.mc.player.getZ(), SelfFillModule.mc.player.onGround(), SelfFillModule.mc.player.horizontalCollision));
                SelfFillModule.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY(), SelfFillModule.mc.player.getZ(), SelfFillModule.mc.player.getYRot(), SelfFillModule.mc.player.getXRot(), SelfFillModule.mc.player.onGround(), SelfFillModule.mc.player.horizontalCollision));
            });
            SelfFillModule.mc.player.setPos(SelfFillModule.mc.player.getX(), SelfFillModule.mc.player.getY(), SelfFillModule.mc.player.getZ());
        }
        this.setToggled(false);
        this.jumped = false;
        this.rotatedBypass = false;
    }

    @SubscribeEvent(priority=4)
    public void onClientRotation(ClientRotationEvent event) {
        if (!this.rotatedBypass || this.lastPosition == null || event.isCancelled()) {
            return;
        }
        Direction direction = WorldUtils.getDirection(this.lastPosition, this.strictDirection.getValue());
        if (direction == null) {
            return;
        }
        float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(this.lastPosition, direction));
        event.setYaw(rotations[0]);
        event.setPitch(rotations[1]);
    }

    @Override
    public void onEnable() {
        if (SelfFillModule.mc.player == null || SelfFillModule.mc.level == null) {
            this.setToggled(false);
            return;
        }
        if (!SelfFillModule.mc.player.onGround()) {
            Night.CHAT_MANAGER.tagged("You are currently in the air.", this.getName());
            this.setToggled(false);
            return;
        }
        this.lastPosition = PositionUtils.getFlooredPosition((Entity)SelfFillModule.mc.player);
    }
}

