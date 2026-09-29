/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.movement;

import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IAutoWalkModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="AutoWalk", description="Holds forward (or backward) movement every tick. Auto-pauses while Baritone is actively driving movement (e.g. EBounce+'s ObstaclePassing) so the two don't fight over input.", category=Module.Category.MOVEMENT)
public class AutoWalkModule
extends Module
implements IAutoWalkModule {
    public final BooleanSetting backward = new BooleanSetting("Backward", "Walk backward instead of forward.", false);

    @Override
    public boolean isBackward() {
        return this.backward.getValue();
    }
}

