/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.core;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;

@RegisterModule(name="Rotations", description="Manages the client's rotation system.", category=Module.Category.CORE, persistent=true, drawn=false)
public class RotationsModule
extends Module {
    public BooleanSetting movementFix = new BooleanSetting("MovementFix", "Makes your movement in accordance with your yaw.", false);
    public BooleanSetting snapBack = new BooleanSetting("SnapBack", "Reverts rotations to previous values after rotating.", false);
    public ModeSetting jitter = new ModeSetting("Jitter", "Adds random noise to sent rotations so they're never bit-for-bit identical, defeating aim-consistency checks like Grim's AimModulo360.", "None", new String[]{"None", "Grim", "Normal"});
}

