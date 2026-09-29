/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.Vec3
 */
package night.managers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.managers.WaypointManager;
import night.modules.Module;
import night.modules.impl.core.PingBypassModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.pingbypass.PingBypassFlags;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ImageSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.PositionSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.minecraft.IdentifierUtils;
import night.utils.system.FileUtils;

public class ConfigManager {
    private String currentConfig = "default";
    private boolean loadingConfig = false;

    public ConfigManager() {
        this.loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveConfig));
    }

    public void loadConfig() {
        block2: {
            try {
                this.migrateOldConfig();
                FileUtils.createDirectory("Cheats MC");
                FileUtils.createDirectory("Night/Configs");
                FileUtils.createDirectory("Night/Client");
                FileUtils.createDirectory("Night/Modules");
                FileUtils.createDirectory("Night/Binds");
                FileUtils.createDirectory("Night/Visuals");
                FileUtils.createDirectory("Night/GlobalConfigs");
                this.loadGeneral();
                this.loadWaypoints();
                this.loadModules(this.currentConfig);
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER == null) break block2;
                Night.CHAT_MANAGER.await("The configuration has not been loaded properly. Read the stacktrace for more information.");
            }
        }
    }

    private void migrateOldConfig() {
        try {
            Path oldDir = Paths.get("EUClient", new String[0]);
            Path newDir = Paths.get("Cheats MC", new String[0]);
            if (!(!Files.exists(oldDir, new LinkOption[0]) || Files.exists(newDir, new LinkOption[0]) && Files.exists(newDir.resolve("Configs"), new LinkOption[0]))) {
                Night.LOGGER.info("Migrating configuration from EUClient to Cheats MC...");
                ConfigManager.copyDirectory(oldDir, newDir);
            }
        }
        catch (Exception e) {
            Night.LOGGER.warn("Failed to migrate old EUClient configuration: {}", (Object)e.getMessage());
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source, new FileVisitOption[0]);){
            stream.forEach(src -> {
                try {
                    Path dest = target.resolve(source.relativize((Path)src));
                    if (Files.isDirectory(src, new LinkOption[0])) {
                        if (!Files.exists(dest, new LinkOption[0])) {
                            Files.createDirectories(dest, new FileAttribute[0]);
                        }
                    } else if (!Files.exists(dest, new LinkOption[0])) {
                        if (dest.getParent() != null && !Files.exists(dest.getParent(), new LinkOption[0])) {
                            Files.createDirectories(dest.getParent(), new FileAttribute[0]);
                        }
                        Files.copy(src, dest, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                }
                catch (IOException e) {
                    Night.LOGGER.warn("Could not copy config file {}: {}", src, (Object)e.getMessage());
                }
            });
        }
    }

    public void saveConfig() {
        try {
            FileUtils.createDirectory("Cheats MC");
            FileUtils.createDirectory("Night/Configs");
            FileUtils.createDirectory("Night/Client");
            FileUtils.createDirectory("Night/Modules");
            FileUtils.createDirectory("Night/Binds");
            FileUtils.createDirectory("Night/Visuals");
            FileUtils.createDirectory("Night/GlobalConfigs");
            this.saveGeneral();
            this.saveWaypoints();
            this.saveModules(this.currentConfig);
        }
        catch (IOException exception) {
            Night.LOGGER.error("Failed to save the client's configuration!", (Throwable)exception);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadWaypoints() throws IOException {
        if (!FileUtils.fileExists("Night/Waypoints.json")) {
            return;
        }
        InputStream stream = Files.newInputStream(Paths.get("Night/Waypoints.json", new String[0]), new OpenOption[0]);
        try {
            JsonObject configObject;
            try {
                configObject = JsonParser.parseReader((Reader)new InputStreamReader(stream)).getAsJsonObject();
            }
            catch (IllegalStateException exception) {
                Night.LOGGER.error("Failed to load the client's Waypoint configuration!", (Throwable)exception);
                Night.CHAT_MANAGER.await("The Waypoint configuration has not been loaded properly. Read the stacktrace for more information.");
                if (Collections.singletonList(stream).get(0) != null) {
                    stream.close();
                }
                return;
            }
            if (configObject.has("Waypoints")) {
                for (JsonElement element : configObject.get("Waypoints").getAsJsonArray()) {
                    String[] args = element.getAsString().split(":");
                    if (Night.WAYPOINT_MANAGER.contains(args[0])) continue;
                    Night.WAYPOINT_MANAGER.add(args[0], new Vec3((double)Integer.parseInt(args[1]), (double)Integer.parseInt(args[2]), (double)Integer.parseInt(args[3])), args[4], args[5]);
                }
            }
        }
        finally {
            if (Collections.singletonList(stream).get(0) != null) {
                stream.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadGeneral() throws IOException {
        if (!FileUtils.fileExists("Night/General.json")) {
            return;
        }
        InputStream stream = Files.newInputStream(Paths.get("Night/General.json", new String[0]), new OpenOption[0]);
        try {
            JsonObject configObject;
            JsonElement parsed = JsonParser.parseReader((Reader)new InputStreamReader(stream));
            if (parsed == null || !parsed.isJsonObject()) {
                Night.LOGGER.warn("General.json is empty or invalid JSON.");
                return;
            }
            try {
                configObject = parsed.getAsJsonObject();
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's General configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER != null) {
                    Night.CHAT_MANAGER.await("The General configuration has not been loaded properly. Read the stacktrace for more information.");
                }
                return;
            }
            if (configObject.has("Config")) {
                this.currentConfig = configObject.get("Config").getAsString();
            }
            if (configObject.has("Prefix") && Night.COMMAND_MANAGER != null) {
                Night.COMMAND_MANAGER.setPrefix(configObject.get("Prefix").getAsString());
            }
            if (configObject.has("Friends") && Night.FRIEND_MANAGER != null) {
                for (JsonElement element : configObject.get("Friends").getAsJsonArray()) {
                    if (Night.FRIEND_MANAGER.contains(element.getAsString())) continue;
                    Night.FRIEND_MANAGER.add(element.getAsString());
                }
            }
            if (configObject.has("Macros") && Night.MACRO_MANAGER != null) {
                for (JsonElement element : configObject.get("Macros").getAsJsonArray()) {
                    try {
                        String[] split = element.getAsString().split(":", 2);
                        if (split.length <= 1) continue;
                        int key = Integer.parseInt(split[0]);
                        String message = split[1];
                        Night.MACRO_MANAGER.add(message, key);
                    }
                    catch (NumberFormatException exception) {
                        Night.LOGGER.error("Failed to load the " + element.getAsString() + " macro!", (Throwable)exception);
                        if (Night.CHAT_MANAGER == null) continue;
                        Night.CHAT_MANAGER.await("The " + element.getAsString() + " macro has failed to load. Read the stacktrace for more information.");
                    }
                }
            }
        }
        finally {
            if (Collections.singletonList(stream).get(0) != null) {
                stream.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveGeneral() throws IOException {
        FileUtils.resetFile("Night/General.json");
        JsonObject configObject = new JsonObject();
        configObject.add("Config", (JsonElement)new JsonPrimitive(this.currentConfig));
        configObject.add("Prefix", (JsonElement)new JsonPrimitive(Night.COMMAND_MANAGER.getPrefix()));
        JsonArray friendsArray = new JsonArray();
        Night.FRIEND_MANAGER.getFriends().forEach(arg_0 -> ((JsonArray)friendsArray).add(arg_0));
        configObject.add("Friends", (JsonElement)friendsArray);
        JsonArray macroArray = new JsonArray();
        Night.MACRO_MANAGER.getMacros().forEach((key, value) -> macroArray.add(value + ":" + key));
        configObject.add("Macros", (JsonElement)macroArray);
        OutputStreamWriter writer = new OutputStreamWriter((OutputStream)new FileOutputStream("Night/General.json"), StandardCharsets.UTF_8);
        try {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)configObject.toString())));
        }
        finally {
            if (Collections.singletonList(writer).get(0) != null) {
                writer.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveWaypoints() throws IOException {
        FileUtils.resetFile("Night/Waypoints.json");
        JsonObject configObject = new JsonObject();
        JsonArray waypointArray = new JsonArray();
        for (WaypointManager.Waypoint waypoint : Night.WAYPOINT_MANAGER.getWaypoints()) {
            waypointArray.add(waypoint.getName() + ":" + (int)waypoint.getPos().x + ":" + (int)waypoint.getPos().y + ":" + (int)waypoint.getPos().z + ":" + waypoint.getDimension() + ":" + waypoint.getServer());
        }
        configObject.add("Waypoints", (JsonElement)waypointArray);
        OutputStreamWriter writer = new OutputStreamWriter((OutputStream)new FileOutputStream("Night/Waypoints.json"), StandardCharsets.UTF_8);
        try {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)configObject.toString())));
        }
        finally {
            if (Collections.singletonList(writer).get(0) != null) {
                writer.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadModules(String config) throws IOException {
        if (!FileUtils.fileExists("Night/Configs/" + config + ".json")) {
            return;
        }
        InputStream stream = Files.newInputStream(Paths.get("Night/Configs/" + config + ".json", new String[0]), new OpenOption[0]);
        try {
            PingBypassModule pbModule;
            JsonObject configObject;
            JsonElement parsed = JsonParser.parseReader((Reader)new InputStreamReader(stream));
            if (parsed == null || !parsed.isJsonObject()) {
                Night.LOGGER.warn("Module configuration is empty or invalid JSON: {}", (Object)config);
                return;
            }
            try {
                configObject = parsed.getAsJsonObject();
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's Module configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER != null) {
                    Night.CHAT_MANAGER.await("The configuration for the Modules has not been loaded properly. Read the stacktrace for more information.");
                }
                return;
            }
            if (!configObject.has("Modules")) {
                return;
            }
            JsonObject modulesObject = configObject.get("Modules").getAsJsonObject();
            this.loadingConfig = true;
            try {
                this.loadModulesInner(modulesObject);
            }
            finally {
                this.loadingConfig = false;
            }
            this.currentConfig = config;
            try {
                this.saveGeneral();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer() && (pbModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class)) != null && pbModule.isToggled()) {
                pbModule.syncAllSettingsToProxy();
            }
        }
        finally {
            if (Collections.singletonList(stream).get(0) != null) {
                stream.close();
            }
        }
    }

    private void loadModulesInner(JsonObject modulesObject) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!modulesObject.has(module.getName())) {
                module.setToggled(false);
                module.resetValues();
                continue;
            }
            this.applyModuleJson(module, modulesObject.get(module.getName()).getAsJsonObject());
        }
    }

    public void applyModuleJson(Module module, JsonObject moduleObject) {
        this.applyModuleJson(module, moduleObject, true);
    }

   public void applyModuleJson(Module module, JsonObject moduleObject, boolean includeBinds) {
      boolean toggled = moduleObject.has("Status") && moduleObject.get("Status").getAsBoolean();
      if (!moduleObject.has("Settings")) {
         if (includeBinds) {
            module.resetValues();
         }

         module.setToggled(toggled, false);
      } else {
         JsonObject settingsObject = moduleObject.get("Settings").getAsJsonObject();

         for (Setting uncastedSetting : module.getSettings()) {
            if (includeBinds || !(uncastedSetting instanceof BindSetting)) {
               JsonElement valueObject = settingsObject.get(uncastedSetting.getName());
               if (valueObject != null && valueObject.isJsonPrimitive()) {
                  Setting var26 = uncastedSetting;
                  switch (var26) {
                     case BooleanSetting setting:
                        if (valueObject.getAsJsonPrimitive().isBoolean()) {
                           setting.setValue(valueObject.getAsBoolean());
                        } else {
                           setting.resetValue();
                        }
                        break;
                     case NumberSetting setting:
                        if (valueObject.getAsJsonPrimitive().isNumber()) {
                           setting.setValue(valueObject.getAsNumber());
                        } else {
                           setting.resetValue();
                        }
                        break;
                     case ModeSetting setting:
                        setting.setValue(valueObject.getAsString());
                        break;
                     case StringSetting setting:
                        String str = valueObject.getAsString();
                        if (str != null && str.equalsIgnoreCase("EUClient")) {
                           str = "Cheats MC";
                        }

                        setting.setValue(str);
                        break;
                     case ImageSetting setting:
                        setting.setValue(valueObject.getAsString());
                        if (Night.IMAGE_MANAGER != null) {
                           Night.IMAGE_MANAGER.setCurrentActiveImage(valueObject.getAsString());
                        }
                        break;
                     case BindSetting setting:
                        if (valueObject.isJsonPrimitive() && valueObject.getAsJsonPrimitive().isNumber()) {
                           setting.setValue(valueObject.getAsInt());
                        } else {
                           String[] dataxx = valueObject.getAsString().split(",", 2);
                           setting.setValue(Integer.parseInt(dataxx[0]));
                           if (dataxx.length > 1) {
                              setting.setMode(dataxx[1]);
                           }
                        }
                        break;
                     case ColorSetting setting:
                        String[] datax = valueObject.getAsString().split(",");
                        if (datax.length == 6) {
                           setting.setColor(
                              new Color(
                                 Math.clamp(Integer.parseInt(datax[0]), 0, 255),
                                 Math.clamp(Integer.parseInt(datax[1]), 0, 255),
                                 Math.clamp(Integer.parseInt(datax[2]), 0, 255),
                                 Math.clamp(Integer.parseInt(datax[3]), 0, 255)
                              )
                           );
                           setting.setSync(Boolean.parseBoolean(datax[4]));
                           setting.setRainbow(Boolean.parseBoolean(datax[5]));
                        }
                        break;
                     case PositionSetting setting:
                        String[] data = valueObject.getAsString().split(",");
                        if (data.length == 2) {
                           setting.set(Float.parseFloat(data[0]), Float.parseFloat(data[1]));
                        }
                        break;
                     case WhitelistSetting var42:
                        WhitelistSetting setting = (WhitelistSetting)var26;
                        setting.clear();
                        String[] dataxx = valueObject.getAsString().split(",");
                        if (dataxx.length != 0) {
                           for (String object : dataxx) {
                              if (setting.getType() == WhitelistSetting.Type.ITEMS) {
                                 Item item = IdentifierUtils.getItem(object);
                                 if (item != null) {
                                    setting.add(item);
                                 }
                              } else if (setting.getType() == WhitelistSetting.Type.BLOCKS) {
                                 Block block = IdentifierUtils.getBlock(object);
                                 if (block != null) {
                                    setting.add(block);
                                 }
                              }
                           }
                        }
                        break;
                     default:
                  }
               } else {
                  switch (uncastedSetting) {
                     case BooleanSetting setting:
                        setting.resetValue();
                        break;
                     case NumberSetting setting:
                        setting.resetValue();
                        break;
                     case ModeSetting setting:
                        setting.resetValue();
                        break;
                     case StringSetting setting:
                        setting.resetValue();
                        break;
                     case ImageSetting setting:
                        setting.resetValue();
                        break;
                     case BindSetting setting:
                        setting.resetValue();
                        break;
                     case ColorSetting setting:
                        setting.resetValue();
                        break;
                     case WhitelistSetting setting:
                        setting.clear();
                        break;
                     case PositionSetting setting:
                        setting.resetValue();
                        break;
                     default:
                  }
               }
            }
         }

         module.setToggled(toggled, false);
      }
   }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveModules(String config) throws IOException {
        FileUtils.resetFile("Night/Configs/" + config + ".json");
        JsonObject configObject = new JsonObject();
        configObject.add("Config", (JsonElement)new JsonPrimitive(config));
        JsonObject modulesObject = new JsonObject();
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            JsonObject moduleObject = this.serializeModuleJson(module);
            if (module instanceof FakePlayerModule) {
                moduleObject.add("Status", (JsonElement)new JsonPrimitive(Boolean.valueOf(false)));
            }
            modulesObject.add(module.getName(), (JsonElement)moduleObject);
        }
        configObject.add("Modules", (JsonElement)modulesObject);
        OutputStreamWriter writer = new OutputStreamWriter((OutputStream)new FileOutputStream("Night/Configs/" + config + ".json"), StandardCharsets.UTF_8);
        try {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)configObject.toString())));
            this.currentConfig = config;
            try {
                this.saveGeneral();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        finally {
            if (Collections.singletonList(writer).get(0) != null) {
                writer.close();
            }
        }
    }

    public JsonObject serializeModuleJson(Module module) {
        return this.serializeModuleJson(module, true);
    }

   public JsonObject serializeModuleJson(Module module, boolean includeBinds) {
      JsonObject moduleObject = new JsonObject();
      moduleObject.add("Status", new JsonPrimitive(module.isToggled()));
      JsonObject settingsObject = new JsonObject();

      for (Setting uncastedSetting : module.getSettings()) {
         if (includeBinds || !(uncastedSetting instanceof BindSetting)) {
            Setting var7 = uncastedSetting;
            switch (var7) {
               case BooleanSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                  break;
               case NumberSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                  break;
               case ModeSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                  break;
               case StringSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                  break;
               case ImageSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                  break;
               case BindSetting setting:
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getValue() + "," + setting.getMode()));
                  break;
               case ColorSetting setting:
                  settingsObject.add(
                     setting.getName(),
                     new JsonPrimitive(
                        setting.getValue().getColor().getRed()
                           + ","
                           + setting.getValue().getColor().getGreen()
                           + ","
                           + setting.getValue().getColor().getBlue()
                           + ","
                           + setting.getValue().getColor().getAlpha()
                           + ","
                           + setting.isSync()
                           + ","
                           + setting.isRainbow()
                     )
                  );
                  break;
               case WhitelistSetting setting:
                  StringJoiner objects = new StringJoiner(",");

                  for (String id : setting.getWhitelistIds()) {
                     objects.add(id);
                  }

                  settingsObject.add(setting.getName(), new JsonPrimitive(objects.toString()));
                  break;
               case PositionSetting var21:
                  PositionSetting setting = (PositionSetting)var7;
                  settingsObject.add(setting.getName(), new JsonPrimitive(setting.getX() + "," + setting.getY()));
                  break;
               default:
            }
         }
      }

      moduleObject.add("Settings", settingsObject);
      return moduleObject;
   }

    @Generated
    public String getCurrentConfig() {
        return this.currentConfig;
    }

    @Generated
    public boolean isLoadingConfig() {
        return this.loadingConfig;
    }

    @Generated
    public void setCurrentConfig(String currentConfig) {
        this.currentConfig = currentConfig;
    }

    @Generated
    public void setLoadingConfig(boolean loadingConfig) {
        this.loadingConfig = loadingConfig;
    }
}

