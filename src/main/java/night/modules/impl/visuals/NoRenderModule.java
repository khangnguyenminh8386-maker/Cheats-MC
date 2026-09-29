/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.visuals;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="NoRender", description="Disables the rendering of certain things.", category=Module.Category.VISUALS)
public class NoRenderModule
extends Module {
    public BooleanSetting hurtCamera = new BooleanSetting("HurtCamera", "Disables the rendering of the hurt camera.", true);
    public BooleanSetting explosions = new BooleanSetting("Explosions", "Disables the rendering of explosion particles.", true);
    public BooleanSetting fireOverlay = new BooleanSetting("FireOverlay", "Disables the rendering of the fire overlay.", true);
    public BooleanSetting blockOverlay = new BooleanSetting("BlockOverlay", "Disables the rendering of the block suffocation overlay.", false);
    public BooleanSetting liquidOverlay = new BooleanSetting("LiquidOverlay", "Disables the rendering of the liquid overlay.", false);
    public BooleanSetting snowOverlay = new BooleanSetting("SnowOverlay", "Disables the rendering of the snow overlay.", false);
    public BooleanSetting pumpkinOverlay = new BooleanSetting("PumpkinOverlay", "Disables the rendering of the pumpkin overlay.", true);
    public BooleanSetting portalOverlay = new BooleanSetting("PortalOverlay", "Disables the rendering of the portal overlay.", false);
    public BooleanSetting totemAnimation = new BooleanSetting("TotemAnimation", "Disables the rendering of the totem pop animation.", false);
    public BooleanSetting totemPop = new BooleanSetting("TotemPop", "Disables the rendering of totem pop particles.", false);
    public BooleanSetting bossBar = new BooleanSetting("BossBar", "Disables the rendering of the boss bar.", false);
    public BooleanSetting scoreboard = new BooleanSetting("Scoreboard", "Disables the rendering of the scoreboard.", false);
    public BooleanSetting vignette = new BooleanSetting("Vignette", "Disables the rendering of the vignette.", true);
    public BooleanSetting blindness = new BooleanSetting("Blindness", "Disables the rendering of the blindness and darkness potion effects.", true);
    public BooleanSetting fog = new BooleanSetting("Fog", "Disables the rendering of the fog.", false);
    public BooleanSetting signText = new BooleanSetting("SignText", "Disables the rendering of sign text.", false);
    public BooleanSetting armor = new BooleanSetting("Armor", "Disables the rendering of armor.", false);
    public BooleanSetting limbSwing = new BooleanSetting("LimbSwing", "Disables the rendering of limb swing animations.", false);
    public BooleanSetting corpses = new BooleanSetting("Corpses", "Disables the rendering of corpses.", false);
    public BooleanSetting player = new BooleanSetting("Player", "Disables the rendering of other players standing in the same spot as you.", false);
    public BooleanSetting background = new BooleanSetting("Background", "Disables the dark background dimming behind inventory/other screens.", false);
    public ModeSetting tileEntities = new ModeSetting("TileEntities", "Disables the rendering of tile entities, such as chests, when meeting requirements.", "Never", new String[]{"Never", "Distance", "Always"});
    public NumberSetting tileDistance = new NumberSetting("TileDistance", "The distance at which tile entities will stop rendering.", new ModeSetting.Visibility(this.tileEntities, "Distance"), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(100.0f));
    public BooleanSetting items = new BooleanSetting("Items", "Limits how many dropped items are rendered in view.", false);
    public NumberSetting limit = new NumberSetting("Limit", "Max number of items to render per frame.", new BooleanSetting.Visibility(this.items, true), (Number)100, (Number)0, (Number)100);
    public BooleanSetting displays = new BooleanSetting("Displays", "Disables the rendering of Display entities (BlockDisplay, ItemDisplay, TextDisplay).", false);
    public BooleanSetting armorStand = new BooleanSetting("ArmorStand", "Disables the rendering of armor stands.", false);
    public BooleanSetting minecart = new BooleanSetting("Minecart", "Disables the rendering of minecarts.", false);
    public BooleanSetting advancements = new BooleanSetting("Advancements", "Disables the rendering of advancement/recipe toast popups.", false);
    public BooleanSetting itemName = new BooleanSetting("ItemName", "Hides the item name popup that shows above the hotbar when switching items.", false);
    public BooleanSetting wither = new BooleanSetting("Wither", "Stops hearts turning black/withered when the player has Wither effect.", false);
    private int renderedItems = 0;

    public void resetItemCounter() {
        this.renderedItems = 0;
    }

    public boolean shouldRenderItem() {
        if (!this.items.getValue()) {
            return true;
        }
        if (this.renderedItems >= this.limit.getValue().intValue()) {
            return false;
        }
        ++this.renderedItems;
        return true;
    }
}

