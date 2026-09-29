/*
 * Decompiled with CFR 0.152.
 */
package night.pingbypass.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import night.pingbypass.modules.PbModule;

public class PbModuleManager {
    public static final Set<String> MIGRATED_MODULE_NAMES = Set.of("AutoCrystal", "AutoTotem", "SpeedMine");
    private final List<PbModule> modules = new ArrayList<PbModule>();

    public void register(PbModule module) {
        this.modules.add(module);
        this.modules.sort(Comparator.comparing(PbModule::getName));
    }

    public PbModule getModule(String name) {
        return this.modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<PbModule> getModules() {
        return this.modules;
    }

    public void tick() {
        for (PbModule module : this.modules) {
            if (!module.isToggled()) continue;
            module.tick();
        }
    }
}

