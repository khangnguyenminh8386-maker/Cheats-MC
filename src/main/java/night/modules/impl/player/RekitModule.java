/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.ShulkerBoxMenu
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.player;
import java.text.Normalizer.Form;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.item.enchantment.Enchantment;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.SpeedMineModule;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.Renderer3D;
import night.utils.input.KeyboardUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.FileUtils;

@RegisterModule(name="Rekit", description="Automatically regears from an ender chest shulker using a saved kit.", category=Module.Category.PLAYER)
public class RekitModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between pull actions.", 1, 0, 10);
    public NumberSetting actionsPerTick = new NumberSetting("Frequency", "Max pull actions per tick.", 1, 1, 5);
    public BooleanSetting auto = new BooleanSetting("Auto", "Auto pull shulkers from the ender chest.", false);
    public BooleanSetting silentContainer = new BooleanSetting("SilentContainer", "Pull items without the container GUI showing on screen.", false);
    public ModeSetting swapMode = new ModeSetting("SwapMode", "Swap type used when placing shulkers and swapping to a breaking tool in Auto mode.", "AltSwap", InventoryUtils.SWITCH_MODES);
    public BooleanSetting considerShulkerName = new BooleanSetting("ConsiderShulkerName", "Prefer the shulker whose custom name matches the active kit's name, overriding content-based scoring.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
    public BindSetting autoPlaceBind = new BindSetting("AutoPlace", "Keybind: places the shulker in your inventory, opens it, pulls kit items, closes, then breaks it. No ender chest -- one-shot version of Auto.", 0).disableHoldModes();
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing the shulker.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public ModeSetting pickaxePref = new ModeSetting("Pickaxe", "Preferred pickaxe enchant when choosing between candidates.", "Efficiency", new String[]{"Efficiency", "SilkTouch"});
    public ModeSetting helmetPref = new ModeSetting("Helmet", "Preferred helmet enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting chestplatePref = new ModeSetting("Chestplate", "Preferred chestplate enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting leggingsPref = new ModeSetting("Leggings", "Preferred leggings enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting bootsPref = new ModeSetting("Boots", "Preferred boots enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public Map<Integer, KitItem> activeKit = new HashMap<Integer, KitItem>();
    public String activeKitName = "";
    private int ticks = 0;
    private static final String FOLDER = "Night/Kits/";
    private static final String LAST_KIT_FILE = "Night/last_kit_save.txt";
    public static volatile long lastContainerActionMs = 0L;
    private AutoState autoState = AutoState.IDLE;
    private int autoTicks = 0;
    private int emptyTicks = 0;
    private int shulkerEnderSlot = -1;
    private BlockPos placedShulkerPos = null;
    private long placedShulkerRenderMs = 0L;
    private BlockPos enderChestPos = null;
    private boolean hotbarSwapSettled = false;
    private int hotbarSwapAttempts = 0;
    private static final int MAX_HOTBAR_SWAP_ATTEMPTS = 3;
    private int placeFailStreak = 0;
    private static final int MAX_PLACE_FAIL_STREAK = 3;
    private int cursorWaitTicks = 0;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;
    private int displacedHotbarSlot = -1;
    private int displacedEnderSlot = -1;
    private boolean autoPlaceActive = false;
    private boolean autoPlaceBindWasDown = false;
    private int savedSlot = -1;
    private static final long PLACE_ANIM_MS = 400L;
    private static final long RECENT_COMBAT_TICKS = 100L;
    private static final int FAR_REACH_DECOY_Y_OFFSET = 2000;

    public boolean isAutoActive() {
        return this.autoState != AutoState.IDLE;
    }

    public RekitModule() {
        this.restoreLastKit();
    }

   private void restoreLastKit() {
      try {
         if (!FileUtils.fileExists("Night/last_kit_save.txt")) {
            return;
         }

         String name = Files.readString(Paths.get("Night/last_kit_save.txt")).trim();
         if (name.isEmpty()) {
            return;
         }

         File kitFile = new File("Night/Kits/", name + ".json");
         if (!kitFile.exists()) {
            return;
         }

         Gson gson = new Gson();

         try (FileReader reader = new FileReader(kitFile)) {
            Type type = (new TypeToken<Map<Integer, RekitModule.KitItem>>() {}).getType();
            this.activeKit = gson.fromJson(reader, type);
            this.activeKitName = name;
         }
      } catch (Exception var9) {
      }
   }

    private void info(String msg) {
        Night.CHAT_MANAGER.tagged(msg, this.getName());
    }

    private void error(String msg) {
        Night.CHAT_MANAGER.tagged("\u00a7c" + msg, this.getName());
    }

    public List<String> getKitNames() {
        ArrayList<String> names = new ArrayList<String>();
        File[] files = new File(FOLDER).listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.getName().endsWith(".json")) continue;
                names.add(f.getName().replace(".json", ""));
            }
        }
        return names;
    }

    public void saveKit(String name) {
        if (RekitModule.mc.player == null) {
            return;
        }
        if (RekitModule.mc.player.isCreative() || RekitModule.mc.player.isSpectator()) {
            return;
        }
        HashMap<Integer, KitItem> kitData = new HashMap<Integer, KitItem>();
        for (int i = 0; i < 36; ++i) {
            int slot = this.getHandlerSlotPlayerOnly(i);
            ItemStack stack = RekitModule.mc.player.inventoryMenu.getSlot(slot).getItem();
            if (stack.isEmpty()) continue;
            KitItem k = new KitItem();
            k.id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            k.maxCount = stack.getMaxStackSize();
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                k.name = stack.getHoverName().getString();
            }
            kitData.put(i, k);
        }
        try {
            FileUtils.createDirectory(FOLDER);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(new File(FOLDER, name + ".json"));){
                gson.toJson(kitData, (Appendable)writer);
            }
            this.activeKit = kitData;
            this.activeKitName = name;
            Files.writeString(Paths.get(LAST_KIT_FILE, new String[0]), (CharSequence)name, new OpenOption[0]);
            this.info("Kit saved and activated: " + name);
        }
        catch (Exception e) {
            this.error("Error saving kit!");
        }
    }

   public void loadKit(String name) {
      try {
         File file = new File("Night/Kits/", name + ".json");
         if (!file.exists()) {
            this.error("Kit not found: " + name);
            return;
         }

         Gson gson = new Gson();

         try (FileReader reader = new FileReader(file)) {
            Type type = (new TypeToken<Map<Integer, RekitModule.KitItem>>() {}).getType();
            this.activeKit = gson.fromJson(reader, type);
            this.activeKitName = name;
         }

         Files.writeString(Paths.get("Night/last_kit_save.txt"), name);
         this.info("Kit loaded: " + name);
      } catch (Exception e) {
         this.error("Error occurred while reading kit!");
      }
   }

    public void listKits() {
        File[] files = new File(FOLDER).listFiles();
        if (files == null || files.length == 0) {
            this.info("You don't have any kits.");
            return;
        }
        this.info("Available kits:");
        for (File f : files) {
            if (!f.getName().endsWith(".json")) continue;
            String name = f.getName().replace(".json", "");
            if (name.equals(this.activeKitName)) {
                Night.CHAT_MANAGER.tagged("\u00a79- " + name + " [active]", this.getName());
                continue;
            }
            Night.CHAT_MANAGER.tagged("\u00a77- " + name, this.getName());
        }
    }

    public void showActiveKit() {
        if (this.activeKitName == null || this.activeKitName.isEmpty()) {
            this.error("No kit is currently active.");
        } else {
            Night.CHAT_MANAGER.tagged("\u00a7aActive kit: \u00a79" + this.activeKitName, this.getName());
        }
    }

    public void deleteKit(String name) {
        File file = new File(FOLDER, name + ".json");
        if (file.exists() && file.delete()) {
            if (name.equals(this.activeKitName)) {
                this.activeKit.clear();
                this.activeKitName = "";
            }
            this.info("Kit deleted: " + name);
        } else {
            this.error("Failed to delete kit.");
        }
    }

    @Override
    public void onEnable() {
        this.autoState = AutoState.IDLE;
        this.autoTicks = 0;
        this.emptyTicks = 0;
        this.shulkerEnderSlot = -1;
        this.placedShulkerPos = null;
        this.enderChestPos = null;
        this.hotbarSwapSettled = false;
        this.cursorWaitTicks = 0;
        this.displacedHotbarSlot = -1;
        this.displacedEnderSlot = -1;
        this.autoPlaceActive = false;
        this.autoPlaceBindWasDown = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (RekitModule.mc.player == null) {
            return;
        }
        if (RekitModule.mc.gui.screen() == null) {
            boolean down = KeyboardUtils.isBindDown(this.autoPlaceBind.getValue());
            if (down && !this.autoPlaceBindWasDown && this.autoState == AutoState.IDLE && !RekitModule.mc.player.isCreative() && !RekitModule.mc.player.isSpectator() && this.findShulkerInInventory() != -1) {
                this.autoPlaceActive = true;
                this.autoState = AutoState.PLACE_SHULKER;
                this.autoTicks = 0;
                this.hotbarSwapSettled = false;
                this.hotbarSwapAttempts = 0;
            }
            this.autoPlaceBindWasDown = down;
        } else {
            this.autoPlaceBindWasDown = false;
        }
        if (RekitModule.mc.player.isCreative() || RekitModule.mc.player.isSpectator()) {
            return;
        }
        if (this.auto.getValue() || this.autoPlaceActive) {
            this.handleAutoTick();
            return;
        }
        this.hideContainerScreenIfSilent();
        if (!RekitModule.mc.player.hasContainerOpen()) {
            return;
        }
        this.manualPullTick();
    }

    private void hideContainerScreenIfSilent() {
        if (this.silentContainer.getValue() && RekitModule.mc.gui.screen() instanceof AbstractContainerScreen && !(RekitModule.mc.gui.screen() instanceof InventoryScreen)) {
            RekitModule.mc.gui.setScreen(null);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent event) {
        if (!this.auto.getValue() || this.placedShulkerPos == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - this.placedShulkerRenderMs;
        float t = Math.min(1.0f, (float)elapsed / 400.0f);
        float scale = 0.15f + 0.85f * t;
        double cx = (double)this.placedShulkerPos.getX() + 0.5;
        double cy = (double)this.placedShulkerPos.getY() + 0.5;
        double cz = (double)this.placedShulkerPos.getZ() + 0.5;
        double half = 0.5 * (double)scale;
        AABB box = new AABB(cx - half, cy - half, cz - half, cx + half, cy + half, cz + half);
        Renderer3D.renderBoxOutline(event.getMatrices(), box, new Color(255, 220, 0, 230));
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = RekitModule.mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerScreen screen2 = (AbstractContainerScreen)screen;
        Component title = screen2.getTitle();
        if (title == null) {
            return false;
        }
        return !(title.getContents() instanceof TranslatableContents);
    }

    private void manualPullTick() {
        if (this.activeKit.isEmpty()) {
            return;
        }
        if (this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(RekitModule.mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        for (int executed = 0; executed < this.actionsPerTick.getValue().intValue() && this.pullFromContainerTick(); ++executed) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleAutoTick() {
        this.hideContainerScreenIfSilent();
        boolean screenOpen = RekitModule.mc.player.hasContainerOpen();
        if (this.autoState == AutoState.IDLE) {
            this.autoPlaceActive = false;
            if (screenOpen) {
                if (RekitModule.mc.player.containerMenu instanceof ShulkerBoxMenu) {
                    this.manualPullTick();
                    return;
                }
                this.detectEnderChestPos();
                if (this.enderChestPos == null) {
                    this.manualPullTick();
                    return;
                }
                if (this.activeKit.isEmpty()) {
                    this.setToggled(false);
                    return;
                }
                this.autoState = AutoState.FIND_SHULKER;
                this.autoTicks = 0;
                this.emptyTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.FIND_SHULKER) {
            if (!screenOpen) {
                return;
            }
            if (this.isKitComplete()) {
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                return;
            }
            int containerSize = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSize <= 0) {
                return;
            }
            this.shulkerEnderSlot = this.findShulkerInContainer(containerSize);
            if (this.shulkerEnderSlot == -1) {
                this.error("Auto stopped: no shulker has the items this kit still needs.");
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                this.setToggled(false);
                return;
            }
            this.autoState = AutoState.GRAB_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == AutoState.GRAB_SHULKER) {
            if (!screenOpen) {
                return;
            }
            int containerSize = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSize <= 0) {
                return;
            }
            AbstractContainerMenu handler = RekitModule.mc.player.containerMenu;
            ItemStack slotStack = handler.getSlot(this.shulkerEnderSlot).getItem();
            if (!this.isShulkerBox(slotStack)) {
                this.autoState = AutoState.FIND_SHULKER;
                return;
            }
            int emptyHotbar = this.firstEmptyHotbarSlot();
            if (emptyHotbar != -1) {
                RekitModule.mc.gameMode.handleContainerInput(handler.containerId, this.shulkerEnderSlot, emptyHotbar, ContainerInput.SWAP, (Player)RekitModule.mc.player);
            } else {
                int hotbarSlot = RekitModule.mc.player.getInventory().getSelectedSlot() == 0 ? 1 : 0;
                RekitModule.mc.gameMode.handleContainerInput(handler.containerId, this.shulkerEnderSlot, hotbarSlot, ContainerInput.SWAP, (Player)RekitModule.mc.player);
                this.displacedHotbarSlot = hotbarSlot;
                this.displacedEnderSlot = this.shulkerEnderSlot;
            }
            this.autoState = AutoState.PLACE_SHULKER;
            this.autoTicks = 0;
            this.hotbarSwapSettled = false;
            this.hotbarSwapAttempts = 0;
            return;
        }
        if (this.autoState == AutoState.PLACE_SHULKER) {
            boolean placed;
            if (screenOpen) {
                RekitModule.mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 1) {
                ++this.autoTicks;
                return;
            }
            int shulkerInvSlot = this.findShulkerInInventory();
            if (shulkerInvSlot == -1) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (shulkerInvSlot > 8 && !this.hotbarSwapSettled) {
                this.ensureHotbar(shulkerInvSlot);
                this.hotbarSwapSettled = true;
                return;
            }
            if (shulkerInvSlot > 8) {
                ++this.hotbarSwapAttempts;
                if (this.hotbarSwapAttempts > 3) {
                    ++this.placeFailStreak;
                    this.autoState = AutoState.RETURN_SHULKER;
                    return;
                }
                this.hotbarSwapSettled = false;
                return;
            }
            BlockPos placePos = this.findPlaceableSpot();
            if (placePos == null) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            BlockHitResult hit = this.getHitResultForPlace(placePos);
            if (hit == null) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            shulkerInvSlot = this.findShulkerInInventory();
            if (shulkerInvSlot < 0 || shulkerInvSlot > 8) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                    float[] rotations = RotationUtils.getRotations(hit.getLocation());
                    Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
                }
                this.swapToSlotNormal(shulkerInvSlot);
                placed = this.isPositionClear(placePos) && RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, hit) != InteractionResult.FAIL && !RekitModule.mc.level.getBlockState(placePos).canBeReplaced();
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
            if (!placed) {
                this.swapBackNormal();
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            this.placedShulkerPos = placePos;
            this.placedShulkerRenderMs = System.currentTimeMillis();
            this.autoState = AutoState.OPEN_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == AutoState.OPEN_SHULKER) {
            if (screenOpen) {
                this.swapBackNormal();
                this.autoState = AutoState.PULL_ITEMS;
                this.autoTicks = 0;
                this.emptyTicks = 0;
                this.placeFailStreak = 0;
                return;
            }
            if (this.autoTicks == 0 || this.autoTicks % 4 == 0) {
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf((Vec3i)this.placedShulkerPos), Direction.UP, this.placedShulkerPos, false);
                RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, hit);
            }
            ++this.autoTicks;
            if (this.autoTicks > 60) {
                this.swapBackNormal();
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.PULL_ITEMS) {
            if (!screenOpen) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.activeKit.isEmpty()) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            this.ticks = 0;
            boolean didWork = false;
            int containerSize = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSize > 0) {
                for (int executed = 0; executed < this.actionsPerTick.getValue().intValue() && this.pullFromContainerTick(); ++executed) {
                    didWork = true;
                }
            }
            this.emptyTicks = didWork ? 0 : ++this.emptyTicks;
            if (this.emptyTicks >= 5) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.BREAK_SHULKER) {
            if (screenOpen) {
                RekitModule.mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 2) {
                ++this.autoTicks;
                return;
            }
            if (this.placedShulkerPos == null || RekitModule.mc.level == null) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (RekitModule.mc.level.getBlockState(this.placedShulkerPos).canBeReplaced()) {
                if (this.findShulkerInInventory() == -1 && this.autoTicks < 100) {
                    ++this.autoTicks;
                    return;
                }
                this.autoState = AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.autoTicks > 200) {
                this.autoState = AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            this.instantBreak(this.placedShulkerPos);
            ++this.autoTicks;
            return;
        }
        if (this.autoState == AutoState.RETURN_SHULKER) {
            if (this.enderChestPos == null) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (!screenOpen) {
                if (this.autoTicks < 2) {
                    ++this.autoTicks;
                    return;
                }
                if (this.autoTicks == 2) {
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf((Vec3i)this.enderChestPos), Direction.UP, this.enderChestPos, false);
                    RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, hit);
                }
                ++this.autoTicks;
                if (this.autoTicks > 40) {
                    this.autoState = AutoState.IDLE;
                }
                return;
            }
            this.autoTicks = 0;
            int shulkerInvSlot = this.findShulkerInInventory();
            if (shulkerInvSlot == -1) {
                if (this.placedShulkerPos != null && RekitModule.mc.level != null && !RekitModule.mc.level.getBlockState(this.placedShulkerPos).canBeReplaced()) {
                    this.autoState = AutoState.BREAK_SHULKER;
                    this.autoTicks = 0;
                    return;
                }
                this.autoState = AutoState.FIND_SHULKER;
                this.autoTicks = 0;
                return;
            }
            int containerSize = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSize <= 0) {
                return;
            }
            AbstractContainerMenu handler = RekitModule.mc.player.containerMenu;
            if (this.displacedHotbarSlot != -1) {
                int shulkerHandlerSlot = this.getPlayerHandlerSlot(containerSize, shulkerInvSlot);
                this.click(handler.containerId, shulkerHandlerSlot, 0, ContainerInput.PICKUP);
                this.click(handler.containerId, this.displacedEnderSlot, 0, ContainerInput.PICKUP);
                int origHandlerSlot = this.getPlayerHandlerSlot(containerSize, this.displacedHotbarSlot);
                this.click(handler.containerId, origHandlerSlot, 0, ContainerInput.PICKUP);
                this.displacedHotbarSlot = -1;
                this.displacedEnderSlot = -1;
            } else {
                int emptyEnderSlot = -1;
                for (int i = 0; i < containerSize; ++i) {
                    if (!handler.getSlot(i).getItem().isEmpty()) continue;
                    emptyEnderSlot = i;
                    break;
                }
                if (emptyEnderSlot == -1) {
                    this.autoState = AutoState.IDLE;
                    return;
                }
                int playerHandlerSlot = this.getPlayerHandlerSlot(containerSize, shulkerInvSlot);
                this.atomicSwap(handler.containerId, playerHandlerSlot, emptyEnderSlot);
            }
            this.placedShulkerPos = null;
            this.shulkerEnderSlot = -1;
            if (this.placeFailStreak >= 3) {
                this.error("Auto stopped: could not place/open a shulker after " + this.placeFailStreak + " attempts.");
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                this.setToggled(false);
                this.placeFailStreak = 0;
            } else if (this.isKitComplete()) {
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
            } else {
                this.autoState = AutoState.FIND_SHULKER;
            }
            this.autoTicks = 0;
        }
    }

    private void detectEnderChestPos() {
        BlockHitResult bhr;
        HitResult hitResult = RekitModule.mc.hitResult;
        if (hitResult instanceof BlockHitResult && RekitModule.mc.level.getBlockState((bhr = (BlockHitResult)hitResult).getBlockPos()).is(Blocks.ENDER_CHEST)) {
            this.enderChestPos = bhr.getBlockPos();
            return;
        }
        BlockPos playerPos = RekitModule.mc.player.blockPosition();
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dy = -1; dy <= 3; ++dy) {
                for (int dz = -3; dz <= 3; ++dz) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (!RekitModule.mc.level.getBlockState(pos).is(Blocks.ENDER_CHEST)) continue;
                    this.enderChestPos = pos;
                    return;
                }
            }
        }
    }

   private int findShulkerInContainer(int containerSize) {
      AbstractContainerMenu handler = mc.player.containerMenu;
      Set<Item> kitItems = new HashSet<>();

      for (Entry<Integer, RekitModule.KitItem> entry : this.activeKit.entrySet()) {
         ItemStack playerStack = mc.player.getInventory().getItem(entry.getKey());
         if (entry.getValue() != null && entry.getValue().id != null && !entry.getValue().id.isEmpty() && !this.isCorrectItem(playerStack, entry.getValue())) {
            Identifier identifier = Identifier.tryParse(entry.getValue().id);
            if (identifier != null) {
               Item item = BuiltInRegistries.ITEM.getValue(identifier);
               if (item != null) {
                  kitItems.add(item);
               }
            }
         }
      }

      int bestSlot = -1;
      int bestScore = -1;

      for (int i = 0; i < containerSize; i++) {
         ItemStack stack = handler.getSlot(i).getItem();
         if (this.isShulkerBox(stack)) {
            int score = 0;
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
            if (contents != null) {
               for (ItemStack inner : (Iterable<ItemStack>)contents.nonEmptyItemCopyStream()::iterator) {
                  if (kitItems.contains(inner.getItem())) {
                     score += inner.getMaxStackSize() == 1 ? 100 : 1;
                  }
               }
            }

            if (this.considerShulkerName.getValue()
               && !this.activeKitName.isEmpty()
               && stack.has(DataComponents.CUSTOM_NAME)
               && normalizeName(stack.getHoverName().getString()).equalsIgnoreCase(normalizeName(this.activeKitName))) {
               score += 1000000;
            }

            if (score > bestScore) {
               bestScore = score;
               bestSlot = i;
            }
         }
      }

      return bestScore <= 0 ? -1 : bestSlot;
   }

    private int ensureHotbar(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return invSlot;
        }
        int hotbar = RekitModule.mc.player.getInventory().getSelectedSlot();
        RekitModule.mc.gameMode.handleContainerInput(RekitModule.mc.player.inventoryMenu.containerId, this.getHandlerSlotPlayerOnly(invSlot), hotbar, ContainerInput.SWAP, (Player)RekitModule.mc.player);
        return hotbar;
    }

    private int findShulkerInInventory() {
        for (int i = 0; i < 36; ++i) {
            if (!this.isShulkerBox(RekitModule.mc.player.getInventory().getItem(i))) continue;
            return i;
        }
        return -1;
    }

    private int firstEmptyHotbarSlot() {
        for (int i = 0; i <= 8; ++i) {
            if (!RekitModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private Player getActiveCombatOpponent() {
        LivingEntity last = RekitModule.mc.player.getLastHurtByMob();
        if (!(last instanceof Player)) {
            return null;
        }
        Player opponent = (Player)last;
        long ticksSinceHit = RekitModule.mc.level.getGameTime() - (long)RekitModule.mc.player.getLastHurtByMobTimestamp();
        return ticksSinceHit <= 100L ? opponent : null;
    }

    private boolean isPositionClear(BlockPos pos) {
        return RekitModule.mc.level.getEntities((Entity)null, new AABB(pos), Entity::isAlive).isEmpty();
    }

    private BlockPos findPlaceableSpot() {
        BlockPos base = RekitModule.mc.player.blockPosition();
        Player opponent = this.getActiveCombatOpponent();
        int r = (int)Math.ceil(RekitModule.mc.player.blockInteractionRange());
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                double score;
                BlockPos candidate = base.offset(dx, 0, dz);
                if (candidate.distSqr((Vec3i)base) > (double)r * (double)r || candidate.below().equals(this.enderChestPos) || !this.isPositionClear(candidate) || !RekitModule.mc.level.getBlockState(candidate).canBeReplaced() || RekitModule.mc.level.getBlockState(candidate.below()).canBeReplaced() || RekitModule.mc.level.getBlockState(candidate.below()).is(Blocks.AIR) || !RekitModule.mc.level.getBlockState(candidate.above()).canBeReplaced()) continue;
                double d = score = opponent != null ? -candidate.distSqr((Vec3i)opponent.blockPosition()) : (double)(dx * dx + dz * dz);
                if (!(score < bestScore)) continue;
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private BlockHitResult getHitResultForPlace(BlockPos placePos) {
        Direction direction = WorldUtils.getDirection(placePos, null, this.strictDirection.getValue());
        if (direction == null) {
            return null;
        }
        BlockPos support = placePos.relative(direction);
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)support).add((double)(-direction.getStepX()) * 0.48, (double)(-direction.getStepY()) * 0.48, (double)(-direction.getStepZ()) * 0.48);
        return new BlockHitResult(hitVec, direction.getOpposite(), support, false);
    }

    private boolean isKitComplete() {
        if (this.activeKit.isEmpty()) {
            return true;
        }
        for (Map.Entry<Integer, KitItem> entry : this.activeKit.entrySet()) {
            if (this.isCorrectItem(RekitModule.mc.player.getInventory().getItem(entry.getKey().intValue()), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    private boolean pullFromContainerTick() {
        ItemStack playerStack;
        int playerSlot;
        KitItem kit;
        int i;
        AbstractContainerMenu handler = RekitModule.mc.player.containerMenu;
        int containerSize = handler.slots.size() - 36;
        if (containerSize <= 0) {
            return false;
        }
        if (!handler.getCarried().isEmpty()) {
            int i2;
            ++this.cursorWaitTicks;
            if (this.cursorWaitTicks < 20) {
                return false;
            }
            this.cursorWaitTicks = 0;
            int clearSlot = -1;
            for (i2 = 0; i2 < containerSize; ++i2) {
                if (!handler.getSlot(i2).getItem().isEmpty()) continue;
                clearSlot = i2;
                break;
            }
            if (clearSlot == -1) {
                for (i2 = 0; i2 < 36; ++i2) {
                    int slot = this.getPlayerHandlerSlot(containerSize, i2);
                    if (!handler.getSlot(slot).getItem().isEmpty()) continue;
                    clearSlot = slot;
                    break;
                }
            }
            if (clearSlot != -1) {
                this.click(handler.containerId, clearSlot, 0, ContainerInput.PICKUP);
                return true;
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        for (i = 0; i < 36; ++i) {
            ItemStack containerStack;
            kit = this.activeKit.get(i);
            if (kit == null) continue;
            playerSlot = this.getPlayerHandlerSlot(containerSize, i);
            playerStack = handler.getSlot(playerSlot).getItem();
            boolean correctNow = this.isCorrectItem(playerStack, kit);
            if (!correctNow) {
                int containerSlot;
                if (this.isShulkerBox(playerStack) || (containerSlot = this.findBestItemInContainer(handler, containerSize, kit)) == -1) continue;
                this.atomicSwap(handler.containerId, containerSlot, playerSlot);
                return true;
            }
            if (playerStack.getCount() >= playerStack.getMaxStackSize()) continue;
            int exactSlot = this.findExactItemInContainer(handler, containerSize, playerStack);
            if (exactSlot != -1) {
                this.atomicSwap(handler.containerId, exactSlot, playerSlot);
                return true;
            }
            int bestSlot = this.findBestItemInContainer(handler, containerSize, kit);
            if (bestSlot == -1 || (containerStack = handler.getSlot(bestSlot).getItem()).getCount() <= playerStack.getCount()) continue;
            this.atomicSwap(handler.containerId, bestSlot, playerSlot);
            return true;
        }
        for (i = 0; i < 36; ++i) {
            kit = this.activeKit.get(i);
            if (kit == null || !this.isShulkerBox(playerStack = handler.getSlot(playerSlot = this.getPlayerHandlerSlot(containerSize, i)).getItem()) || this.isItemCompensated(handler, containerSize, kit)) continue;
            int containerSlot = this.findBestItemInContainer(handler, containerSize, kit);
            int emptySlot = this.findEmptyUnassignedSlot(handler, containerSize);
            if (containerSlot == -1 || emptySlot == -1) continue;
            this.atomicSwap(handler.containerId, containerSlot, emptySlot);
            return true;
        }
        return false;
    }

    private void atomicSwap(int syncId, int containerSlot, int playerSlot) {
        this.click(syncId, containerSlot, 0, ContainerInput.PICKUP);
        this.click(syncId, playerSlot, 0, ContainerInput.PICKUP);
        this.click(syncId, containerSlot, 0, ContainerInput.PICKUP);
    }

    private void click(int syncId, int slotId, int button, ContainerInput type) {
        lastContainerActionMs = System.currentTimeMillis();
        RekitModule.mc.gameMode.handleContainerInput(syncId, slotId, button, type, (Player)RekitModule.mc.player);
    }

    private void swapToSlotNormal(int slot) {
        this.savedSlot = RekitModule.mc.player.getInventory().getSelectedSlot();
        RekitModule.mc.player.getInventory().setSelectedSlot(slot);
        ((ClientPlayerInteractionManagerAccessor)RekitModule.mc.gameMode).invokeSyncSelectedSlot();
    }

    private void swapBackNormal() {
        if (this.savedSlot == -1) {
            return;
        }
        RekitModule.mc.player.getInventory().setSelectedSlot(this.savedSlot);
        ((ClientPlayerInteractionManagerAccessor)RekitModule.mc.gameMode).invokeSyncSelectedSlot();
        this.savedSlot = -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void instantBreak(BlockPos pos) {
        Direction direction = Direction.UP;
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean farReach = speedMine != null && speedMine.farReach.getValue();
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(pos, direction));
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            }
            mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction));
            if (farReach) {
                BlockPos decoyPos = pos.below(2000);
                mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, decoyPos, WorldUtils.getClosestDirection(decoyPos, true)));
            }
            mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
    }

    private boolean isCorrectItem(ItemStack stack, KitItem kit) {
        if (kit == null) {
            return true;
        }
        if (stack.isEmpty()) {
            return false;
        }
        if (kit.id == null || kit.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kit.id);
        if (identifier == null) {
            return false;
        }
        Item expected = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        return expected != null && stack.getItem() == expected;
    }

    private int findBestItemInContainer(AbstractContainerMenu handler, int containerSize, KitItem kit) {
        if (kit == null || kit.id == null || kit.id.isEmpty()) {
            return -1;
        }
        Identifier identifier = Identifier.tryParse((String)kit.id);
        if (identifier == null) {
            return -1;
        }
        Item expected = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (expected == null) {
            return -1;
        }
        int bestSlot = -1;
        long bestScore = -1L;
        for (int i = 0; i < containerSize; ++i) {
            long score;
            ItemStack stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || stack.getItem() != expected || (score = this.scoreCandidate(stack)) <= bestScore) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private long scoreCandidate(ItemStack stack) {
        if (stack.getMaxStackSize() > 1) {
            return stack.getCount();
        }
        long score = 0L;
        String prefEnchant = this.preferredEnchantId(stack);
        if (prefEnchant != null && this.hasEnchant(stack, prefEnchant)) {
            score += 1000000L;
        }
        if (stack.isDamageableItem()) {
            score += (long)(stack.getMaxDamage() - stack.getDamageValue());
        }
        return score;
    }

    private String preferredEnchantId(ItemStack stack) {
        String pref;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.endsWith("_pickaxe")) {
            return this.pickaxePref.getValue().equalsIgnoreCase("SilkTouch") ? "silk_touch" : "efficiency";
        }
        if (id.endsWith("_helmet") || id.equals("turtle_helmet")) {
            pref = this.helmetPref.getValue();
        } else if (id.endsWith("_chestplate")) {
            pref = this.chestplatePref.getValue();
        } else if (id.endsWith("_leggings")) {
            pref = this.leggingsPref.getValue();
        } else if (id.endsWith("_boots")) {
            pref = this.bootsPref.getValue();
        } else {
            return null;
        }
        return pref.equalsIgnoreCase("Blast") ? "blast_protection" : "protection";
    }

    private boolean hasEnchant(ItemStack stack, String enchantPath) {
        ItemEnchantments enchants = (ItemEnchantments)stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }
        Identifier id = Identifier.tryParse((String)("minecraft:" + enchantPath));
        if (id == null) {
            return false;
        }
        for (Holder holder : enchants.keySet()) {
            if (!holder.is(id)) continue;
            return true;
        }
        return false;
    }

    private int findExactItemInContainer(AbstractContainerMenu handler, int containerSize, ItemStack targetStack) {
        for (int i = 0; i < containerSize; ++i) {
            ItemStack stack = handler.getSlot(i).getItem();
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)targetStack)) continue;
            return i;
        }
        return -1;
    }

    private int getPlayerHandlerSlot(int containerSize, int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return containerSize + 27 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return containerSize + (invSlot - 9);
        }
        return -1;
    }

    private int getHandlerSlotPlayerOnly(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return invSlot;
        }
        return -1;
    }

    private static String normalizeName(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFKC).trim();
    }

    private boolean isShulkerBox(ItemStack stack) {
        BlockItem blockItem;
        Item item;
        return !stack.isEmpty() && (item = stack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
    }

    private boolean isItemCompensated(AbstractContainerMenu handler, int containerSize, KitItem kit) {
        if (kit == null || kit.id == null || kit.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kit.id);
        if (identifier == null) {
            return false;
        }
        Item expected = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (expected == null) {
            return false;
        }
        for (int i = 0; i < 36; ++i) {
            int slot;
            if (this.activeKit.get(i) != null || handler.getSlot(slot = this.getPlayerHandlerSlot(containerSize, i)).getItem().getItem() != expected) continue;
            return true;
        }
        return false;
    }

    private int findEmptyUnassignedSlot(AbstractContainerMenu handler, int containerSize) {
        for (int i = 0; i < 36; ++i) {
            int slot;
            if (this.activeKit.get(i) != null || !handler.getSlot(slot = this.getPlayerHandlerSlot(containerSize, i)).getItem().isEmpty()) continue;
            return slot;
        }
        return -1;
    }

    @Generated
    public Map<Integer, KitItem> getActiveKit() {
        return this.activeKit;
    }

    @Generated
    public String getActiveKitName() {
        return this.activeKitName;
    }

    private static enum AutoState {
        IDLE,
        FIND_SHULKER,
        GRAB_SHULKER,
        PLACE_SHULKER,
        OPEN_SHULKER,
        PULL_ITEMS,
        BREAK_SHULKER,
        RETURN_SHULKER;

    }

    public static class KitItem {
        public String id;
        public String name;
        public int maxCount;
    }

    public static enum ArmorPref {
        Blast,
        Prot;

    }

    public static enum PickaxePref {
        Efficiency,
        SilkTouch;

    }
}

