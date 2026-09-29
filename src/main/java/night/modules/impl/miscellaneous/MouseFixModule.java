/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.miscellaneous;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="MouseFix", description="Fixes multiple mouse issues.", category=Module.Category.MISCELLANEOUS)
public class MouseFixModule
extends Module {
    public BooleanSetting customDebounce = new BooleanSetting("CustomDebounce", "Implements a custom debounce timer on mouse inputs.", true);
}

