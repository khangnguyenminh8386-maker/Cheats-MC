/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.ChestType
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.visuals.chestscan.ChestScanChain;
import night.modules.impl.visuals.chestscan.ChestScanStore;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.Renderer3D;

@RegisterModule(name="ChestScan", description="Highlights opened chests by contents (empty/partial/full), with optional hopper-chain inference.", category=Module.Category.VISUALS)
public class ChestScanModule
extends Module {
    public NumberSetting scanRadius = new NumberSetting("ScanRadius", "How far (in blocks) to render tracked chests and consider hopper chains.", 64.0, 8.0, 128.0);
    public BooleanSetting hopperChain = new BooleanSetting("HopperChain", "Smart mode to check chests linked to the bottom chest by hoppers.", false);
    public ModeSetting renderMode = new ModeSetting("Mode", "Render mode for chests.", "Both", new String[]{"Both", "Fill", "Outline"});
    public CategorySetting emptyCategory = new CategorySetting("Empty", "Color for empty chests.");
    public ColorSetting emptyFillColor = new ColorSetting("EmptyFillColor", "Fill", "Fill color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 120), false, false));
    public ColorSetting emptyOutlineColor = new ColorSetting("EmptyOutlineColor", "Outline", "Outline color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 255), false, false));
    public CategorySetting partialCategory = new CategorySetting("Partial", "Color for partially filled chests.");
    public ColorSetting partialFillColor = new ColorSetting("PartialFillColor", "Fill", "Fill color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 120), false, false));
    public ColorSetting partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "Outline color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 255), false, false));
    public CategorySetting fullCategory = new CategorySetting("Full", "Color for full chests.");
    public ColorSetting fullFillColor = new ColorSetting("FullFillColor", "Fill", "Fill color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 120), false, false));
    public ColorSetting fullOutlineColor = new ColorSetting("FullOutlineColor", "Outline", "Outline color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 255), false, false));
    private final ChestScanStore store = new ChestScanStore();
    private String lastWorldKey = null;
    private BlockPos lastLookedAtChestPos = null;
    private boolean wasChestMenuOpenLastTick = false;
    private BlockPos openChestPos = null;
    private ChestScanStore.ChestStatus lastSnapshotStatus = null;
    private int chainTicks = 0;
    private Set<BlockPos> lastInferredEmpty = Collections.emptySet();
    private final Map<BlockPos, Long> missingSinceMs = new HashMap<BlockPos, Long>();
    private static final long PRUNE_GRACE_MS = 1500L;

    public ChestScanStore getStore() {
        return this.store;
    }

    @Override
    public void onEnable() {
        this.store.loadForWorld();
        this.lastWorldKey = ChestScanStore.currentWorldKey();
    }

    @Override
    public void onDisable() {
        if (this.openChestPos != null && this.lastSnapshotStatus != null) {
            this.finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
        }
        this.lastWorldKey = null;
        this.lastLookedAtChestPos = null;
        this.wasChestMenuOpenLastTick = false;
        this.openChestPos = null;
        this.lastSnapshotStatus = null;
        this.missingSinceMs.clear();
    }

    private void maybeReloadStoreForWorld() {
        String key = ChestScanStore.currentWorldKey();
        if (!key.equals(this.lastWorldKey)) {
            this.store.loadForWorld();
            this.lastWorldKey = key;
            this.missingSinceMs.clear();
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (ChestScanModule.mc.player == null || ChestScanModule.mc.level == null) {
            return;
        }
        this.maybeReloadStoreForWorld();
        boolean chestMenuOpenNow = ChestScanModule.mc.player.containerMenu instanceof ChestMenu;
        if (!chestMenuOpenNow && ChestScanModule.mc.gui.screen() == null) {
            this.lastLookedAtChestPos = this.resolveLookedAtChestPos(mc);
        }
        if (chestMenuOpenNow && !this.wasChestMenuOpenLastTick) {
            this.openChestPos = this.lastLookedAtChestPos;
            this.lastSnapshotStatus = null;
        }
        if (chestMenuOpenNow) {
            ChestMenu menu = (ChestMenu)ChestScanModule.mc.player.containerMenu;
            this.lastSnapshotStatus = this.computeStatus(menu.getContainer());
        }
        if (!chestMenuOpenNow && this.wasChestMenuOpenLastTick) {
            this.finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
            this.openChestPos = null;
            this.lastSnapshotStatus = null;
        }
        this.wasChestMenuOpenLastTick = chestMenuOpenNow;
        this.tickChainRecompute(mc);
    }

    private void tickChainRecompute(Minecraft mc) {
        if (!this.hopperChain.getValue()) {
            this.lastInferredEmpty = Collections.emptySet();
            return;
        }
        ++this.chainTicks;
        if (this.chainTicks < 20) {
            return;
        }
        this.chainTicks = 0;
        BlockPos center = mc.player.blockPosition();
        int radius = this.scanRadius.getValue().intValue();
        HashSet<BlockPos> tracked = new HashSet<BlockPos>(this.store.positions());
        Map<BlockPos, BlockPos> edges = ChestScanChain.findEdges((Level)mc.level, tracked, center, radius);
        HashMap<BlockPos, ChestScanStore.ChestStatus> real = new HashMap<BlockPos, ChestScanStore.ChestStatus>();
        for (BlockPos pos : tracked) {
            real.put(pos, this.store.get(pos));
        }
        this.lastInferredEmpty = ChestScanChain.inferEmpty(edges, real);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (ChestScanModule.mc.player == null || ChestScanModule.mc.level == null) {
            return;
        }
        double radius = this.scanRadius.getValue().doubleValue();
        double radiusSq = radius * radius;
        BlockPos center = ChestScanModule.mc.player.blockPosition();
        boolean doFill = this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean doOutline = this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both");
        HashSet<BlockPos> renderedPositions = new HashSet<BlockPos>();
        for (BlockPos pos : new ArrayList<BlockPos>(this.store.positions())) {
            ChestScanStore.ChestStatus status;
            if (center.distSqr((Vec3i)pos) > radiusSq || !ChestScanModule.mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;
            if (!(ChestScanModule.mc.level.getBlockState(pos).getBlock() instanceof ChestBlock)) {
                long now = System.currentTimeMillis();
                Long since = this.missingSinceMs.putIfAbsent(pos, now);
                if (since == null || now - since < 1500L) continue;
                this.store.remove(pos);
                this.missingSinceMs.remove(pos);
                continue;
            }
            this.missingSinceMs.remove(pos);
            if (renderedPositions.contains(pos) || (status = this.store.get(pos)) == null) continue;
            AABB box = this.getChestBox(pos, renderedPositions);
            Color fill = this.getFillColor(status);
            Color outline = this.getOutlineColor(status);
            if (doFill) {
                Renderer3D.renderBox(event.getMatrices(), box, fill);
            }
            if (!doOutline) continue;
            Renderer3D.renderBoxOutline(event.getMatrices(), box, outline);
        }
        if (this.hopperChain.getValue()) {
            Color fill = this.emptyFillColor.getColor();
            Color outline = this.emptyOutlineColor.getColor();
            for (BlockPos pos : this.lastInferredEmpty) {
                BlockState state;
                if (this.store.get(pos) != null || center.distSqr((Vec3i)pos) > radiusSq || !ChestScanModule.mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) || renderedPositions.contains(pos) || !((state = ChestScanModule.mc.level.getBlockState(pos)).getBlock() instanceof ChestBlock)) continue;
                AABB box = this.getChestBox(pos, renderedPositions);
                if (doFill) {
                    Renderer3D.renderBox(event.getMatrices(), box, fill);
                }
                if (!doOutline) continue;
                Renderer3D.renderBoxOutline(event.getMatrices(), box, outline);
            }
        }
    }

    private AABB getChestBox(BlockPos pos, Set<BlockPos> renderedPositions) {
        renderedPositions.add(pos);
        BlockState state = ChestScanModule.mc.level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock && state.hasProperty((Property)ChestBlock.TYPE) && state.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction dir = (Direction)state.getValue((Property)ChestBlock.FACING);
            ChestType type = (ChestType)state.getValue((Property)ChestBlock.TYPE);
            Direction connectedDir = type == ChestType.LEFT ? dir.getClockWise() : dir.getCounterClockWise();
            BlockPos other = pos.relative(connectedDir);
            BlockState otherState = ChestScanModule.mc.level.getBlockState(other);
            if (otherState.getBlock() instanceof ChestBlock && otherState.hasProperty((Property)ChestBlock.TYPE) && otherState.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
                renderedPositions.add(other);
                return new AABB((double)Math.min(pos.getX(), other.getX()), (double)Math.min(pos.getY(), other.getY()), (double)Math.min(pos.getZ(), other.getZ()), (double)Math.max(pos.getX(), other.getX()) + 1.0, (double)Math.max(pos.getY(), other.getY()) + 1.0, (double)Math.max(pos.getZ(), other.getZ()) + 1.0);
            }
        }
        return new AABB(pos);
    }

    private Color getFillColor(ChestScanStore.ChestStatus status) {
        return switch (status) {
            default -> throw new MatchException(null, null);
            case ChestScanStore.ChestStatus.EMPTY -> this.emptyFillColor.getColor();
            case ChestScanStore.ChestStatus.PARTIAL -> this.partialFillColor.getColor();
            case ChestScanStore.ChestStatus.FULL -> this.fullFillColor.getColor();
        };
    }

    private Color getOutlineColor(ChestScanStore.ChestStatus status) {
        return switch (status) {
            default -> throw new MatchException(null, null);
            case ChestScanStore.ChestStatus.EMPTY -> this.emptyOutlineColor.getColor();
            case ChestScanStore.ChestStatus.PARTIAL -> this.partialOutlineColor.getColor();
            case ChestScanStore.ChestStatus.FULL -> this.fullOutlineColor.getColor();
        };
    }

    private BlockPos resolveLookedAtChestPos(Minecraft mc) {
        BlockHitResult bhr;
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult) || (bhr = (BlockHitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = bhr.getBlockPos();
        return mc.level.getBlockState(pos).getBlock() instanceof ChestBlock ? pos : null;
    }

    private ChestScanStore.ChestStatus computeStatus(Container container) {
        int total = container.getContainerSize();
        if (total == 0) {
            return ChestScanStore.ChestStatus.EMPTY;
        }
        int filled = 0;
        for (int i = 0; i < total; ++i) {
            if (container.getItem(i).isEmpty()) continue;
            ++filled;
        }
        if (filled == 0) {
            return ChestScanStore.ChestStatus.EMPTY;
        }
        return filled == total ? ChestScanStore.ChestStatus.FULL : ChestScanStore.ChestStatus.PARTIAL;
    }

    private void finalizeChestState(Minecraft mc, BlockPos pos, ChestScanStore.ChestStatus status) {
        if (pos == null || status == null) {
            return;
        }
        this.store.put(pos, status);
        if (mc == null || mc.level == null) {
            return;
        }
        BlockState state = mc.level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock && state.hasProperty((Property)ChestBlock.TYPE) && state.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction dir = (Direction)state.getValue((Property)ChestBlock.FACING);
            ChestType type = (ChestType)state.getValue((Property)ChestBlock.TYPE);
            Direction connectedDir = type == ChestType.LEFT ? dir.getClockWise() : dir.getCounterClockWise();
            BlockPos other = pos.relative(connectedDir);
            if (mc.level.getBlockState(other).getBlock() instanceof ChestBlock) {
                this.store.put(other, status);
            }
        }
    }
}

