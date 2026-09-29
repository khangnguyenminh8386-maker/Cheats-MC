/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.visuals;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="ViewModel", description="Modifies the position, scale and rotation of the player viewmodel.", category=Module.Category.VISUALS)
public class ViewModelModule
extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Hand modification mode.", "Each", new String[]{"Each", "Both"});
    public final BooleanSetting mainhandSwap = new BooleanSetting("MainhandSwap", "Disables the drop down animation when switching mainhand items.", true);
    public final BooleanSetting offhandSwap = new BooleanSetting("OffhandSwap", "Disables the drop down animation when switching offhand items.", true);
    public final CategorySetting mainHandCategory = new CategorySetting("MainHand", "Settings for main hand item rendering.");
    public final NumberSetting scaleMainX = new NumberSetting("ScaleMainX", "Scale X", "Scale on X axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting scaleMainY = new NumberSetting("ScaleMainY", "Scale Y", "Scale on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting scaleMainZ = new NumberSetting("ScaleMainZ", "Scale Z", "Scale on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting positionMainX = new NumberSetting("PositionMainX", "Pos X", "Translation on X axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting positionMainY = new NumberSetting("PositionMainY", "Pos Y", "Translation on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting positionMainZ = new NumberSetting("PositionMainZ", "Pos Z", "Translation on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting rotationMainX = new NumberSetting("RotationMainX", "Rot X", "Rotation on X axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final NumberSetting rotationMainY = new NumberSetting("RotationMainY", "Rot Y", "Rotation on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final NumberSetting rotationMainZ = new NumberSetting("RotationMainZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final CategorySetting offHandCategory = new CategorySetting("OffHand", "Settings for off hand item rendering.", new ModeSetting.Visibility(this.mode, "Each"));
    public final NumberSetting scaleOffX = new NumberSetting("ScaleOffX", "Scale X", "Scale on X axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting scaleOffY = new NumberSetting("ScaleOffY", "Scale Y", "Scale on Y axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting scaleOffZ = new NumberSetting("ScaleOffZ", "Scale Z", "Scale on Z axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public final NumberSetting positionOffX = new NumberSetting("PositionOffX", "Pos X", "Translation on X axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting positionOffY = new NumberSetting("PositionOffY", "Pos Y", "Translation on Y axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting positionOffZ = new NumberSetting("PositionOffZ", "Pos Z", "Translation on Z axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-3.0f), Float.valueOf(3.0f));
    public final NumberSetting rotationOffX = new NumberSetting("RotationOffX", "Rot X", "Rotation on X axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final NumberSetting rotationOffY = new NumberSetting("RotationOffY", "Rot Y", "Rotation on Y axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final NumberSetting rotationOffZ = new NumberSetting("RotationOffZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.Visibility(this.offHandCategory), Float.valueOf(0.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f));
    public final CategorySetting eatingCategory = new CategorySetting("Eating", "Settings for eating animation.");
    public final BooleanSetting eatAnimation = new BooleanSetting("EatAnimation", "Modify eating animation position.", new CategorySetting.Visibility(this.eatingCategory), false);
    public final NumberSetting eatX = new NumberSetting("EatX", "Eat X", "Eating offset X.", new BooleanSetting.Visibility(this.eatAnimation, true), Float.valueOf(1.0f), Float.valueOf(-1.0f), Float.valueOf(2.0f));
    public final NumberSetting eatY = new NumberSetting("EatY", "Eat Y", "Eating offset Y.", new BooleanSetting.Visibility(this.eatAnimation, true), Float.valueOf(1.0f), Float.valueOf(-1.0f), Float.valueOf(2.0f));
    public final NumberSetting eatingMultiplier = new NumberSetting("EatingMultiplier", "Eating Multiplier", "Scales the eating bob: 1 = vanilla, 0 = no bobbing (only the bring-to-mouth motion stays).", new CategorySetting.Visibility(this.eatingCategory), Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.1f));
}

