/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.CraftingMenu
 *  net.minecraft.world.item.BedItem
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult.Type;


import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseInputEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="BedAura", description="Automatically places and detonates beds in the Nether and End.", category=Module.Category.COMBAT)
public class BedAuraModule
extends Module {
    public CategorySetting targetingCategory = new CategorySetting("Targeting", "Settings related to target selection.");
    public ModeSetting targetMode = new ModeSetting("Target", "Mode", "How targets are sorted.", new CategorySetting.Visibility(this.targetingCategory), "Closest", new String[]{"Closest", "Furthest", "Health"});
    public NumberSetting targetRange = new NumberSetting("TargetRange", "TargetRange", "The range within which players are considered targets.", new CategorySetting.Visibility(this.targetingCategory), 8.0, 1.0, 16.0);
    public NumberSetting maxTargets = new NumberSetting("MaxTargets", "MaxTargets", "Maximum number of targets to evaluate simultaneously.", new CategorySetting.Visibility(this.targetingCategory), 3.0, 1.0, 8.0);
    public NumberSetting bedRange = new NumberSetting("BedRange", "BedRange", "Search cube radius around the target to look for placement spots.", new CategorySetting.Visibility(this.targetingCategory), 4.0, 1.0, 8.0);
    public BooleanSetting ignoreFriends = new BooleanSetting("IgnoreFriends", "Ignore friends when targeting.", new CategorySetting.Visibility(this.targetingCategory), true);
    public CategorySetting damageCategory = new CategorySetting("Damage", "Damage calculation and safety settings.");
    public NumberSetting minDamage = new NumberSetting("MinDamage", "MinDamage", "Minimum predicted damage to target.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting maxSelfDamage = new NumberSetting("MaxSelfDamage", "MaxSelfDamage", "Maximum allowed damage to yourself.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting minHealth = new NumberSetting("MinHealth", "MinHealth", "Do not act if your health (+absorption) is at or below this.", new CategorySetting.Visibility(this.damageCategory), 4.0, 0.0, 36.0);
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Cancel if self damage drops health below MinHealth.", new CategorySetting.Visibility(this.damageCategory), true);
    public NumberSetting balance = new NumberSetting("Balance", "Balance", "Minimum target to self damage ratio (0 = off).", new CategorySetting.Visibility(this.damageCategory), 0.0, 0.0, 10.0);
    public BooleanSetting predict = new BooleanSetting("Predict", "Extrapolate target velocity for damage estimation.", new CategorySetting.Visibility(this.damageCategory), true);
    public CategorySetting behaviorCategory = new CategorySetting("Behavior", "Settings for placement and detonation behavior.");
    public NumberSetting delay = new NumberSetting("Delay", "Delay", "Delay in milliseconds between actions.", new CategorySetting.Visibility(this.behaviorCategory), 100, 0, 500);
    public NumberSetting range = new NumberSetting("Range", "Range", "Direct reach for placing and detonating beds.", new CategorySetting.Visibility(this.behaviorCategory), 4.5, 1.0, 6.0);
    public NumberSetting wallsRange = new NumberSetting("WallsRange", "WallsRange", "Reach through walls / obstacles.", new CategorySetting.Visibility(this.behaviorCategory), 4.0, 0.0, 6.0);
    public ModeSetting swapMode = new ModeSetting("SwapMode", "SwapMode", "How to swap to the bed and non-bed item.", new CategorySetting.Visibility(this.behaviorCategory), "Silent", new String[]{"Silent", "Normal", "Alt"});
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotate towards bed before placing and detonating.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Enforce strict raycast line of sight on placement face.", new CategorySetting.Visibility(this.behaviorCategory), false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing beds without a solid supporting block below.", new CategorySetting.Visibility(this.behaviorCategory), false);
    public BooleanSetting placeOnFeet = new BooleanSetting("PlaceOnFeet", "Allow bed head to overlap target entity hitbox for maximum damage.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or consuming items.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public CategorySetting autoCraftCategory = new CategorySetting("AutoCraft", "Automated bed crafting from wool and planks.");
    public BooleanSetting autoCraft = new BooleanSetting("AutoCraft", "Enable automated bed crafting.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public BindSetting autoCraftBind = new BindSetting("AutoCraftBind", "Keybind to toggle AutoCraft.", new CategorySetting.Visibility(this.autoCraftCategory), -1).disableHoldModes();
    public BooleanSetting autoPlaceTable = new BooleanSetting("AutoPlace", "Place and open a crafting table when out of beds.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public BooleanSetting autoCloseTable = new BooleanSetting("AutoClose", "Close the crafting table GUI automatically when crafting is finished.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public CategorySetting renderCategory = new CategorySetting("Render", "Placement preview rendering.");
    public BooleanSetting renderPreview = new BooleanSetting("Render", "Render preview box at target bed placement.", new CategorySetting.Visibility(this.renderCategory), true);
    public BooleanSetting interpolate = new BooleanSetting("Interpolate", "Smooth linear grow-in animation for preview box.", new CategorySetting.Visibility(this.renderCategory), true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Mode", "Render mode for preview box.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "FillColor", "Color used for fill rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "OutlineColor", "Color used for outline rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultOutlineColor());
    private static final int RECOMPUTE_INTERVAL_TICKS = 10;
    private int recomputeTicks = 0;
    private BlockPos currentFoot = null;
    private Direction currentDir = null;
    private Player currentTarget = null;
    private float currentEstimatedDamage = 0.0f;
    private BlockPos currentHead = null;
    private boolean currentAdopt = false;
    private long lastActionMs = 0L;
    private boolean bedPlacedThisCycle = false;
    private BlockPos placedBedPos = null;
    private BlockPos placedBedHeadPos = null;
    private PlacementCandidate currentPlacement = null;
    private final List<Player> targets = new ArrayList<Player>();
    private long placementRenderMs = 0L;
    private static final long PLACEMENT_ANIM_MS = 350L;
    private BlockPos lastRenderedPos = null;
    private boolean autoCraftRunning = false;
    private int autoCraftTicks = 0;
    private BlockPos placedTablePos = null;
    private boolean tableOpenSent = false;
    private boolean lastHadBeds = true;
    private static final int CRAFTING_RESULT_SLOT = 0;
    private static final int[] WOOL_GRID_SLOTS = new int[]{1, 2, 3};
    private static final int[] PLANK_GRID_SLOTS = new int[]{4, 5, 6};
    private static final int CRAFTING_CONTAINER_SIZE = 10;

    @Override
    public void onEnable() {
        this.clearSelection();
        this.recomputeTicks = 0;
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.autoCraftRunning = false;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    @Override
    public void onDisable() {
        this.clearSelection();
        this.autoCraftRunning = false;
        this.placedTablePos = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (BedAuraModule.mc.player == null || BedAuraModule.mc.level == null) {
            return;
        }
        if (this.autoCraft.getValue() && this.autoPlaceTable.getValue()) {
            boolean hasBeds;
            boolean bl = hasBeds = this.countBeds() > 0;
            if (!hasBeds && this.lastHadBeds) {
                this.startAutoCraft();
            }
            this.lastHadBeds = hasBeds;
        }
        if (this.autoCraftRunning) {
            this.handleAutoCraftTick();
        }
        this.runBedCycle();
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (BedAuraModule.mc.gui.screen() != null) {
            return;
        }
        if (this.autoCraftBind.getValue() != -1 && event.getKey() == this.autoCraftBind.getValue()) {
            this.startAutoCraft();
            Night.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (BedAuraModule.mc.gui.screen() != null) {
            return;
        }
        if (this.autoCraftBind.getValue() != -1 && event.getButton() == this.autoCraftBind.getValue()) {
            this.startAutoCraft();
            Night.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    private void runBedCycle() {
        boolean hasBed;
        ResourceKey dim = BedAuraModule.mc.player.level().dimension();
        if (dim != Level.NETHER && dim != Level.END) {
            this.clearSelection();
            return;
        }
        if (this.pauseOnEat.getValue() && BedAuraModule.mc.player.isUsingItem() && (BedAuraModule.mc.player.getUseItem().has(DataComponents.FOOD) || BedAuraModule.mc.player.getUseItem().has(DataComponents.POTION_CONTENTS))) {
            return;
        }
        float totalHealth = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
        if (totalHealth <= this.minHealth.getValue().floatValue()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastActionMs < this.delay.getValue().longValue()) {
            return;
        }
        if (this.bedPlacedThisCycle) {
            this.submitDetonate();
            return;
        }
        boolean bl = hasBed = this.findBedSlot() != -1;
        if (!hasBed) {
            if (this.autoCraft.getValue() && this.countBeds() == 0 && !this.autoCraftRunning) {
                this.startAutoCraft();
            }
            this.clearSelection();
            return;
        }
        this.updateTargets();
        if (this.targets.isEmpty()) {
            this.clearSelection();
            return;
        }
        if (this.recomputeTicks <= 0) {
            this.recomputeTicks = 10;
            ExistingBed existing = this.findExistingBed();
            if (existing != null) {
                this.currentFoot = existing.pos();
                this.currentHead = this.findOtherBedHalf(existing.pos());
                this.currentDir = null;
                this.currentTarget = existing.target();
                this.currentAdopt = true;
                this.currentEstimatedDamage = existing.damage();
                this.bedPlacedThisCycle = true;
                this.placedBedPos = this.currentFoot;
                this.placedBedHeadPos = this.currentHead;
                this.currentPlacement = null;
                this.submitDetonate();
                return;
            }
            JointPlacement jointPlacement = this.searchPlacement();
            if (jointPlacement == null) {
                this.clearSelection();
                return;
            }
            this.currentFoot = jointPlacement.foot();
            this.currentDir = jointPlacement.dir();
            this.currentTarget = jointPlacement.target();
            this.currentEstimatedDamage = jointPlacement.estimatedDamage();
            this.currentAdopt = false;
            this.currentHead = this.currentFoot.relative(this.currentDir);
        } else {
            --this.recomputeTicks;
            if (this.currentFoot != null && !this.currentAdopt && !this.currentSelectionStillGood()) {
                this.recomputeTicks = 0;
            }
        }
        if (this.currentFoot == null || this.currentDir == null || this.currentTarget == null) {
            this.clearSelection();
            return;
        }
        this.currentPlacement = new PlacementCandidate(this.currentFoot, this.currentDir, this.currentEstimatedDamage);
        this.submitPlaceBed(this.currentFoot, this.currentDir);
    }

    private void submitPlaceBed(BlockPos pos, Direction dir) {
        BlockHitResult hit = this.computeBedHit(pos);
        if (hit == null) {
            this.clearSelection();
            this.recomputeTicks = 4;
            return;
        }
        float yaw = dir.toYRot();
        float[] lookRots = RotationUtils.getRotations(hit.getLocation());
        float pitch = lookRots[1];
        Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
        if (this.executePlaceBed(pos, hit)) {
            this.bedPlacedThisCycle = true;
            this.placedBedPos = pos;
            this.placedBedHeadPos = pos.relative(dir);
            this.lastActionMs = System.currentTimeMillis();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void submitDetonate() {
        String mode;
        BlockHitResult hit;
        if (this.placedBedPos == null) {
            this.bedPlacedThisCycle = false;
            return;
        }
        BlockPos target = this.placedBedPos;
        if (this.placedBedHeadPos != null && this.currentTarget != null) {
            float dAnchor = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedPos), this.currentTarget);
            float dHead = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedHeadPos), this.currentTarget);
            if (dHead > dAnchor) {
                target = this.placedBedHeadPos;
            }
        }
        if ((hit = this.computeBedFace(target)) == null) {
            this.bedPlacedThisCycle = false;
            this.placedBedPos = null;
            this.placedBedHeadPos = null;
            this.currentPlacement = null;
            this.recomputeTicks = 4;
            return;
        }
        int detonateSlot = this.findDetonateSlot();
        float[] rots = RotationUtils.getRotations(hit.getLocation());
        Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
        int prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        boolean swapped = false;
        String string = mode = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
        if (detonateSlot != Integer.MIN_VALUE) {
            swapped = InventoryUtils.switchSlot(mode, detonateSlot, prevSlot);
        }
        try {
            BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, hit);
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        finally {
            if (swapped) {
                InventoryUtils.switchBack(mode, detonateSlot, prevSlot);
            }
        }
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.currentPlacement = null;
        this.lastActionMs = System.currentTimeMillis();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean executePlaceBed(BlockPos pos, BlockHitResult hit) {
        boolean swapped;
        int prevSlot;
        String mode;
        int bedSlot;
        block11: {
            Input real;
            boolean needSneak;
            bedSlot = this.findBedSlot();
            if (bedSlot == -1) {
                return false;
            }
            mode = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
            swapped = InventoryUtils.switchSlot(mode, bedSlot, prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot());
            if (!swapped && mc.getConnection() != null) {
                mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(BedAuraModule.mc.player.getInventory().getSelectedSlot()));
            }
            boolean bl = needSneak = WorldUtils.isInteractable(BedAuraModule.mc.level.getBlockState(hit.getBlockPos())) && !BedAuraModule.mc.player.isShiftKeyDown();
            if (needSneak) {
                real = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                Input sneakInput = new Input(real.forward(), real.backward(), real.left(), real.right(), real.jump(), true, real.sprint());
                mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(sneakInput));
                LocalPlayer localPlayer = BedAuraModule.mc.player;
                if (localPlayer instanceof ClientPlayerEntityAccessor) {
                    ClientPlayerEntityAccessor accessor = (ClientPlayerEntityAccessor)localPlayer;
                    accessor.setLastSentInput(sneakInput);
                }
            }
            try {
                BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, hit);
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (!needSneak) break block11;
                real = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            }
            catch (Throwable throwable) {
                if (needSneak) {
                    Input real2 = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                    mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(real2));
                    LocalPlayer localPlayer = BedAuraModule.mc.player;
                    if (localPlayer instanceof ClientPlayerEntityAccessor) {
                        ClientPlayerEntityAccessor accessor = (ClientPlayerEntityAccessor)localPlayer;
                        accessor.setLastSentInput(real2);
                    }
                }
                if (swapped) {
                    InventoryUtils.switchBack(mode, bedSlot, prevSlot);
                }
                throw throwable;
            }
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(real));
            LocalPlayer localPlayer = BedAuraModule.mc.player;
            if (localPlayer instanceof ClientPlayerEntityAccessor) {
                ClientPlayerEntityAccessor accessor = (ClientPlayerEntityAccessor)localPlayer;
                accessor.setLastSentInput(real);
            }
        }
        if (swapped) {
            InventoryUtils.switchBack(mode, bedSlot, prevSlot);
        }
        return true;
    }

    private boolean canSeePlacementFace(Vec3 eye, Vec3 hitVec, BlockPos targetBlock) {
        BlockHitResult result = BedAuraModule.mc.level.clip(new ClipContext(eye, hitVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player));
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (result instanceof BlockHitResult) {
            BlockHitResult bhr = result;
            return bhr.getBlockPos().equals((Object)targetBlock);
        }
        return false;
    }

    private BlockHitResult computeBedHit(BlockPos pos) {
        double maxReach = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(pos, maxReach)) {
            return null;
        }
        BlockPos support = pos.below();
        boolean hasFloor = !BedAuraModule.mc.level.getBlockState(support).canBeReplaced();
        Vec3 eye = BedAuraModule.mc.player.getEyePosition();
        if (hasFloor) {
            Vec3 hitVec = new Vec3((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            if (this.strictDirection.getValue() && (eye.y < (double)pos.getY() || !this.canSeePlacementFace(eye, hitVec, support))) {
                return null;
            }
            return new BlockHitResult(hitVec, Direction.UP, support, false);
        }
        if (!this.airPlace.getValue()) {
            return null;
        }
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (BedAuraModule.mc.level.getBlockState(neighbor).canBeReplaced()) continue;
            Direction placeDir = dir.getOpposite();
            Vec3 hitVec = new Vec3((double)pos.getX() + 0.5 - (double)dir.getStepX() * 0.5, (double)pos.getY() + 0.5 - (double)dir.getStepY() * 0.5, (double)pos.getZ() + 0.5 - (double)dir.getStepZ() * 0.5);
            if (this.strictDirection.getValue() && !this.canSeePlacementFace(eye, hitVec, neighbor)) continue;
            return new BlockHitResult(hitVec, placeDir, neighbor, false);
        }
        if (this.strictDirection.getValue()) {
            return null;
        }
        Vec3 hitVec = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        return new BlockHitResult(hitVec, Direction.UP, pos, false);
    }

    private BlockHitResult computeBedFace(BlockPos pos) {
        Vec3 eye = BedAuraModule.mc.player.getEyePosition();
        double reach = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        BlockHitResult best = null;
        double bestDist = Double.MAX_VALUE;
        BlockHitResult fallback = null;
        double fallbackDist = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            BlockHitResult bhr;
            BlockPos hitP;
            BlockHitResult rc;
            double dist;
            Vec3 normal = new Vec3((double)dir.getStepX(), (double)dir.getStepY(), (double)dir.getStepZ());
            Vec3 face = center.add(normal.scale(0.5));
            if (eye.subtract(face).dot(normal) <= 0.0 || (dist = face.distanceTo(eye)) > reach) continue;
            if (dist < fallbackDist) {
                fallbackDist = dist;
                fallback = new BlockHitResult(face, dir, pos, false);
            }
            if ((rc = BedAuraModule.mc.level.clip(new ClipContext(eye, face, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player))).getType() == HitResult.Type.BLOCK && rc instanceof BlockHitResult && !(hitP = (bhr = rc).getBlockPos()).equals((Object)pos) && (this.placedBedPos == null || !hitP.equals((Object)this.placedBedPos)) && (this.placedBedHeadPos == null || !hitP.equals((Object)this.placedBedHeadPos)) || !(dist < bestDist)) continue;
            bestDist = dist;
            best = new BlockHitResult(face, dir, pos, false);
        }
        return best != null ? best : fallback;
    }

    private JointPlacement searchPlacement() {
        int r = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        double minD = this.minDamage.getValue().doubleValue();
        double maxD = this.maxSelfDamage.getValue().doubleValue();
        double exReach = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        JointPlacement best = null;
        double bestScore = -1.0;
        Vec3 eye = BedAuraModule.mc.player.getEyePosition();
        double reachEye = exReach + 0.5;
        for (Player targetPlayer : this.targets) {
            int dzHi;
            int dzLo;
            int dyHi;
            int dyLo;
            int dxHi;
            int dxLo;
            BlockPos center = targetPlayer.blockPosition();
            Vec3 targetCenter = targetPlayer.getBoundingBox().getCenter();
            if (Vec3.atCenterOf((Vec3i)center).distanceTo(eye) > (double)r * Math.sqrt(3.0) + reachEye || (dxLo = Math.max(-r, (int)Math.floor(eye.x - reachEye - (double)center.getX()))) > (dxHi = Math.min(r, (int)Math.ceil(eye.x + reachEye - (double)center.getX()))) || (dyLo = Math.max(-r, (int)Math.floor(eye.y - reachEye - (double)center.getY()))) > (dyHi = Math.min(r, (int)Math.ceil(eye.y + reachEye - (double)center.getY()))) || (dzLo = Math.max(-r, (int)Math.floor(eye.z - reachEye - (double)center.getZ()))) > (dzHi = Math.min(r, (int)Math.ceil(eye.z + reachEye - (double)center.getZ())))) continue;
            for (int dx = dxLo; dx <= dxHi; ++dx) {
                for (int dy = dyLo; dy <= dyHi; ++dy) {
                    for (int dz = dzLo; dz <= dzHi; ++dz) {
                        BlockPos foot = center.offset(dx, dy, dz);
                        if (Vec3.atCenterOf((Vec3i)foot).distanceTo(eye) > reachEye || !this.footPlaceable(foot, exReach)) continue;
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            float selfDmg;
                            Vec3 detonateCenter;
                            BlockPos head = foot.relative(dir);
                            if (!this.headPlaceable(head)) continue;
                            Vec3 footCenter = Vec3.atCenterOf((Vec3i)foot);
                            Vec3 headCenter = Vec3.atCenterOf((Vec3i)head);
                            float dFootTarget = this.computeDamage(footCenter, targetPlayer);
                            float dHeadTarget = this.computeDamage(headCenter, targetPlayer);
                            boolean useHead = dHeadTarget >= dFootTarget;
                            Vec3 vec3 = detonateCenter = useHead ? headCenter : footCenter;
                            float dmg = Math.max(dFootTarget, dHeadTarget);
                            if ((double)dmg < minD || (double)(selfDmg = this.computeDamage(detonateCenter, (Player)BedAuraModule.mc.player)) > maxD) continue;
                            float myHp = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
                            if (this.antiSuicide.getValue() && myHp - selfDmg <= this.minHealth.getValue().floatValue() || this.balance.getValue().doubleValue() > 0.0 && selfDmg > 0.0f && (double)(dmg / selfDmg) < this.balance.getValue().doubleValue()) continue;
                            double score = dmg;
                            if (headCenter.distanceTo(targetCenter) < footCenter.distanceTo(targetCenter)) {
                                score += 0.01;
                            }
                            if (best != null && !(score > bestScore)) continue;
                            best = new JointPlacement(foot, dir, targetPlayer, dmg);
                            bestScore = score;
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean footPlaceable(BlockPos foot, double reach) {
        if (!this.withinReach(foot, reach)) {
            return false;
        }
        if (!BedAuraModule.mc.level.getBlockState(foot).canBeReplaced()) {
            return false;
        }
        if (!this.airPlace.getValue() && BedAuraModule.mc.level.getBlockState(foot.below()).canBeReplaced()) {
            return false;
        }
        return !this.strictDirection.getValue() || this.computeBedHit(foot) != null;
    }

    private boolean headPlaceable(BlockPos head) {
        if (!BedAuraModule.mc.level.getBlockState(head).canBeReplaced()) {
            return false;
        }
        if (this.placeOnFeet.getValue()) {
            return true;
        }
        return !this.entityAt(head);
    }

    private boolean entityAt(BlockPos pos) {
        AABB box = new AABB(pos);
        for (Entity e : BedAuraModule.mc.level.entitiesForRendering()) {
            if (e == BedAuraModule.mc.player || e instanceof ItemEntity || !e.isAlive() || !e.getBoundingBox().intersects(box)) continue;
            return true;
        }
        return false;
    }

    private boolean currentSelectionStillGood() {
        float dHead;
        if (this.currentFoot == null || this.currentDir == null || this.currentTarget == null) {
            return false;
        }
        if (!this.currentTarget.isAlive() || this.currentTarget.getHealth() <= 0.0f) {
            return false;
        }
        BlockPos head = this.currentFoot.relative(this.currentDir);
        double exReach = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(this.currentFoot, exReach) && !this.withinReach(head, exReach)) {
            return false;
        }
        if (this.strictDirection.getValue() && this.computeBedHit(this.currentFoot) == null) {
            return false;
        }
        Vec3 footCenter = Vec3.atCenterOf((Vec3i)this.currentFoot);
        Vec3 headCenter = Vec3.atCenterOf((Vec3i)head);
        float dFoot = this.computeDamage(footCenter, this.currentTarget);
        float dmg = Math.max(dFoot, dHead = this.computeDamage(headCenter, this.currentTarget));
        if ((double)dmg < this.minDamage.getValue().doubleValue()) {
            return false;
        }
        Vec3 detonateCenter = dHead >= dFoot ? headCenter : footCenter;
        float selfDmg = this.computeDamage(detonateCenter, (Player)BedAuraModule.mc.player);
        if ((double)selfDmg > this.maxSelfDamage.getValue().doubleValue()) {
            return false;
        }
        this.currentEstimatedDamage = dmg;
        return true;
    }

    private ExistingBed findExistingBed() {
        double exReach = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        int r = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        ExistingBed best = null;
        for (Player targetPlayer : this.targets) {
            BlockPos center = targetPlayer.blockPosition();
            Vec3 targetCenter = targetPlayer.getBoundingBox().getCenter();
            for (int dx = -r; dx <= r; ++dx) {
                for (int dy = -r; dy <= r; ++dy) {
                    for (int dz = -r; dz <= r; ++dz) {
                        float dmg;
                        float selfDmg;
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (!(BedAuraModule.mc.level.getBlockState(pos).getBlock() instanceof BedBlock) || Vec3.atCenterOf((Vec3i)pos).distanceTo(targetCenter) > 6.0 || !this.withinReach(pos, exReach) || (double)(selfDmg = this.computeDamage(Vec3.atCenterOf((Vec3i)pos), (Player)BedAuraModule.mc.player)) > this.maxSelfDamage.getValue().doubleValue() || (double)(dmg = this.computeDamage(Vec3.atCenterOf((Vec3i)pos), targetPlayer)) < this.minDamage.getValue().doubleValue() || best != null && !(dmg > best.damage())) continue;
                        best = new ExistingBed(pos, targetPlayer, dmg);
                    }
                }
            }
        }
        return best;
    }

    private float computeDamage(Vec3 explosionCenter, Player target) {
        if (this.predict.getValue()) {
            Vec3 predictedPos = target.position().add(target.getDeltaMovement());
            AABB predictedBox = target.getBoundingBox().move(predictedPos.subtract(target.position()));
            return DamageUtils.getDamage((Entity)target, predictedBox, explosionCenter, 5.0f, null, false);
        }
        return DamageUtils.getDamage((Entity)target, target.getBoundingBox(), explosionCenter, 5.0f, null, false);
    }

    private boolean withinReach(BlockPos pos, double reach) {
        return Vec3.atCenterOf((Vec3i)pos).distanceTo(BedAuraModule.mc.player.getEyePosition()) <= reach + 0.5;
    }

   private void updateTargets() {
      this.targets.clear();
      Vec3 eye = mc.player.getEyePosition();
      List<Player> found = new ArrayList<>();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Player p
            && p != mc.player
            && p.isAlive()
            && !(p.getHealth() <= 0.0F)
            && (!this.ignoreFriends.getValue() || !Night.FRIEND_MANAGER.contains(p.getName().getString()))
            && (
               !(Vec3.atCenterOf(p.blockPosition()).distanceTo(eye) > this.targetRange.getValue().doubleValue())
                  || !(p.distanceTo(mc.player) > this.targetRange.getValue().doubleValue())
            )) {
            found.add(p);
         }
      }
      Comparator<Player> comparator = switch (this.targetMode.getValue()) {
         case "Furthest" -> Comparator.<Player>comparingDouble(px -> px.distanceTo(mc.player)).reversed();
         case "Health" -> Comparator.comparingDouble(LivingEntity::getHealth);
         default -> Comparator.comparingDouble(px -> px.distanceTo(mc.player));
      };
      found.sort(comparator);
      int max = this.maxTargets.getValue().intValue();

      for (Player p : found) {
         if (this.targets.size() >= max) {
            break;
         }

         this.targets.add(p);
      }
   }

    private BlockPos findOtherBedHalf(BlockPos foot) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = foot.relative(d);
            if (!(BedAuraModule.mc.level.getBlockState(neighbour).getBlock() instanceof BedBlock)) continue;
            return neighbour;
        }
        return null;
    }

    private int findBedSlot() {
        boolean wholeInv = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> isBed = s -> !s.isEmpty() && s.getItem() instanceof BedItem;
        if (wholeInv) {
            for (int i = 0; i < 36; ++i) {
                if (!isBed.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
                return i;
            }
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (!isBed.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
            return i;
        }
        return -1;
    }

    private int findDetonateSlot() {
        int i;
        boolean wholeInv = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> safeItem = s -> !s.isEmpty() && !(s.getItem() instanceof BedItem) && !(s.getItem() instanceof BlockItem);
        if (wholeInv) {
            for (i = 0; i < 36; ++i) {
                if (!safeItem.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
                return i;
            }
        } else {
            for (i = 0; i < 9; ++i) {
                if (!safeItem.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
                return i;
            }
        }
        for (i = 0; i < 9; ++i) {
            if (!BedAuraModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        for (i = 0; i < 9; ++i) {
            if (BedAuraModule.mc.player.getInventory().getItem(i).getItem() instanceof BedItem) continue;
            return i;
        }
        return Integer.MIN_VALUE;
    }

    private void clearSelection() {
        this.currentFoot = null;
        this.currentDir = null;
        this.currentTarget = null;
        this.currentEstimatedDamage = 0.0f;
        this.currentHead = null;
        this.currentAdopt = false;
        this.currentPlacement = null;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        boolean doOutline;
        BlockPos head;
        BlockPos pos;
        if (!this.renderPreview.getValue() || BedAuraModule.mc.level == null || BedAuraModule.mc.player == null) {
            return;
        }
        if (this.bedPlacedThisCycle && this.placedBedPos != null) {
            pos = this.placedBedPos;
            BlockPos blockPos = head = this.placedBedHeadPos != null ? this.placedBedHeadPos : this.findOtherBedHalf(pos);
            if (head != null) {
                this.placedBedHeadPos = head;
            }
        } else if (this.currentPlacement != null) {
            pos = this.currentPlacement.pos();
            head = pos.relative(this.currentPlacement.dir());
        } else if (this.currentFoot != null) {
            pos = this.currentFoot;
            head = this.currentHead != null ? this.currentHead : (this.currentDir != null ? this.currentFoot.relative(this.currentDir) : this.findOtherBedHalf(pos));
        } else {
            this.lastRenderedPos = null;
            return;
        }
        if (!pos.equals((Object)this.lastRenderedPos)) {
            this.lastRenderedPos = pos;
            this.placementRenderMs = System.currentTimeMillis();
        }
        float scale = 1.0f;
        if (this.interpolate.getValue()) {
            long elapsed = System.currentTimeMillis() - this.placementRenderMs;
            float t = Math.min(1.0f, (float)elapsed / 350.0f);
            scale = 0.15f + 0.85f * t;
        }
        double bedHeight = 0.5625;
        PoseStack matrices = event.getMatrices();
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = (double)pos.getX() + 1.0;
        double maxY = (double)pos.getY() + 0.5625;
        double maxZ = (double)pos.getZ() + 1.0;
        if (head != null) {
            minX = Math.min(pos.getX(), head.getX());
            minY = Math.min(pos.getY(), head.getY());
            minZ = Math.min(pos.getZ(), head.getZ());
            maxX = (double)Math.max(pos.getX(), head.getX()) + 1.0;
            maxY = (double)Math.max(pos.getY(), head.getY()) + 0.5625;
            maxZ = (double)Math.max(pos.getZ(), head.getZ()) + 1.0;
        }
        double cx = (minX + maxX) * 0.5;
        double cy = (minY + maxY) * 0.5;
        double cz = (minZ + maxZ) * 0.5;
        double halfX = (maxX - minX) * 0.5 * (double)scale;
        double halfY = (maxY - minY) * 0.5 * (double)scale;
        double halfZ = (maxZ - minZ) * 0.5 * (double)scale;
        AABB box = new AABB(cx - halfX, cy - halfY, cz - halfZ, cx + halfX, cy + halfY, cz + halfZ);
        Color fill = this.fillColor.getColor();
        Color outline = this.outlineColor.getColor();
        boolean doFill = this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean bl = doOutline = this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both");
        if (doFill) {
            Renderer3D.renderBox(matrices, box, fill);
        }
        if (doOutline) {
            Renderer3D.renderBoxOutline(matrices, box, outline);
        }
    }

    private void startAutoCraft() {
        if (this.autoCraftRunning) {
            return;
        }
        this.autoCraftRunning = true;
        this.autoCraftTicks = 0;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    private void handleAutoCraftTick() {
        ++this.autoCraftTicks;
        if (this.autoCraftTicks > 200) {
            this.autoCraftRunning = false;
            return;
        }
        boolean craftingOpen = BedAuraModule.mc.player.containerMenu instanceof CraftingMenu;
        if (!craftingOpen) {
            boolean tableThere;
            if (!this.autoPlaceTable.getValue()) {
                this.autoCraftRunning = false;
                return;
            }
            if (this.placedTablePos == null) {
                this.placedTablePos = this.tryPlaceCraftingTable();
                if (this.placedTablePos == null) {
                    this.autoCraftRunning = false;
                    return;
                }
                this.tableOpenSent = false;
                this.autoCraftTicks = 0;
                return;
            }
            boolean bl = tableThere = BedAuraModule.mc.level.getBlockState(this.placedTablePos).getBlock() == Blocks.CRAFTING_TABLE;
            if (tableThere && !this.tableOpenSent) {
                if (this.rotate.getValue()) {
                    float[] rots = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)this.placedTablePos));
                    Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
                }
                BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)this.placedTablePos), Direction.UP, this.placedTablePos, false));
                this.tableOpenSent = true;
                this.autoCraftTicks = 0;
                return;
            }
            if (this.autoCraftTicks > 40) {
                this.autoCraftRunning = false;
                this.placedTablePos = null;
                this.tableOpenSent = false;
            }
            return;
        }
        this.placedTablePos = null;
        this.tableOpenSent = false;
        AbstractContainerMenu handler = BedAuraModule.mc.player.containerMenu;
        if (!this.gridHasMaterials(handler) && !this.fillCraftingGrid(handler)) {
            if (this.autoCloseTable.getValue()) {
                BedAuraModule.mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
            return;
        }
        BedAuraModule.mc.gameMode.handleContainerInput(handler.containerId, 0, 0, ContainerInput.QUICK_MOVE, (Player)BedAuraModule.mc.player);
        if (this.isInventoryFull()) {
            if (this.autoCloseTable.getValue()) {
                BedAuraModule.mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
        }
    }

    private int countBeds() {
        int count = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = BedAuraModule.mc.player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof BedItem)) continue;
            count += stack.getCount();
        }
        return count;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < 36; ++i) {
            if (!BedAuraModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean gridHasMaterials(AbstractContainerMenu handler) {
        for (int slot : WOOL_GRID_SLOTS) {
            if (this.isWool(handler.getSlot(slot).getItem())) continue;
            return false;
        }
        for (int slot : PLANK_GRID_SLOTS) {
            if (this.isPlanks(handler.getSlot(slot).getItem())) continue;
            return false;
        }
        return true;
    }

    private boolean isWool(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.WOOL);
    }

    private boolean isPlanks(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.PLANKS);
    }

    private boolean fillCraftingGrid(AbstractContainerMenu handler) {
        int slot;
        int woolSlot = this.findMenuSlot(handler, this::isWool);
        int plankSlot = this.findMenuSlot(handler, this::isPlanks);
        if (woolSlot == -1 || plankSlot == -1) {
            return false;
        }
        for (int gridSlot : WOOL_GRID_SLOTS) {
            if (this.isWool(handler.getSlot(gridSlot).getItem())) continue;
            slot = this.findMenuSlot(handler, this::isWool);
            if (slot == -1) {
                return false;
            }
            this.placeOneItem(handler, slot, gridSlot);
        }
        for (int gridSlot : PLANK_GRID_SLOTS) {
            if (this.isPlanks(handler.getSlot(gridSlot).getItem())) continue;
            slot = this.findMenuSlot(handler, this::isPlanks);
            if (slot == -1) {
                return false;
            }
            this.placeOneItem(handler, slot, gridSlot);
        }
        return true;
    }

    private void placeOneItem(AbstractContainerMenu handler, int source, int dest) {
        BedAuraModule.mc.gameMode.handleContainerInput(handler.containerId, source, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(handler.containerId, dest, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(handler.containerId, source, 0, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
    }

    private int findMenuSlot(AbstractContainerMenu handler, Predicate<ItemStack> test) {
        for (int i = 10; i < handler.slots.size(); ++i) {
            if (!test.test(handler.getSlot(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    private BlockPos tryPlaceCraftingTable() {
        int tableSlot = -1;
        for (int i = 0; i < 9; ++i) {
            if (BedAuraModule.mc.player.getInventory().getItem(i).getItem() != Blocks.CRAFTING_TABLE.asItem()) continue;
            tableSlot = i;
            break;
        }
        if (tableSlot == -1) {
            return null;
        }
        BlockPos base = BedAuraModule.mc.player.blockPosition();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        int r = (int)Math.ceil(this.range.getValue().doubleValue());
        for (int dx = -r; dx <= r; ++dx) {
            for (int dy = -r; dy <= r; ++dy) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos candidate = base.offset(dx, dy, dz);
                    double distSq = BedAuraModule.mc.player.distanceToSqr((double)candidate.getX() + 0.5, (double)candidate.getY() + 0.5, (double)candidate.getZ() + 0.5);
                    if (distSq > this.range.getValue().doubleValue() * this.range.getValue().doubleValue() || !BedAuraModule.mc.level.getBlockState(candidate).canBeReplaced() || BedAuraModule.mc.level.getBlockState(candidate.below()).canBeReplaced() || new AABB(candidate).intersects(BedAuraModule.mc.player.getBoundingBox()) || !(distSq < bestDistSq)) continue;
                    bestDistSq = distSq;
                    best = candidate;
                }
            }
        }
        if (best == null) {
            return null;
        }
        BlockPos support = best.below();
        BlockHitResult hit = new BlockHitResult(new Vec3((double)best.getX() + 0.5, (double)best.getY(), (double)best.getZ() + 0.5), Direction.UP, support, false);
        int prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Normal", tableSlot, prevSlot);
        BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, hit);
        InventoryUtils.switchBack("Normal", tableSlot, prevSlot);
        return best;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent event) {
        this.clearSelection();
        this.autoCraftRunning = false;
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent event) {
        this.clearSelection();
        this.autoCraftRunning = false;
    }

    public record PlacementCandidate(BlockPos pos, Direction dir, float estimatedDamage) {
    }

    private record ExistingBed(BlockPos pos, Player target, float damage) {
    }

    public record JointPlacement(BlockPos foot, Direction dir, Player target, float estimatedDamage) {
    }
}

