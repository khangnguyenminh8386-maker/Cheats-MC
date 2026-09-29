/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="ESP", description="Renders a box ESP around entities that you have selected.", category=Module.Category.VISUALS)
public class ESPModule
extends Module {
    public BooleanSetting players = new BooleanSetting("Players", "Renders the box ESP on player entities.", true);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Renders the box ESP on hostile entities.", true);
    public BooleanSetting animals = new BooleanSetting("Animals", "Renders the box ESP on animal entities.", true);
    public BooleanSetting ambient = new BooleanSetting("Ambient", "Renders the box ESP on ambient entities.", false);
    public BooleanSetting invisibles = new BooleanSetting("Invisibles", "Renders the box ESP on invisible entities.", true);
    public BooleanSetting items = new BooleanSetting("Items", "Renders the box ESP on item entities.", true);
    public BooleanSetting others = new BooleanSetting("Others", "Renders the box ESP on miscellaneous entities.", false);
    public BooleanSetting blocks = new BooleanSetting("Block", "Renders a box ESP on whitelisted blocks (portals, ores, ...).", false);
    public WhitelistSetting blockWhitelist = new WhitelistSetting("Whitelist", "Blocks to highlight.", new BooleanSetting.Visibility(this.blocks, true), WhitelistSetting.Type.BLOCKS);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target entities.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private List<Entity> targetEntities = new ArrayList<Entity>();
    private final Map<Long, List<BlockPos>> matchedBlocks = new HashMap<Long, List<BlockPos>>();
    private final Map<Long, Integer> pendingChunks = new HashMap<Long, Integer>();
    private static final int SCAN_DELAY_TICKS = 5;
    private static final int MAX_CHUNK_EVALS_PER_TICK = 2;
    private static boolean chunkListenerRegistered = false;
    private Predicate<BlockState> blockPredicate = state -> false;
    private String lastWhitelistSignature = "";
    private boolean lastBlocksEnabled = false;

    public ESPModule() {
        this.registerChunkListener();
    }

    private void registerChunkListener() {
        if (chunkListenerRegistered) {
            return;
        }
        chunkListenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            FabricChunkEventsHelper.register();
        }
        catch (Throwable t) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", (Object)t.getMessage());
        }
    }

    private static long chunkKey(int x, int z) {
        return (long)x & 0xFFFFFFFFL | (long)z << 32;
    }

    private void queueChunk(int chunkX, int chunkZ) {
        this.pendingChunks.putIfAbsent(ESPModule.chunkKey(chunkX, chunkZ), 5);
    }

    @Override
    public void onEnable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = this.blocks.getValue();
        if (!this.blocks.getValue()) {
            return;
        }
        this.rebuildPredicate();
    }

    @Override
    public void onDisable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = false;
    }

    private String whitelistSignature() {
        return String.join((CharSequence)",", this.blockWhitelist.getWhitelistIds());
    }

    private void rebuildPredicate() {
        this.lastWhitelistSignature = this.whitelistSignature();
        this.blockPredicate = state -> !state.isAir() && this.blockWhitelist.isWhitelistContains(state.getBlock());
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        if (ESPModule.mc.player == null || ESPModule.mc.level == null) {
            return;
        }
        int pcx = ESPModule.mc.player.blockPosition().getX() >> 4;
        int pcz = ESPModule.mc.player.blockPosition().getZ() >> 4;
        int radius = ESPModule.mc.options.getEffectiveRenderDistance();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                if (!ESPModule.mc.level.hasChunk(cx, cz)) continue;
                this.queueChunk(cx, cz);
            }
        }
    }

    @SubscribeEvent
    public void onBlockScanTick(TickEvent event) {
        if (ESPModule.mc.player == null || ESPModule.mc.level == null) {
            return;
        }
        if (!this.blocks.getValue()) {
            if (this.lastBlocksEnabled) {
                this.lastBlocksEnabled = false;
                this.matchedBlocks.clear();
                this.pendingChunks.clear();
            }
            return;
        }
        if (!this.lastBlocksEnabled) {
            this.lastBlocksEnabled = true;
            this.rebuildPredicate();
            return;
        }
        if (!this.whitelistSignature().equals(this.lastWhitelistSignature)) {
            this.rebuildPredicate();
            return;
        }
        if (this.pendingChunks.isEmpty()) {
            return;
        }
        int budget = 2;
        Iterator<Map.Entry<Long, Integer>> it = this.pendingChunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Integer> e = it.next();
            int left = e.getValue() - 1;
            if (left > 0) {
                e.setValue(left);
                continue;
            }
            if (budget == 0) {
                e.setValue(1);
                continue;
            }
            --budget;
            it.remove();
            long key = e.getKey();
            int chunkX = (int)key;
            int chunkZ = (int)(key >>> 32);
            this.evaluateChunk(chunkX, chunkZ, key);
        }
    }

    private void evaluateChunk(int chunkX, int chunkZ, long key) {
        if (ESPModule.mc.level == null || !ESPModule.mc.level.hasChunk(chunkX, chunkZ)) {
            return;
        }
        LevelChunk chunk = ESPModule.mc.level.getChunk(chunkX, chunkZ);
        ArrayList<BlockPos> matches = new ArrayList<BlockPos>();
        LevelChunkSection[] sections = chunk.getSections();
        int minSection = ESPModule.mc.level.getMinSectionY();
        for (int i = 0; i < sections.length; ++i) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir() || !section.maybeHas(this.blockPredicate)) continue;
            int sectionY = minSection + i << 4;
            int baseX = chunkX << 4;
            int baseZ = chunkZ << 4;
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!this.blockPredicate.test(state)) continue;
                        matches.add(new BlockPos(baseX + x, sectionY + y, baseZ + z));
                    }
                }
            }
        }
        if (matches.isEmpty()) {
            this.matchedBlocks.remove(key);
        } else {
            this.matchedBlocks.put(key, matches);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (ESPModule.mc.level == null) {
            return;
        }
        ArrayList<Entity> targetEntities = new ArrayList<Entity>();
        for (Entity entity : ESPModule.mc.level.entitiesForRendering()) {
            if (entity == ESPModule.mc.player || !this.isValidEntity(entity)) continue;
            targetEntities.add(entity);
        }
        this.targetEntities = targetEntities;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (ESPModule.mc.level == null) {
            return;
        }
        if (!this.targetEntities.isEmpty()) {
            for (Entity entity : this.targetEntities) {
                Vec3 pos = EntityUtils.getRenderPos(entity, event.getTickDelta());
                AABB box = new AABB(pos.x - entity.getBoundingBox().getXsize() / 2.0, pos.y, pos.z - entity.getBoundingBox().getZsize() / 2.0, pos.x + entity.getBoundingBox().getXsize() / 2.0, pos.y + entity.getBoundingBox().getYsize(), pos.z + entity.getBoundingBox().getZsize() / 2.0);
                if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                    Renderer3D.renderBox(event.getMatrices(), box, this.fillColor.getColor());
                }
                if (!this.mode.getValue().equalsIgnoreCase("Outline") && !this.mode.getValue().equalsIgnoreCase("Both")) continue;
                Renderer3D.renderBoxOutline(event.getMatrices(), box, this.outlineColor.getColor());
            }
        }
        this.renderBlockTargets(event);
    }

    private void renderBlockTargets(RenderWorldEvent event) {
        boolean outline;
        if (ESPModule.mc.level == null || !this.blocks.getValue() || this.matchedBlocks.isEmpty()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) {
            return;
        }
        boolean fill = this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
        boolean bl = outline = this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
        if (!fill && !outline) {
            return;
        }
        for (List<BlockPos> positions : this.matchedBlocks.values()) {
            for (BlockPos pos : positions) {
                BlockState state = ESPModule.mc.level.getBlockState(pos);
                if (!this.blockPredicate.test(state)) continue;
                VoxelShape shape = state.getShape((BlockGetter)ESPModule.mc.level, pos);
                List<AABB> boxes = shape.isEmpty() ? List.of(new AABB(pos)) : shape.toAabbs().stream().map(b -> b.move(pos)).toList();
                for (AABB box : boxes) {
                    if (fill) {
                        Renderer3D.renderBox(event.getMatrices(), box, this.fillColor.getColor());
                    }
                    if (!outline) continue;
                    Renderer3D.renderBoxOutline(event.getMatrices(), box, this.outlineColor.getColor());
                }
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (this.players.getValue() && entity.getType() == EntityTypes.PLAYER) {
            return true;
        }
        if (this.hostiles.getValue() && EntityUtils.isHostile(entity)) {
            return true;
        }
        if (this.animals.getValue() && EntityUtils.isAnimal(entity) && !EntityUtils.isHostile(entity)) {
            return true;
        }
        if (this.ambient.getValue() && entity.getType().getCategory() == MobCategory.AMBIENT) {
            return true;
        }
        if (this.invisibles.getValue() && entity.isInvisible()) {
            return true;
        }
        if (this.items.getValue() && (entity.getType() == EntityTypes.ITEM || entity.getType() == EntityTypes.EXPERIENCE_BOTTLE)) {
            return true;
        }
        return this.others.getValue();
    }

    private static class FabricChunkEventsHelper {
        private FabricChunkEventsHelper() {
        }

        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
                ESPModule module;
                ESPModule eSPModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ESPModule.class) : null;
                if (module == null || !module.isToggled() || !module.blocks.getValue()) {
                    return;
                }
                if (Minecraft.getInstance().level != level) {
                    return;
                }
                module.queueChunk(chunk.getPos().x(), chunk.getPos().z());
            });
            ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
                ESPModule module;
                ESPModule eSPModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ESPModule.class) : null;
                if (module == null) {
                    return;
                }
                long key = ESPModule.chunkKey(chunk.getPos().x(), chunk.getPos().z());
                module.matchedBlocks.remove(key);
                module.pendingChunks.remove(key);
            });
        }
    }
}

