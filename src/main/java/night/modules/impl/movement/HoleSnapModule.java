/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.movement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.RenderWorldEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IHoleSnapModule;
import night.modules.impl.combat.SurroundModule;
import night.modules.impl.movement.SpeedModule;
import night.modules.impl.movement.StepModule;
import night.modules.impl.movement.TickShiftModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.MovementUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="HoleSnap", description="Pulls you toward your nearest hole.", category=Module.Category.MOVEMENT)
public class HoleSnapModule
extends Module
implements IHoleSnapModule {
    public NumberSetting range = new NumberSetting("Range", "Range for the holes.", 5, 1, 8);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to snap you to double holes.", true);
    public BooleanSetting quadHoles = new BooleanSetting("QuadHoles", "Whether or not to snap you to quad holes.", true);
    public BooleanSetting step = new BooleanSetting("Step", "Keeps snapping to the next nearest hole instead of stopping once you've reached one.", false);
    public BooleanSetting fillHole = new BooleanSetting("FillHole", "Fills every cell of the hole you just stepped out of, once you've fully left its footprint (needs Step).", new BooleanSetting.Visibility(this.step, true), false);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public NumberSetting timer = new NumberSetting("Timer", "The tick-speed multiplier to run at while HoleSnap is active (1.0 = normal speed).", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(10.0f));
    public BooleanSetting renderTarget = new BooleanSetting("RenderTarget", "Draws an outline around the hole HoleSnap is currently walking toward.", true);
    public ColorSetting renderTargetColor = new ColorSetting("RenderTargetColor", "The color of the target hole outline.", new BooleanSetting.Visibility(this.renderTarget, true), ColorUtils.getDefaultOutlineColor());
    public AABB hole = null;
    private HoleUtils.Hole startingHole = null;
    private AABB lastUsedHole = null;
    private boolean jumpPressed = false;
    private int jumpAttempts = 0;
    private AABB unreachableHole = null;
    private int giveUpCount = 0;
    private static final int MAX_CONSECUTIVE_GIVE_UPS = 3;
    private int jumpWaitTicks = 0;
    private final Deque<BlockPos> fillQueue = new ArrayDeque<BlockPos>();
    private AABB pendingFillHole = null;
    private int fillWaitTicks = 0;
    private static final int MAX_FILL_WAIT_TICKS = 20;
    private final List<BlockPos> filledPositions = new ArrayList<BlockPos>();

    @Override
    public boolean isStepEnabled() {
        return this.step.getValue();
    }

    public static boolean isInHole(BlockPos pos) {
        return HoleUtils.getSingleHole(pos, 1.0, false) != null || HoleUtils.getDoubleHole(pos, 1.0) != null || HoleUtils.getQuadHole(pos, 1.0) != null;
    }

    @Override
    public void onEnable() {
        SurroundModule surround;
        this.hole = null;
        this.startingHole = null;
        this.fillQueue.clear();
        this.pendingFillHole = null;
        this.filledPositions.clear();
        this.fillWaitTicks = 0;
        this.jumpAttempts = 0;
        this.jumpWaitTicks = 0;
        this.unreachableHole = null;
        this.giveUpCount = 0;
        Night.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (this.step.getValue() && (surround = Night.MODULE_MANAGER.getModule(SurroundModule.class)).isToggled()) {
            surround.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
        if (this.jumpPressed) {
            HoleSnapModule.mc.options.keyJump.setDown(false);
            this.jumpPressed = false;
        }
        this.jumpWaitTicks = 0;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.renderTarget.getValue() || this.hole == null) {
            return;
        }
        Renderer3D.renderBoxOutline(event.getMatrices(), this.hole, this.renderTargetColor.getColor());
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        boolean centered;
        Night.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (this.getNull()) {
            return;
        }
        if (this.pendingFillHole != null) {
            if (this.pendingFillHole.contains(HoleSnapModule.mc.player.getX(), this.pendingFillHole.minY + 0.5, HoleSnapModule.mc.player.getZ()) && this.fillWaitTicks++ < 20) {
                return;
            }
            if (!this.fillQueue.isEmpty()) {
                this.fillNext();
                return;
            }
            this.pendingFillHole = null;
            this.filledPositions.clear();
            this.setToggled(false);
            return;
        }
        if (HoleSnapModule.mc.player.fallDistance >= 5.0 && this.hole == null) {
            return;
        }
        if (this.hole == null && !this.pickTarget()) {
            return;
        }
        if (this.hole != null && !this.isHoleReachable(this.hole)) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            if (++this.giveUpCount >= 3) {
                Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                this.setToggled(false);
                return;
            }
            if (!this.pickTarget()) {
                return;
            }
        }
        if (this.hole != null && !this.isHoleStillValid(this.hole)) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            if (!this.pickTarget()) {
                return;
            }
        }
        boolean bl = centered = Math.abs(HoleSnapModule.mc.player.getX() - this.hole.getCenter().x) < 0.03 && Math.abs(HoleSnapModule.mc.player.getY() - this.hole.minY) < 0.05 && Math.abs(HoleSnapModule.mc.player.getZ() - this.hole.getCenter().z) < 0.03;
        if (!centered) {
            if (HoleSnapModule.mc.player.horizontalCollision && HoleSnapModule.mc.player.onGround() && !this.jumpPressed) {
                double dx = this.hole.getCenter().x - HoleSnapModule.mc.player.getX();
                double dz = this.hole.getCenter().z - HoleSnapModule.mc.player.getZ();
                Vec3 dir = dx == 0.0 && dz == 0.0 ? Vec3.ZERO : new Vec3(dx, 0.0, dz).normalize();
                AABB playerBox = HoleSnapModule.mc.player.getBoundingBox();
                AABB wallCheck = new AABB(playerBox.minX, HoleSnapModule.mc.player.getY() + 2.0, playerBox.minZ, playerBox.maxX, HoleSnapModule.mc.player.getY() + 2.5, playerBox.maxZ).move(dir.x * 0.5, 0.0, dir.z * 0.5);
                if (!HoleSnapModule.mc.level.noCollision(wallCheck)) {
                    this.unreachableHole = this.hole;
                    this.hole = null;
                    this.startingHole = null;
                    this.jumpAttempts = 0;
                    if (++this.giveUpCount >= 3) {
                        Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                        this.setToggled(false);
                        return;
                    }
                    this.pickTarget();
                    return;
                }
                HoleSnapModule.mc.options.keyJump.setDown(true);
                this.jumpPressed = true;
                this.jumpWaitTicks = 0;
                ++this.jumpAttempts;
                if (this.jumpAttempts > 5) {
                    HoleSnapModule.mc.options.keyJump.setDown(false);
                    this.jumpPressed = false;
                    this.jumpAttempts = 0;
                    this.unreachableHole = this.hole;
                    this.hole = null;
                    this.startingHole = null;
                    if (++this.giveUpCount >= 3) {
                        Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                        this.setToggled(false);
                    }
                    return;
                }
            }
            if (this.jumpPressed) {
                ++this.jumpWaitTicks;
                if (!HoleSnapModule.mc.player.onGround() || this.jumpWaitTicks > 10) {
                    HoleSnapModule.mc.options.keyJump.setDown(false);
                    this.jumpPressed = false;
                    this.jumpWaitTicks = 0;
                }
            }
            MovementUtils.moveTowards(event, this.hole.getCenter(), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
            return;
        }
        this.jumpAttempts = 0;
        this.giveUpCount = 0;
        event.setMovement(new Vec3(0.0, event.getMovement().y, 0.0));
        ((Vec3dAccessor)HoleSnapModule.mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor)HoleSnapModule.mc.player.getDeltaMovement()).setZ(0.0);
        event.setCancelled(true);
        if (Night.MODULE_MANAGER.getModule(StepModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(StepModule.class).setToggled(false);
        }
        if (Night.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(SpeedModule.class).setToggled(false);
        }
        if (Night.MODULE_MANAGER.getModule(TickShiftModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(TickShiftModule.class).setToggled(false);
        }
        if (this.fillHole.getValue() && this.step.getValue() && this.startingHole != null) {
            this.pendingFillHole = this.startingHole.box();
            this.fillWaitTicks = 0;
            this.queueFillPositions(this.startingHole.box());
            this.hole = null;
            this.startingHole = null;
            return;
        }
        this.setToggled(false);
        this.hole = null;
        this.startingHole = null;
    }

    private boolean isHoleReachable(AABB box) {
        BlockPos head;
        int holeFloorY;
        BlockPos holePos = BlockPos.containing((double)(box.minX + 0.5), (double)box.minY, (double)(box.minZ + 0.5));
        int playerFloorY = (int)Math.floor(HoleSnapModule.mc.player.getY());
        if (playerFloorY > (holeFloorY = holePos.getY())) {
            for (int y = holeFloorY + 3; y <= playerFloorY + 1; ++y) {
                if (HoleSnapModule.mc.level.getBlockState(new BlockPos(holePos.getX(), y, holePos.getZ())).canBeReplaced()) continue;
                return false;
            }
            return true;
        }
        return holeFloorY - playerFloorY < 2 || HoleSnapModule.mc.level.getBlockState(head = HoleSnapModule.mc.player.blockPosition().above(2)).canBeReplaced() && HoleSnapModule.mc.level.getBlockState(head.above()).canBeReplaced();
    }

    private boolean pickTarget() {
        List<HoleUtils.Hole> filtered;
        HoleUtils.Hole starting;
        HoleUtils.Hole detected = this.currentHole();
        HoleUtils.Hole hole = starting = detected != null && this.lastUsedHole != null && detected.box().equals((Object)this.lastUsedHole) ? null : detected;
        List<HoleUtils.Hole> holes = starting != null && !this.step.getValue() ? List.of(starting) : (starting != null ? this.prioritizeSingle(this.getHoles().stream().filter(h -> !h.box().equals((Object)starting.box())).toList()) : this.getHoles());
        if (this.lastUsedHole != null && !(filtered = holes.stream().filter(h -> !h.box().equals((Object)this.lastUsedHole)).toList()).isEmpty()) {
            holes = filtered;
        }
        if (this.unreachableHole != null) {
            holes = holes.stream().filter(h -> !h.box().equals((Object)this.unreachableHole)).toList();
        }
        if (holes.isEmpty()) {
            this.setToggled(false);
            return false;
        }
        this.startingHole = starting;
        this.hole = holes.get(0).box();
        if (!this.hole.equals((Object)this.lastUsedHole)) {
            this.lastUsedHole = this.hole;
        }
        return true;
    }

    private boolean isHoleStillValid(AABB box) {
        HoleUtils.Hole quadHole;
        HoleUtils.Hole doubleHole;
        BlockPos origin = BlockPos.containing((double)(box.minX + 0.5), (double)box.minY, (double)(box.minZ + 0.5));
        HoleUtils.Hole single = HoleUtils.getSingleHole(origin, 1.0);
        if (single != null && single.box().equals((Object)box)) {
            return true;
        }
        if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(origin, 1.0)) != null && doubleHole.box().equals((Object)box)) {
            return true;
        }
        return this.quadHoles.getValue() && (quadHole = HoleUtils.getQuadHole(origin, 1.0)) != null && quadHole.box().equals((Object)box);
    }

    private HoleUtils.Hole currentHole() {
        BlockPos pos = HoleSnapModule.mc.player.blockPosition();
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.5;
        double cz = (double)pos.getZ() + 0.5;
        for (int dx = -1; dx <= 0; ++dx) {
            for (int dz = -1; dz <= 0; ++dz) {
                HoleUtils.Hole quadHole;
                HoleUtils.Hole doubleHole;
                BlockPos origin = pos.offset(dx, 0, dz);
                HoleUtils.Hole single = HoleUtils.getSingleHole(origin, 1.0);
                if (single != null && single.box().contains(cx, cy, cz)) {
                    return single;
                }
                if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(origin, 1.0)) != null && doubleHole.box().contains(cx, cy, cz)) {
                    return doubleHole;
                }
                if (!this.quadHoles.getValue() || (quadHole = HoleUtils.getQuadHole(origin, 1.0)) == null || !quadHole.box().contains(cx, cy, cz)) continue;
                return quadHole;
            }
        }
        return null;
    }

    private List<HoleUtils.Hole> prioritizeSingle(List<HoleUtils.Hole> holes) {
        return holes.stream().sorted(Comparator.comparingInt(h -> h.type() == HoleUtils.HoleType.SINGLE ? 0 : 1)).toList();
    }

    private void queueFillPositions(AABB box) {
        int minX = (int)Math.floor(box.minX);
        int maxX = (int)Math.ceil(box.maxX) - 1;
        int minZ = (int)Math.floor(box.minZ);
        int maxZ = (int)Math.ceil(box.maxZ) - 1;
        int y = (int)box.minY;
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                this.fillQueue.add(new BlockPos(x, y, z));
            }
        }
    }

    private void fillNext() {
        BlockPos pos = this.fillQueue.peek();
        if (pos == null) {
            return;
        }
        if (!WorldUtils.isPlaceable(pos)) {
            this.fillQueue.poll();
            return;
        }
        boolean altSlots = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup");
        int slot = InventoryUtils.findHardestBlock(0, altSlots ? 35 : 8);
        if (slot == -1) {
            this.fillQueue.clear();
            return;
        }
        int previousSlot = HoleSnapModule.mc.player.getInventory().getSelectedSlot();
        Direction direction = WorldUtils.getDirection(pos, this.filledPositions, this.strictDirection.getValue());
        if (direction == null) {
            BlockPos supportPosition = pos.offset(0, -1, 0);
            if (!WorldUtils.isPlaceable(supportPosition)) {
                this.fillQueue.poll();
                return;
            }
            Direction supportDirection = WorldUtils.getDirection(supportPosition, this.filledPositions, this.strictDirection.getValue());
            if (supportDirection == null) {
                this.fillQueue.poll();
                return;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
            boolean supportPlaced = WorldUtils.placeBlock(supportPosition, supportDirection, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
            if (!supportPlaced) {
                return;
            }
            this.filledPositions.add(supportPosition);
            direction = WorldUtils.getDirection(pos, this.filledPositions, this.strictDirection.getValue());
            if (direction == null) {
                return;
            }
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
        boolean placed = WorldUtils.placeBlock(pos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        if (placed) {
            this.filledPositions.add(pos);
            this.fillQueue.poll();
        }
    }

    private List<HoleUtils.Hole> getHoles() {
        ArrayList<HoleUtils.Hole> holes = new ArrayList<HoleUtils.Hole>();
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue()); ++i) {
            HoleUtils.Hole quadHole;
            HoleUtils.Hole doubleHole;
            BlockPos position = HoleSnapModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if ((double)position.getY() > HoleSnapModule.mc.player.getY()) continue;
            HoleUtils.Hole singleHole = HoleUtils.getSingleHole(position, 1.0);
            if (singleHole != null) {
                holes.add(singleHole);
                continue;
            }
            if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(position, 1.0)) != null) {
                holes.add(doubleHole);
                continue;
            }
            if (!this.quadHoles.getValue() || (quadHole = HoleUtils.getQuadHole(position, 1.0)) == null) continue;
            holes.add(quadHole);
        }
        return holes.stream().sorted(Comparator.comparing(h -> HoleSnapModule.mc.player.distanceToSqr(h.box().getCenter().x, h.box().getCenter().y, h.box().getCenter().z))).toList();
    }
}

