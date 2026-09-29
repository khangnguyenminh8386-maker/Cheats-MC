/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package night.modules.impl.visuals.stashfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashFinderStore {
    private final Set<Long> evaluated = new HashSet<Long>();
    private final List<StashData> stashes = new ArrayList<StashData>();
    private File file;
    private boolean dirty = false;
    private long lastSaveMs = 0L;
    private static final long SAVE_INTERVAL_MS = 10000L;

    public static String currentWorldKey() {
        Minecraft mc = Minecraft.getInstance();
        Object base = mc.getCurrentServer() != null ? "server_" + mc.getCurrentServer().ip : (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null ? "sp_" + mc.getSingleplayerServer().getWorldData().getLevelName() : "unknown");
        String dimension = mc.level != null ? mc.level.dimension().identifier().toString() : "no_dimension";
        return StashFinderStore.sanitize((String)base) + "__" + StashFinderStore.sanitize(dimension);
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
    }

   public void loadForWorld() {
      this.flush();
      this.evaluated.clear();
      this.stashes.clear();
      File dir = new File(FabricLoader.getInstance().getGameDir().toFile(), "night/stashfinder");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      this.file = new File(dir, currentWorldKey() + ".json");
      if (this.file.exists()) {
         try (FileReader reader = new FileReader(this.file)) {
            Gson gson = new Gson();
            Type type = (new TypeToken<StashFinderStore.PersistentData>() {}).getType();
            StashFinderStore.PersistentData data = gson.fromJson(reader, type);
            if (data != null) {
               if (data.evaluated != null) {
                  this.evaluated.addAll(data.evaluated);
               }

               if (data.stashes != null) {
                  this.stashes.addAll(data.stashes);
               }
            }
         } catch (Exception var8) {
         }
      }
   }

    private void save() {
        if (this.file == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(this.file);){
            PersistentData data = new PersistentData();
            data.evaluated = this.evaluated;
            data.stashes = this.stashes;
            new GsonBuilder().create().toJson((Object)data, (Appendable)writer);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean isEvaluated(long chunkKey) {
        return this.evaluated.contains(chunkKey);
    }

    public void markEvaluated(long chunkKey) {
        if (this.evaluated.add(chunkKey)) {
            this.dirty = true;
        }
    }

    public void recordStash(int x, int z, String dimension, Map<String, Integer> counts) {
        this.stashes.add(new StashData(x, z, dimension, counts, System.currentTimeMillis()));
        this.dirty = true;
    }

    public List<StashData> getStashes() {
        return Collections.unmodifiableList(this.stashes);
    }

    public void flushIfStale() {
        if (this.dirty && System.currentTimeMillis() - this.lastSaveMs >= 10000L) {
            this.flush();
        }
    }

    public void flush() {
        if (!this.dirty) {
            return;
        }
        this.save();
        this.dirty = false;
        this.lastSaveMs = System.currentTimeMillis();
    }

    public void clearAll() {
        this.evaluated.clear();
        this.stashes.clear();
        this.dirty = true;
        this.flush();
    }

    private static class PersistentData {
        Set<Long> evaluated = new HashSet<Long>();
        List<StashData> stashes = new ArrayList<StashData>();

        private PersistentData() {
        }
    }

    public record StashData(int x, int z, String dimension, Map<String, Integer> counts, long timestamp) {
    }
}

