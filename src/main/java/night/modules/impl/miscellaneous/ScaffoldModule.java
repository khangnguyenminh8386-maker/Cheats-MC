/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="Scaffold", description="Places blocks under the player to bridge automatically.", category=Module.Category.MISCELLANEOUS)
public class ScaffoldModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to blocks.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting range = new NumberSetting("Range", "Range to place blocks.", 4.0, 1.0, 6.0);
    public BooleanSetting keepY = new BooleanSetting("KeepY", "Maintains the player's Y level while moving.", false);
    public BooleanSetting downwards = new BooleanSetting("Downwards", "Places blocks below your feet when sneaking.", true);
    public BooleanSetting tower = new BooleanSetting("Tower", "Quickly towers up when jumping.", true);
    public NumberSetting towerSpeed = new NumberSetting("TowerSpeed", "Vertical tower speed.", new BooleanSetting.Visibility(this.tower, true), (Number)0.42, (Number)0.1, (Number)1.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates towards block placements.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions facing you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without support.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Delay in ticks between placements.", 0, 0, 10);
    public BooleanSetting render = new BooleanSetting("Render", "Renders placed positions.", true);
    private int groundPosY = Integer.MIN_VALUE;
    private BlockPos lastPlacement = null;
    private Block currentScaffoldBlock = null;
    private int ticks = 0;
    private final Map<BlockPos, Long> pendingAttempts = new HashMap<BlockPos, Long>();

    @Override
    public void onEnable() {
        this.groundPosY = Integer.MIN_VALUE;
        this.lastPlacement = null;
        this.ticks = 0;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    @Override
    public void onDisable() {
        this.groundPosY = Integer.MIN_VALUE;
        this.lastPlacement = null;
        this.currentScaffoldBlock = null;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (ScaffoldModule.mc.player == null || ScaffoldModule.mc.level == null) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        int maxSlot = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int slot = this.findValidBlockSlot(0, maxSlot);
        if (slot == -1) {
            Input real;
            Input input = real = ScaffoldModule.mc.player.input != null ? ScaffoldModule.mc.player.input.keyPresses : Input.EMPTY;
            if (!real.shift()) {
                ScaffoldModule.mc.player.input.keyPresses = new Input(real.forward(), real.backward(), real.left(), real.right(), real.jump(), true, real.sprint());
            }
            return;
        }
        if (this.tower.getValue() && ScaffoldModule.mc.options.keyJump.isDown() && !this.isMovingHorizontally()) {
            ScaffoldModule.mc.player.setDeltaMovement(ScaffoldModule.mc.player.getDeltaMovement().x, this.towerSpeed.getValue().doubleValue(), ScaffoldModule.mc.player.getDeltaMovement().z);
        }
        int posY = (int)Math.floor(ScaffoldModule.mc.player.getY());
        if (this.keepY.getValue() && this.isMovingHorizontally()) {
            if (ScaffoldModule.mc.player.onGround() || this.groundPosY < ScaffoldModule.mc.level.getMinY()) {
                this.groundPosY = posY;
            }
            posY = this.groundPosY;
        } else if (this.downwards.getValue() && ScaffoldModule.mc.options.keyShift.isDown()) {
            posY = ScaffoldModule.mc.player.getBlockY() - 1;
        } else {
            this.groundPosY = posY;
        }
        BlockPos playerBlockPos = new BlockPos(ScaffoldModule.mc.player.getBlockX(), posY, ScaffoldModule.mc.player.getBlockZ());
        BlockPos targetPos = playerBlockPos.below();
        List<BlockPos> placements = this.getScaffoldPlacements(targetPos);
        if (placements.isEmpty()) {
            return;
        }
        int prevSlot = ScaffoldModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, prevSlot)) {
            return;
        }
        for (BlockPos pos : placements) {
            if (!ScaffoldModule.mc.level.getBlockState(pos).canBeReplaced() || this.isOnCooldown(pos)) continue;
            Direction dir = WorldUtils.getDirection(pos, this.strictDirection.getValue());
            if (dir == null) {
                if (!this.airPlace.getValue()) continue;
                WorldUtils.airPlaceBlock(pos, InteractionHand.MAIN_HAND, this.rotate.getValue() ? "Normal" : "None", this.render.getValue());
                this.lastPlacement = pos;
                this.predictCollision(pos);
                this.markAttempted(pos);
                continue;
            }
            if (!WorldUtils.placeBlock(pos, dir, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue())) continue;
            this.lastPlacement = pos;
            this.predictCollision(pos);
            this.markAttempted(pos);
        }
        InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, prevSlot);
        this.ticks = 0;
    }

    private void predictCollision(BlockPos pos) {
        if (this.currentScaffoldBlock != null) {
            WorldUtils.predictPlacement(pos, this.currentScaffoldBlock);
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundBlockUpdatePacket) {
            ClientboundBlockUpdatePacket packet2 = (ClientboundBlockUpdatePacket)packet;
            WorldUtils.clearPredictedPlacement(packet2.getPos());
            this.pendingAttempts.remove(packet2.getPos());
        }
    }

    private boolean isOnCooldown(BlockPos pos) {
        Long last = this.pendingAttempts.get(pos);
        if (last == null) {
            return false;
        }
        long cooldownMs = Math.max(50L, (long)Night.SERVER_MANAGER.getPing() * 2L);
        if (System.currentTimeMillis() - last < cooldownMs) {
            return true;
        }
        this.pendingAttempts.remove(pos);
        return false;
    }

    private void markAttempted(BlockPos pos) {
        this.pendingAttempts.put(pos, System.currentTimeMillis());
    }

    private boolean isMovingHorizontally() {
        return Math.abs(ScaffoldModule.mc.player.getDeltaMovement().x) > 0.05 || Math.abs(ScaffoldModule.mc.player.getDeltaMovement().z) > 0.05;
    }

    private int findValidBlockSlot(int start, int end) {
        for (int i = start; i <= end; ++i) {
            BlockItem blockItem;
            Block block;
            Item item;
            ItemStack stack = ScaffoldModule.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || !((item = stack.getItem()) instanceof BlockItem) || (block = (blockItem = (BlockItem)item).getBlock()).defaultBlockState().canBeReplaced() || !block.defaultBlockState().blocksMotion()) continue;
            this.currentScaffoldBlock = block;
            return i;
        }
        return -1;
    }

    private List<BlockPos> getScaffoldPlacements(BlockPos targetPos) {
        ArrayList<BlockPos> placements = new ArrayList<BlockPos>();
        if (this.airPlace.getValue()) {
            if (this.isWithinRange(targetPos)) {
                placements.add(targetPos);
            }
            return placements;
        }
        this.ensurePlaceableWithSupport(targetPos, placements);
        if (this.lastPlacement != null) {
            int x0 = this.lastPlacement.getX();
            int y0 = this.lastPlacement.getY();
            int z0 = this.lastPlacement.getZ();
            int x1 = targetPos.getX();
            int y1 = targetPos.getY();
            int z1 = targetPos.getZ();
            int dx = x1 - x0;
            int dy = y1 - y0;
            int dz = z1 - z0;
            int sx = Integer.compare(dx, 0);
            int sy = Integer.compare(dy, 0);
            int sz = Integer.compare(dz, 0);
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            dz = Math.abs(dz);
            int ax = dx << 1;
            int ay = dy << 1;
            int az = dz << 1;
            int steps = 0;
            if (dx >= dy && dx >= dz) {
                int yd = ay - dx;
                int zd = az - dx;
                while (true) {
                    BlockPos p = new BlockPos(x0, y0, z0);
                    this.ensurePlaceableWithSupport(p, placements);
                    if (++steps <= 8 && (x0 != x1 || y0 != y1 || z0 != z1)) {
                        if (yd >= 0) {
                            y0 += sy;
                            yd -= ax;
                        }
                        if (zd >= 0) {
                            z0 += sz;
                            zd -= ax;
                        }
                        x0 += sx;
                        yd += ay;
                        zd += az;
                        continue;
                    }
                    break;
                }
            } else if (dy >= dx && dy >= dz) {
                int xd = ax - dy;
                int zd = az - dy;
                while (true) {
                    BlockPos p = new BlockPos(x0, y0, z0);
                    this.ensurePlaceableWithSupport(p, placements);
                    if (++steps <= 8 && (x0 != x1 || y0 != y1 || z0 != z1)) {
                        if (xd >= 0) {
                            x0 += sx;
                            xd -= ay;
                        }
                        if (zd >= 0) {
                            z0 += sz;
                            zd -= ay;
                        }
                        y0 += sy;
                        xd += ax;
                        zd += az;
                        continue;
                    }
                    break;
                }
            } else {
                int xd = ax - dz;
                int yd = ay - dz;
                while (true) {
                    BlockPos p = new BlockPos(x0, y0, z0);
                    this.ensurePlaceableWithSupport(p, placements);
                    if (++steps > 8 || x0 == x1 && y0 == y1 && z0 == z1) break;
                    if (xd >= 0) {
                        x0 += sx;
                        xd -= az;
                    }
                    if (yd >= 0) {
                        y0 += sy;
                        yd -= az;
                    }
                    z0 += sz;
                    xd += ax;
                    yd += ay;
                }
            }
        }
        return placements;
    }

    private void ensurePlaceableWithSupport(BlockPos pos, List<BlockPos> out) {
        if (!ScaffoldModule.mc.level.getBlockState(pos).canBeReplaced()) {
            return;
        }
        Direction face = WorldUtils.getDirection(pos, this.strictDirection.getValue());
        if (face != null) {
            if (!out.contains(pos) && this.isWithinRange(pos)) {
                out.add(pos);
            }
            return;
        }
        BlockPos support = this.getSupportingBlock(pos);
        if (support != null) {
            if (!out.contains(support) && this.isWithinRange(support)) {
                out.add(support);
            }
        } else {
            BlockPos down = pos.below();
            int depth = 0;
            while (depth++ < 3 && ScaffoldModule.mc.level.getBlockState(down).canBeReplaced()) {
                if (WorldUtils.getDirection(down, this.strictDirection.getValue()) != null) {
                    if (out.contains(down) || !this.isWithinRange(down)) break;
                    out.add(down);
                    break;
                }
                down = down.below();
            }
        }
        if (!out.contains(pos) && this.isWithinRange(pos)) {
            out.add(pos);
        }
    }

    private BlockPos getSupportingBlock(BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(dir);
            if (WorldUtils.getDirection(side, this.strictDirection.getValue()) == null) continue;
            return side;
        }
        return null;
    }

    private boolean isWithinRange(BlockPos pos) {
        return ScaffoldModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= this.range.getValue().doubleValue() * this.range.getValue().doubleValue();
    }
}

