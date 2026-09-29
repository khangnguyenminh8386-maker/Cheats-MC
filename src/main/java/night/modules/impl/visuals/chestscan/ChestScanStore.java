/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 */
package night.modules.impl.visuals.chestscan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ChestScanStore {
    private final Map<BlockPos, ChestStatus> records = new HashMap<BlockPos, ChestStatus>();
    private File file;

    public static String currentWorldKey() {
        Minecraft mc = Minecraft.getInstance();
        Object base = mc.getCurrentServer() != null ? "server_" + mc.getCurrentServer().ip : (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null ? "sp_" + mc.getSingleplayerServer().getWorldData().getLevelName() : "unknown");
        String dimension = mc.level != null ? mc.level.dimension().identifier().toString() : "no_dimension";
        return ChestScanStore.sanitize((String)base) + "__" + ChestScanStore.sanitize(dimension);
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

   public void loadForWorld() {
      this.records.clear();
      File dir = new File(FabricLoader.getInstance().getGameDir().toFile(), "night/chestscan");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      this.file = new File(dir, currentWorldKey() + ".json");
      if (this.file.exists()) {
         try (FileReader reader = new FileReader(this.file)) {
            Gson gson = new Gson();
            Type type = (new TypeToken<List<ChestScanStore.Entry>>() {}).getType();
            List<ChestScanStore.Entry> entries = gson.fromJson(reader, type);
            if (entries != null) {
               for (ChestScanStore.Entry e : entries) {
                  try {
                     this.records.put(new BlockPos(e.x, e.y, e.z), ChestScanStore.ChestStatus.valueOf(e.status));
                  } catch (Exception var10) {
                  }
               }
            }
         } catch (Exception var12) {
         }
      }
   }

    private void save() {
        if (this.file == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(this.file);){
            ArrayList<Entry> entries = new ArrayList<Entry>();
            for (Map.Entry<BlockPos, ChestStatus> e : this.records.entrySet()) {
                Entry entry = new Entry();
                entry.x = e.getKey().getX();
                entry.y = e.getKey().getY();
                entry.z = e.getKey().getZ();
                entry.status = e.getValue().name();
                entries.add(entry);
            }
            new GsonBuilder().setPrettyPrinting().create().toJson(entries, (Appendable)writer);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void put(BlockPos pos, ChestStatus status) {
        this.records.put(pos.immutable(), status);
        this.save();
    }

    public ChestStatus get(BlockPos pos) {
        return this.records.get(pos);
    }

    public void remove(BlockPos pos) {
        if (this.records.remove(pos) != null) {
            this.save();
        }
    }

    public Set<BlockPos> positions() {
        return this.records.keySet();
    }

    private static class Entry {
        int x;
        int y;
        int z;
        String status;

        private Entry() {
        }
    }

    public static enum ChestStatus {
        EMPTY,
        PARTIAL,
        FULL;

    }
}

