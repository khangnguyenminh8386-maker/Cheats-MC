/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.core.BlockPos
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
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.pingbypass.modules.submodules.crystal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.DestroyBlockEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerDeathEvent;
import night.modules.impl.combat.CrystalPlacementHelper;
import night.modules.impl.combat.SuicideModule;
import night.modules.impl.movement.PhaseModule;
import night.modules.impl.player.KeyActionModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.modules.PbModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.IMinecraft;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.Counter;
import night.utils.system.Timer;

public class ServerAutoCrystal
extends PbModule
implements IMinecraft {
    public BooleanSetting attack = new BooleanSetting("Attack", "Automatically attacks crystals that are deemed safe.", true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "The speed at which crystals will be attacked.", Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting attackRange = new NumberSetting("AttackRange", "The maximum distance at which crystals will be attacked.", 4.5, 0.0, 8.0);
    public NumberSetting attackWallsRange = new NumberSetting("AttackWallsRange", "The maximum distance at which crystals will be attacked through walls.", 4.5, 0.0, 8.0);
    public ModeSetting antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", "None", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", true);
    public BooleanSetting inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", true);
    public BooleanSetting place = new BooleanSetting("Place", "Automatically places crystals on positions that are deemed safe and lethal enough.", true);
    public NumberSetting placeSpeed = new NumberSetting("PlaceSpeed", "The speed at which crystals will be placed.", Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "The maximum distance at which positions will be placed on.", 4.5, 0.0, 8.0);
    public NumberSetting placeWallsRange = new NumberSetting("PlaceWallsRange", "The maximum distance at which positions will be placed on through walls.", 4.5, 0.0, 8.0);
    public ModeSetting placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", "Native", new String[]{"Native", "Protocol"});
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", true);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", "None", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals.", false);
    public NumberSetting swapDelay = new NumberSetting("SwapDelay", "The delay in ticks after swapping before placing or attacking crystals.", 0, 0, 20);
    public ModeSetting sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", "Strong", new String[]{"None", "Strict", "Strong"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", "Normal", new String[]{"None", "Normal", "Packet", "Silent"});
    public ModeSetting swing = new ModeSetting("Swing", "The hand that will be used for swinging.", "Default", new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"});
    public BooleanSetting yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", false);
    public NumberSetting yawStepThreshold = new NumberSetting("YawStepThreshold", "The threshold in order for yaw to be modified.", 75, 1, 180);
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", false);
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", 0, 0, 20);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", 10.0, 0.0, 24.0);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", false);
    public BooleanSetting chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", false);
    public BooleanSetting pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", true);
    public BooleanSetting pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", true);
    public BooleanSetting gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", false);
    public NumberSetting loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", 50, 0, 1000);
    public ModeSetting whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", "Both", new String[]{"None", "Attack", "Place", "Both"});
    public BooleanSetting godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", false);
    public NumberSetting predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", 10, 1, 20);
    public NumberSetting offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", 0, 0, 2);
    public ModeSetting godSwing = new ModeSetting("GodSwing", "The swinging that will be done for each predicted attack.", "Normal", new String[]{"None", "Normal", "Strict"});
    public BooleanSetting fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", false);
    public BooleanSetting antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", false);
    public NumberSetting kickThreshold = new NumberSetting("KickThreshold", "The tick threshold for the kick prevention.", 5, 1, 10);
    public ModeSetting facePlaceMode = new ModeSetting("FaceplaceMode", "The checks that will be done in order to faceplace.", "Dynamic", new String[]{"None", "Dynamic", "Always"});
    public ModeSetting facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "The speed that players will be faceplaced at.", "Normal", new String[]{"Normal", "Custom"});
    public NumberSetting facePlaceDelay = new NumberSetting("FaceplaceDelay", "The ticks that have to be waited for before faceplacing again.", 11, 0, 20);
    public BooleanSetting healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", true);
    public NumberSetting health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", Float.valueOf(8.0f), Float.valueOf(0.0f), Float.valueOf(36.0f));
    public BooleanSetting armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", true);
    public NumberSetting percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", 10, 1, 100);
    public NumberSetting minimumDamage = new NumberSetting("MinimumDamage", "The minimum damage that has to be dealt to enemies.", 6.0, 0.0, 36.0);
    public NumberSetting maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "The maximum damage that can be dealt to you by crystals.", 10.0, 0.0, 36.0);
    public NumberSetting lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", Float.valueOf(1.5f), Float.valueOf(0.0f), Float.valueOf(4.0f));
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", true);
    public BooleanSetting ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", true);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> pendingCalc = null;
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
    private String calculationTime = "0.00ms";
    private int calculationCount = 0;
    private String calculationDamage = "0.00";
    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;
    private int highestID = -100000;
    private int kickTicks = 0;
    private int savedSlot = -1;
    private static final float STICKY_EPSILON = 0.5f;
    private long lastPearlThrowTime = 0L;
    private long lastXpThrowTime = 0L;

    public ServerAutoCrystal() {
        super("AutoCrystal");
    }

    @Override
    public void onEnable() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @Override
    public void onDisable() {
        Night.EVENT_HANDLER.unsubscribe(this);
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
    }

    @Override
    public List<Setting> getSettings() {
        return List.of(this.attack, this.attackSpeed, this.attackRange, this.attackWallsRange, this.antiWeakness, this.instant, this.inhibit, this.place, this.placeSpeed, this.placeRange, this.placeWallsRange, this.placements, this.blockDestruction, this.autoSwitch, this.swapBack, this.swapDelay, this.sequential, this.rotate, this.swing, this.yawStep, this.yawStepThreshold, this.raytrace, this.extrapolation, this.enemyRange, this.chestBreak, this.gameLoop, this.loopDelay, this.whileEating, this.godSync, this.predictions, this.offset, this.godSwing, this.fast, this.antiKick, this.kickThreshold, this.facePlaceMode, this.facePlaceSpeed, this.facePlaceDelay, this.healthPlace, this.health, this.armorPlace, this.percentage, this.minimumDamage, this.maximumSelfDamage, this.lethalMultiplier, this.antiSuicide, this.ignoreTerrain);
    }

    private boolean isDead() {
        if (ServerAutoCrystal.mc.player == null || ServerAutoCrystal.mc.level == null) {
            return true;
        }
        if (!ServerAutoCrystal.mc.player.isAlive() || ServerAutoCrystal.mc.player.isDeadOrDying() || ServerAutoCrystal.mc.player.getHealth() <= 0.0f) {
            return true;
        }
        return ServerAutoCrystal.mc.gui.screen() instanceof DeathScreen;
    }

    @Override
    public void tick() {
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
        int currentSlot = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
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
        Runnable runnable = () -> {
            long startTime = System.nanoTime();
            this.attackTarget = this.calculateCrystals();
            this.placeTarget = this.calculatePlacements(null);
            long calcNanos = System.nanoTime() - startTime;
            this.calculationTime = new DecimalFormat("0.00").format((double)calcNanos / 1000000.0) + "ms";
            this.calculationCount = this.placeTarget == null ? 0 : this.placeTarget.getCalculations();
            this.calculationDamage = this.placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(this.placeTarget.getDamage());
            Player player = this.target = this.placeTarget == null ? null : this.placeTarget.getPlayer();
            if (this.blockDestruction.getValue()) {
                SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
                BlockPos position = null;
                if (module.getPrimary() != null && module.getPrimary().isMining()) {
                    position = module.getPrimary().getPosition();
                }
                if (position != null) {
                    this.mineTarget = this.calculatePlacements(position);
                }
            }
        };
        if (this.pendingCalc == null || this.pendingCalc.isDone()) {
            this.pendingCalc = this.executor.submit(runnable);
        }
        if (this.gameLoop.getValue()) {
            if (!this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue())) {
                return;
            }
            this.loopTimer.reset();
        }
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
        if (this.isDead() || this.isPearlPaused() || this.isXpPaused()) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
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

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!this.isToggled() || this.isDead() || this.getPlayers().isEmpty() || this.shouldPause("Attack")) {
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
        if (!this.placedCrystals.containsKey(crystal.blockPosition().below())) {
            return;
        }
        if (crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            return;
        }
        if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(crystal.blockPosition())) {
            return;
        }
        if (!WorldUtils.canSee((Entity)crystal) && (this.raytrace.getValue() || crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue()))) {
            return;
        }
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())));
        }
        this.attack(crystal);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            this.placeCrystals(true);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        boolean flag;
        Vec3 targetAimVec;
        PlaceTarget mineTarget;
        if (!this.isToggled() || this.isDead() || this.getPlayers().isEmpty() || this.shouldPause("Place")) {
            return;
        }
        this.kickTicks = 0;
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
        int previousSlot = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        boolean switched = false;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1 && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        PlaceTarget placeTarget = mineTarget = this.mineTarget == null ? null : this.mineTarget.clone();
        if (mineTarget == null || mineTarget.getPosition() != null && !minedPosition.equals((Object)mineTarget.getException())) {
            mineTarget = this.calculatePlacements(minedPosition);
        }
        if (mineTarget == null || mineTarget.getPosition() == null || mineTarget.getDamage() <= 0.0f) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        BlockPos position = mineTarget.getPosition();
        if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(position);
        if (!WorldUtils.canSeeBlock(position) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue()))) {
            return;
        }
        EndCrystal existingCrystal = null;
        for (Entity entity2 : ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(position.above()), entity -> true)) {
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
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations((Entity)existingCrystal));
            }
            this.attack(existingCrystal);
            return;
        }
        CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(position);
        Vec3 vec3 = targetAimVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0);
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(targetAimVec));
        }
        SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean bl = flag = module.switchReset.getValue() && (module.switchMode.getValue().equalsIgnoreCase("Normal") || module.switchMode.getValue().equalsIgnoreCase("AltSwap") || module.switchMode.getValue().equalsIgnoreCase("AltPickup"));
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (!flag && this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = previousSlot;
            }
            InventoryUtils.switchSlot(flag ? "AltSwap" : this.autoSwitch.getValue(), slot, previousSlot);
            switched = true;
        }
        this.place(position);
        if (switched) {
            InventoryUtils.switchBack(flag ? "AltSwap" : this.autoSwitch.getValue(), slot, previousSlot);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (ServerAutoCrystal.mc.player == null || ServerAutoCrystal.mc.level == null) {
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
    public void onPlayerDeath(PlayerDeathEvent event) {
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
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket) {
            this.swapTimer.reset();
        }
        if (event.getPacket() instanceof ServerboundUseItemPacket && ServerAutoCrystal.mc.player != null) {
            if (ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
                this.lastPearlThrowTime = System.currentTimeMillis();
            } else if (ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
                this.lastXpThrowTime = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        this.highestID = -100000;
    }

    public String getMetaData() {
        return this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond;
    }

    private void attackCrystals() {
        EndCrystal crystal;
        if (this.isDead() || this.shouldPause("Attack")) {
            return;
        }
        if (this.getPlayers().isEmpty()) {
            this.attackTarget = null;
            return;
        }
        EndCrystal overrideCrystal = null;
        PlaceTarget pt = this.placeTarget;
        boolean flag = pt != null && pt.getPosition() == null && pt.obstructions != null && !pt.obstructions.isEmpty();
        for (Entity entity : flag ? pt.obstructions : ServerAutoCrystal.mc.level.entitiesForRendering()) {
            EndCrystal crystal2;
            if (!(entity instanceof EndCrystal) || !(crystal2 = (EndCrystal)entity).isAlive() || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || !flag && !this.placedCrystals.containsKey(crystal2.blockPosition().below()) || crystal2.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(crystal2.blockPosition()) || !WorldUtils.canSee((Entity)crystal2) && (this.raytrace.getValue() || crystal2.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue()))) continue;
            overrideCrystal = crystal2;
            break;
        }
        EndCrystal endCrystal = crystal = overrideCrystal == null ? this.attackTarget : overrideCrystal;
        if (crystal == null) {
            return;
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f)) || this.attackedSequentially) {
            if (this.attackedSequentially) {
                this.attackedSequentially = false;
            }
            return;
        }
        Entity entity = ServerAutoCrystal.mc.level.getEntity(crystal.getId());
        String bailReason = null;
        if (entity == null) {
            bailReason = "entity-gone";
        } else if (!(entity instanceof EndCrystal)) {
            bailReason = "not-end-crystal";
        } else if (!((EndCrystal)entity).isAlive()) {
            bailReason = "dead";
        } else if (this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId())) {
            bailReason = "inhibit";
        } else if (entity.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            bailReason = "range";
        } else if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            bailReason = "border";
        } else if (!WorldUtils.canSee(entity) && (this.raytrace.getValue() || entity.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue()))) {
            bailReason = "cannot-see";
        }
        if (bailReason != null) {
            return;
        }
        this.attackRunnable = () -> {
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystal.blockPosition())));
            }
            this.attack(crystal);
        };
    }

    private void placeCrystals(boolean sequential) {
        PlaceTarget placeTarget;
        if (this.isDead() || this.shouldPause("Place")) {
            return;
        }
        PlaceTarget placeTarget2 = placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
        if (placeTarget == null || placeTarget.getPosition() == null) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        if (placeTarget.getPlayer() == null || !placeTarget.getPlayer().isAlive() || placeTarget.getPlayer().isDeadOrDying() || placeTarget.getPlayer().getHealth() <= 0.0f) {
            this.placeTarget = null;
            this.target = null;
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && slot == -1 && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        BlockPos position = placeTarget.getPosition();
        if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(position);
        if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(position)) {
            return;
        }
        if (ServerAutoCrystal.mc.level.getBlockState(position).getBlock() != Blocks.OBSIDIAN && ServerAutoCrystal.mc.level.getBlockState(position).getBlock() != Blocks.BEDROCK) {
            return;
        }
        if (!ServerAutoCrystal.mc.level.getBlockState(position.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !ServerAutoCrystal.mc.level.getBlockState(position.offset(0, 2, 0)).isAir()) {
            return;
        }
        if (!WorldUtils.canSeeBlock(position) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue()))) {
            return;
        }
        if (ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(position.offset(0, 1, 0)), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) {
            return;
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        if (!sequential && this.placedSequentially) {
            this.placedSequentially = false;
            return;
        }
        this.placeRunnable = () -> {
            Vec3 targetAimVec;
            boolean switched = false;
            CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(position);
            Vec3 vec3 = targetAimVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0);
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(targetAimVec));
            }
            if (ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
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
            if (this.godSync.getValue()) {
                boolean flag;
                boolean bl = flag = !this.antiKick.getValue() || !(ServerAutoCrystal.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(ServerAutoCrystal.mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !Night.MODULE_MANAGER.getModule(KeyActionModule.class).isXpActive();
                if ((!this.antiKick.getValue() || this.kickTicks > this.kickThreshold.getValue().intValue()) && flag) {
                    if (!this.fast.getValue()) {
                        for (Entity entity : ServerAutoCrystal.mc.level.entitiesForRendering()) {
                            if (entity.getId() <= this.highestID) continue;
                            this.highestID = entity.getId();
                        }
                    }
                    for (int i = 1 - this.offset.getValue().intValue(); i < this.predictions.getValue().intValue(); ++i) {
                        Entity entity;
                        entity = ServerAutoCrystal.mc.level.getEntity(this.highestID);
                        if (entity != null && !(entity instanceof EndCrystal)) continue;
                        int id = this.highestID + i;
                        mc.getConnection().send((Packet)new ServerboundAttackPacket(id));
                        if (this.godSwing.getValue().equals("Strict")) {
                            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        }
                        this.attackedCrystals.put(id, System.currentTimeMillis());
                    }
                    if (this.godSwing.getValue().equals("Normal")) {
                        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                }
                ++this.kickTicks;
            }
        };
        if (sequential) {
            this.placeRunnable.run();
            this.placeRunnable = null;
            this.placedSequentially = true;
        }
    }

    private EndCrystal calculateCrystals() {
        if (!this.attack.getValue()) {
            return null;
        }
        if (this.shouldPause("Attack")) {
            return null;
        }
        List<Player> players = this.getPlayers();
        if (players.isEmpty()) {
            return null;
        }
        EndCrystal optimalCrystal = null;
        float optimalDamage = 0.0f;
        for (Entity entity : ServerAutoCrystal.mc.level.entitiesForRendering()) {
            float damage;
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive() || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(crystal.blockPosition()) || !WorldUtils.canSee((Entity)crystal) && (this.raytrace.getValue() || crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue())) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((damage = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, crystal, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && damage > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount())) continue;
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
        if (!this.place.getValue()) {
            return null;
        }
        if (this.shouldPause("Place") || (this.autoSwitch.getValue().equalsIgnoreCase("None") || InventoryUtils.findHotbar(Items.END_CRYSTAL) == -1) && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return null;
        }
        List<Player> players = this.getPlayers();
        if (players.isEmpty()) {
            return null;
        }
        BlockPos optimalPosition = null;
        Player optimalPlayer = null;
        ArrayList<Entity> obstructions = new ArrayList<Entity>();
        float optimalDamage = 0.0f;
        BlockPos stickyPosition = this.placeTarget == null ? null : this.placeTarget.getPosition();
        int calculations = 0;
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue())); ++i) {
            float selfDamage;
            BlockPos position = ServerAutoCrystal.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(position) || ServerAutoCrystal.mc.level.getBlockState(position).getBlock() != Blocks.OBSIDIAN && ServerAutoCrystal.mc.level.getBlockState(position).getBlock() != Blocks.BEDROCK || !ServerAutoCrystal.mc.level.getBlockState(position.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !ServerAutoCrystal.mc.level.getBlockState(position.offset(0, 2, 0)).isAir() || !WorldUtils.canSeeBlock(position) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue())) || ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(position.offset(0, 1, 0)), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) continue;
            List<Entity> obstructingCrystals = ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(position.offset(0, 1, 0)), entity -> true).stream().filter(entity -> {
                EndCrystal crystal;
                return entity instanceof EndCrystal && (!this.placedCrystals.containsKey((crystal = (EndCrystal)entity).blockPosition().below()) || crystal.tickCount >= 20 - this.attackSpeed.getValue().intValue() + 15);
            }).toList();
            if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, position, exception, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount())) continue;
            boolean override = false;
            for (Player player : players) {
                ++calculations;
                float damage = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), position, exception, this.ignoreTerrain.getValue());
                if (damage < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount())) continue;
                if (exception == null && !obstructingCrystals.isEmpty()) {
                    obstructions.add(obstructingCrystals.getFirst());
                    break;
                }
                float comparisonDamage = damage + (position.equals((Object)stickyPosition) ? 0.5f : 0.0f);
                if (!(comparisonDamage > optimalDamage) && !(damage > player.getHealth() + player.getAbsorptionAmount())) continue;
                optimalPosition = position;
                optimalPlayer = player;
                optimalDamage = comparisonDamage;
                if (!(damage > player.getHealth() + player.getAbsorptionAmount())) continue;
                override = true;
                break;
            }
            if (override) break;
        }
        if (optimalPosition == null) {
            return new PlaceTarget(null, null, obstructions, null, 0.0f, calculations);
        }
        return new PlaceTarget(optimalPosition, optimalPlayer, obstructions, exception, optimalDamage, calculations);
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

    private boolean isEnemyAtMinePos(BlockPos pPos) {
        if (pPos == null || ServerAutoCrystal.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pPos).inflate(1.5, 2.0, 1.5);
        for (Player player : ServerAutoCrystal.mc.level.players()) {
            if (player.equals((Object)ServerAutoCrystal.mc.player) || EntityUtils.isGhost((Entity)player) || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !player.getBoundingBox().intersects(box)) continue;
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
        if (crystal == null || ServerAutoCrystal.mc.level == null || ServerAutoCrystal.mc.player == null) {
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

    private void attack(EndCrystal crystal) {
        int slot;
        if (crystal == null || !crystal.isAlive() || crystal.isRemoved()) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(crystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float damage = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, crystal, this.ignoreTerrain.getValue());
            if (damage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && damage > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        int previousSlot = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        int switchedSlot = -1;
        if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && ServerAutoCrystal.mc.player.hasEffect(MobEffects.WEAKNESS) && (slot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
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
        InteractionHand hand = ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(position);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(hand, new BlockHitResult(placement.hitVec, placement.direction, position, false), sequence));
        switch (this.swing.getValue()) {
            case "Default": {
                ServerAutoCrystal.mc.player.swing(hand);
                break;
            }
            case "Packet": {
                mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
                break;
            }
            case "Mainhand": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
            case "Offhand": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.OFF_HAND);
                break;
            }
            case "Both": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.MAIN_HAND);
                ServerAutoCrystal.mc.player.swing(InteractionHand.OFF_HAND);
            }
        }
        this.placedCrystals.put(position, System.currentTimeMillis());
        this.countedCrystals.put(position, System.currentTimeMillis());
        this.placeTimer.reset();
        ++this.totalPlaces;
    }

    private List<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            players.add((Player)ServerAutoCrystal.mc.player);
            return players;
        }
        for (Player player : ServerAutoCrystal.mc.level.players()) {
            boolean holdingCrystal;
            if (player == ServerAutoCrystal.mc.player || EntityUtils.isGhost((Entity)player) || !player.isAlive() || ServerAutoCrystal.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            boolean bl = holdingCrystal = player.getMainHandItem().getItem() == Items.END_CRYSTAL || player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(player) && !holdingCrystal) continue;
            players.add(player);
        }
        return players;
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
        if (ServerAutoCrystal.mc.player != null) {
            boolean holdingPearl;
            boolean bl = holdingPearl = ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
            if (holdingPearl && (ServerAutoCrystal.mc.options.keyUse.isDown() || ServerAutoCrystal.mc.player.isUsingItem())) {
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
        if (ServerAutoCrystal.mc.player != null) {
            boolean holdingXp;
            boolean bl = holdingXp = ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE;
            if (holdingXp && (ServerAutoCrystal.mc.options.keyUse.isDown() || ServerAutoCrystal.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldPause(String process) {
        if (this.isPearlPaused()) {
            return true;
        }
        if (this.isXpPaused()) {
            return true;
        }
        boolean eatingFlag = this.whileEating.getValue().equalsIgnoreCase("None") || process.equalsIgnoreCase("Attack") && this.whileEating.getValue().equalsIgnoreCase("Place") || process.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack");
        return eatingFlag && ServerAutoCrystal.mc.player.isUsingItem();
    }

    private float getMinimumDamage(Player player, float minimumDamage) {
        if (player == null) {
            return minimumDamage;
        }
        if (this.chestBreak.getValue() && ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity item;
            return entity instanceof ItemEntity && (item = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && item.getItem().getCount() >= 8 && item.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        }) && !ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(ServerAutoCrystal.mc.player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
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

