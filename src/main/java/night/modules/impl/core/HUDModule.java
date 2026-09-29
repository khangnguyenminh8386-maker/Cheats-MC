/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.client.gui.components.PlayerFaceExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.resources.Identifier
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix3x2fStack
 */
package night.modules.impl.core;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderOverlayEvent;
import night.events.impl.SettingChangeEvent;
import night.events.impl.TickEvent;
import night.gui.HUDEditorScreen;
import night.managers.FontManager;
import night.managers.HudElementRegistry;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.core.FontModule;
import night.modules.impl.core.IgnoreNakedModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.PositionSetting;
import night.settings.impl.StringSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;
import night.utils.input.KeyboardUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.text.FormattingUtils;
import org.joml.Matrix3x2fStack;

@RegisterModule(name="HUD", description="Renders information about the game and the client on the screen.", category=Module.Category.CORE, toggled=true, drawn=false)
public class HUDModule
extends Module {
    private static final Identifier LOGO_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/gui/logo.png");
    private static final int LOGO_SIZE = 64;
    public CategorySetting watermarkCategory = new CategorySetting("Watermark", "The settings for the client's watermark.");
    public BooleanSetting watermark = new BooleanSetting("Watermark", "Enabled", "Renders the client's name and version at the top left.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public StringSetting watermarkText = new StringSetting("WatermarkText", "Component", "The client name that will be rendered.", new CategorySetting.Visibility(this.watermarkCategory), "Cheats MC");
    public BooleanSetting watermarkLogo = new BooleanSetting("WatermarkLogo", "Logo", "Renders the client's logo before the watermark text, scaled to the text's own line height.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public BooleanSetting watermarkVersion = new BooleanSetting("WatermarkVersion", "Version", "Renders the client's version after the name.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public BooleanSetting watermarkMinecraftVersion = new BooleanSetting("WatermarkMinecraftVersion", "MinecraftVersion", "Renders the client's minecraft version after the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
    public ModeSetting watermarkVersionMode = new ModeSetting("WatermarkVersionMode", "Revision", "What to render next to the version: the git revision/hash, or a nightly-build date stamp.", new BooleanSetting.Visibility(this.watermarkVersion, true), "GitHash", new String[]{"GitHash", "Nightly"});
    public BooleanSetting watermarkSync = new BooleanSetting("WatermarkSync", "ColorSync", "Uses the client's color for the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
    public CategorySetting welcomerCategory = new CategorySetting("Welcomer", "The settings for the client's welcomer.");
    public BooleanSetting welcomer = new BooleanSetting("Welcomer", "Enabled", "Renders a nice welcome message directed to you.", new CategorySetting.Visibility(this.welcomerCategory), true);
    public StringSetting welcomerText = new StringSetting("WelcomerText", "Component", "The message that will be rendered.", new CategorySetting.Visibility(this.welcomerCategory), "Cracked By \u00a7f@6mf5");
    public BooleanSetting welcomerSync = new BooleanSetting("WelcomerSync", "ColorSync", "Uses the client's color for the username.", new CategorySetting.Visibility(this.welcomerCategory), false);
    public CategorySetting moduleListCategory = new CategorySetting("ModuleList", "The settings for the client's list of enabled modules.");
    public BooleanSetting moduleList = new BooleanSetting("ModuleList", "Enabled", "Renders every enabled module in an organized list.", new CategorySetting.Visibility(this.moduleListCategory), true);
    public ModeSetting moduleListMode = new ModeSetting("ModuleListMode", "Mode", "The display mode for the module list.", new CategorySetting.Visibility(this.moduleListCategory), "Pro", new String[]{"Pro", "Newbie"});
    public BooleanSetting metaData = new BooleanSetting("MetaData", "Whether or not to show module metadata in the module list.", new CategorySetting.Visibility(this.moduleListCategory), true);
    public BooleanSetting bindOnly = new BooleanSetting("BindOnly", "Only lists modules that have a keybind set.", new CategorySetting.Visibility(this.moduleListCategory), false);
    public ModeSetting moduleColorMode = new ModeSetting("ModuleColor", "Color", "The color mode for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Default", new String[]{"Default", "Rainbow", "Random"});
    public ModeSetting moduleListSorting = new ModeSetting("ModuleListSorting", "Sorting", "The sorting for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Width", new String[]{"Width", "Alphabetical"});
    public CategorySetting playerRadarCategory = new CategorySetting("Player Radar", "The settings for the client's list of players in render distance.");
    public BooleanSetting playerRadar = new BooleanSetting("PlayerRadar", "Enabled", "Renders the name of every player in render distance.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public NumberSetting playerRadarLimit = new NumberSetting("PlayerRadarLimit", "Limit", "The maximum amount of players that will be listed. Setting it to 0 means removing the limiter.", new CategorySetting.Visibility(this.playerRadarCategory), 8, 0, 100);
    public ModeSetting playerRadarSorting = new ModeSetting("PlayerRadarSorting", "Sorting", "The sorting for the players on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), "Distance", new String[]{"None", "Distance", "Alphabetical"});
    public BooleanSetting playerRadarAntiBot = new BooleanSetting("PlayerRadarAntiBot", "AntiBot", "Prevents bots from being listed on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerIcons = new BooleanSetting("PlayerIcons", "Icons", "Renders the player's head icon next to their name.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarDistance = new BooleanSetting("PlayerRadarDistance", "Distance", "Renders the distance between you and the player.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarEntityID = new BooleanSetting("PlayerRadarEntityID", "EntityID", "Renders the player's entity ID.", new CategorySetting.Visibility(this.playerRadarCategory), false);
    public BooleanSetting playerRadarGameMode = new BooleanSetting("PlayerRadarGameMode", "GameType", "Renders the player's current gamemode.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarPing = new BooleanSetting("PlayerRadarPing", "Ping", "Renders the player's current latency.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarHealth = new BooleanSetting("PlayerRadarHealth", "Health", "Renders the player's current health.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarTotems = new BooleanSetting("PlayerRadarTotems", "Totems", "Renders the amount of totems that the player has popped.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarTurtlePot = new BooleanSetting("PlayerRadarTurtlePot", "TurtlePotIndicator", "Indicates if the player has Resistance 3 or higher.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public CategorySetting itemsCategory = new CategorySetting("Items", "The settings for information about items in your inventory and specific item counters.");
    public BooleanSetting armor = new BooleanSetting("Armor", "Renders the armor you're currently wearing and its status.", new CategorySetting.Visibility(this.itemsCategory), true);
    public ModeSetting armorDurability = new ModeSetting("ArmorDurability", "Durability", "The way that the durability will be rendered in.", new BooleanSetting.Visibility(this.armor, true), "Both", new String[]{"None", "Bar", "Percentage", "Both"});
    public BooleanSetting totemCounter = new BooleanSetting("TotemCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting crystalCounter = new BooleanSetting("CrystalCounter", "Renders the amount of crystals that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting xpCounter = new BooleanSetting("XPCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting counterChatOffset = new BooleanSetting("CounterChatOffset", "ChatOffset", "Offsets the crystal and XP counter's positions whenever the chat is open.", new CategorySetting.Visibility(this.itemsCategory), false);
    public BooleanSetting itemsSmooth = new BooleanSetting("ItemsSmooth", "Smooth", "Smoothly animates item counts when they change (sliding/lerping).", new CategorySetting.Visibility(this.itemsCategory), true);
    public CategorySetting inventoryCategory = new CategorySetting("Inventory", "The settings for displaying the inventory on the HUD.");
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Enabled", "Renders your inventory (excluding hotbar) on the screen even when closed.", new CategorySetting.Visibility(this.inventoryCategory), false);
    public BooleanSetting inventoryBackground = new BooleanSetting("InventoryBackground", "Background", "Renders a dark background behind the inventory grid.", new CategorySetting.Visibility(this.inventoryCategory), true);
    public BooleanSetting inventorySlotBackground = new BooleanSetting("InventorySlotBackground", "SlotBoxes", "Renders subtle slot boxes for each inventory slot.", new CategorySetting.Visibility(this.inventoryCategory), true);
    public NumberSetting inventoryAlpha = new NumberSetting("InventoryAlpha", "Alpha", "Background opacity (0-255).", new CategorySetting.Visibility(this.inventoryCategory), 140.0, 0.0, 255.0);
    public PositionSetting inventoryPosition = new PositionSetting("InventoryPosition", "Drag offset for the inventory HUD element.");
    public CategorySetting informationCategory = new CategorySetting("Information", "The settings for information about the game and the client.");
    public BooleanSetting health = new BooleanSetting("Health", "Renders your current health in the middle of the screen.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting ping = new BooleanSetting("Ping", "Renders your current latency to the server in milliseconds.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting tps = new BooleanSetting("TPS", "Renders the server's current tick-rate.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting fps = new BooleanSetting("FPS", "Renders the game's frames per second counter.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting durability = new BooleanSetting("Durability", "Renders your held item durability.", new CategorySetting.Visibility(this.informationCategory), true);
    public ModeSetting speed = new ModeSetting("Speed", "Renders the speed that you are currently moving at.", new CategorySetting.Visibility(this.informationCategory), "Kilometers", new String[]{"None", "Meters", "Kilometers"});
    public BooleanSetting uptime = new BooleanSetting("Uptime", "Renders the uptime of the client.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting serverBrand = new BooleanSetting("ServerBrand", "Renders the brand of the server that you are currently on.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting informationSync = new BooleanSetting("InformationSync", "ColorSync", "Uses the client's color for the information elements.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting informationChatOffset = new BooleanSetting("InformationChatOffset", "ChatOffset", "Offsets the rendering when the chat is open.", new CategorySetting.Visibility(this.informationCategory), true);
    public CategorySetting potionsCategory = new CategorySetting("Potions", "The settings for information about potion effects and their status.");
    public BooleanSetting potions = new BooleanSetting("Potions", "Enabled", "Renders the name and status of every potion effect you have.", new CategorySetting.Visibility(this.potionsCategory), true);
    public BooleanSetting potionIcons = new BooleanSetting("PotionIcons", "Icons", "Whether or not to render the icons next to the potion's name.", new CategorySetting.Visibility(this.potionsCategory), true);
    public ModeSetting potionColor = new ModeSetting("PotionColor", "Color", "The color that will be used in rendering the potion's text.", new CategorySetting.Visibility(this.potionsCategory), "Enhanced", new String[]{"Vanilla", "Enhanced", "Client"});
    public ModeSetting potionSorting = new ModeSetting("PotionSorting", "Sorting", "The sorting for the potion effects rendered.", new CategorySetting.Visibility(this.potionsCategory), "Alphabetical", new String[]{"None", "Width", "Alphabetical"});
    public ModeSetting vanillaPotions = new ModeSetting("VanillaPotions", "The way that the vanilla potion icons will be handled.", new CategorySetting.Visibility(this.potionsCategory), "Hide", new String[]{"Keep", "Move", "Hide"});
    public CategorySetting positionCategory = new CategorySetting("Position", "The settings for information about your current position and velocity.");
    public BooleanSetting coordinates = new BooleanSetting("Coordinates", "Renders your current coordinates.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting netherCoordinates = new BooleanSetting("NetherCoordinates", "Renders your current coordinates in the alternate dimension.", new BooleanSetting.Visibility(this.coordinates, true), true);
    public BooleanSetting fakeCoords = new BooleanSetting("FakeCoords", "Fakes your coordinates to prevent leaking.", new CategorySetting.Visibility(this.positionCategory), false);
    public NumberSetting fakeThreshold = new NumberSetting("FakeThreshold", "Threshold", "Minimum coordinate value required to start faking.", new BooleanSetting.Visibility(this.fakeCoords, true), 1000, 0, 100000);
    public BooleanSetting direction = new BooleanSetting("Direction", "Renders the current direction that you are facing.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting positionSync = new BooleanSetting("PositionSync", "ColorSync", "Uses the client's color for the position elements.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting positionChatOffset = new BooleanSetting("PositionChatOffset", "ChatOffset", "Offsets the text when the chat is open.", new CategorySetting.Visibility(this.positionCategory), true);
    public CategorySetting colorCategory = new CategorySetting("Color", "The settings for the coloring of the text.");
    public ModeSetting colorMode = new ModeSetting("Color", "The color that will be applied to the text.", new CategorySetting.Visibility(this.colorCategory), "Default", new String[]{"Default", "Rainbow", "Wave", "Custom"});
    public ColorSetting customColor = new ColorSetting("CustomColor", "The color that will be used for the Custom mode.", new ModeSetting.Visibility(this.colorMode, "Custom"), ColorUtils.getDefaultColor());
    public ModeSetting rainbowMode = new ModeSetting("Rainbow", "The mode for the HUD Rainbow.", new ModeSetting.Visibility(this.colorMode, "Rainbow"), "Vertical", new String[]{"Vertical", "Horizontal"});
    public NumberSetting rainbowOffset = new NumberSetting("RainbowOffset", "Offset", "The offset that will be applied to the rainbow.", new ModeSetting.Visibility(this.colorMode, "Rainbow", "Wave"), 10L, 1L, 50L);
    public BooleanSetting inversion = new BooleanSetting("Inversion", "Inverts primary and secondary colors.", new CategorySetting.Visibility(this.colorCategory), false);
    public BooleanSetting textGlow = new BooleanSetting("TextGlow", "Glow", "Renders a bloom glow effect for HUD text.", new CategorySetting.Visibility(this.colorCategory), false);
    public BooleanSetting informationElement = new BooleanSetting("InformationElement", "Whether the ping/fps/tps/etc information block is shown at all. See HUDEditor.", true);
    public PositionSetting watermarkPosition = new PositionSetting("WatermarkPosition", "Drag offset for the watermark HUD element.");
    public PositionSetting welcomerPosition = new PositionSetting("WelcomerPosition", "Drag offset for the welcomer HUD element.");
    public PositionSetting moduleListPosition = new PositionSetting("ModuleListPosition", "Drag offset for the module list HUD element.");
    public PositionSetting playerRadarPosition = new PositionSetting("PlayerRadarPosition", "Drag offset for the player radar HUD element.");
    public PositionSetting armorPosition = new PositionSetting("ArmorPosition", "Drag offset for the armor HUD element.");
    public PositionSetting totemCounterPosition = new PositionSetting("TotemCounterPosition", "Drag offset for the totem counter HUD element.");
    public PositionSetting crystalCounterPosition = new PositionSetting("CrystalCounterPosition", "Drag offset for the crystal counter HUD element.");
    public PositionSetting xpCounterPosition = new PositionSetting("XPCounterPosition", "Drag offset for the XP counter HUD element.");
    public PositionSetting informationPosition = new PositionSetting("InformationPosition", "Drag offset for the information HUD element.");
    public PositionSetting coordinatesPosition = new PositionSetting("CoordinatesPosition", "Drag offset for the coordinates HUD element.");
    public PositionSetting potionsPosition = new PositionSetting("PotionsPosition", "Drag offset for the potions HUD element.");
    public PositionSetting musicHudPosition = new PositionSetting("MusicHUDPosition", "Drag offset for the Music HUD element.");
    public CategorySetting musicHudCategory = new CategorySetting("MusicHUD", "Settings for the Music HUD.");
    public BooleanSetting musicHud = new BooleanSetting("MusicHUD", "Enabled", "Toggle Music HUD display.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public NumberSetting musicHudBarAlpha = new NumberSetting("MusicHudBarAlpha", "BarAlpha", "Transparency of the visualizer bars (0-255).", new CategorySetting.Visibility(this.musicHudCategory), 160.0, 0.0, 255.0);
    public BooleanSetting musicHudGradientBars = new BooleanSetting("MusicHudGradientBars", "GradientBars", "Gradient colored visualizer bars.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public ModeSetting musicHudBgMode = new ModeSetting("MusicHudBackground", "Background", "Background style of the HUD.", new CategorySetting.Visibility(this.musicHudCategory), "LiquidGlass", new String[]{"LiquidGlass", "Blur", "Default"});
    public NumberSetting musicHudBlurIntensity = new NumberSetting("MusicHudBlurIntensity", "BlurIntensity", "Intensity of the blur effect.", new CategorySetting.Visibility(this.musicHudCategory), 3.0, 0.0, 50.0);
    public BooleanSetting musicHudCompactMode = new BooleanSetting("MusicHudCompactMode", "CompactMode", "Collapse the HUD, limiting its width.", new CategorySetting.Visibility(this.musicHudCategory), false);
    public BooleanSetting musicHudDisk = new BooleanSetting("MusicHudDisk", "Disk", "Enable to show the spinning record.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public BooleanSetting musicHudUltraDisk = new BooleanSetting("MusicHudUltraDisk", "UltraDisk", "Only display the record, hide everything else.", new CategorySetting.Visibility(this.musicHudCategory), false);
    public NumberSetting musicHudDiskSize = new NumberSetting("MusicHudDiskSize", "DiskSize", "Size of the music disk (Ultra Disk).", new CategorySetting.Visibility(this.musicHudCategory), 100.0, 30.0, 300.0);
    public BooleanSetting musicHudTextBloom = new BooleanSetting("MusicHudTextBloom", "TextBloom", "Text bloom glow effect.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public BooleanSetting musicHudTextBloomPlus = new BooleanSetting("MusicHudTextBloomPlus", "TextBloomPlus", "Enhanced Skia text bloom glow.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public ColorSetting musicHudColor = new ColorSetting("MusicHudColor", "Color", "Accent color used for the visualizer.", new CategorySetting.Visibility(this.musicHudCategory), ColorUtils.getDefaultColor());
    public ModeSetting musicHudButtonEffect = new ModeSetting("MusicHudButtonEffect", "ButtonEffect", "Visual effect when clicking buttons.", new CategorySetting.Visibility(this.musicHudCategory), "Gradient", new String[]{"Splash", "Topo", "Gradient"});
    public static HUDModule INSTANCE;
    private final SmoothCounter totemSmoothCounter = new SmoothCounter();
    private final SmoothCounter crystalSmoothCounter = new SmoothCounter();
    private final SmoothCounter xpSmoothCounter = new SmoothCounter();
    private final Animation potionsAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
    private final Animation chatAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
    private float chatOffset;
    private List<ModuleEntry> moduleEntries = new ArrayList<ModuleEntry>();
    private List<PlayerEntry> playerEntries = new ArrayList<PlayerEntry>();
    private List<PotionEntry> potionEntries = new ArrayList<PotionEntry>();
    private final int fakeOffsetX = 2000000 + new Random().nextInt(8000000);
    private final int fakeOffsetZ = 2000000 + new Random().nextInt(8000000);
    private static final EquipmentSlot[] ARMOR_SLOTS;
    private static ItemStack[] dummyArmor;
    private final Map<Module, Animation> moduleColorAnimations = new HashMap<Module, Animation>();

    public static boolean isMusicAddonLoaded() {
        return FabricLoader.getInstance().isModLoaded("night-music") || Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule("PlayMusic") != null;
    }

    public HUDModule() {
        INSTANCE = this;
        HudElementRegistry.register("Watermark", this.watermark, this.watermarkPosition, this.watermarkCategory);
        HudElementRegistry.register("Welcomer", this.welcomer, this.welcomerPosition, this.welcomerCategory);
        HudElementRegistry.register("ModuleList", this.moduleList, this.moduleListPosition, this.moduleListCategory);
        HudElementRegistry.register("PlayerRadar", this.playerRadar, this.playerRadarPosition, this.playerRadarCategory);
        HudElementRegistry.register("Armor", this.armor, this.armorPosition);
        HudElementRegistry.register("TotemCounter", this.totemCounter, this.totemCounterPosition);
        HudElementRegistry.register("CrystalCounter", this.crystalCounter, this.crystalCounterPosition);
        HudElementRegistry.register("XPCounter", this.xpCounter, this.xpCounterPosition);
        HudElementRegistry.register("Inventory", this.inventory, this.inventoryPosition, this.inventoryCategory);
        HudElementRegistry.register("Information", this.informationElement, this.informationPosition, this.informationCategory);
        HudElementRegistry.register("Coordinates", this.coordinates, this.coordinatesPosition, this.positionCategory);
        HudElementRegistry.register("Potions", this.potions, this.potionsPosition, this.potionsCategory);
        this.musicHudPosition.set(20.0f, 60.0f);
        this.inventoryPosition.set(20.0f, 120.0f);
    }

    private int getFakedCoord(int real, int baseOffset) {
        long finalVal;
        if (!this.fakeCoords.getValue()) {
            return real;
        }
        if (Math.abs(real) < this.fakeThreshold.getValue().intValue()) {
            return real;
        }
        long scaledOffset = (long)baseOffset + (long)Math.abs(real) / 3L;
        boolean negativeSign = (Math.abs(real) * 31 + baseOffset) % 2 == 0;
        long l = finalVal = negativeSign ? -scaledOffset : scaledOffset;
        if (HUDModule.mc.player != null && HUDModule.mc.player.level() != null && HUDModule.mc.player.level().dimension() == Level.NETHER) {
            finalVal /= 8L;
        }
        return (int)finalVal;
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent event) {
        if (event.getSetting() == this.textGlow) {
            FontModule fontModule;
            FontModule fontModule2 = fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
            if (fontModule != null && fontModule.glow.getValue() != this.textGlow.getValue()) {
                fontModule.glow.setValue(this.textGlow.getValue());
            }
        }
    }

    @SubscribeEvent
   public void onTick(TickEvent event) {
      if (mc.player != null && mc.level != null) {
         if (this.moduleList.getValue()) {
            Comparator<Module> widthComparator = Comparator.comparingInt(m -> -Night.FONT_MANAGER.getWidth(this.getModuleText(m)));
            Comparator<Module> alphabeticalComparator = Comparator.comparing(Module::getName);
            boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
            List<HUDModule.ModuleEntry> entries = new ArrayList<>();

            for (Module module : Night.MODULE_MANAGER.getModules().stream().filter(modulex -> modulex.drawn.getValue()).filter(modulex -> {
               if (isNewbie) {
                  return modulex.getBind() != 0 ? true : !this.bindOnly.getValue() && (modulex.isToggled() || modulex.getAnimationOffset().value() > 0.01F);
               } else {
                  return this.bindOnly.getValue() && modulex.getBind() == 0 ? false : modulex.isToggled() || modulex.getAnimationOffset().value() > 0.01F;
               }
            }).sorted(this.moduleListSorting.getValue().equalsIgnoreCase("Width") ? widthComparator : alphabeticalComparator).toList()) {
               String text = this.getModuleText(module);
               entries.add(new HUDModule.ModuleEntry(module, text));
            }

            this.moduleEntries = entries;
         }

         if (this.playerRadar.getValue()) {
            Comparator<AbstractClientPlayer> distanceComparator = Comparator.comparingDouble(p -> mc.player.distanceTo(p));
            Comparator<AbstractClientPlayer> alphabeticalComparator = Comparator.comparing(p -> p.getName().getString());
            List<HUDModule.PlayerEntry> entries = new ArrayList<>();

            for (Player player : mc.level
               .players()
               .stream()
               .filter(p -> p != mc.player)
               .filter(p -> !EntityUtils.isGhost(p))
               .filter(p -> !this.playerRadarAntiBot.getValue() || !EntityUtils.isBot(p))
               .filter(p -> !Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() || !EntityUtils.isNaked(p))
               .sorted(this.playerRadarSorting.getValue().equalsIgnoreCase("Distance") ? distanceComparator : alphabeticalComparator)
               .limit(this.playerRadarLimit.getValue().longValue())
               .toList()) {
               Identifier headTexture = null;
               if (this.playerIcons.getValue() && mc.getConnection() != null) {
                  PlayerInfo entry = mc.getConnection().getPlayerInfo(player.getName().getString());
                  if (entry != null) {
                     headTexture = entry.getSkin().body().texturePath();
                  }
               }

               String text = player.getName().getString();
               if (this.playerRadarDistance.getValue()) {
                  text = text + ChatFormatting.WHITE + " " + new DecimalFormat("0.0").format(mc.player.distanceTo(player));
               }

               if (this.playerRadarEntityID.getValue()) {
                  text = text + ChatFormatting.WHITE + " " + player.getId();
               }

               if (this.playerRadarGameMode.getValue()) {
                  text = text + ChatFormatting.WHITE + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
               }

               if (this.playerRadarPing.getValue()) {
                  text = text + ChatFormatting.WHITE + " " + EntityUtils.getLatency(player) + "ms";
               }

               if (this.playerRadarHealth.getValue()) {
                  text = text
                     + ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount())
                     + " "
                     + new DecimalFormat("0.0").format(player.getHealth() + player.getAbsorptionAmount())
                     + ChatFormatting.RESET;
               }

               int pops = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
               if (this.playerRadarTotems.getValue() && pops > 0) {
                  text = text + ColorUtils.getTotemColor(pops) + " -" + pops + ChatFormatting.RESET;
               }

               if (this.playerRadarTurtlePot.getValue()) {
                  MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
                  if (resistance != null && resistance.getAmplifier() >= 2) {
                     text = text + ChatFormatting.RED + " [TM]" + ChatFormatting.RESET;
                  }
               }

               entries.add(new HUDModule.PlayerEntry(player, text, headTexture));
            }

            this.playerEntries = entries;
         }

         if (this.potions.getValue()) {
            Comparator<MobEffectInstance> widthComparator = Comparator.comparingInt(
               e -> -Night.FONT_MANAGER.getWidth(e.getEffect().value().getDisplayName().getString() + " " + (e.getAmplifier() + 1))
            );
            Comparator<MobEffectInstance> alphabeticalComparator = Comparator.comparing(e -> e.getEffect().value().getDisplayName().getString());
            List<HUDModule.PotionEntry> entries = new ArrayList<>();

            for (MobEffectInstance effect : mc.player
               .getActiveEffects()
               .stream()
               .sorted(this.potionSorting.getValue().equalsIgnoreCase("Width") ? widthComparator : alphabeticalComparator)
               .toList()) {
               String text = this.getPotionText(effect);
               Identifier sprite = null;
               if (this.potionIcons.getValue()) {
                  sprite = Hud.getMobEffectSprite(effect.getEffect());
               }

               entries.add(
                  new HUDModule.PotionEntry(
                     text,
                     sprite,
                     this.potionColor.getValue().equalsIgnoreCase("Vanilla")
                        ? new Color(effect.getEffect().value().getColor())
                        : (
                           this.potionColor.getValue().equalsIgnoreCase("Enhanced")
                              ? (
                                 EntityUtils.POTION_COLORS.containsKey(effect.getEffect().value())
                                    ? EntityUtils.POTION_COLORS.get(effect.getEffect().value())
                                    : new Color(effect.getEffect().value().getColor())
                              )
                              : null
                        )
                  )
               );
            }

            this.potionEntries = entries;
         }
      }
   }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        FontManager.setHudRendering(true);
        try {
            this.chatOffset = this.chatAnimation.get(HUDModule.mc.gui.screen() instanceof ChatScreen ? 14.0f : 0.0f);
            Renderer2D.renderQuad(event.getContext(), 2.0f, (float)mc.getWindow().getGuiScaledHeight() - this.chatOffset, mc.getWindow().getGuiScaledWidth() - 2, (float)(mc.getWindow().getGuiScaledHeight() + 12) - this.chatOffset, new Color(0, 0, 0, (int)((Double)HUDModule.mc.options.textBackgroundOpacity().get() * 255.0)));
            this.renderWatermark(event);
            this.renderModuleList(event);
            this.renderPlayerRadar(event);
            this.renderItems(event);
            this.renderInventory(event);
            this.renderPotions(event);
            this.renderInformation(event);
            this.renderCoordinates(event);
        }
        finally {
            FontManager.setHudRendering(false);
        }
    }

    private void renderWatermark(RenderOverlayEvent event) {
        Matrix3x2fStack matrices = event.getMatrices();
        if (this.watermark.getValue() || this.uptime.getValue()) {
            matrices.pushMatrix();
            matrices.translate(this.watermarkPosition.getX(), this.watermarkPosition.getY());
            int width = 0;
            int lines = 0;
            if (this.watermark.getValue()) {
                String displayVersion;
                boolean revisionAllowed = true;
                String string = displayVersion = revisionAllowed ? "v26" : "26";
                String revisionSegment = !revisionAllowed || !this.watermarkVersion.getValue() ? "" : (!this.watermarkVersionMode.getValue().equals("GitHash") ? "-nightly " + String.valueOf(ChatFormatting.GRAY) + "(2026-09-17)" : "+436.eeeb439b78");
                String text = this.watermarkText.getValue() + (String)(this.watermarkVersion.getValue() ? String.valueOf(this.watermarkSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + " " + displayVersion + (this.watermarkMinecraftVersion.getValue() ? "-mc26.2" : "") + revisionSegment : "");
                float textX = 2.0f;
                if (this.watermarkLogo.getValue()) {
                    int logoSize = Night.FONT_MANAGER.getHeight();
                    int logoColor = this.getHudColor(2.0f).getRGB();
                    event.getContext().blit(RenderPipelines.GUI_TEXTURED, LOGO_ID, 2, 2, 0.0f, 0.0f, logoSize, logoSize, 64, 64, 64, 64, logoColor);
                    textX = 2 + logoSize + 3;
                }
                this.drawText(event.getContext(), text, textX, 2.0f);
                width = Math.max(width, (int)(textX - 2.0f) + Night.FONT_MANAGER.getWidth(text));
                ++lines;
            }
            if (this.uptime.getValue()) {
                String[] hms = FormattingUtils.formatSeconds((System.currentTimeMillis() - Night.UPTIME) / 1000L);
                String text = "Uptime " + String.valueOf(ChatFormatting.WHITE) + hms[0] + ":" + hms[1] + ":" + hms[2];
                this.drawText(event.getContext(), text, 2.0f, 2 + (this.watermark.getValue() ? Night.FONT_MANAGER.getHeight() : 0), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                width = Math.max(width, Night.FONT_MANAGER.getWidth(text));
                ++lines;
            }
            HudElementRegistry.reportBounds("Watermark", 2.0f, 2.0f, 2 + width, 2 + lines * Night.FONT_MANAGER.getHeight());
            matrices.popMatrix();
        }
        if (this.welcomer.getValue()) {
            matrices.pushMatrix();
            matrices.translate(this.welcomerPosition.getX(), this.welcomerPosition.getY());
            String text = this.welcomerText.getValue().replace("[username]", String.valueOf(this.welcomerSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + HUDModule.mc.player.getName().getString() + String.valueOf(ChatFormatting.RESET));
            float x = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - (float)Night.FONT_MANAGER.getWidth(text) / 2.0f;
            this.drawText(event.getContext(), text, x, 6.0f);
            HudElementRegistry.reportBounds("Welcomer", x, 6.0f, x + (float)Night.FONT_MANAGER.getWidth(text), 6 + Night.FONT_MANAGER.getHeight());
            matrices.popMatrix();
        }
    }

    private void renderModuleList(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.moduleList.getValue()) {
            return;
        }
        float potionOffset = this.potionsAnimation.get(!this.vanillaPotions.getValue().equalsIgnoreCase("Move") || HUDModule.mc.player.getActiveEffects().isEmpty() ? 0.0f : (float)(EntityUtils.hasNegativeEffects((Player)HUDModule.mc.player) ? 51 : 25));
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.moduleListPosition.getX(), this.moduleListPosition.getY());
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        int maxWidth = 0;
        int index = 0;
        for (ModuleEntry entry : this.moduleEntries) {
            boolean shouldShow = entry.module().isToggled() || isNewbie && entry.module().getBind() != 0;
            float targetWidth = shouldShow ? (float)(Night.FONT_MANAGER.getWidth(entry.text()) + 2) : 0.0f;
            float x = (float)mc.getWindow().getGuiScaledWidth() - entry.module().getAnimationOffset().get(targetWidth);
            float y = 2.0f + potionOffset + (float)(index * Night.FONT_MANAGER.getHeight());
            this.drawModuleText(entry.module(), event.getContext(), entry.text(), x, y);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(entry.text()) + 2);
            ++index;
        }
        if (!this.moduleEntries.isEmpty()) {
            float right = mc.getWindow().getGuiScaledWidth();
            HudElementRegistry.reportBounds("ModuleList", right - (float)maxWidth, 2.0f + potionOffset, right, 2.0f + potionOffset + (float)(this.moduleEntries.size() * Night.FONT_MANAGER.getHeight()));
        }
        matrices.popMatrix();
    }

    private void renderPlayerRadar(RenderOverlayEvent event) {
        if (!this.playerRadar.getValue()) {
            return;
        }
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.playerRadarPosition.getX(), this.playerRadarPosition.getY());
        int maxWidth = 0;
        int offset = 0;
        for (PlayerEntry entry : this.playerEntries) {
            if (entry.headTexture() != null) {
                PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)event.getContext(), (Identifier)entry.headTexture(), (int)2, (int)(1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * offset), (int)Night.FONT_MANAGER.getHeight(), (boolean)true, (boolean)false, (int)Color.WHITE.getRGB());
            }
            int textX = 2 + (entry.headTexture() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            this.drawText(event.getContext(), entry.text(), textX, 2 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * offset, Night.FRIEND_MANAGER.contains(entry.player().getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : null);
            maxWidth = Math.max(maxWidth, textX + Night.FONT_MANAGER.getWidth(entry.text()));
            ++offset;
        }
        if (!this.playerEntries.isEmpty()) {
            HudElementRegistry.reportBounds("PlayerRadar", 2.0f, 1 + Night.FONT_MANAGER.getHeight() * 2, maxWidth, 1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * this.playerEntries.size());
        }
        matrices.popMatrix();
    }

    private static int countItem(Player player, Item item) {
        if (player == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.is(item)) continue;
            count += stack.getCount();
        }
        if (player.getOffhandItem().is(item)) {
            count += player.getOffhandItem().getCount();
        }
        return count;
    }

    private static ItemStack[] dummyArmor() {
        if (dummyArmor == null) {
            dummyArmor = new ItemStack[]{new ItemStack((ItemLike)Items.NETHERITE_BOOTS), new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS), new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE), new ItemStack((ItemLike)Items.NETHERITE_HELMET)};
        }
        return dummyArmor;
    }

    private void renderItems(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        this.renderArmor(event);
        this.renderTotemCounter(event);
        this.renderCrystalCounter(event);
        this.renderXPCounter(event);
    }

    private void renderArmor(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        int wateroffset = HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) || HUDModule.mc.player.getAirSupply() < HUDModule.mc.player.getMaxAirSupply() ? 10 : 0;
        int rawLeft = mc.getWindow().getGuiScaledWidth() / 2 + 15;
        int rawTop = mc.getWindow().getGuiScaledHeight() - 55 - wateroffset;
        HudElementRegistry.reportBounds("Armor", rawLeft - 1, rawTop - 3, rawLeft + 71, rawTop + 17);
        if (!this.armor.getValue() && !inEditor) {
            return;
        }
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.armorPosition.getX(), this.armorPosition.getY());
        boolean hasAnyArmor = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (HUDModule.mc.player.getItemBySlot(slot).isEmpty()) continue;
            hasAnyArmor = true;
            break;
        }
        if (hasAnyArmor && this.armor.getValue()) {
            int offset = 0;
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = HUDModule.mc.player.getItemBySlot(slot);
                if (stack.isEmpty()) continue;
                int x = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * offset;
                int y = rawTop;
                event.getContext().item(stack, x, y);
                if (this.armorDurability.getValue().equalsIgnoreCase("Bar") || this.armorDurability.getValue().equalsIgnoreCase("Both")) {
                    event.getContext().itemDecorations(HUDModule.mc.font, stack, x, y);
                }
                int damage = stack.getDamageValue();
                int maxDamage = stack.getMaxDamage();
                if ((this.armorDurability.getValue().equalsIgnoreCase("Percentage") || this.armorDurability.getValue().equalsIgnoreCase("Both")) && maxDamage > 0) {
                    matrices.pushMatrix();
                    matrices.scale(0.625f, 0.625f);
                    this.drawText(event.getContext(), (maxDamage - damage) * 100 / maxDamage + "%", (int)((float)((mc.getWindow().getGuiScaledWidth() >> 1) + 70 - 18 * offset) * 1.6f), (int)((float)(rawTop - 3) * 1.6f - 5.0f), false, new Color(1.0f - (float)(maxDamage - damage) / (float)maxDamage, (float)(maxDamage - damage) / (float)maxDamage, 0.0f));
                    matrices.popMatrix();
                }
                ++offset;
            }
        } else if (inEditor) {
            ItemStack[] dummyArmor = HUDModule.dummyArmor();
            for (int i = 0; i < dummyArmor.length; ++i) {
                int x = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * i;
                int y = rawTop;
                event.getContext().item(dummyArmor[i], x, y);
            }
        }
        matrices.popMatrix();
    }

    private void renderTotemCounter(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float rawX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - 9.0f;
        float rawY = mc.getWindow().getGuiScaledHeight() - 55 - (HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) && HUDModule.mc.gameMode != null && HUDModule.mc.gameMode.getPlayerMode() != GameType.CREATIVE ? 10 : 0);
        HudElementRegistry.reportBounds("TotemCounter", rawX - 1.0f, rawY - 1.0f, rawX + 19.0f, rawY + 19.0f);
        if (!this.totemCounter.getValue() && !inEditor) {
            return;
        }
        int totems = HUDModule.countItem((Player)HUDModule.mc.player, Items.TOTEM_OF_UNDYING);
        this.totemSmoothCounter.update(totems);
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.totemCounterPosition.getX(), this.totemCounterPosition.getY());
        if (this.totemCounter.getValue() && (totems > 0 || this.totemSmoothCounter.lastCount > 0)) {
            ItemStack stack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            this.totemSmoothCounter.render(event.getContext(), this.itemsSmooth.getValue());
            matrices.popMatrix();
        } else if (inEditor) {
            ItemStack stack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(event.getContext(), "1", 17 - Night.FONT_MANAGER.getWidth("1"), 9, Color.WHITE);
            } else {
                event.getContext().text(HUDModule.mc.font, "1", 17 - HUDModule.mc.font.width("1"), 9, -1, true);
            }
            matrices.popMatrix();
        }
        matrices.popMatrix();
    }

    private void renderCrystalCounter(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float rawX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float rawY = (float)(mc.getWindow().getGuiScaledHeight() - 40) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("CrystalCounter", rawX - 1.0f, rawY - 1.0f, rawX + 19.0f, rawY + 19.0f);
        if (!this.crystalCounter.getValue() && !inEditor) {
            return;
        }
        int crystals = HUDModule.countItem((Player)HUDModule.mc.player, Items.END_CRYSTAL);
        this.crystalSmoothCounter.update(crystals);
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.crystalCounterPosition.getX(), this.crystalCounterPosition.getY());
        if (this.crystalCounter.getValue() && (crystals > 0 || this.crystalSmoothCounter.lastCount > 0)) {
            ItemStack stack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            this.crystalSmoothCounter.render(event.getContext(), this.itemsSmooth.getValue());
            matrices.popMatrix();
        } else if (inEditor) {
            ItemStack stack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(event.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                event.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrices.popMatrix();
        }
        matrices.popMatrix();
    }

    private void renderXPCounter(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float rawX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float rawY = (float)(mc.getWindow().getGuiScaledHeight() - 20) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("XPCounter", rawX - 1.0f, rawY - 1.0f, rawX + 19.0f, rawY + 19.0f);
        if (!this.xpCounter.getValue() && !inEditor) {
            return;
        }
        int experienceBottles = HUDModule.countItem((Player)HUDModule.mc.player, Items.EXPERIENCE_BOTTLE);
        this.xpSmoothCounter.update(experienceBottles);
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.xpCounterPosition.getX(), this.xpCounterPosition.getY());
        if (this.xpCounter.getValue() && (experienceBottles > 0 || this.xpSmoothCounter.lastCount > 0)) {
            ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            this.xpSmoothCounter.render(event.getContext(), this.itemsSmooth.getValue());
            matrices.popMatrix();
        } else if (inEditor) {
            ItemStack stack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrices.pushMatrix();
            matrices.translate(rawX, rawY);
            event.getContext().item(stack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(event.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                event.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrices.popMatrix();
        }
        matrices.popMatrix();
    }

    private void renderInventory(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.inventory.getValue()) {
            return;
        }
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.inventoryPosition.getX(), this.inventoryPosition.getY());
        int padding = 3;
        int slotSize = 18;
        int totalWidth = 9 * slotSize + padding * 2;
        int totalHeight = 3 * slotSize + padding * 2;
        HudElementRegistry.reportBounds("Inventory", 0.0f, 0.0f, totalWidth, totalHeight);
        if (this.inventoryBackground.getValue()) {
            int alpha = (int)Math.clamp(this.inventoryAlpha.getValue().floatValue(), 0.0f, 255.0f);
            Color bgColor = new Color(15, 15, 18, alpha);
            Color borderColor = new Color(255, 255, 255, Math.min(alpha, 30));
            Renderer2D.renderQuad(event.getContext(), 0.0f, 0.0f, totalWidth, totalHeight, bgColor);
            Renderer2D.renderOutline(event.getContext(), 0.0f, 0.0f, totalWidth, totalHeight, borderColor);
        }
        for (int i = 9; i <= 35; ++i) {
            ItemStack stack;
            int col = (i - 9) % 9;
            int row = (i - 9) / 9;
            int slotX = padding + col * slotSize;
            int slotY = padding + row * slotSize;
            if (this.inventorySlotBackground.getValue()) {
                Color slotBg = new Color(255, 255, 255, 10);
                Renderer2D.renderQuad(event.getContext(), slotX, slotY, slotX + 18, slotY + 18, slotBg);
            }
            if ((stack = HUDModule.mc.player.getInventory().getItem(i)).isEmpty()) continue;
            event.getContext().item(stack, slotX + 1, slotY + 1);
            event.getContext().itemDecorations(HUDModule.mc.font, stack, slotX + 1, slotY + 1);
        }
        matrices.popMatrix();
    }

    private void renderPotions(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.potions.getValue()) {
            return;
        }
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.potionsPosition.getX(), this.potionsPosition.getY());
        float chatOffset = this.informationChatOffset.getValue() ? this.chatOffset : 0.0f;
        int offset = 0;
        int maxWidth = 0;
        for (PotionEntry entry : this.potionEntries) {
            int textWidth = Night.FONT_MANAGER.getWidth(entry.text());
            int totalWidth = textWidth + (entry.sprite() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            maxWidth = Math.max(maxWidth, totalWidth);
            int x = mc.getWindow().getGuiScaledWidth() - 2 - textWidth;
            int y = mc.getWindow().getGuiScaledHeight() - (int)chatOffset - 2 - Night.FONT_MANAGER.getHeight() - Night.FONT_MANAGER.getHeight() * offset;
            if (entry.sprite() != null) {
                matrices.pushMatrix();
                matrices.translate((float)(x - Night.FONT_MANAGER.getHeight() - 2), (float)(y - 1));
                event.getContext().blitSprite(RenderPipelines.GUI_TEXTURED, entry.sprite(), 0, 0, Night.FONT_MANAGER.getHeight(), Night.FONT_MANAGER.getHeight());
                matrices.popMatrix();
            }
            this.drawText(event.getContext(), entry.text(), x, y, this.potionColor.getValue().equals("Client") && this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), entry.color());
            ++offset;
        }
        if (offset > 0) {
            float right = mc.getWindow().getGuiScaledWidth();
            float bottom = (float)mc.getWindow().getGuiScaledHeight() - chatOffset;
            HudElementRegistry.reportBounds("Potions", right - 2.0f - (float)maxWidth - 4.0f, bottom - 2.0f - (float)(offset * Night.FONT_MANAGER.getHeight()), right, bottom);
        }
        matrices.popMatrix();
    }

   private void renderInformation(RenderOverlayEvent event) {
      if (mc.player != null) {
         if (this.informationElement.getValue()) {
            Matrix3x2fStack matrices = event.getMatrices();
            matrices.pushMatrix();
            matrices.translate(this.informationPosition.getX(), this.informationPosition.getY());
            int offset = 0;
            float chatOffset = this.informationChatOffset.getValue() ? this.chatOffset : 0.0F;
            if (this.health.getValue()) {
               String text = new DecimalFormat("0").format(mc.player.getHealth() + mc.player.getAbsorptionAmount());
               Color healthColor = new Color(
                  TextColor.fromLegacyFormat(ColorUtils.getHealthColor(mc.player.getHealth() + mc.player.getAbsorptionAmount())).getValue()
               );
               Night.FONT_MANAGER
                  .drawTextWithOutline(
                     event.getContext(),
                     text,
                     mc.getWindow().getGuiScaledWidth() / 2 - Night.FONT_MANAGER.getWidth(text) / 2,
                     mc.getWindow().getGuiScaledHeight() / 2 + 16,
                     healthColor,
                     Color.BLACK
                  );
            }

            List<String> informationEntries = new ArrayList<>();
            if (this.ping.getValue()) {
               informationEntries.add(this.getPrimary() + "Ping " + this.getSecondary() + Night.SERVER_MANAGER.getPing() + "ms");
            }

            if (this.fps.getValue()) {
               informationEntries.add(this.getPrimary() + "FPS " + this.getSecondary() + Night.RENDER_MANAGER.getFps());
            }

            if (this.durability.getValue()) {
               informationEntries.add("Durability " + (mc.player.getMainHandItem().getMaxDamage() - mc.player.getMainHandItem().getDamageValue()));
            }

            if (!this.speed.getValue().equalsIgnoreCase("None")) {
               informationEntries.add(
                  this.getPrimary()
                     + "Speed "
                     + this.getSecondary()
                     + new DecimalFormat("0.00")
                        .format(
                           EntityUtils.getSpeed(
                              mc.player, this.speed.getValue().equalsIgnoreCase("Meters") ? EntityUtils.SpeedUnit.METERS : EntityUtils.SpeedUnit.KILOMETERS
                           )
                        )
                     + (this.speed.getValue().equalsIgnoreCase("Meters") ? "m/s" : "km/h")
               );
            }

            if (this.serverBrand.getValue()) {
               informationEntries.add(this.getPrimary() + "Brand " + this.getSecondary() + Night.SERVER_MANAGER.getServerBrand());
            }

            float tickRate = Night.SERVER_MANAGER.getTickRate();
            if (this.tps.getValue()) {
               informationEntries.add(
                  this.getPrimary() + "TPS " + this.getSecondary() + (tickRate > 19.79 ? "20.00" : new DecimalFormat("00.00").format(tickRate))
               );
            }

            if (!informationEntries.isEmpty()) {
               informationEntries.sort(Comparator.comparingInt(Night.FONT_MANAGER::getWidth).reversed());

               for (String text : informationEntries) {
                  if (text.startsWith("Durability")) {
                     if (mc.player.getMainHandItem().isDamageableItem()) {
                        int maxDamage = mc.player.getMainHandItem().getMaxDamage();
                        int damage = mc.player.getMainHandItem().getDamageValue();
                        String s = String.valueOf(maxDamage - damage);
                        this.drawText(
                           event.getContext(),
                           this.getPrimary() + "Durability ",
                           mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth("Durability ") - Night.FONT_MANAGER.getWidth(s),
                           mc.getWindow().getGuiScaledHeight() - chatOffset - 2.0F - Night.FONT_MANAGER.getHeight() + offset * -Night.FONT_MANAGER.getHeight(),
                           this.informationSync.getValue() ? null : new Color(170, 170, 170)
                        );
                        this.drawText(
                           event.getContext(),
                           s,
                           mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(s),
                           mc.getWindow().getGuiScaledHeight() - chatOffset - 2.0F - Night.FONT_MANAGER.getHeight() + offset * -Night.FONT_MANAGER.getHeight(),
                           false,
                           new Color(1.0F - (float)(maxDamage - damage) / maxDamage, (float)(maxDamage - damage) / maxDamage, 0.0F)
                        );
                        offset++;
                     }
                  } else {
                     this.drawText(
                        event.getContext(),
                        text,
                        mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(text),
                        mc.getWindow().getGuiScaledHeight() - chatOffset - 2.0F - Night.FONT_MANAGER.getHeight() + offset * -Night.FONT_MANAGER.getHeight(),
                        this.informationSync.getValue() ? null : new Color(170, 170, 170)
                     );
                     offset++;
                  }
               }

               int maxWidth = 0;

               for (String text : informationEntries) {
                  maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(text));
               }

               float right = mc.getWindow().getGuiScaledWidth();
               float bottom = mc.getWindow().getGuiScaledHeight() - chatOffset;
               HudElementRegistry.reportBounds("Information", right - 2.0F - maxWidth, bottom - 2.0F - offset * Night.FONT_MANAGER.getHeight(), right, bottom);
            }

            matrices.popMatrix();
         }
      }
   }

    private void renderCoordinates(RenderOverlayEvent event) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.coordinates.getValue() && !this.direction.getValue()) {
            return;
        }
        Matrix3x2fStack matrices = event.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.coordinatesPosition.getX(), this.coordinatesPosition.getY());
        float chatOffset = this.positionChatOffset.getValue() ? this.chatOffset : 0.0f;
        int offset = 0;
        int maxWidth = 0;
        int lines = 0;
        if (this.coordinates.getValue()) {
            int cx = this.getFakedCoord(HUDModule.mc.player.getBlockX(), this.fakeOffsetX);
            int cz = this.getFakedCoord(HUDModule.mc.player.getBlockZ(), this.fakeOffsetZ);
            String text = String.valueOf(this.getSecondary()) + String.valueOf(cx) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(cx) + String.valueOf(ChatFormatting.GRAY) + "]" : "") + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + HUDModule.mc.player.getBlockY() + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + String.valueOf(cz) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(cz) + String.valueOf(ChatFormatting.GRAY) + "]" : "");
            this.drawText(event.getContext(), text, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - chatOffset - (float)offset - (float)Night.FONT_MANAGER.getHeight() - 2.0f);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(text));
            offset += Night.FONT_MANAGER.getHeight();
            ++lines;
        }
        if (this.direction.getValue()) {
            String text = String.valueOf(this.getPrimary()) + WorldUtils.getFacingName(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + " [" + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.WHITE) + WorldUtils.getFacingAxes(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + "]";
            this.drawText(event.getContext(), text, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - chatOffset - (float)offset - (float)Night.FONT_MANAGER.getHeight() - 2.0f, this.positionSync.getValue() ? null : Color.WHITE);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(text));
            ++lines;
        }
        float bottom = (float)mc.getWindow().getGuiScaledHeight() - chatOffset;
        HudElementRegistry.reportBounds("Coordinates", 2.0f, bottom - 2.0f - (float)(lines * Night.FONT_MANAGER.getHeight()), 2 + maxWidth, bottom - 2.0f);
        matrices.popMatrix();
    }

    private static Color lerpColor(Color c1, Color c2, float factor) {
        float f = Math.max(0.0f, Math.min(1.0f, factor));
        int r = Math.round((float)c1.getRed() + f * (float)(c2.getRed() - c1.getRed()));
        int g = Math.round((float)c1.getGreen() + f * (float)(c2.getGreen() - c1.getGreen()));
        int b = Math.round((float)c1.getBlue() + f * (float)(c2.getBlue() - c1.getBlue()));
        int a = Math.round((float)c1.getAlpha() + f * (float)(c2.getAlpha() - c1.getAlpha()));
        return new Color(r, g, b, a);
    }

    private void drawModuleText(Module module, GuiGraphicsExtractor context, String text, float x, float y) {
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        if (!isNewbie) {
            if (!module.isToggled()) {
                this.drawText(context, text, x, y, false, new Color(170, 170, 170));
                return;
            }
            Color color = this.getHudColor(y);
            if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
                long index = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
                color = ColorUtils.getOffsetRainbow(index);
            } else if (this.moduleColorMode.getValue().equals("Random")) {
                color = ColorUtils.getHashColor(module.getName());
            }
            this.drawText(context, text, x, y, (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal"), color);
            return;
        }
        Animation colorAnim = this.moduleColorAnimations.computeIfAbsent(module, m -> new Animation(m.isToggled() ? 1.0f : 0.0f, m.isToggled() ? 1.0f : 0.0f, 350, Easing.Method.EASE_IN_OUT_CUBIC));
        float progress = colorAnim.get(module.isToggled() ? 1.0f : 0.0f);
        Color baseColor = this.getHudColor(y);
        if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long index = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            baseColor = ColorUtils.getOffsetRainbow(index);
        } else if (this.moduleColorMode.getValue().equals("Random")) {
            baseColor = ColorUtils.getHashColor(module.getName());
        }
        boolean isHorizontalRainbow = (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal");
        this.drawNewbieModuleText(module, context, x, y, progress, baseColor, isHorizontalRainbow);
    }

    private void drawNewbieModuleText(Module module, GuiGraphicsExtractor context, float x, float y, float progress, Color baseColor, boolean isHorizontalRainbow) {
        String metaPart;
        String namePart;
        Object bindPart = module.getBind() != 0 ? "[" + KeyboardUtils.getKeyName(module.getBind()) + "] " : "";
        String fullText = (String)bindPart + (namePart = module.getName()) + (metaPart = module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : " [" + module.getMetaData() + "]");
        if (fullText.isEmpty()) {
            return;
        }
        Color gray = new Color(170, 170, 170);
        Color white = Color.WHITE;
        float totalWidth = Night.FONT_MANAGER.getWidth(fullText);
        float currentPixelX = 0.0f;
        float wBand = 0.35f;
        MutableComponent builder = Component.empty();
        int bindLen = ((String)bindPart).length();
        int nameLen = namePart.length();
        for (int i = 0; i < fullText.length(); ++i) {
            char c = fullText.charAt(i);
            String chStr = String.valueOf(c);
            float charWidth = Night.FONT_MANAGER.getWidth(chStr);
            float xi = totalWidth > 0.0f ? (currentPixelX + charWidth / 2.0f) / totalWidth : 0.0f;
            currentPixelX += charWidth;
            Color offColor = gray;
            Color onColor = i < bindLen ? (c == '[' || c == ']' || c == ' ' ? gray : white) : (i < bindLen + nameLen ? (isHorizontalRainbow ? ColorUtils.getOffsetRainbow((long)i * this.rainbowOffset.getValue().longValue() * 5L) : baseColor) : (c == '[' || c == ']' || c == ' ' ? gray : white));
            float t = Math.max(0.0f, Math.min(1.0f, (progress * (1.0f + wBand) - xi) / wBand));
            float smoothT = t * t * (3.0f - 2.0f * t);
            Color finalColor = HUDModule.lerpColor(offColor, onColor, smoothT);
            builder.append((Component)Component.literal((String)chStr).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)finalColor.getRGB()))));
        }
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        Night.FONT_MANAGER.drawTextWithShadow(context, builder.getVisualOrderText(), 0, 0, Color.WHITE);
        matrices.popMatrix();
    }

    private void drawText(GuiGraphicsExtractor context, String text, float x, float y) {
        this.drawText(context, text, x, y, null);
    }

    public void drawText(GuiGraphicsExtractor context, String text, float x, float y, Color color) {
        this.drawText(context, text, x, y, this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), color);
    }

    public void drawText(GuiGraphicsExtractor context, String text, float x, float y, boolean rainbow, Color color) {
        if (color == null) {
            color = this.getHudColor(y);
        }
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (rainbow) {
            Night.FONT_MANAGER.drawRainbowString(context, text, 0, 0, this.rainbowOffset.getValue().longValue() * 5L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(context, text, 0, 0, color);
        }
        matrices.popMatrix();
    }

    private Color getHudColor(float offset) {
        if (this.colorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long index = (long)offset / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            return ColorUtils.getOffsetRainbow(index);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Wave")) {
            long index = (long)offset / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 20L);
            return ColorUtils.getOffsetWave(ColorUtils.getGlobalColor(), index);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Custom")) {
            return ColorUtils.getColor(this.customColor.getColor(), 255);
        }
        return ColorUtils.getGlobalColor();
    }

    @Override
    public Setting getSetting(String name) {
        if (name.equalsIgnoreCase("ModuleListMode") || name.equalsIgnoreCase("Mode")) {
            return this.moduleListMode;
        }
        return super.getSetting(name);
    }

    private String getModuleText(Module module) {
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        Object bindPrefix = "";
        if (isNewbie && module.getBind() != 0) {
            bindPrefix = String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.WHITE) + KeyboardUtils.getKeyName(module.getBind()) + String.valueOf(ChatFormatting.GRAY) + "] ";
        }
        if (!isNewbie && !module.isToggled()) {
            return (String)bindPrefix + String.valueOf(ChatFormatting.GRAY) + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.DARK_GRAY) + " [" + String.valueOf(ChatFormatting.GRAY) + module.getMetaData() + String.valueOf(ChatFormatting.DARK_GRAY) + "]");
        }
        return (String)bindPrefix + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(ChatFormatting.WHITE) + module.getMetaData() + String.valueOf(ChatFormatting.GRAY) + "]");
    }

    private String getPotionText(MobEffectInstance effect) {
        String duration;
        if (effect.isInfiniteDuration()) {
            duration = "**:**";
        } else {
            int seconds = Math.round((float)effect.getDuration() / HUDModule.mc.level.tickRateManager().tickrate());
            duration = String.format("%02d:%02d", seconds / 60, seconds % 60);
        }
        return ((MobEffect)effect.getEffect().value()).getDisplayName().getString() + " " + (effect.getAmplifier() + 1) + " " + String.valueOf(ChatFormatting.WHITE) + duration;
    }

    private ChatFormatting getPrimary() {
        return this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET;
    }

    private ChatFormatting getSecondary() {
        return this.inversion.getValue() ? ChatFormatting.RESET : ChatFormatting.WHITE;
    }

    static {
        ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    }

    private static class SmoothCounter {
        private int lastCount = -1;
        private int prevCount = -1;
        private long changeTime = 0L;
        private int direction = 0;

        private SmoothCounter() {
        }

        public void update(int newCount) {
            if (this.lastCount == -1) {
                this.lastCount = newCount;
                this.prevCount = newCount;
                return;
            }
            if (newCount != this.lastCount) {
                this.prevCount = this.lastCount;
                this.lastCount = newCount;
                this.direction = newCount < this.prevCount ? -1 : 1;
                this.changeTime = System.currentTimeMillis();
            }
        }

        public void render(GuiGraphicsExtractor context, boolean smooth) {
            float curOffset;
            float oldOffset;
            float progress;
            if (this.lastCount <= 0 && this.prevCount <= 0) {
                return;
            }
            String currentText = String.valueOf(Math.max(0, this.lastCount));
            int fontHeight = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getHeight() : 9;
            long elapsed = System.currentTimeMillis() - this.changeTime;
            float duration = 320.0f;
            float f = progress = this.changeTime == 0L ? 1.0f : Math.min(1.0f, (float)elapsed / duration);
            if (!smooth || progress >= 1.0f || this.prevCount == -1 || this.prevCount == this.lastCount) {
                int width = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(currentText) : Minecraft.getInstance().font.width(currentText);
                float tx = 17 - width;
                float ty = 9.0f;
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(context, currentText, (int)tx, (int)ty, Color.WHITE);
                } else {
                    context.text(Minecraft.getInstance().font, currentText, (int)tx, (int)ty, -1, true);
                }
                return;
            }
            float eased = Easing.ease(progress, Easing.Method.EASE_OUT_CUBIC);
            String oldText = String.valueOf(Math.max(0, this.prevCount));
            int curWidth = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(currentText) : Minecraft.getInstance().font.width(currentText);
            int oldWidth = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(oldText) : Minecraft.getInstance().font.width(oldText);
            float curTx = 17 - curWidth;
            float oldTx = 17 - oldWidth;
            float baseTy = 9.0f;
            float slideDist = fontHeight + 3;
            if (this.direction < 0) {
                oldOffset = eased * slideDist;
                curOffset = -(1.0f - eased) * slideDist;
            } else {
                oldOffset = -eased * slideDist;
                curOffset = (1.0f - eased) * slideDist;
            }
            int oldAlpha = Math.max(0, Math.min(255, Math.round((1.0f - eased) * 255.0f)));
            int curAlpha = Math.max(0, Math.min(255, Math.round(eased * 255.0f)));
            if (oldAlpha > 5) {
                Color oldColor = new Color(255, 255, 255, oldAlpha);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(context, oldText, (int)oldTx, (int)(baseTy + oldOffset), oldColor);
                } else {
                    context.text(Minecraft.getInstance().font, oldText, (int)oldTx, (int)(baseTy + oldOffset), oldAlpha << 24 | 0xFFFFFF, true);
                }
            }
            if (curAlpha > 5) {
                Color curColor = new Color(255, 255, 255, curAlpha);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(context, currentText, (int)curTx, (int)(baseTy + curOffset), curColor);
                } else {
                    context.text(Minecraft.getInstance().font, currentText, (int)curTx, (int)(baseTy + curOffset), curAlpha << 24 | 0xFFFFFF, true);
                }
            }
        }
    }

    public record ModuleEntry(Module module, String text) {
    }

    public record PlayerEntry(Player player, String text, Identifier headTexture) {
    }

    public record PotionEntry(String text, Identifier sprite, Color color) {
    }
}

