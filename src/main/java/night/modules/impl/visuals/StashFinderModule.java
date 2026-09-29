/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.animal.equine.Donkey
 *  net.minecraft.world.entity.animal.equine.Llama
 *  net.minecraft.world.entity.vehicle.boat.ChestBoat
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BarrelBlockEntity
 *  net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.ChestBlockEntity
 *  net.minecraft.world.level.block.entity.CrafterBlockEntity
 *  net.minecraft.world.level.block.entity.DispenserBlockEntity
 *  net.minecraft.world.level.block.entity.DropperBlockEntity
 *  net.minecraft.world.level.block.entity.EnderChestBlockEntity
 *  net.minecraft.world.level.block.entity.FurnaceBlockEntity
 *  net.minecraft.world.level.block.entity.HopperBlockEntity
 *  net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.world.level.block.entity.SmokerBlockEntity
 *  net.minecraft.world.level.block.entity.TrappedChestBlockEntity
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.phys.AABB
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.visuals.stashfinder.StashFinderStore;
import night.modules.impl.visuals.stashfinder.StashWebhook;
import night.modules.impl.visuals.stashfinder.StructureSignature;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.graphics.Renderer3D;

@RegisterModule(name="StashFinder", description="Scans loaded chunks for loot stashes and storage animals, alerting via chat and Discord webhook.", category=Module.Category.VISUALS)
public class StashFinderModule
extends Module {
    public CategorySetting containersCategory = new CategorySetting("Containers", "Threshold settings for containers.");
    public NumberSetting minChests = new NumberSetting("Chests", "Minimum chests required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minBarrels = new NumberSetting("Barrels", "Minimum barrels required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minShulkers = new NumberSetting("Shulkers", "Minimum shulker boxes required.", new CategorySetting.Visibility(this.containersCategory), (Number)4, (Number)1, (Number)64);
    public NumberSetting minEnderChests = new NumberSetting("EnderChests", "Minimum ender chests required.", new CategorySetting.Visibility(this.containersCategory), (Number)2, (Number)1, (Number)64);
    public NumberSetting minHoppers = new NumberSetting("Hoppers", "Minimum hoppers required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minDispensersDroppers = new NumberSetting("Dispensers/Droppers", "Minimum dispensers or droppers required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minFurnaces = new NumberSetting("Furnaces", "Minimum furnaces required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minCrafters = new NumberSetting("Crafters", "Minimum crafters required.", new CategorySetting.Visibility(this.containersCategory), (Number)4, (Number)1, (Number)64);
    public CategorySetting animalsCategory = new CategorySetting("Animals", "Threshold settings for storage animals.");
    public NumberSetting minDonkeys = new NumberSetting("Donkeys", "Minimum donkeys required.", new CategorySetting.Visibility(this.animalsCategory), (Number)2, (Number)1, (Number)16);
    public NumberSetting minLlamas = new NumberSetting("Llamas", "Minimum llamas required.", new CategorySetting.Visibility(this.animalsCategory), (Number)2, (Number)1, (Number)16);
    public NumberSetting minChestBoats = new NumberSetting("ChestBoats", "Minimum chest boats required.", new CategorySetting.Visibility(this.animalsCategory), (Number)1, (Number)1, (Number)16);
    public CategorySetting generalCategory = new CategorySetting("General", "General settings.");
    public BooleanSetting ignoreNatural = new BooleanSetting("IgnoreNatural", "Ignores naturally generated structures.", new CategorySetting.Visibility(this.generalCategory), true);
    public BooleanSetting chatNotify = new BooleanSetting("ChatNotify", "Notifies in chat when a stash is found.", new CategorySetting.Visibility(this.generalCategory), true);
    public BooleanSetting renderHighlight = new BooleanSetting("RenderHighlight", "Highlights found stash chunks in the world.", new CategorySetting.Visibility(this.generalCategory), true);
    public ColorSetting highlightColor = new ColorSetting("HighlightColor", "Highlight color for stash chunks.", new CategorySetting.Visibility(this.generalCategory), new ColorSetting.Color(new Color(0, 230, 120, 100), false, false));
    public CategorySetting webhookCategory = new CategorySetting("Webhook", "Discord webhook settings.");
    public BooleanSetting sendWebhook = new BooleanSetting("SendWebhook", "Sends stash alerts to Discord webhook.", new CategorySetting.Visibility(this.webhookCategory), true);
    public StringSetting webhookUrl = new StringSetting("WebhookURL", "Discord webhook URL.", new CategorySetting.Visibility(this.webhookCategory), "");
    public StringSetting userId = new StringSetting("UserID", "Discord User ID to ping.", new CategorySetting.Visibility(this.webhookCategory), "");
    private final StashFinderStore store = new StashFinderStore();
    private String lastWorldKey = null;
    private final Map<Long, Integer> pending = new HashMap<Long, Integer>();
    private static final int SCAN_DELAY_TICKS = 25;
    private static final int MAX_EVALS_PER_TICK = 4;
    private static final int BOOTSTRAP_RADIUS_CHUNKS = 12;
    private static boolean listenerRegistered = false;

    public StashFinderModule() {
        this.registerChunkListener();
    }

    private void registerChunkListener() {
        if (listenerRegistered) {
            return;
        }
        listenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            FabricChunkEventsHelper.register();
        }
        catch (Throwable t) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", (Object)t.getMessage());
        }
    }

    @Override
    public void onEnable() {
        this.pending.clear();
        this.store.loadForWorld();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
        if (StashFinderModule.mc.player == null || StashFinderModule.mc.level == null) {
            return;
        }
        int pcx = StashFinderModule.mc.player.blockPosition().getX() >> 4;
        int pcz = StashFinderModule.mc.player.blockPosition().getZ() >> 4;
        for (int dx = -12; dx <= 12; ++dx) {
            for (int dz = -12; dz <= 12; ++dz) {
                long key;
                int cx = pcx + dx;
                int cz = pcz + dz;
                if (!StashFinderModule.mc.level.hasChunk(cx, cz) || this.store.isEvaluated(key = StashFinderStore.chunkKey(cx, cz))) continue;
                this.pending.putIfAbsent(key, 25);
            }
        }
    }

    @Override
    public void onDisable() {
        this.store.flush();
        this.pending.clear();
    }

    private void maybeReloadStoreForWorld() {
        String key = StashFinderStore.currentWorldKey();
        if (!key.equals(this.lastWorldKey)) {
            this.store.loadForWorld();
            this.lastWorldKey = key;
            this.pending.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (StashFinderModule.mc.player == null || StashFinderModule.mc.level == null) {
            return;
        }
        this.maybeReloadStoreForWorld();
        this.store.flushIfStale();
        if (this.pending.isEmpty()) {
            return;
        }
        int budget = 4;
        Iterator<Map.Entry<Long, Integer>> it = this.pending.entrySet().iterator();
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
            int chunkX = (int)(key & 0xFFFFFFFFL);
            int chunkZ = (int)(key >>> 32);
            this.evaluateChunk(chunkX, chunkZ, key);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.renderHighlight.getValue() || StashFinderModule.mc.level == null) {
            return;
        }
        Color fill = this.highlightColor.getColor();
        Color outline = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 255);
        for (StashFinderStore.StashData stash : this.store.getStashes()) {
            AABB box = new AABB((double)stash.x(), (double)StashFinderModule.mc.level.getMinY(), (double)stash.z(), (double)(stash.x() + 16), (double)StashFinderModule.mc.level.getMaxY(), (double)(stash.z() + 16));
            Renderer3D.renderBox(event.getMatrices(), box, fill);
            Renderer3D.renderBoxOutline(event.getMatrices(), box, outline);
        }
    }

    public void resetCurrentWorldStore() {
        this.store.loadForWorld();
        this.store.clearAll();
        this.pending.clear();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
    }

    private void evaluateChunk(int chunkX, int chunkZ, long key) {
        if (StashFinderModule.mc.level == null || !StashFinderModule.mc.level.hasChunk(chunkX, chunkZ)) {
            return;
        }
        LevelChunk chunk = StashFinderModule.mc.level.getChunk(chunkX, chunkZ);
        this.store.markEvaluated(key);
        Map<String, Integer> counts = this.countContainers(chunk);
        counts.putAll(this.countAnimals(chunk));
        if (!this.meetsAnyThreshold(counts)) {
            return;
        }
        if (this.ignoreNatural.getValue() && StructureSignature.isNatural(mc, chunk, counts)) {
            return;
        }
        LinkedHashMap<String, Integer> nonZero = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() <= 0) continue;
            nonZero.put(e.getKey(), e.getValue());
        }
        ChunkPos pos = chunk.getPos();
        int reportX = pos.getMinBlockX();
        int reportZ = pos.getMinBlockZ();
        String dimension = StashFinderModule.mc.level.dimension().identifier().toString();
        this.store.recordStash(reportX, reportZ, dimension, nonZero);
        if (this.chatNotify.getValue()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Stash found at X: ").append(reportX).append(" Z: ").append(reportZ).append(" (");
            int idx = 0;
            for (Map.Entry entry : nonZero.entrySet()) {
                if (idx++ > 0) {
                    sb.append(", ");
                }
                sb.append((String)entry.getKey()).append(": ").append(entry.getValue());
            }
            sb.append(")");
            Night.CHAT_MANAGER.tagged(sb.toString(), "StashFinder");
        }
        if (this.sendWebhook.getValue()) {
            String url = this.webhookUrl.getValue();
            String id = this.userId.getValue();
            StashWebhook.send(reportX, reportZ, dimension, nonZero, url, id);
        }
    }

    private boolean meetsAnyThreshold(Map<String, Integer> counts) {
        return this.check(counts, "Chests", this.minChests) || this.check(counts, "Barrels", this.minBarrels) || this.check(counts, "Shulkers", this.minShulkers) || this.check(counts, "Ender Chests", this.minEnderChests) || this.check(counts, "Hoppers", this.minHoppers) || this.check(counts, "Dispensers/Droppers", this.minDispensersDroppers) || this.check(counts, "Furnaces", this.minFurnaces) || this.check(counts, "Crafters", this.minCrafters) || this.check(counts, "Donkey", this.minDonkeys) || this.check(counts, "Llama", this.minLlamas) || this.check(counts, "Chest Boat", this.minChestBoats);
    }

    private boolean check(Map<String, Integer> counts, String key, NumberSetting min) {
        int threshold = min.getValue().intValue();
        if (threshold <= 0) {
            return false;
        }
        return counts.getOrDefault(key, 0) >= threshold;
    }

    private Map<String, Integer> countContainers(LevelChunk chunk) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        int chests = 0;
        int barrels = 0;
        int shulkers = 0;
        int enderChests = 0;
        int hoppers = 0;
        int dispensersDroppers = 0;
        int furnaces = 0;
        int crafters = 0;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof ChestBlockEntity || be instanceof TrappedChestBlockEntity) {
                ++chests;
                continue;
            }
            if (be instanceof BarrelBlockEntity) {
                ++barrels;
                continue;
            }
            if (be instanceof ShulkerBoxBlockEntity) {
                ++shulkers;
                continue;
            }
            if (be instanceof EnderChestBlockEntity) {
                ++enderChests;
                continue;
            }
            if (be instanceof HopperBlockEntity) {
                ++hoppers;
                continue;
            }
            if (be instanceof DispenserBlockEntity || be instanceof DropperBlockEntity) {
                ++dispensersDroppers;
                continue;
            }
            if (be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity || be instanceof SmokerBlockEntity) {
                ++furnaces;
                continue;
            }
            if (!(be instanceof CrafterBlockEntity)) continue;
            ++crafters;
        }
        counts.put("Chests", chests);
        counts.put("Barrels", barrels);
        counts.put("Shulkers", shulkers);
        counts.put("Ender Chests", enderChests);
        counts.put("Hoppers", hoppers);
        counts.put("Dispensers/Droppers", dispensersDroppers);
        counts.put("Furnaces", furnaces);
        counts.put("Crafters", crafters);
        return counts;
    }

    private Map<String, Integer> countAnimals(LevelChunk chunk) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        int donkeys = 0;
        int llamas = 0;
        int chestBoats = 0;
        if (StashFinderModule.mc.level == null) {
            return counts;
        }
        ChunkPos pos = chunk.getPos();
        AABB box = new AABB((double)pos.getMinBlockX(), (double)StashFinderModule.mc.level.getMinY(), (double)pos.getMinBlockZ(), (double)(pos.getMaxBlockX() + 1), (double)StashFinderModule.mc.level.getMaxY(), (double)(pos.getMaxBlockZ() + 1));
        for (Entity entity : StashFinderModule.mc.level.getEntities((Entity)null, box, e -> true)) {
            if (entity instanceof Donkey) {
                ++donkeys;
                continue;
            }
            if (entity instanceof Llama) {
                ++llamas;
                continue;
            }
            if (!(entity instanceof ChestBoat)) continue;
            ++chestBoats;
        }
        counts.put("Donkey", donkeys);
        counts.put("Llama", llamas);
        counts.put("Chest Boat", chestBoats);
        return counts;
    }

    private static class FabricChunkEventsHelper {
        private FabricChunkEventsHelper() {
        }

        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
                StashFinderModule module;
                StashFinderModule stashFinderModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(StashFinderModule.class) : null;
                if (module == null || !module.isToggled()) {
                    return;
                }
                Minecraft client = Minecraft.getInstance();
                if (client.level != level) {
                    return;
                }
                long key = StashFinderStore.chunkKey(chunk.getPos().x(), chunk.getPos().z());
                if (module.store.isEvaluated(key)) {
                    return;
                }
                module.pending.putIfAbsent(key, 25);
            });
        }
    }
}

