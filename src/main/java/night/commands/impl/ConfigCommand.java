/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 */
package night.commands.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.Module;
import night.modules.impl.core.CapesModule;
import night.modules.impl.core.ClickGuiModule;
import night.modules.impl.core.ColorModule;
import night.modules.impl.core.HUDModule;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.utils.chat.ChatUtils;
import night.utils.system.FileUtils;

@RegisterCommand(name="config", aliases={"cfg"}, tag="Config", description="Allows you to manage the client's configuration system.", syntax="<load|save|reset> <[name]> | <name> reset | global <[module]> <load|save|reset> <[name]> | bind <load|save|reset> <[name]> | visual <load|save|reset> <[name]> | modules <load|save|reset> <[name]> | <reload|save|current|reset>")
public class ConfigCommand
extends Command {
    private static final List<Class<? extends Module>> EXTRA_VISUAL_MODULES = List.of(ColorModule.class, ClickGuiModule.class, HUDModule.class, CapesModule.class);

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("load", "save", "reset", "reload", "current", "global", "bind", "visual", "modules");
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("reset")) {
                return this.savedConfigNames();
            }
            if (args[0].equalsIgnoreCase("global")) {
                return Night.MODULE_MANAGER.getModules().stream().map(Module::getName).toList();
            }
            if (args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("visual") || args[0].equalsIgnoreCase("modules")) {
                return List.of("load", "save", "reset");
            }
            return List.of("reset", "load", "save");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("global")) {
                return List.of("load", "save", "reset");
            }
            if (args[0].equalsIgnoreCase("bind") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.bindProfileNames();
            }
            if (args[0].equalsIgnoreCase("visual") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.visualConfigNames();
            }
            if (args[0].equalsIgnoreCase("modules") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.moduleStateNames();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("global") && (args[2].equalsIgnoreCase("load") || args[2].equalsIgnoreCase("save") || args[2].equalsIgnoreCase("reset"))) {
            return this.globalConfigNames(args[1]);
        }
        return List.of();
    }

    private List<String> savedConfigNames() {
        File[] files;
        File dir = new File("Night/Configs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if ((files = dir.listFiles((d, name) -> name.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
    }

    private List<String> globalConfigNames(String moduleName) {
        File[] files;
        File dir = new File("Night/GlobalConfigs/" + moduleName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if ((files = dir.listFiles((d, name) -> name.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
    }

    private List<String> bindProfileNames() {
        File[] files;
        File dir = new File("Night/Binds");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if ((files = dir.listFiles((d, name) -> name.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
    }

    private List<String> visualConfigNames() {
        File[] files;
        File dir = new File("Night/Visuals");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if ((files = dir.listFiles((d, name) -> name.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
    }

    private List<String> moduleStateNames() {
        File[] files;
        File dir = new File("Night/Modules");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if ((files = dir.listFiles((d, name) -> name.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(name -> name.substring(0, name.length() - ".json".length())).sorted().toList();
    }

    private List<Module> visualModules() {
        ArrayList<Module> modules = new ArrayList<Module>(Night.MODULE_MANAGER.getModules(Module.Category.VISUALS));
        for (Class<? extends Module> extra : EXTRA_VISUAL_MODULES) {
            Module module = Night.MODULE_MANAGER.getModule(extra);
            if (module == null) continue;
            modules.add(module);
        }
        return modules;
    }

    @Override
    public void execute(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("bind")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String profileName = args[2];
                switch (action) {
                    case "save": {
                        this.saveBindProfile(profileName);
                        break;
                    }
                    case "load": {
                        this.loadBindProfile(profileName);
                        break;
                    }
                    case "reset": {
                        this.resetBindProfile(profileName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("visual")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String name = args[2];
                switch (action) {
                    case "save": {
                        this.saveVisualConfig(name);
                        break;
                    }
                    case "load": {
                        this.loadVisualConfig(name);
                        break;
                    }
                    case "reset": {
                        this.resetVisualConfig(name);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("modules")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String name = args[2];
                switch (action) {
                    case "save": {
                        this.saveModulesState(name);
                        break;
                    }
                    case "load": {
                        this.loadModulesState(name);
                        break;
                    }
                    case "reset": {
                        this.resetModulesState(name);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("global")) {
            if (args.length == 4) {
                Module module = Night.MODULE_MANAGER.getModule(args[1]);
                if (module == null) {
                    Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                    return;
                }
                String action = args[2].toLowerCase();
                String configName = args[3];
                switch (action) {
                    case "save": {
                        this.saveGlobalConfig(module, configName);
                        break;
                    }
                    case "load": {
                        this.loadGlobalConfig(module, configName);
                        break;
                    }
                    case "reset": {
                        this.resetGlobalConfig(module, configName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
            }
            return;
        }
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("reset")) {
                this.resetConfig(args[0]);
                return;
            }
            switch (args[0].toLowerCase()) {
                case "load": {
                    if (!FileUtils.fileExists("Night/Configs/" + args[1] + ".json")) {
                        Night.CHAT_MANAGER.tagged("The specified configuration does not exist.", this.getTag(), this.getName());
                        return;
                    }
                    try {
                        Night.CONFIG_MANAGER.loadModules(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    catch (IOException exception) {
                        Night.CHAT_MANAGER.tagged("Failed to load the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "save": {
                    try {
                        Night.CONFIG_MANAGER.saveModules(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully saved the configuration to " + String.valueOf(ChatUtils.getPrimary()) + args[1] + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    }
                    catch (IOException exception) {
                        Night.CHAT_MANAGER.tagged("Failed to save the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "reset": {
                    this.resetConfig(args[1]);
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload": {
                    Night.CONFIG_MANAGER.loadConfig();
                    Night.CHAT_MANAGER.tagged("Successfully reloaded the current configuration.", this.getTag(), this.getName());
                    break;
                }
                case "save": {
                    Night.CONFIG_MANAGER.saveConfig();
                    Night.CHAT_MANAGER.tagged("Successfully saved the current configuration.", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    this.resetConfig(Night.CONFIG_MANAGER.getCurrentConfig());
                    break;
                }
                case "current": {
                    Night.CHAT_MANAGER.tagged("The client is currently using the " + String.valueOf(ChatUtils.getPrimary()) + Night.CONFIG_MANAGER.getCurrentConfig() + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else {
            this.messageSyntax();
        }
    }

    private void resetConfig(String configName) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        try {
            Night.CONFIG_MANAGER.saveModules(configName);
            Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " configuration to default.", this.getTag(), this.getName());
        }
        catch (IOException exception) {
            Night.CHAT_MANAGER.tagged("Failed to reset the " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
        }
    }

    private void resetGlobalConfig(Module module, String configName) {
        module.resetValues();
        this.saveGlobalConfig(module, configName);
    }

    private void resetBindProfile(String name) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.bind.resetValue();
            for (Setting uncastedSetting : module.getSettings()) {
                if (!(uncastedSetting instanceof BindSetting)) continue;
                BindSetting setting = (BindSetting)uncastedSetting;
                setting.resetValue();
            }
        }
        this.saveBindProfile(name);
    }

    private void resetVisualConfig(String name) {
        for (Module module : this.visualModules()) {
            module.resetValues();
        }
        this.saveVisualConfig(name);
    }

    private void resetModulesState(String name) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        this.saveModulesState(name);
    }

    private void saveGlobalConfig(Module module, String configName) {
        try {
            JsonObject moduleObject = Night.CONFIG_MANAGER.serializeModuleJson(module);
            File dir = new File("Night/GlobalConfigs/" + module.getName());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File globalFile = new File(dir, configName + ".json");
            try (FileWriter writer = new FileWriter(globalFile);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)moduleObject, (Appendable)writer);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to save global config: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void loadGlobalConfig(Module module, String configName) {
        File globalFile = new File("Night/GlobalConfigs/" + module.getName() + "/" + configName + ".json");
        if (!globalFile.exists()) {
            Night.CHAT_MANAGER.tagged("Global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " does not exist for " + module.getName() + ".", this.getTag(), this.getName());
            return;
        }
        try (FileReader reader = new FileReader(globalFile);){
            JsonObject globalModuleJson = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            File configsDir = new File("Night/Configs");
            File[] configFiles = configsDir.listFiles((d, name) -> name.endsWith(".json"));
            if (configFiles != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                for (File file : configFiles) {
                    JsonObject root;
                    try (FileReader fr = new FileReader(file);){
                        root = JsonParser.parseReader((Reader)fr).getAsJsonObject();
                    }
                    JsonObject modules = root.has("Modules") ? root.getAsJsonObject("Modules") : new JsonObject();
                    modules.add(module.getName(), (JsonElement)globalModuleJson);
                    root.add("Modules", (JsonElement)modules);
                    try (FileWriter fw = new FileWriter(file);){
                        gson.toJson((JsonElement)root, (Appendable)fw);
                    }
                }
            }
            Night.CONFIG_MANAGER.loadModules(Night.CONFIG_MANAGER.getCurrentConfig());
            Night.CHAT_MANAGER.tagged("Successfully loaded global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " across all profiles.", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to load global config: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void saveBindProfile(String name) {
        try {
            JsonObject root = new JsonObject();
            for (Module module : Night.MODULE_MANAGER.getModules()) {
                JsonObject moduleBinds = new JsonObject();
                moduleBinds.add(module.bind.getName(), (JsonElement)new JsonPrimitive(module.bind.getValue() + "," + module.bind.getMode()));
                for (Setting uncastedSetting : module.getSettings()) {
                    if (uncastedSetting == module.bind || !(uncastedSetting instanceof BindSetting)) continue;
                    BindSetting setting = (BindSetting)uncastedSetting;
                    moduleBinds.add(setting.getName(), (JsonElement)new JsonPrimitive(setting.getValue() + "," + setting.getMode()));
                }
                root.add(module.getName(), (JsonElement)moduleBinds);
            }
            File dir = new File("Night/Binds");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, name + ".json");
            try (FileWriter writer = new FileWriter(file);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)root, (Appendable)writer);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current binds to " + String.valueOf(ChatUtils.getPrimary()) + name + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to save bind profile: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void loadBindProfile(String name) {
        File file = new File("Night/Binds/" + name + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Bind profile " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader reader = new FileReader(file);){
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            for (String moduleName : root.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null) continue;
                JsonObject moduleBinds = root.getAsJsonObject(moduleName);
                for (String settingName : moduleBinds.keySet()) {
                    BindSetting target;
                    BindSetting bindSetting = target = settingName.equals(module.bind.getName()) ? module.bind : this.findBindSetting(module, settingName);
                    if (target == null) continue;
                    String[] parts = moduleBinds.get(settingName).getAsString().split(",", 2);
                    target.setValue(Integer.parseInt(parts[0]));
                    if (parts.length <= 1) continue;
                    target.setMode(parts[1]);
                }
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " bind profile.", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to load bind profile: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private BindSetting findBindSetting(Module module, String settingName) {
        for (Setting uncastedSetting : module.getSettings()) {
            BindSetting setting;
            if (!(uncastedSetting instanceof BindSetting) || !(setting = (BindSetting)uncastedSetting).getName().equals(settingName)) continue;
            return setting;
        }
        return null;
    }

    private void saveVisualConfig(String name) {
        try {
            JsonObject root = new JsonObject();
            for (Module module : this.visualModules()) {
                root.add(module.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(module));
            }
            File dir = new File("Night/Visuals");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, name + ".json");
            try (FileWriter writer = new FileWriter(file);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)root, (Appendable)writer);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current visual settings to " + String.valueOf(ChatUtils.getPrimary()) + name + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to save visual config: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void loadVisualConfig(String name) {
        File file = new File("Night/Visuals/" + name + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Visual config " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        Set allowedNames = this.visualModules().stream().map(Module::getName).collect(Collectors.toSet());
        try (FileReader reader = new FileReader(file);){
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            for (String moduleName : root.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null || !allowedNames.contains(module.getName())) continue;
                Night.CONFIG_MANAGER.applyModuleJson(module, root.getAsJsonObject(moduleName));
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " visual config.", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to load visual config: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void saveModulesState(String name) {
        try {
            JsonObject root = new JsonObject();
            for (Module module : Night.MODULE_MANAGER.getModules()) {
                root.add(module.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(module, false));
            }
            File dir = new File("Night/Modules");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, name + ".json");
            try (FileWriter writer = new FileWriter(file);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)root, (Appendable)writer);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current module states to " + String.valueOf(ChatUtils.getPrimary()) + name + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to save module states: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }

    private void loadModulesState(String name) {
        File file = new File("Night/Modules/" + name + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Module state profile " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader reader = new FileReader(file);){
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            for (String moduleName : root.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null) continue;
                JsonElement moduleElement = root.get(moduleName);
                if (moduleElement.isJsonObject()) {
                    Night.CONFIG_MANAGER.applyModuleJson(module, moduleElement.getAsJsonObject(), false);
                    continue;
                }
                if (!moduleElement.isJsonPrimitive() || !moduleElement.getAsJsonPrimitive().isBoolean()) continue;
                module.setToggled(moduleElement.getAsBoolean());
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " module states.", this.getTag(), this.getName());
        }
        catch (Exception e) {
            Night.CHAT_MANAGER.tagged("Failed to load module states: " + e.getMessage(), this.getTag(), this.getName());
            e.printStackTrace();
        }
    }
}

