/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  lombok.Generated
 */
package night.modules;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import night.Night;
import night.core.JarClassScanner;
import night.events.SubscribeEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseInputEvent;
import night.events.impl.TickEvent;
import night.gui.api.Frame;
import night.gui.impl.ModuleButton;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.core.HUDModule;
import night.modules.impl.core.PingBypassModule;
import night.modules.impl.player.AutoShopModule;
import night.settings.Setting;
import night.settings.impl.CategorySetting;
import night.utils.IMinecraft;
import night.utils.input.KeyboardUtils;

public class ModuleManager
implements IMinecraft {
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<? extends Module>, Module> moduleClasses = new Reference2ReferenceOpenHashMap();

    @lombok.SneakyThrows
    public ModuleManager() {
        Night.EVENT_HANDLER.subscribe(this);
        for (Class<?> clazz : JarClassScanner.findSubtypesOf(Module.class, Night.class)) {
            this.registerIfAnnotated(clazz);
        }
        Class<ModuleManager> clazz = ModuleManager.class;
        this.modules.sort(Comparator.comparing(Module::getName));
    }

    private void registerIfAnnotated(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (clazz.getAnnotation(RegisterModule.class) == null) {
            return;
        }
        Module module = (Module)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        this.register(module);
    }

    public void register(Module module) {
        if (module == null) {
            return;
        }
        try {
            for (Field field : module.getClass().getDeclaredFields()) {
                Setting setting;
                if (!Setting.class.isAssignableFrom(field.getType())) continue;
                if (!field.canAccess(module)) {
                    field.setAccessible(true);
                }
                if ((setting = (Setting)field.get(module)) == null) continue;
                if (module instanceof HUDModule) {
                    CategorySetting.Visibility v;
                    Setting.Visibility visibility;
                    HUDModule hud = (HUDModule)module;
                    if (!HUDModule.isMusicAddonLoaded() && (setting == hud.musicHudCategory || setting == hud.musicHudPosition || (visibility = setting.getVisibility()) instanceof CategorySetting.Visibility && (v = (CategorySetting.Visibility)visibility).getValue() == hud.musicHudCategory)) continue;
                }
                module.getSettings().add(setting);
            }
        }
        catch (IllegalAccessException exception) {
            Night.LOGGER.error("Failed to register settings for module " + module.getName(), (Throwable)exception);
        }
        module.getSettings().add(module.chatNotify);
        module.getSettings().add(module.drawn);
        if (!module.isPersistent() && !(module instanceof AutoShopModule)) {
            module.getSettings().add(module.bind);
        }
        if (!this.modules.contains(module)) {
            this.modules.add(module);
            this.moduleClasses.put(module.getClass(), module);
            this.modules.sort(Comparator.comparing(Module::getName));
            if (Night.CLICK_GUI != null) {
                for (Frame frame : Night.CLICK_GUI.getFrames()) {
                    if (frame.getCategory() != module.getCategory() || module instanceof PingBypassModule) continue;
                    frame.getButtons().add(new ModuleButton(module, frame, frame.getHeight()));
                }
            }
        }
    }

    public void unregister(Module module) {
        if (module == null) {
            return;
        }
        if (module.isToggled()) {
            module.setToggled(false);
        }
        this.modules.remove(module);
        this.moduleClasses.remove(module.getClass());
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        this.modules.stream().filter(m -> m.getBind() == event.getKey() && m.bind.getMode().equals("Bind")).forEach(m -> m.setToggled(!m.isToggled()));
        AutoShopModule autoShop = this.getModule(AutoShopModule.class);
        if (autoShop != null) {
            autoShop.handleKey(event.getKey());
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        this.modules.stream().filter(m -> m.getBind() == -event.getButton() - 1 && m.bind.getMode().equals("Bind")).forEach(m -> m.setToggled(!m.isToggled()));
        AutoShopModule autoShop = this.getModule(AutoShopModule.class);
        if (autoShop != null) {
            autoShop.handleKey(-event.getButton() - 1);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        for (Module module : this.modules) {
            boolean shouldBeToggled;
            String mode = module.bind.getMode();
            if (mode.equals("Bind") || module.getBind() == 0) continue;
            boolean held = KeyboardUtils.isBindDown(module.getBind());
            boolean bl = shouldBeToggled = mode.equals("Hold") == held;
            if (module.isToggled() == shouldBeToggled) continue;
            module.setToggled(shouldBeToggled);
        }
    }

    public List<Module> getModules(Module.Category category) {
        return this.modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    public Module getModule(String name) {
        return this.modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return (T)this.moduleClasses.get(clazz);
    }

    @Generated
    public List<Module> getModules() {
        return this.modules;
    }

    @Generated
    public Map<Class<? extends Module>, Module> getModuleClasses() {
        return this.moduleClasses;
    }
}
