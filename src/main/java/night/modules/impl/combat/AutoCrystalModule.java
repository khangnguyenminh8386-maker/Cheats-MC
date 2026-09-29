/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult.Type;


import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.DestroyBlockEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.GameLoopEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.UpdateMovementEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.CrystalPlacementHelper;
import night.modules.impl.combat.SuicideModule;
import night.modules.impl.movement.PhaseModule;
import night.modules.impl.player.AutoShopModule;
import night.modules.impl.player.KeyActionModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.PingBypassFlags;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.EspShader;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.Counter;
import night.utils.system.Timer;

@RegisterModule(name="AutoCrystal", description="Automatically places and attacks crystals to annihilate your opponents.", category=Module.Category.COMBAT)
public class AutoCrystalModule
extends Module {
    public CategorySetting attackCategory = new CategorySetting("Attack", "The category for settings related to attacking crystals.");
    public BooleanSetting attack = new BooleanSetting("Attack", "Enabled", "Automatically attacks crystals that are deemed safe.", new CategorySetting.Visibility(this.attackCategory), true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "Speed", "The speed at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum distance at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
    public NumberSetting attackWallsRange = new NumberSetting("AttackWallsRange", "WallsRange", "The maximum distance at which crystals will be attacked through walls.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
    public ModeSetting antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", new CategorySetting.Visibility(this.attackCategory), "None", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", new CategorySetting.Visibility(this.attackCategory), true);
    public BooleanSetting inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", new CategorySetting.Visibility(this.attackCategory), true);
    public CategorySetting placeCategory = new CategorySetting("Place", "The category for settings related to placing crystals.");
    public BooleanSetting place = new BooleanSetting("Place", "Enabled", "Automatically places crystals on positions that are deemed safe and lethal enough.", new CategorySetting.Visibility(this.placeCategory), true);
    public NumberSetting placeSpeed = new NumberSetting("PlaceSpeed", "Speed", "The speed at which crystals will be placed.", new CategorySetting.Visibility(this.placeCategory), Float.valueOf(10.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Range", "The maximum distance at which positions will be placed on.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
    public NumberSetting placeWallsRange = new NumberSetting("PlaceWallsRange", "WallsRange", "The maximum distance at which positions will be placed on through walls.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
    public ModeSetting placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", new CategorySetting.Visibility(this.placeCategory), "Native", new String[]{"Native", "Protocol"});
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", new CategorySetting.Visibility(this.placeCategory), true);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", new CategorySetting.Visibility(this.placeCategory), "None", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals, once the module is disabled.", new ModeSetting.Visibility(this.autoSwitch, "Normal"), false);
    public NumberSetting swapDelay = new NumberSetting("SwapDelay", "Delay", "The delay in ticks after swapping before placing or attacking crystals.", new CategorySetting.Visibility(this.placeCategory), 0, 0, 20);
    public CategorySetting miscellaneousCategory = new CategorySetting("Miscellaneous", "The category for all miscellaneous settings.");
    public ModeSetting sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", new CategorySetting.Visibility(this.miscellaneousCategory), "Strong", new String[]{"None", "Strict", "Strong"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", new CategorySetting.Visibility(this.miscellaneousCategory), "Normal", new String[]{"None", "Normal", "Packet", "Silent"});
    public ModeSetting swing = new ModeSetting("Swing", "The hand that will be used for swinging.", new CategorySetting.Visibility(this.miscellaneousCategory), "Default", new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"});
    public BooleanSetting yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting yawStepThreshold = new NumberSetting("YawStepThreshold", "Threshold", "The threshold in order for yaw to be modified.", new BooleanSetting.Visibility(this.yawStep, true), 75, 1, 180);
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting losDebug = new BooleanSetting("LosDebug", "Debug", "Prints once a second why the block you are aiming at passes/fails the place gates (LOS steps + damage).", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public ModeSetting targetMode = new ModeSetting("Target", "Which player to target when multiple are in range -- narrowing this to one collapses the per-position player scan from O(players) to O(1), the actual fix for multitarget FPS drops.", new CategorySetting.Visibility(this.miscellaneousCategory), "All", new String[]{"All", "Nearest", "Farthest", "Health"});
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", new CategorySetting.Visibility(this.miscellaneousCategory), (Number)0, (Number)0, (Number)20);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", new CategorySetting.Visibility(this.miscellaneousCategory), (Number)10.0, (Number)0.0, (Number)24.0);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting mineIgnore = new BooleanSetting("MineIgnore", "Pre-places a crystal on the block SpeedMine is about to break, and detonates it the instant that block is gone.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting mineIgnoreTicks = new NumberSetting("MineIgnoreTicks", "Tick", "How many ticks before the block breaks to place the crystal.", new BooleanSetting.Visibility(this.mineIgnore, true), 3, 0, 10);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting pauseOnSecondaryMine = new BooleanSetting("PauseOnSecondaryMine", "Pauses AutoCrystal for one tick while SpeedMine's secondary block is about to break, so it finishes first.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting awaitMine = new BooleanSetting("AwaitMine", "Pauses AutoCrystal when AutoMine selects a target block so AutoMine can lock and mine properly.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting awaitMineTicks = new NumberSetting("AwaitMineTicks", "Ticks", "The number of ticks to wait for AutoMine to lock target.", new BooleanSetting.Visibility(this.awaitMine, true), 2, 1, 10);
    public BooleanSetting pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", new BooleanSetting.Visibility(this.gameLoop, true), (Number)50, (Number)0, (Number)1000);
    public ModeSetting whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", new CategorySetting.Visibility(this.miscellaneousCategory), "Both", new String[]{"None", "Attack", "Place", "Both"});
    public CategorySetting predictionCategory = new CategorySetting("Prediction", "The category for settings related to attack prediction.");
    public BooleanSetting godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
    public NumberSetting predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", new CategorySetting.Visibility(this.predictionCategory), (Number)10, (Number)1, (Number)20);
    public NumberSetting offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", new CategorySetting.Visibility(this.predictionCategory), (Number)0, (Number)0, (Number)2);
    public ModeSetting godSwing = new ModeSetting("GodSwing", "Swing", "The swinging that will be done for each predicted attack.", new CategorySetting.Visibility(this.predictionCategory), "Normal", new String[]{"None", "Normal", "Strict"});
    public BooleanSetting fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", new CategorySetting.Visibility(this.predictionCategory), false);
    public BooleanSetting antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
    public NumberSetting kickThreshold = new NumberSetting("KickThreshold", "Threshold", "The tick threshold for the kick prevention.", new BooleanSetting.Visibility(this.antiKick, true), 5, 1, 10);
    public CategorySetting facePlaceCategory = new CategorySetting("Faceplace", "The category for settings relating to faceplacing.");
    public ModeSetting facePlaceMode = new ModeSetting("FaceplaceMode", "Mode", "The checks that will be done in order to faceplace.", new CategorySetting.Visibility(this.facePlaceCategory), "Dynamic", new String[]{"None", "Dynamic", "Always"});
    public ModeSetting facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "Speed", "The speed that players will be faceplaced at.", new CategorySetting.Visibility(this.facePlaceCategory), "Normal", new String[]{"Normal", "Custom"});
    public NumberSetting facePlaceDelay = new NumberSetting("FaceplaceDelay", "Delay", "The ticks that have to be waited for before faceplacing again.", new ModeSetting.Visibility(this.facePlaceSpeed, "Custom"), 11, 0, 20);
    public BooleanSetting healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", new ModeSetting.Visibility(this.facePlaceMode, "Dynamic"), true);
    public NumberSetting health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", new BooleanSetting.Visibility(this.healthPlace, true), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(36.0f));
    public BooleanSetting armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", new ModeSetting.Visibility(this.facePlaceMode, "Dynamic"), true);
    public NumberSetting percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", new BooleanSetting.Visibility(this.armorPlace, true), (Number)10, (Number)1, (Number)100);
    public CategorySetting basePlaceCategory = new CategorySetting("BasePlace", "The category for settings related to placing base obsidian blocks for elevated crystal attacks.");
    public BooleanSetting basePlace = new BooleanSetting("BasePlace", "Enabled", "Automatically places obsidian base blocks under/next to targets.", new CategorySetting.Visibility(this.basePlaceCategory), true);
    public ModeSetting basePlaceSwitch = new ModeSetting("BaseSwitch", "Switch", "Switch mode for placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), "Silent", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting basePlaceAirPlace = new BooleanSetting("BaseAirPlace", "AirPlace", "Allows placing base blocks in the air without neighboring blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public BooleanSetting basePlaceStrictDirection = new BooleanSetting("BaseStrictDirection", "StrictDirection", "Only places using directions that face you.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public NumberSetting basePlaceMinDamage = new NumberSetting("BaseDamage", "Min Damage", "The minimum damage a crystal on the base must deal to the target.", new CategorySetting.Visibility(this.basePlaceCategory), 6.0, 0.0, 36.0);
    public NumberSetting basePlaceRatio = new NumberSetting("BaseRatio", "Ratio", "Damage ratio required over existing standard crystal placement to trigger baseplace.", new CategorySetting.Visibility(this.basePlaceCategory), 1.5, 1.0, 3.0);
    public NumberSetting basePlaceRadius = new NumberSetting("BaseRadius", "Scan Radius", "The horizontal radius around the target to scan for base placements.", new CategorySetting.Visibility(this.basePlaceCategory), 3.0, 1.0, 6.0);
    public NumberSetting basePlaceRange = new NumberSetting("BaseRange", "Range", "The maximum distance at which base blocks will be placed.", new CategorySetting.Visibility(this.basePlaceCategory), 4.5, 0.0, 6.0);
    public NumberSetting basePlaceDelay = new NumberSetting("BaseDelay", "Delay", "Delay in ticks between placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), 0, 0, 10);
    public BooleanSetting basePlaceRotate = new BooleanSetting("BaseRotate", "Rotate", "Rotates when placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public CategorySetting damageCategory = new CategorySetting("Damage", "The category for settings related to damage calculations.");
    public NumberSetting minimumDamage = new NumberSetting("MinimumDamage", "Minimum", "The minimum damage that has to be dealt to enemies.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "MaximumSelf", "The maximum damage that can be dealt to you by crystals.", new CategorySetting.Visibility(this.damageCategory), 10.0, 0.0, 36.0);
    public NumberSetting lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", new CategorySetting.Visibility(this.damageCategory), (Number)Float.valueOf(1.5f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(4.0f));
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", new CategorySetting.Visibility(this.damageCategory), true);
    public BooleanSetting ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", new CategorySetting.Visibility(this.damageCategory), true);
    public CategorySetting renderCategory = new CategorySetting("Render", "Contains all of the settings relating to position rendering.");
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", new CategorySetting.Visibility(this.renderCategory), "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the auto crystal render.", new CategorySetting.Visibility(this.renderCategory), "Fade", new String[]{"Fade", "Shrink"});
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the place render.", new CategorySetting.Visibility(this.renderCategory), (Number)300, (Number)0, (Number)1000);
    public NumberSetting slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while place position is changing.", new CategorySetting.Visibility(this.renderCategory), (Number)1, (Number)0, (Number)20);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the target position.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColorUp = new ColorSetting("FillColorUp", "The color that will be used for the fill gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting fillColorDown = new ColorSetting("FillColorDown", "The color that will be used for the fill gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColorUp = new ColorSetting("OutlineColorUp", "The color that will be used for the outline gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public ColorSetting outlineColorDown = new ColorSetting("OutlineColorDown", "The color that will be used for the outline gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting renderDamage = new BooleanSetting("RenderDamage", "Damage", "Renders the damage that the position will do to the opponent.", new CategorySetting.Visibility(this.renderCategory), false);
    public BooleanSetting icon = new BooleanSetting("Icon", "Renders a customizable crystal icon on the rendered position.", new CategorySetting.Visibility(this.renderCategory), false);
    public NumberSetting iconScale = new NumberSetting("IconScale", "The scaling that will be applied to the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), (Number)3, (Number)1, (Number)5);
    public NumberSetting iconRadius = new NumberSetting("IconRadius", "The difference between the outer circle and the inner circle.", new BooleanSetting.Visibility(this.icon, true), (Number)Float.valueOf(2.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(5.0f));
    public ColorSetting iconColor = new ColorSetting("IconColor", "The color that will be used for the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), ColorUtils.getDefaultColor());
    private static final String[] SHADER_MODES = EspShader.MODES;
    private static final String[] SHADER_ACTIVE_MODES = Arrays.copyOfRange(SHADER_MODES, 1, SHADER_MODES.length);
    public ModeSetting shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target position instead of a flat color.", new CategorySetting.Visibility(this.renderCategory), "None", SHADER_MODES);
    public NumberSetting shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public NumberSetting shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the shader rendering.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 100, 0, 100);
    public BooleanSetting shaderDistanceScaling = new BooleanSetting("ShaderDistanceScaling", "DistanceScaling", "Scales the shader pattern by your distance to the position.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), false);
    public NumberSetting shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.Visibility(this.shader, "Gradient"), Float.valueOf(50.0f), Float.valueOf(0.1f), Float.valueOf(200.0f));
    public ColorSetting shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    public ColorSetting shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public ColorSetting shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 0, 255, 255), false, false));
    public ColorSetting shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", new ModeSetting.Visibility(this.shader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> pendingCalc = null;
    private volatile boolean asyncLoopActive = false;
    private volatile Set<BlockPos> latestReservedPlacements = Set.of();
    private Runnable attackRunnable = null;
    private Runnable placeRunnable = null;
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<Integer, Long>();
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<BlockPos, Long>();
    private final Map<BlockPos, Long> countedCrystals = new ConcurrentHashMap<BlockPos, Long>();
    private final Timer attackTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer facePlaceTimer = new Timer();
    private final Timer loopTimer = new Timer();
    private final Timer swapTimer = new Timer();
    private final Timer losDebugTimer = new Timer();
    private int lastSelectedSlot = -1;
    private long totalPlaces = 0L;
    private long totalAttacks = 0L;
    private boolean sequenceAttack = false;
    private boolean sequencePlace = true;
    private boolean attackedSequentially = false;
    private boolean placedSequentially = false;
    private Player target = null;
    private EndCrystal attackTarget = null;
    private PlaceTarget placeTarget = null;
    private PlaceTarget mineTarget = null;
    private PlaceTarget mineTargetSecondary = null;
    private BlockPos mineIgnoreMinedPos = null;
    private BlockPos mineIgnorePlacedPos = null;
    private String calculationTime = "0.00ms";
    private int calculationCount = 0;
    private String calculationDamage = "0.00";
    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;
    private int highestID = -100000;
    private int kickTicks = 0;
    private volatile List<Player> cachedPlayers = Collections.emptyList();
    private volatile long lastPlayerCacheTime = 0L;
    private static final long PLAYER_CACHE_DURATION = 50L;
    private int basePlaceTicks = 0;
    private int savedSlot = -1;
    private static final float SECONDARY_PAUSE_PROGRESS = 0.85f;
    private long lastPearlThrowTime = 0L;
    private long lastXpThrowTime = 0L;

    private boolean isDead() {
        if (AutoCrystalModule.mc.player == null || AutoCrystalModule.mc.level == null) {
            return true;
        }
        if (!AutoCrystalModule.mc.player.isAlive() || AutoCrystalModule.mc.player.isDeadOrDying() || AutoCrystalModule.mc.player.getHealth() <= 0.0f) {
            return true;
        }
        return AutoCrystalModule.mc.gui.screen() instanceof DeathScreen;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        AutoShopModule autoShop = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
        if (autoShop != null && autoShop.isRunning()) {
            return;
        }
        if (this.isDead()) {
            this.attackRunnable = null;
            this.placeRunnable = null;
            this.target = null;
            this.placeTarget = null;
            this.attackTarget = null;
            this.mineTarget = null;
            this.placedCrystals.clear();
            this.attackedCrystals.clear();
            this.countedCrystals.clear();
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int currentSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (this.lastSelectedSlot != -1 && this.lastSelectedSlot != currentSlot) {
            this.swapTimer.reset();
        }
        this.lastSelectedSlot = currentSlot;
        long minTtl = 50L;
        this.attackedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, minTtl));
        long placedTtl = Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 500L) + (long)((20.0f - this.attackSpeed.getValue().floatValue()) * 50.0f);
        this.placedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > placedTtl);
        this.countedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, minTtl));
        this.crystalsPerSecond = this.crystalCounter.getCount();
        if (this.losDebug.getValue() && this.losDebugTimer.hasTimeElapsed(1000L)) {
            this.losDebugTimer.reset();
            this.losDebug();
        }
        if (this.gameLoop.getValue()) {
            return;
        }
        this.run();
    }

    /*
     * Unable to fully structure code
     */
   private void losDebug() {
      Vec3 eye = mc.player.getEyePosition();
      HitResult aim = mc.level.clip(new ClipContext(eye, eye.add(mc.player.getViewVector(1.0F).scale(8.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
      if (aim instanceof BlockHitResult aimHit && aim.getType() == HitResult.Type.BLOCK) {
         BlockPos pos = aimHit.getBlockPos();
         HitResult centre = mc.level.clip(new ClipContext(eye, Vec3.atCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
         boolean step1 = centre.getType() == HitResult.Type.MISS || centre instanceof BlockHitResult hit1 && hit1.getBlockPos().equals(pos);
         HitResult top = mc.level
            .clip(new ClipContext(eye, new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
         boolean step2 = top.getType() == HitResult.Type.MISS || top instanceof BlockHitResult hit2 && hit2.getBlockPos().equals(pos);
         boolean step3 = CrystalPlacementHelper.isPlacementVisible(pos);
         String blocker = centre instanceof BlockHitResult hit3 && centre.getType() == HitResult.Type.BLOCK ? hit3.getBlockPos().toShortString() : "-";
         float self = DamageUtils.getCrystalDamage(
            mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), pos, null, this.ignoreTerrain.getValue()
         );
         Player enemy = this.target;
         float dmg = enemy == null
            ? -1.0F
            : DamageUtils.getCrystalDamage(
               enemy, PositionUtils.extrapolate(enemy, this.extrapolation.getValue().intValue()), pos, null, this.ignoreTerrain.getValue()
            );
         BlockPos picked = this.placeTarget == null ? null : this.placeTarget.getPosition();
         Night.CHAT_MANAGER
            .tagged(
               pos.toShortString()
                  + " los="
                  + step1
                  + "/"
                  + step2
                  + "/"
                  + step3
                  + " blockedBy="
                  + blocker
                  + " dist="
                  + String.format("%.2f", Math.sqrt(eye.distanceToSqr(Vec3.atCenterOf(pos))))
                  + " base="
                  + (mc.level.getBlockState(pos).getBlock() == Blocks.OBSIDIAN || mc.level.getBlockState(pos).getBlock() == Blocks.BEDROCK)
                  + " air="
                  + mc.level.getBlockState(pos.above()).isAir()
                  + " self="
                  + String.format("%.1f", self)
                  + " dmg="
                  + String.format("%.1f", dmg)
                  + " picked="
                  + (picked == null ? "none" : picked.toShortString())
                  + " calc="
                  + this.calculationTime,
               this.getName()
            );
         EndCrystal nearest = null;
         double nearestDistSq = Double.MAX_VALUE;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal c && c.isAlive()) {
               double d = c.getBoundingBox().distanceToSqr(eye);
               if (d < nearestDistSq) {
                  nearestDistSq = d;
                  nearest = c;
               }
            }
         }

         if (nearest != null) {
            float attackSelf = DamageUtils.getCrystalDamage(
               mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), nearest, this.ignoreTerrain.getValue()
            );
            boolean suicideOn = Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled();
            boolean overMax = attackSelf > this.maximumSelfDamage.getValue().floatValue();
            boolean overHp = this.antiSuicide.getValue() && attackSelf > mc.player.getHealth() + mc.player.getAbsorptionAmount();
            Night.CHAT_MANAGER
               .tagged(
                  "nearestCrystal="
                     + nearest.blockPosition().toShortString()
                     + " attackSelf="
                     + String.format("%.1f", attackSelf)
                     + " maxSelf="
                     + this.maximumSelfDamage.getValue().floatValue()
                     + " suicideOn="
                     + suicideOn
                     + " overMax="
                     + overMax
                     + " antiSuicide="
                     + this.antiSuicide.getValue()
                     + " overHp="
                     + overHp
                     + " gatePasses="
                     + (suicideOn || !overMax && !overHp),
                  this.getName()
               );
         }
      }
   }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        this.mineIgnoreTick();
        this.latestReservedPlacements = Set.copyOf(Night.WORLD_MANAGER.getReservedPlacements());
        if (this.asynchronous.getValue()) {
            if (!this.asyncLoopActive || this.pendingCalc == null || this.pendingCalc.isDone() || this.pendingCalc.isCancelled()) {
                this.asyncLoopActive = true;
                this.pendingCalc = this.executor.submit(this::asyncLoopStep);
            }
        } else {
            this.asyncLoopActive = false;
            this.runCalculation(this.latestReservedPlacements, false);
        }
    }

    private void asyncLoopStep() {
        if (!(this.asyncLoopActive && this.asynchronous.getValue() && this.isToggled())) {
            this.asyncLoopActive = false;
            return;
        }
        try {
            if (AutoCrystalModule.mc.player != null && AutoCrystalModule.mc.level != null) {
                this.runCalculation(this.latestReservedPlacements, true);
            }
        }
        catch (Throwable ignored) {
        }
        finally {
            if (this.asyncLoopActive && this.asynchronous.getValue() && this.isToggled()) {
                try {
                    Thread.sleep(5L);
                    this.pendingCalc = this.executor.submit(this::asyncLoopStep);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    this.asyncLoopActive = false;
                }
                catch (Throwable ignored) {
                    this.asyncLoopActive = false;
                }
            } else {
                this.asyncLoopActive = false;
            }
        }
    }

    private void runCalculation(Set<BlockPos> reservedPlacements, boolean async) {
        long startTime = System.nanoTime();
        try {
            this.attackTarget = this.calculateCrystals();
            this.placeTarget = this.calculatePlacements(null, reservedPlacements);
        }
        catch (Throwable ignored) {
            return;
        }
        long calcNanos = System.nanoTime() - startTime;
        this.calculationTime = new DecimalFormat("0.00").format((double)calcNanos / 1000000.0) + "ms";
        this.calculationCount = this.placeTarget == null ? 0 : this.placeTarget.getCalculations();
        this.calculationDamage = this.placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(this.placeTarget.getDamage());
        Player player = this.target = this.placeTarget == null ? null : this.placeTarget.getPlayer();
        if (this.blockDestruction.getValue() && async) {
            SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
            BlockPos position = null;
            if (module.getPrimary() != null && module.getPrimary().isMining()) {
                position = module.getPrimary().getPosition();
            }
            if (position != null) {
                this.mineTarget = this.calculatePlacements(position, reservedPlacements);
            }
            BlockPos secondaryPosition = null;
            if (module.farReach.getValue()) {
                if (module.getSecondary() != null && module.getSecondary().isMining()) {
                    secondaryPosition = module.getSecondary().getPosition();
                }
            } else if (module.getSecondary() != null && module.getSecondary().isMining()) {
                secondaryPosition = module.getSecondary().getPosition();
            }
            if (secondaryPosition != null) {
                this.mineTargetSecondary = this.calculatePlacements(secondaryPosition, reservedPlacements);
            }
        }
    }

    @SubscribeEvent
    public void onGameLoop(GameLoopEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        if (!this.gameLoop.getValue()) {
            return;
        }
        if (!this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue())) {
            return;
        }
        this.loopTimer.reset();
        this.run();
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
            this.attackRunnable = null;
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
            this.placeRunnable = null;
        }
    }

    private void run() {
        if (this.isDead()) {
            return;
        }
        if (this.pauseForSecondaryMine() || this.pauseForAutoMine()) {
            return;
        }
        if (this.isPearlPaused() || this.isXpPaused()) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        if (this.basePlace.getValue()) {
            if (this.basePlaceTicks < this.basePlaceDelay.getValue().intValue()) {
                ++this.basePlaceTicks;
            } else if (this.executeBasePlace()) {
                this.basePlaceTicks = 0;
            }
        }
        if (this.sequential.getValue().equalsIgnoreCase("None")) {
            if (this.sequenceAttack) {
                this.sequenceAttack = false;
                this.sequencePlace = true;
                this.attackCrystals();
                return;
            }
            if (this.sequencePlace) {
                this.sequenceAttack = true;
                this.sequencePlace = false;
                this.placeCrystals(false);
            }
        } else {
            if (this.attack.getValue()) {
                this.attackCrystals();
            }
            if (this.place.getValue()) {
                this.placeCrystals(false);
            }
        }
    }

    private boolean pauseForSecondaryMine() {
        if (!this.pauseOnSecondaryMine.getValue()) {
            return false;
        }
        SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (module == null) {
            return false;
        }
        if (module.farReach.getValue()) {
            SpeedMineModule.Secondary secondary = module.getSecondary();
            return secondary != null && secondary.isMining() && secondary.getProgress() >= 0.85f;
        }
        SpeedMineModule.Secondary legacySecondary = module.getSecondary();
        return legacySecondary != null && legacySecondary.isMining() && legacySecondary.getProgress() >= 0.85f;
    }

    private boolean pauseForAutoMine() {
        SpeedMineModule module;
        if (!this.awaitMine.getValue()) {
            return false;
        }
        SpeedMineModule speedMineModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        if (module == null) {
            return false;
        }
        SpeedMineModule.Action primary = module.getPrimary();
        return primary != null && primary.isMining() && primary.getTicks() < this.awaitMineTicks.getValue().intValue();
    }

    private boolean isPearlPaused() {
        KeyActionModule keyAction;
        PhaseModule phase;
        if (!this.pauseOnPearl.getValue()) {
            return false;
        }
        PhaseModule phaseModule = phase = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PhaseModule.class) : null;
        if (phase != null && phase.isToggled()) {
            return true;
        }
        KeyActionModule keyActionModule = keyAction = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
        if (keyAction != null && keyAction.isPearlActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastPearlThrowTime < 250L) {
            return true;
        }
        if (AutoCrystalModule.mc.player != null) {
            boolean holdingPearl;
            boolean bl = holdingPearl = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
            if (holdingPearl && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    private boolean isXpPaused() {
        KeyActionModule keyAction;
        if (!this.pauseOnXP.getValue()) {
            return false;
        }
        KeyActionModule keyActionModule = keyAction = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
        if (keyAction != null && keyAction.isXpActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastXpThrowTime < 250L) {
            return true;
        }
        if (AutoCrystalModule.mc.player != null) {
            boolean holdingXp;
            boolean bl = holdingXp = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE;
            if (holdingXp && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onUpdateMovement$POST(UpdateMovementEvent.Post event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        boolean needSpawnLos;
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead() || this.getPlayers().isEmpty()) {
            return;
        }
        if (this.shouldPause("Attack")) {
            return;
        }
        if (!this.attack.getValue() || !this.instant.getValue()) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof EndCrystal)) {
            return;
        }
        EndCrystal crystal = (EndCrystal)entity;
        if (this.inhibit.getValue() && this.attackedCrystals.containsKey(crystal.getId())) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(crystal)) {
            return;
        }
        BlockPos basePos = crystal.blockPosition().below();
        if (!this.placedCrystals.containsKey(basePos)) {
            basePos = BlockPos.containing((double)crystal.getX(), (double)(crystal.getY() - 0.5), (double)crystal.getZ());
        }
        if (!this.placedCrystals.containsKey(basePos)) {
            return;
        }
        if (crystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            return;
        }
        if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(crystal.blockPosition())) {
            return;
        }
        boolean bl = needSpawnLos = this.raytrace.getValue() || crystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
        if (needSpawnLos && !WorldUtils.canSee((Entity)crystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), crystal, this.ignoreTerrain.getValue());
            if (damage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] entityAttackRotations = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), entityAttackRotations);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        this.attack(crystal);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            this.placeCrystals(true);
            this.placeRunnable = null;
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        boolean needDestroyLos;
        PlaceTarget mineTarget;
        PlaceTarget secondaryCandidate;
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead() || this.getPlayers().isEmpty()) {
            return;
        }
        if (this.shouldPause("Place")) {
            return;
        }
        this.kickTicks = 0;
        if (this.mineIgnore.getValue() && event.getPosition() != null && event.getPosition().equals((Object)this.mineIgnoreMinedPos)) {
            this.mineIgnoreDetonate();
        }
        if (!this.blockDestruction.getValue()) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        BlockPos minedPosition = event.getPosition();
        if (minedPosition == null) {
            return;
        }
        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        boolean switched = false;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        PlaceTarget primaryCandidate = this.mineTarget == null ? null : this.mineTarget.clone();
        PlaceTarget placeTarget = secondaryCandidate = this.mineTargetSecondary == null ? null : this.mineTargetSecondary.clone();
        if (primaryCandidate != null && primaryCandidate.getPosition() != null && minedPosition.equals((Object)primaryCandidate.getException())) {
            mineTarget = primaryCandidate;
        } else if (secondaryCandidate != null && secondaryCandidate.getPosition() != null && minedPosition.equals((Object)secondaryCandidate.getException())) {
            mineTarget = secondaryCandidate;
        } else if (!this.asynchronous.getValue()) {
            mineTarget = this.calculatePlacements(minedPosition);
        } else {
            PlaceTarget placeTarget2 = mineTarget = primaryCandidate != null ? primaryCandidate : secondaryCandidate;
        }
        if (mineTarget == null || mineTarget.getPosition() == null || mineTarget.getDamage() <= 0.0f) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        BlockPos position = mineTarget.getPosition();
        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(position);
        boolean bl = needDestroyLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        if (needDestroyLos && !WorldUtils.canSeeBlock(position)) {
            return;
        }
        EndCrystal existingCrystal = null;
        for (Entity entity2 : AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(position.above()), entity -> true)) {
            EndCrystal crystal;
            if (!(entity2 instanceof EndCrystal)) continue;
            existingCrystal = crystal = (EndCrystal)entity2;
            break;
        }
        if (existingCrystal != null) {
            if (this.isAtActiveUnbrokenMinePos(existingCrystal)) {
                Night.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!this.attack.getValue()) {
                Night.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            float[] entityRotations = RotationUtils.getRotations((Entity)existingCrystal);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(entityRotations, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), entityRotations);
            }
            this.attack(existingCrystal);
            return;
        }
        CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(position);
        Vec3 targetAimVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0);
        float[] placeRotations = this.calculateRotations(targetAimVec);
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(placeRotations, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this) + 1);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), placeRotations);
        }
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = previousSlot;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
            switched = true;
        }
        this.place(position);
        if (switched) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (AutoCrystalModule.mc.player == null || AutoCrystalModule.mc.level == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundAddEntityPacket) {
            BlockPos position;
            ClientboundAddEntityPacket packet2 = (ClientboundAddEntityPacket)packet;
            if (packet2.getId() > this.highestID) {
                this.highestID = packet2.getId();
            }
            if (this.countedCrystals.containsKey(position = BlockPos.containing((double)packet2.getX(), (double)packet2.getY(), (double)packet2.getZ()).offset(0, -1, 0))) {
                if (this.facePlaceTimer.hasTimeElapsed(this.facePlaceDelay.getValue().longValue() * 50L)) {
                    this.facePlaceTimer.reset();
                }
                this.countedCrystals.remove(position);
                this.crystalCounter.increment();
                this.crystalsPerSecond = this.crystalCounter.getCount();
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket) {
            this.swapTimer.reset();
        }
        if (event.getPacket() instanceof ServerboundUseItemPacket && AutoCrystalModule.mc.player != null) {
            if (AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
                this.lastPearlThrowTime = System.currentTimeMillis();
            } else if (AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
                this.lastXpThrowTime = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        this.kickTicks = 0;
        this.attackRunnable = null;
        this.placeRunnable = null;
        this.target = null;
        this.placeTarget = null;
        this.attackTarget = null;
        this.mineTarget = null;
        this.placedCrystals.clear();
        this.attackedCrystals.clear();
        this.countedCrystals.clear();
        Night.RENDER_MANAGER.setRenderPosition(null);
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        this.highestID = -100000;
    }

    @Override
    public void onDisable() {
        this.asyncLoopActive = false;
        if (this.pendingCalc != null) {
            this.pendingCalc.cancel(false);
            this.pendingCalc = null;
        }
        if (this.savedSlot != -1) {
            InventoryUtils.switchBackNormal(this.savedSlot);
            this.savedSlot = -1;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        Night.RENDER_MANAGER.setRenderPosition(null);
        this.attackedCrystals.clear();
        this.placedCrystals.clear();
        this.countedCrystals.clear();
        this.attackedSequentially = false;
        this.placedSequentially = false;
        this.target = null;
        this.placeTarget = null;
        this.mineTarget = null;
        this.calculationTime = "0.00ms";
        this.calculationCount = 0;
        this.calculationDamage = "0.00";
        this.crystalCounter.reset();
        this.highestID = -100000;
        this.cachedPlayers.clear();
        this.lastPlayerCacheTime = 0L;
    }

    @Override
    public String getMetaData() {
        return this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond;
    }

    public boolean hasLiveCrystalNear(BlockPos position) {
        if (position == null || AutoCrystalModule.mc.level == null) {
            return false;
        }
        return !AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(position).inflate(1.0), entity -> entity instanceof EndCrystal && entity.isAlive()).isEmpty();
    }

    private boolean isEnemyAtMinePos(BlockPos pPos) {
        if (pPos == null || AutoCrystalModule.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pPos).inflate(1.5, 2.0, 1.5);
        for (Player player : AutoCrystalModule.mc.level.players()) {
            if (player.equals((Object)AutoCrystalModule.mc.player) || EntityUtils.isGhost((Entity)player) || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !player.getBoundingBox().intersects(box)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalNearMinePos(EndCrystal crystal, BlockPos minePos) {
        if (crystal == null || minePos == null) {
            return false;
        }
        BlockPos crystalBase = crystal.blockPosition().below();
        if (Math.abs(crystalBase.getX() - minePos.getX()) <= 2 && Math.abs(crystalBase.getZ() - minePos.getZ()) <= 2 && Math.abs(crystalBase.getY() - minePos.getY()) <= 2) {
            return true;
        }
        return crystal.getBoundingBox().distanceToSqr(Vec3.atCenterOf((Vec3i)minePos)) <= 9.0;
    }

    private boolean isAtActiveUnbrokenMinePos(EndCrystal crystal) {
        BlockPos sPos;
        BlockPos pPos;
        SpeedMineModule speedMine;
        if (crystal == null || AutoCrystalModule.mc.level == null || AutoCrystalModule.mc.player == null) {
            return false;
        }
        SpeedMineModule speedMineModule = speedMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        if (speedMine == null || !speedMine.isToggled()) {
            return false;
        }
        SpeedMineModule.Action primary = speedMine.getPrimary();
        if (primary != null && primary.getPosition() != null && this.isCrystalNearMinePos(crystal, pPos = primary.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(pPos);
        }
        SpeedMineModule.Secondary secondary = speedMine.getSecondary();
        if (secondary != null && secondary.getPosition() != null && this.isCrystalNearMinePos(crystal, sPos = secondary.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(sPos);
        }
        return false;
    }

    private void attackCrystals() {
        if (this.isDead()) {
            return;
        }
        if (this.shouldPause("Attack")) {
            return;
        }
        if (this.getPlayers().isEmpty()) {
            this.attackTarget = null;
            return;
        }
        EndCrystal targetCrystal = null;
        boolean isObstructionTarget = false;
        PlaceTarget pt = this.placeTarget;
        if (pt != null && pt.getPosition() == null && pt.getObstructions() != null && !pt.getObstructions().isEmpty()) {
            for (Entity entity : pt.getObstructions()) {
                float damage;
                boolean needLos;
                EndCrystal c;
                if (!(entity instanceof EndCrystal) || !(c = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(c) || c.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(c.blockPosition())) continue;
                boolean bl = needLos = this.raytrace.getValue() || c.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                if (needLos && !WorldUtils.canSee((Entity)c) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), c, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                targetCrystal = c;
                isObstructionTarget = true;
                break;
            }
        }
        if (targetCrystal == null) {
            targetCrystal = this.attackTarget;
        }
        if (targetCrystal == null) {
            float bestFallbackDamage = -1.0f;
            List<Player> players = this.selectTargets(this.getPlayers());
            for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                float damage;
                boolean needLos;
                EndCrystal c;
                if (!(entity instanceof EndCrystal) || !(c = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(c) || this.inhibit.getValue() && this.attackedCrystals.containsKey(c.getId()) && System.currentTimeMillis() - this.attackedCrystals.get(c.getId()) < Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 100L)) continue;
                BlockPos base = c.blockPosition().below();
                if (!this.placedCrystals.containsKey(base)) {
                    base = BlockPos.containing((double)c.getX(), (double)(c.getY() - 0.5), (double)c.getZ());
                }
                if (!this.placedCrystals.containsKey(base) || c.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(c.blockPosition())) continue;
                boolean bl = needLos = this.raytrace.getValue() || c.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                if (needLos && !WorldUtils.canSee((Entity)c) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), c, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                float enemyDmg = 0.0f;
                for (Player p : players) {
                    float d = DamageUtils.getCrystalDamage((Entity)p, PositionUtils.extrapolate(p, this.extrapolation.getValue().intValue()), c, this.ignoreTerrain.getValue());
                    if (!(d > enemyDmg)) continue;
                    enemyDmg = d;
                }
                if (!(enemyDmg > bestFallbackDamage)) continue;
                bestFallbackDamage = enemyDmg;
                targetCrystal = c;
                isObstructionTarget = true;
            }
        }
        if (targetCrystal == null) {
            return;
        }
        EndCrystal crystal = targetCrystal;
        if (this.isAtActiveUnbrokenMinePos(crystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), crystal, this.ignoreTerrain.getValue());
            if (damage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] attackTargetRotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(attackTargetRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        Entity entity = AutoCrystalModule.mc.level.getEntity(crystal.getId());
        String bailReason = null;
        if (entity == null) {
            bailReason = "entity-gone";
        } else if (!(entity instanceof EndCrystal)) {
            bailReason = "not-end-crystal";
        } else if (!((EndCrystal)entity).isAlive()) {
            bailReason = "dead";
        } else if (this.inhibit.getValue() && !isObstructionTarget && this.attackedCrystals.containsKey(entity.getId())) {
            bailReason = "inhibit";
        } else if (entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            bailReason = "range";
        } else if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            bailReason = "border";
        } else {
            boolean needLos;
            boolean bl = needLos = this.raytrace.getValue() || entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            if (needLos && !WorldUtils.canSee(entity)) {
                bailReason = "cannot-see";
            }
        }
        if (bailReason != null) {
            return;
        }
        this.attackRunnable = () -> {
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())));
            }
            this.attack(crystal);
        };
    }

   private void placeCrystals(boolean sequential) {
      if (!this.isDead()) {
         if (!this.shouldPause("Place")) {
            AutoCrystalModule.PlaceTarget placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
            if (placeTarget != null && placeTarget.getPosition() != null) {
               if (placeTarget.getPlayer() != null
                  && placeTarget.getPlayer().isAlive()
                  && !placeTarget.getPlayer().isDeadOrDying()
                  && !(placeTarget.getPlayer().getHealth() <= 0.0F)) {
                  int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                  int previousSlot = mc.player.getInventory().getSelectedSlot();
                  if (this.autoSwitch.getValue().equalsIgnoreCase("None")
                     || slot != -1
                     || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
                     || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                     BlockPos position = placeTarget.getPosition();
                     if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) > Mth.square(this.placeRange.getValue().doubleValue())) {
                        Night.RENDER_MANAGER.setRenderPosition(null);
                     } else {
                        Night.RENDER_MANAGER.setRenderPosition(position);
                        if (mc.level.getWorldBorder().isWithinBounds(position)) {
                           if (mc.level.getBlockState(position).getBlock() == Blocks.OBSIDIAN || mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK) {
                              if (mc.level.getBlockState(position.offset(0, 1, 0)).isAir()
                                 && (!this.placements.getValue().equalsIgnoreCase("Protocol") || mc.level.getBlockState(position.offset(0, 2, 0)).isAir())) {
                                 boolean needPlaceLos = this.raytrace.getValue()
                                    || mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position))
                                       > Mth.square(this.placeWallsRange.getValue().doubleValue());
                                 if (!needPlaceLos || WorldUtils.canSeeBlock(position)) {
                                    if (!mc.level
                                       .getEntities((Entity)null, this.getCrystalPlacementBox(position), entity -> true)
                                       .stream()
                                       .anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) {
                                       EndCrystal existingCrystal = null;
                                       Iterator placement = mc.level
                                          .getEntities(
                                             (Entity)null, this.getCrystalPlacementBox(position), entity -> entity instanceof EndCrystal && entity.isAlive()
                                          )
                                          .iterator();
                                       if (placement.hasNext()) {
                                          Entity entity = (Entity)placement.next();
                                          existingCrystal = (EndCrystal)entity;
                                       }

                                       if (existingCrystal != null) {
                                          if (this.attack.getValue() && !this.isAtActiveUnbrokenMinePos(existingCrystal)) {
                                             if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                float damage = DamageUtils.getCrystalDamage(
                                                   mc.player,
                                                   PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()),
                                                   existingCrystal,
                                                   this.ignoreTerrain.getValue()
                                                );
                                                if (damage <= this.maximumSelfDamage.getValue().floatValue()
                                                   && (!this.antiSuicide.getValue() || damage <= mc.player.getHealth() + mc.player.getAbsorptionAmount())) {
                                                   float[] entityRotations = this.calculateRotations(Vec3.atCenterOf(existingCrystal.blockPosition()));
                                                   if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                      Night.ROTATION_MANAGER
                                                         .legacyRotate(entityRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                   }

                                                   if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                      Night.ROTATION_MANAGER
                                                         .wireRotate(
                                                            this.rotate.getValue(),
                                                            RotationUtils.getRotations(Vec3.atCenterOf(existingCrystal.blockPosition()))
                                                         );
                                                   }

                                                   this.attack(existingCrystal);
                                                }
                                             } else {
                                                float[] entityRotations = this.calculateRotations(Vec3.atCenterOf(existingCrystal.blockPosition()));
                                                if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                   Night.ROTATION_MANAGER.legacyRotate(entityRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                }

                                                if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                   Night.ROTATION_MANAGER
                                                      .wireRotate(
                                                         this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(existingCrystal.blockPosition()))
                                                      );
                                                }

                                                this.attack(existingCrystal);
                                             }
                                          }
                                       } else {
                                          for (Entity entity : mc.level.entitiesForRendering()) {
                                             if (entity instanceof EndCrystal c && c.isAlive() && !this.isAtActiveUnbrokenMinePos(c)) {
                                                BlockPos base = c.blockPosition().below();
                                                if (!this.placedCrystals.containsKey(base)) {
                                                   base = BlockPos.containing(c.getX(), c.getY() - 0.5, c.getZ());
                                                }

                                                if (this.placedCrystals.containsKey(base)) {
                                                   double distSq = c.getBoundingBox().distanceToSqr(mc.player.getEyePosition());
                                                   boolean inRange = distSq <= Mth.square(this.attackRange.getValue().doubleValue());
                                                   boolean needLos = this.raytrace.getValue()
                                                      || distSq > Mth.square(this.attackWallsRange.getValue().doubleValue());
                                                   boolean canHit = inRange && (!needLos || WorldUtils.canSee(c));
                                                   if (canHit) {
                                                      if (this.attack.getValue()) {
                                                         if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                            float damage = DamageUtils.getCrystalDamage(
                                                               mc.player,
                                                               PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()),
                                                               c,
                                                               this.ignoreTerrain.getValue()
                                                            );
                                                            if (damage <= this.maximumSelfDamage.getValue().floatValue()
                                                               && (
                                                                  !this.antiSuicide.getValue()
                                                                     || damage <= mc.player.getHealth() + mc.player.getAbsorptionAmount()
                                                               )) {
                                                               float[] entityRotations = this.calculateRotations(Vec3.atCenterOf(c.blockPosition()));
                                                               if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                                  Night.ROTATION_MANAGER
                                                                     .legacyRotate(entityRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                               }

                                                               if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                                  || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                                  Night.ROTATION_MANAGER
                                                                     .wireRotate(
                                                                        this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(c.blockPosition()))
                                                                     );
                                                               }

                                                               this.attack(c);
                                                            }
                                                         } else {
                                                            float[] entityRotations = this.calculateRotations(Vec3.atCenterOf(c.blockPosition()));
                                                            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                               Night.ROTATION_MANAGER
                                                                  .legacyRotate(entityRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                            }

                                                            if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                               || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                               Night.ROTATION_MANAGER
                                                                  .wireRotate(
                                                                     this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(c.blockPosition()))
                                                                  );
                                                            }

                                                            this.attack(c);
                                                         }
                                                      }

                                                      return;
                                                   }

                                                   if (base.equals(position)) {
                                                      return;
                                                   }
                                                }
                                             }
                                          }

                                          CrystalPlacementHelper.PlacementResult placementx = CrystalPlacementHelper.getVisiblePlacement(position);
                                          Vec3 targetAimVec = placementx != null && placementx.hitVec != null
                                             ? placementx.hitVec
                                             : Vec3.atCenterOf(position).add(0.0, 0.5, 0.0);
                                          float[] placeTargetRotations = this.calculateRotations(targetAimVec);
                                          if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                             Night.ROTATION_MANAGER.legacyRotate(placeTargetRotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                          }

                                          if (this.swapDelay.getValue().intValue() <= 0
                                             || this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
                                             if (this.placeTimer.hasTimeElapsed(1000.0F - this.placeSpeed.getValue().floatValue() * 50.0F)) {
                                                if (!sequential && this.placedSequentially) {
                                                   this.placedSequentially = false;
                                                } else {
                                                   this.placeRunnable = () -> {
                                                      boolean switched = false;
                                                      if (this.rotate.getValue().equalsIgnoreCase("Packet")
                                                         || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                         Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(targetAimVec));
                                                      }

                                                      if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                                                         if (this.autoSwitch.getValue().equalsIgnoreCase("Normal")
                                                            && this.swapBack.getValue()
                                                            && this.savedSlot == -1) {
                                                            this.savedSlot = previousSlot;
                                                         }

                                                         InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                                                         switched = true;
                                                      }

                                                      this.place(position);
                                                      if (switched) {
                                                         InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
                                                      }

                                                      if (this.godSync.getValue()) {
                                                         boolean flag = !this.antiKick.getValue()
                                                            || !(mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem)
                                                               && !(mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem)
                                                               && !Night.MODULE_MANAGER.getModule(KeyActionModule.class).isXpActive();
                                                         if ((!this.antiKick.getValue() || this.kickTicks > this.kickThreshold.getValue().intValue()) && flag) {
                                                            if (!this.fast.getValue()) {
                                                               for (Entity entity : mc.level.entitiesForRendering()) {
                                                                  if (entity.getId() > this.highestID) {
                                                                     this.highestID = entity.getId();
                                                                  }
                                                               }
                                                            }

                                                            for (int i = 1 - this.offset.getValue().intValue(); i < this.predictions.getValue().intValue(); i++) {
                                                               Entity entity = mc.level.getEntity(this.highestID);
                                                               if (entity == null || entity instanceof EndCrystal) {
                                                                  int id = this.highestID + i;
                                                                  mc.getConnection().send(new ServerboundAttackPacket(id));
                                                                  if (this.godSwing.getValue().equals("Strict")) {
                                                                     mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                                                  }

                                                                  this.attackedCrystals.put(id, System.currentTimeMillis());
                                                               }
                                                            }

                                                            if (this.godSwing.getValue().equals("Normal")) {
                                                               mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                                            }
                                                         }

                                                         this.kickTicks++;
                                                      }
                                                   };
                                                   if (sequential) {
                                                      this.placeRunnable.run();
                                                      this.placeRunnable = null;
                                                      this.placedSequentially = true;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  this.placeTarget = null;
                  this.target = null;
                  Night.RENDER_MANAGER.setRenderPosition(null);
               }
            } else {
               Night.RENDER_MANAGER.setRenderPosition(null);
            }
         }
      }
   }

    private EndCrystal calculateCrystals() {
        if (!this.attack.getValue()) {
            return null;
        }
        if (this.shouldPause("Attack")) {
            return null;
        }
        List<Player> players = this.selectTargets(this.getPlayers());
        if (players.isEmpty()) {
            return null;
        }
        EndCrystal optimalCrystal = null;
        float optimalDamage = 0.0f;
        for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
            float damage;
            boolean needLos;
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(crystal) || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || crystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(crystal.blockPosition())) continue;
            boolean bl = needLos = this.raytrace.getValue() || crystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            if (needLos && !WorldUtils.canSee((Entity)crystal) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), crystal, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean override = false;
            for (Player player : players) {
                float damage2 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), crystal, this.ignoreTerrain.getValue());
                if (damage2 < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && damage2 < player.getHealth() + player.getAbsorptionAmount() && !(damage2 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()) || !(damage2 > optimalDamage) && !(damage2 > player.getHealth() + player.getAbsorptionAmount())) continue;
                optimalCrystal = crystal;
                optimalDamage = damage2;
                if (!(damage2 > player.getHealth() + player.getAbsorptionAmount())) continue;
                override = true;
                break;
            }
            if (!override) continue;
            break;
        }
        return optimalCrystal;
    }

    private PlaceTarget calculatePlacements(BlockPos exception) {
        return this.calculatePlacements(exception, Night.WORLD_MANAGER.getReservedPlacements());
    }

   private AutoCrystalModule.PlaceTarget calculatePlacements(BlockPos exception, Set<BlockPos> reservedPlacements) {
      if (!this.place.getValue()) {
         return null;
      }

      if (!this.shouldPause("Place")
         && (
            !this.autoSwitch.getValue().equalsIgnoreCase("None") && InventoryUtils.findHotbar(Items.END_CRYSTAL) != -1
               || mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
               || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL
         )) {
         List<Player> players = this.selectTargets(this.getPlayers());
         if (players.isEmpty()) {
            return null;
         }

         BlockPos bestPosition = null;
         Player bestPlayer = null;
         float bestDamage = 0.0F;
         float bestRawDamage = 0.0F;
         BlockPos stickyPos = this.placeTarget == null ? null : this.placeTarget.getPosition();
         float STICKY_EPSILON = 0.5F;
         List<Entity> obstructions = new ArrayList<>();
         int calculations = 0;

         for (int i = 0;
            i < Night.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue()));
            i++
         ) {
            BlockPos position = mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if (!(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) > Mth.square(this.placeRange.getValue().doubleValue()))
               && mc.level.getWorldBorder().isWithinBounds(position)
               && (mc.level.getBlockState(position).getBlock() == Blocks.OBSIDIAN || mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK)
               && mc.level.getBlockState(position.offset(0, 1, 0)).isAir()
               && (!this.placements.getValue().equalsIgnoreCase("Protocol") || mc.level.getBlockState(position.offset(0, 2, 0)).isAir())) {
               boolean needLos = this.raytrace.getValue()
                  || mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) > Mth.square(this.placeWallsRange.getValue().doubleValue());
               if (!needLos || WorldUtils.canSeeBlock(position)) {
                  AABB crystalBox = new AABB(
                     position.getX() - 1, position.getY() + 1, position.getZ() - 1, position.getX() + 2, position.getY() + 3, position.getZ() + 2
                  );
                  if (!reservedPlacements.stream().anyMatch(reserved -> crystalBox.intersects(new AABB(reserved)))) {
                     List<Entity> entitiesAtPos = mc.level
                        .getEntities((Entity)null, this.getCrystalPlacementBox(position), entityx -> entityx.isAlive() && !(entityx instanceof ExperienceOrb));
                     boolean hasNonCrystalEntity = false;
                     List<Entity> obstructingCrystals = new ArrayList<>();

                     for (Entity entity : entitiesAtPos) {
                        if (!(entity instanceof EndCrystal crystal)) {
                           hasNonCrystalEntity = true;
                           break;
                        }

                        if (this.isAtActiveUnbrokenMinePos(crystal)) {
                           hasNonCrystalEntity = true;
                           break;
                        }

                        BlockPos base = crystal.blockPosition().below();
                        if (!this.placedCrystals.containsKey(base)) {
                           base = BlockPos.containing(crystal.getX(), crystal.getY() - 0.5, crystal.getZ());
                        }

                        if (!this.placedCrystals.containsKey(base) || crystal.tickCount >= 20 - this.attackSpeed.getValue().intValue() + 15) {
                           obstructingCrystals.add(crystal);
                        }
                     }

                     if (!hasNonCrystalEntity) {
                        double maxDistSq = Mth.square(10.0);
                        boolean inRange = false;

                        for (Player player : players) {
                           if (player.distanceToSqr(position.getX() + 0.5, position.getY() + 1.0, position.getZ() + 0.5) <= maxDistSq) {
                              inRange = true;
                              break;
                           }
                        }

                        if (inRange) {
                           if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                              float selfDamage = DamageUtils.getCrystalDamage(
                                 mc.player,
                                 PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()),
                                 position,
                                 exception,
                                 this.ignoreTerrain.getValue()
                              );
                              if (selfDamage > this.maximumSelfDamage.getValue().floatValue()
                                 || this.antiSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                                 continue;
                              }
                           }

                           for (Player player : players) {
                              calculations++;
                              float damage = DamageUtils.getCrystalDamage(
                                 player,
                                 PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()),
                                 position,
                                 exception,
                                 this.ignoreTerrain.getValue()
                              );
                              if ((
                                    !(damage < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()))
                                       || !(damage < player.getHealth() + player.getAbsorptionAmount())
                                       || damage * (1.0F + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()
                                 )
                                 && !(damage <= 0.0F)) {
                                 if (exception == null && !obstructingCrystals.isEmpty()) {
                                    obstructions.add(obstructingCrystals.getFirst());
                                    break;
                                 }

                                 float comparisonDamage = damage + (position.equals(stickyPos) ? 0.5F : 0.0F);
                                 if (comparisonDamage > bestDamage) {
                                    bestPosition = position;
                                    bestPlayer = player;
                                    bestDamage = comparisonDamage;
                                    bestRawDamage = damage;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return bestPosition == null
            ? new AutoCrystalModule.PlaceTarget(null, null, obstructions, null, 0.0F, calculations)
            : new AutoCrystalModule.PlaceTarget(bestPosition, bestPlayer, obstructions, exception, bestRawDamage, calculations);
      } else {
         return null;
      }
   }

    private float[] calculateRotations(Vec3 vec3d) {
        float[] rotations = RotationUtils.getRotations(vec3d);
        if (this.yawStep.getValue()) {
            float difference = Night.ROTATION_MANAGER.getServerYaw() - rotations[0];
            if (Math.abs(difference) > 180.0f) {
                difference += difference > 0.0f ? -360.0f : 360.0f;
            }
            float deltaYaw = (float)(difference > 0.0f ? -1 : 1) * this.yawStepThreshold.getValue().floatValue();
            float yaw = Math.abs(difference) > this.yawStepThreshold.getValue().floatValue() ? Night.ROTATION_MANAGER.getServerYaw() + deltaYaw : rotations[0];
            rotations[0] = yaw;
        }
        return rotations;
    }

    private void attack(EndCrystal crystal) {
        int slot;
        if (crystal == null || !crystal.isAlive() || crystal.isRemoved()) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(crystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float damage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), crystal, this.ignoreTerrain.getValue());
            if (damage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && damage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        int previousSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        int switchedSlot = -1;
        if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && AutoCrystalModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (slot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            InventoryUtils.switchSlot(this.antiWeakness.getValue(), slot, previousSlot);
            switchedSlot = slot;
        }
        mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (switchedSlot != -1) {
            InventoryUtils.switchBack(this.antiWeakness.getValue(), switchedSlot, previousSlot);
        }
        this.attackedCrystals.put(crystal.getId(), System.currentTimeMillis());
        this.attackTimer.reset();
        ++this.totalAttacks;
    }

    private void place(BlockPos position) {
        InteractionHand hand = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(position);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(hand, new BlockHitResult(placement.hitVec, placement.direction, position, false), sequence));
        switch (this.swing.getValue()) {
            case "Default": {
                AutoCrystalModule.mc.player.swing(hand);
                break;
            }
            case "Packet": {
                mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
                break;
            }
            case "Mainhand": {
                AutoCrystalModule.mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
            case "Offhand": {
                AutoCrystalModule.mc.player.swing(InteractionHand.OFF_HAND);
                break;
            }
            case "Both": {
                AutoCrystalModule.mc.player.swing(InteractionHand.MAIN_HAND);
                AutoCrystalModule.mc.player.swing(InteractionHand.OFF_HAND);
            }
        }
        this.placedCrystals.put(position, System.currentTimeMillis());
        this.countedCrystals.put(position, System.currentTimeMillis());
        this.placeTimer.reset();
        ++this.totalPlaces;
    }

    private void mineIgnoreTick() {
        if (!this.mineIgnore.getValue()) {
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        SpeedMineModule.Action primary = Night.MODULE_MANAGER.getModule(SpeedMineModule.class).getPrimary();
        if (primary == null || !primary.isMining()) {
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        BlockPos minePos = primary.getPosition();
        if (!minePos.equals((Object)this.mineIgnoreMinedPos)) {
            this.mineIgnoreMinedPos = minePos;
            this.mineIgnorePlacedPos = null;
        }
        if (this.mineIgnorePlacedPos != null) {
            return;
        }
        if (primary.getTicksRemaining() > this.mineIgnoreTicks.getValue().intValue()) {
            return;
        }
        this.mineIgnoreTryPlace(minePos);
    }

    private void mineIgnoreTryPlace(BlockPos minePos) {
        if (this.shouldPause("Place")) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        List<Player> enemies = this.selectTargets(this.getPlayers());
        if (enemies.isEmpty()) {
            return;
        }
        boolean isEnemySurroundOrHole = false;
        for (Player player : enemies) {
            if (player.equals((Object)AutoCrystalModule.mc.player)) continue;
            BlockPos enemyPos = player.blockPosition();
            if (Math.abs(minePos.getX() - enemyPos.getX()) > 1 || Math.abs(minePos.getZ() - enemyPos.getZ()) > 1 || Math.abs(minePos.getY() - enemyPos.getY()) > 1) continue;
            isEnemySurroundOrHole = true;
            break;
        }
        if (!isEnemySurroundOrHole) {
            return;
        }
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        candidates.add(minePos.above());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            candidates.add(minePos.relative(direction).above());
        }
        int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && n == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        for (BlockPos candidate : candidates) {
            Vec3 targetAimVec;
            float selfDamage;
            boolean needLos;
            BlockPos support = candidate.below();
            if (AutoCrystalModule.mc.level.getBlockState(support).getBlock() != Blocks.OBSIDIAN && AutoCrystalModule.mc.level.getBlockState(support).getBlock() != Blocks.BEDROCK || !AutoCrystalModule.mc.level.getBlockState(candidate).isAir() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)candidate)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(candidate)) continue;
            boolean bl = needLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)candidate)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
            if (needLos && !WorldUtils.canSeeBlock(support) || AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(candidate), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) continue;
            AABB crystalBox = new AABB((double)(candidate.getX() - 1), (double)candidate.getY(), (double)(candidate.getZ() - 1), (double)(candidate.getX() + 2), (double)(candidate.getY() + 2), (double)(candidate.getZ() + 2));
            if (Night.WORLD_MANAGER.getReservedPlacements().stream().anyMatch(reserved -> crystalBox.intersects(new AABB(reserved))) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, null, candidate, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean dealsDamage = false;
            for (Player enemy : enemies) {
                float dmg;
                if (enemy.equals((Object)AutoCrystalModule.mc.player) || !((dmg = DamageUtils.getCrystalDamage((Entity)enemy, PositionUtils.extrapolate(enemy, this.extrapolation.getValue().intValue()), candidate, minePos, this.ignoreTerrain.getValue())) >= this.getMinimumDamage(enemy, this.minimumDamage.getValue().floatValue()))) continue;
                dealsDamage = true;
                break;
            }
            if (!dealsDamage) continue;
            CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(candidate);
            Vec3 vec3 = targetAimVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf((Vec3i)candidate).add(0.0, 0.5, 0.0);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(targetAimVec), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(targetAimVec));
            }
            boolean switched = false;
            if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                    this.savedSlot = previousSlot;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, previousSlot);
                switched = true;
            }
            this.place(candidate);
            if (switched) {
                InventoryUtils.switchBack(this.autoSwitch.getValue(), n, previousSlot);
            }
            this.mineIgnorePlacedPos = candidate;
            return;
        }
    }

    private void mineIgnoreDetonate() {
        if (this.shouldPause("Attack")) {
            return;
        }
        BlockPos placedPos = this.mineIgnorePlacedPos;
        this.mineIgnoreMinedPos = null;
        this.mineIgnorePlacedPos = null;
        if (placedPos == null) {
            return;
        }
        if (!this.attack.getValue()) {
            return;
        }
        for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive() || !crystal.blockPosition().below().equals((Object)placedPos)) continue;
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())));
            }
            this.attack(crystal);
            return;
        }
    }

    private List<Player> getPlayers() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            return List.of(AutoCrystalModule.mc.player);
        }
        long now = System.currentTimeMillis();
        if (now - this.lastPlayerCacheTime < 50L) {
            return this.cachedPlayers;
        }
        ArrayList<Player> players = new ArrayList<Player>();
        for (Player player : AutoCrystalModule.mc.level.players()) {
            boolean holdingCrystal;
            if (player == AutoCrystalModule.mc.player || EntityUtils.isGhost((Entity)player) || !player.isAlive() || AutoCrystalModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            boolean bl = holdingCrystal = player.getMainHandItem().getItem() == Items.END_CRYSTAL || player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(player) && !holdingCrystal) continue;
            players.add(player);
        }
        this.cachedPlayers = players;
        this.lastPlayerCacheTime = now;
        return players;
    }

    private List<Player> selectTargets(List<Player> players) {
        if (players.size() <= 1 || this.targetMode.getValue().equalsIgnoreCase("All")) {
            return players;
        }
        Player best = null;
        double bestScore = 0.0;
        for (Player player : players) {
            double score;
            switch (this.targetMode.getValue()) {
                case "Nearest": {
                    score = -AutoCrystalModule.mc.player.distanceToSqr((Entity)player);
                    break;
                }
                case "Farthest": {
                    score = AutoCrystalModule.mc.player.distanceToSqr((Entity)player);
                    break;
                }
                default: {
                    double d = score = (double)(-(player.getHealth() + player.getAbsorptionAmount()));
                }
            }
            if (best != null && !(score > bestScore)) continue;
            best = player;
            bestScore = score;
        }
        return List.of(best);
    }

    private boolean shouldPause(String process) {
        if (this.isPearlPaused()) {
            return true;
        }
        if (this.isXpPaused()) {
            return true;
        }
        if (this.awaitMine.getValue() && this.pauseForAutoMine()) {
            return true;
        }
        boolean eatingFlag = this.whileEating.getValue().equalsIgnoreCase("None") || process.equalsIgnoreCase("Attack") && this.whileEating.getValue().equalsIgnoreCase("Place") || process.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack");
        return eatingFlag && EntityUtils.isEating();
    }

    private float getMinimumDamage(Player player, float minimumDamage) {
        if (player == null) {
            return minimumDamage;
        }
        if (this.chestBreak.getValue() && AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity item;
            return entity instanceof ItemEntity && (item = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && item.getItem().getCount() >= 8 && item.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        }) && !AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(AutoCrystalModule.mc.player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity item;
            return entity instanceof ItemEntity && (item = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && item.getItem().getCount() >= 8 && item.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        })) {
            return 2.0f;
        }
        if (this.facePlaceMode.getValue().equalsIgnoreCase("None")) {
            return minimumDamage;
        }
        if (this.facePlaceSpeed.getValue().equalsIgnoreCase("Normal") || this.facePlaceTimer.hasTimeElapsed(this.facePlaceDelay.getValue().longValue() * 50L)) {
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Always")) {
                return Math.min(minimumDamage, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue() && player.getHealth() + player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
                return Math.min(minimumDamage, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.armorPlace.getValue()) {
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (stack.isEmpty() || stack.get(DataComponents.EQUIPPABLE) == null || Math.round((double)(stack.getMaxDamage() - stack.getDamageValue()) * 100.0 / (double)stack.getMaxDamage()) > (long)this.percentage.getValue().intValue()) continue;
                    return Math.min(minimumDamage, 2.0f);
                }
            }
        }
        return minimumDamage;
    }

    private boolean executeBasePlace() {
        boolean placed;
        int obsidianSlot;
        float existingDamage;
        if (this.isDead()) {
            return false;
        }
        if (!this.basePlace.getValue()) {
            return false;
        }
        if (this.shouldPause("Place")) {
            return false;
        }
        Player targetPlayer = this.target;
        if (targetPlayer == null) {
            double bestDistSq = Double.MAX_VALUE;
            for (Player player : AutoCrystalModule.mc.level.players()) {
                double distSq;
                if (EntityUtils.isGhost((Entity)player) || player == AutoCrystalModule.mc.player || !player.isAlive() || Night.FRIEND_MANAGER.contains(player.getName().getString()) || !((distSq = AutoCrystalModule.mc.player.distanceToSqr((Entity)player)) <= Mth.square((double)this.enemyRange.getValue().doubleValue())) || !(distSq < bestDistSq)) continue;
                bestDistSq = distSq;
                targetPlayer = player;
            }
        }
        if (targetPlayer == null) {
            return false;
        }
        PlaceTarget existingPlacement = this.placeTarget;
        float f = existingDamage = existingPlacement != null && existingPlacement.getPosition() != null ? existingPlacement.getDamage() : 0.0f;
        if (existingDamage >= targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount()) {
            return false;
        }
        BlockPos targetFeet = PositionUtils.getFlooredPosition((Entity)targetPlayer);
        double radius = this.basePlaceRadius.getValue().doubleValue();
        int radCeil = (int)Math.ceil(radius);
        int startY = targetFeet.getY() - 1;
        int endY = targetFeet.getY() - 3;
        LinkedHashSet<BlockPos> candidatePositions = new LinkedHashSet<BlockPos>();
        for (int y = startY; y >= endY; --y) {
            for (int dx = -radCeil; dx <= radCeil; ++dx) {
                for (int dz = -radCeil; dz <= radCeil; ++dz) {
                    if ((double)(dx * dx + dz * dz) > radius * radius) continue;
                    candidatePositions.add(new BlockPos(targetFeet.getX() + dx, y, targetFeet.getZ() + dz));
                }
            }
        }
        BlockPos bestCandidate = null;
        float bestDamage = 0.0f;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos pos : candidatePositions) {
            double score;
            float targetDamage;
            float selfDamage;
            BlockState state;
            if (!this.isBaseCandidateValid(pos) || (state = AutoCrystalModule.mc.level.getBlockState(pos)).getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), pos, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) || (targetDamage = DamageUtils.getCrystalDamage((Entity)targetPlayer, PositionUtils.extrapolate(targetPlayer, this.extrapolation.getValue().intValue()), pos, null, this.ignoreTerrain.getValue())) < this.basePlaceMinDamage.getValue().floatValue() || targetDamage < this.getMinimumDamage(targetPlayer, this.minimumDamage.getValue().floatValue()) && targetDamage < targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount() && !(targetDamage * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount()) || existingDamage > 0.0f && targetDamage < existingDamage * this.basePlaceRatio.getValue().floatValue() || !((score = (double)(-targetDamage) * 10.0 + AutoCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) + targetPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) < bestScore)) continue;
            bestScore = score;
            bestCandidate = pos;
            bestDamage = targetDamage;
        }
        if (bestCandidate == null) {
            return false;
        }
        obsidianSlot = this.basePlaceSwitch.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.find(Items.OBSIDIAN, 0, this.basePlaceSwitch.getValue().equalsIgnoreCase("AltSwap") || this.basePlaceSwitch.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (obsidianSlot == -1) {
            return false;
        }
        Direction placeDir = WorldUtils.getDirection(bestCandidate, this.basePlaceStrictDirection.getValue());
        if (placeDir == null && !this.basePlaceAirPlace.getValue()) {
            return false;
        }
        int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.basePlaceSwitch.getValue(), obsidianSlot, prevSlot)) {
            return false;
        }
        if (placeDir != null) {
            placed = WorldUtils.placeBlock(bestCandidate, placeDir, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", true, this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
        } else {
            WorldUtils.airPlaceBlock(bestCandidate, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
            placed = true;
        }
        InventoryUtils.switchBack(this.basePlaceSwitch.getValue(), obsidianSlot, prevSlot);
        if (placed) {
            this.placeTarget = new PlaceTarget(bestCandidate, targetPlayer, Collections.emptyList(), null, bestDamage, 1);
            if (this.place.getValue()) {
                boolean hasCrystal;
                int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                int prevCrystalSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
                boolean bl = hasCrystal = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || crystalSlot != -1 && !this.autoSwitch.getValue().equalsIgnoreCase("None");
                if (hasCrystal) {
                    Vec3 targetAimVec;
                    CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(bestCandidate);
                    Vec3 vec3 = targetAimVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf((Vec3i)bestCandidate).add(0.0, 0.5, 0.0);
                    if (this.rotate.getValue().equalsIgnoreCase("Normal") || this.basePlaceRotate.getValue()) {
                        Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(targetAimVec), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                    } else if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                        Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(targetAimVec));
                    }
                    boolean switchedCrystal = false;
                    if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && crystalSlot != -1) {
                        InventoryUtils.switchSlot(this.autoSwitch.getValue(), crystalSlot, prevCrystalSlot);
                        switchedCrystal = true;
                    }
                    this.place(bestCandidate);
                    if (switchedCrystal) {
                        InventoryUtils.switchBack(this.autoSwitch.getValue(), crystalSlot, prevCrystalSlot);
                    }
                }
            }
            Night.RENDER_MANAGER.setRenderPosition(bestCandidate);
        }
        return placed;
    }

    private boolean isBaseCandidateValid(BlockPos pos) {
        BlockState spaceState2;
        boolean needLos;
        if (AutoCrystalModule.mc.level == null || AutoCrystalModule.mc.player == null) {
            return false;
        }
        if (AutoCrystalModule.mc.level.isOutsideBuildHeight(pos)) {
            return false;
        }
        if (AutoCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.basePlaceRange.getValue().doubleValue())) {
            return false;
        }
        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            return false;
        }
        boolean bl = needLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        if (needLos && !WorldUtils.canSeeBlock(pos)) {
            return false;
        }
        BlockState state = AutoCrystalModule.mc.level.getBlockState(pos);
        if (!state.canBeReplaced()) {
            return false;
        }
        if (!WorldUtils.isPlaceable(pos)) {
            return false;
        }
        if (!this.basePlaceAirPlace.getValue() && WorldUtils.getDirection(pos, this.basePlaceStrictDirection.getValue()) == null) {
            return false;
        }
        BlockPos crystalSpace = pos.above();
        BlockState spaceState = AutoCrystalModule.mc.level.getBlockState(crystalSpace);
        if (!spaceState.canBeReplaced() || !AutoCrystalModule.mc.level.getFluidState(crystalSpace).isEmpty()) {
            return false;
        }
        if (this.placements.getValue().equalsIgnoreCase("Protocol") && !(spaceState2 = AutoCrystalModule.mc.level.getBlockState(pos.above(2))).canBeReplaced()) {
            return false;
        }
        return AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(pos), entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal)).isEmpty();
    }

    private AABB getCrystalPlacementBox(BlockPos position) {
        return new AABB(position.offset(0, 1, 0));
    }

    public float getBestPlaceDamage() {
        return this.placeTarget == null ? 0.0f : this.placeTarget.getDamage();
    }

    @Generated
    public Player getTarget() {
        return this.target;
    }

    @Generated
    public String getCalculationDamage() {
        return this.calculationDamage;
    }

    public static class PlaceTarget {
        private BlockPos position;
        private Player player;
        private List<Entity> obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public PlaceTarget clone() {
            return new PlaceTarget(this.position, this.player, this.obstructions, this.exception, this.damage, this.calculations);
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public Player getPlayer() {
            return this.player;
        }

        @Generated
        public List<Entity> getObstructions() {
            return this.obstructions;
        }

        @Generated
        public BlockPos getException() {
            return this.exception;
        }

        @Generated
        public float getDamage() {
            return this.damage;
        }

        @Generated
        public int getCalculations() {
            return this.calculations;
        }

        @Generated
        public PlaceTarget(BlockPos position, Player player, List<Entity> obstructions, BlockPos exception, float damage, int calculations) {
            this.position = position;
            this.player = player;
            this.obstructions = obstructions;
            this.exception = exception;
            this.damage = damage;
            this.calculations = calculations;
        }
    }
}

