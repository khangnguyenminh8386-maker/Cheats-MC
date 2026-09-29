/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.state.BlockState
 */
package night.utils.minecraft;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import night.Night;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.S2CSlotSyncPacket;
import night.pingbypass.protocol.packets.S2CWindowClickPacket;
import night.utils.IMinecraft;

public class InventoryUtils
implements IMinecraft {
    public static String[] SWITCH_MODES = new String[]{"None", "Normal", "Silent", "AltPickup", "AltSwap"};
    public static String[] SWAP_MODES = new String[]{"Pickup", "Swap"};
    public static int HOTBAR_START = 0;
    public static int HOTBAR_END = 8;
    public static int INVENTORY_START = 9;
    public static int INVENTORY_END = 35;
    private static final ArrayDeque<Frame> STACK = new ArrayDeque();
    private static final Set<Integer> LONG_HOLD = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Boolean> PROGRAMMATIC_SWITCH = ThreadLocal.withInitial(() -> false);
    private static final long STALE_FRAME_MS = 30000L;

    public static void markLongHold(int slot) {
        LONG_HOLD.add(slot);
    }

    public static void clearLongHold(int slot) {
        LONG_HOLD.remove(slot);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hasActiveSilentSwitch() {
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            for (Frame f : STACK) {
                if (LONG_HOLD.contains(f.key())) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isProgrammaticSwitching() {
        return PROGRAMMATIC_SWITCH.get();
    }

    private static int restingSlot() {
        return InventoryUtils.mc.player == null ? Night.POSITION_MANAGER.getServerSlot() : InventoryUtils.mc.player.getInventory().getSelectedSlot();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tickPendingRestore() {
        boolean using;
        if (InventoryUtils.mc.player == null) {
            ArrayDeque<Frame> arrayDeque = STACK;
            synchronized (arrayDeque) {
                STACK.clear();
            }
            LONG_HOLD.clear();
            return;
        }
        boolean bl = using = InventoryUtils.mc.options.keyUse.isDown() || InventoryUtils.mc.player.isUsingItem();
        if (using || InventoryUtils.mc.options.keyAttack.isDown()) {
            InventoryUtils.flushPendingRestores();
        }
        if (using) {
            InventoryUtils.send(InventoryUtils.mc.player.getInventory().getSelectedSlot());
        }
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            if (STACK.removeIf(f -> !LONG_HOLD.contains(f.key()) && System.currentTimeMillis() - f.time() >= 30000L)) {
                InventoryUtils.settle();
            }
            if (!using) {
                InventoryUtils.settle();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flushPendingRestores() {
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            InventoryUtils.settle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseAllBorrows() {
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            STACK.removeIf(f -> !LONG_HOLD.contains(f.key()));
            InventoryUtils.settle();
        }
    }

    private static void settle() {
        Frame top = STACK.peek();
        InventoryUtils.send(top != null ? top.held() : InventoryUtils.restingSlot());
    }

    private static void send(int target) {
        if (target < 0 || mc.getConnection() == null) {
            return;
        }
        if (target == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        boolean was = PROGRAMMATIC_SWITCH.get();
        PROGRAMMATIC_SWITCH.set(true);
        try {
            mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(target));
        }
        finally {
            PROGRAMMATIC_SWITCH.set(was);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void push(int key, int held) {
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            STACK.push(new Frame(key, held, System.currentTimeMillis()));
            InventoryUtils.send(held);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void release(int key) {
        ArrayDeque<Frame> arrayDeque = STACK;
        synchronized (arrayDeque) {
            boolean found = false;
            Iterator<Frame> it = STACK.iterator();
            while (it.hasNext()) {
                if (it.next().key() != key) continue;
                it.remove();
                found = true;
                break;
            }
            if (!found) {
                return;
            }
            Frame top = STACK.peek();
            if (top != null) {
                InventoryUtils.send(top.held());
                return;
            }
            InventoryUtils.settle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean switchSlot(String mode, int slot, int previousSlot) {
        if (mode.equalsIgnoreCase("None")) {
            return true;
        }
        if (slot == -1 || previousSlot == -1) {
            return false;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            if (!mc.isSameThread() && !mode.equalsIgnoreCase("Silent")) {
                if (slot > HOTBAR_END) {
                    boolean bl = false;
                    return bl;
                }
                mode = "Silent";
            }
            switch (mode) {
                case "Normal": {
                    InventoryUtils.mc.player.getInventory().setSelectedSlot(slot);
                    ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
                    InventoryUtils.syncSlotToClientIfProxy(slot);
                    break;
                }
                case "Silent": {
                    InventoryUtils.push(slot, slot);
                    break;
                }
                case "AltPickup": 
                case "AltSwap": {
                    if (slot <= HOTBAR_END) {
                        InventoryUtils.push(slot, slot);
                        break;
                    }
                    InventoryUtils.swap(mode.equals("AltPickup") ? "Pickup" : "Swap", slot, previousSlot);
                    InventoryUtils.push(slot, previousSlot);
                }
            }
            boolean bl = true;
            return bl;
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void switchBackNormal(int previousSlot) {
        if (previousSlot == -1 || previousSlot == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            InventoryUtils.mc.player.getInventory().setSelectedSlot(previousSlot);
            ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
            InventoryUtils.syncSlotToClientIfProxy(previousSlot);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    private static void syncSlotToClientIfProxy(int slot) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CSlotSyncPacket(slot)));
        for (Connection conn : Night.PROXY_SERVER.getConnections()) {
            if (!conn.isConnected()) continue;
            conn.send((Packet)packet);
        }
    }

    public static void switchBack(String mode, int slot, int previousSlot) {
        if (mode.equalsIgnoreCase("None")) {
            return;
        }
        if (previousSlot == -1) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            if ((mode.equals("AltPickup") || mode.equals("AltSwap")) && slot > HOTBAR_END && mc.isSameThread()) {
                InventoryUtils.swap(mode.equals("AltPickup") ? "Pickup" : "Swap", slot, previousSlot);
            }
            InventoryUtils.release(slot);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void swapEquipment(int invIndex, int equipmentSlot) {
        if (invIndex >= 0 && invIndex <= 8) {
            InventoryUtils.click(equipmentSlot, invIndex, ContainerInput.SWAP);
            return;
        }
        InventoryUtils.click(InventoryUtils.indexToSlot(invIndex), 0, ContainerInput.PICKUP);
        InventoryUtils.click(equipmentSlot, 0, ContainerInput.PICKUP);
        InventoryUtils.click(InventoryUtils.indexToSlot(invIndex), 0, ContainerInput.PICKUP);
    }

    public static void swap(String mode, int slot, int targetSlot) {
        switch (mode) {
            case "Pickup": {
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(targetSlot), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
                break;
            }
            case "Swap": {
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), targetSlot, ContainerInput.SWAP);
            }
        }
    }

    public static void click(int slotNum, int buttonNum, ContainerInput containerInput) {
        InventoryUtils.mc.gameMode.handleContainerInput(InventoryUtils.mc.player.containerMenu.containerId, slotNum, buttonNum, containerInput, (Player)InventoryUtils.mc.player);
        InventoryUtils.broadcastClickToClientIfProxy(slotNum, buttonNum, containerInput);
    }

    private static void broadcastClickToClientIfProxy(int slotNum, int buttonNum, ContainerInput containerInput) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CWindowClickPacket(InventoryUtils.mc.player.containerMenu.containerId, slotNum, buttonNum, containerInput)));
        for (Connection conn : Night.PROXY_SERVER.getConnections()) {
            if (!conn.isConnected()) continue;
            conn.send((Packet)packet);
        }
    }

    public static int indexToSlot(int index) {
        int playerInvBase;
        AbstractContainerMenu menu;
        AbstractContainerMenu abstractContainerMenu = menu = InventoryUtils.mc.player != null ? InventoryUtils.mc.player.containerMenu : null;
        if (menu != null && InventoryUtils.mc.player.inventoryMenu != menu && index >= 0 && index <= 35 && (playerInvBase = menu.slots.size() - 36) >= 0) {
            return index <= 8 ? playerInvBase + 27 + index : playerInvBase + (index - 9);
        }
        if (index >= 0 && index <= 8) {
            return 36 + index;
        }
        return index;
    }

    public static int find(Item item) {
        return InventoryUtils.find(item, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Item item) {
        return InventoryUtils.find(item, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Item item) {
        return InventoryUtils.find(item, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Item item, int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int find(Class<? extends Item> item) {
        return InventoryUtils.find(item, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Class<? extends Item> item) {
        return InventoryUtils.find(item, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Class<? extends Item> item) {
        return InventoryUtils.find(item, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Class<? extends Item> item, int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!stack.getItem().getClass().isAssignableFrom(item)) continue;
            return i;
        }
        return -1;
    }

    public static int findInventory(Item item, int count) {
        for (int i = INVENTORY_END; i >= INVENTORY_START; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (stack.getItem() != item || InventoryUtils.mc.player.getInventory().getItem(i).getCount() < count) continue;
            return i;
        }
        return -1;
    }

    public static int findHardestBlock(int start, int end) {
        float bestHardness = -1.0f;
        int bestSlot = -1;
        for (int i = start; i <= end; ++i) {
            Item item = InventoryUtils.mc.player.getInventory().getItem(i).getItem();
            if (!(item instanceof BlockItem)) continue;
            BlockItem item2 = (BlockItem)item;
            float hardness = item2.getBlock().defaultDestroyTime();
            if (hardness == -1.0f) {
                return i;
            }
            if (!(hardness > bestHardness)) continue;
            bestHardness = hardness;
            bestSlot = i;
        }
        return bestSlot;
    }

    public static int findFastestItem(BlockState blockState, int start, int end) {
        double bestScore = -1.0;
        int bestSlot = -1;
        for (int i = start; i <= end; ++i) {
            double score = InventoryUtils.mc.player.getInventory().getItem(i).getItem().getDestroySpeed(InventoryUtils.mc.player.getInventory().getItem(i), blockState);
            if (!(score > bestScore)) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    public static int findBestSword(int start, int end) {
        int netheriteSlot = -1;
        int diamondSlot = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int stoneSlot = -1;
        int woodenSlot = -1;
        for (int i = end; i >= start; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.NETHERITE_SWORD) {
                netheriteSlot = i;
            }
            if (stack.getItem() == Items.DIAMOND_SWORD) {
                diamondSlot = i;
            }
            if (stack.getItem() == Items.IRON_SWORD) {
                ironSlot = i;
            }
            if (stack.getItem() == Items.GOLDEN_SWORD) {
                goldenSlot = i;
            }
            if (stack.getItem() == Items.STONE_SWORD) {
                stoneSlot = i;
            }
            if (stack.getItem() != Items.WOODEN_SWORD) continue;
            woodenSlot = i;
        }
        if (netheriteSlot != -1) {
            return netheriteSlot;
        }
        if (diamondSlot != -1) {
            return diamondSlot;
        }
        if (ironSlot != -1) {
            return ironSlot;
        }
        if (goldenSlot != -1) {
            return goldenSlot;
        }
        if (stoneSlot != -1) {
            return stoneSlot;
        }
        return woodenSlot;
    }

    public static int findEmptySlot(int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static boolean inInventoryScreen() {
        return InventoryUtils.mc.gui.screen() instanceof InventoryScreen || InventoryUtils.mc.gui.screen() instanceof CreativeModeInventoryScreen || InventoryUtils.mc.gui.screen() instanceof ContainerScreen || InventoryUtils.mc.gui.screen() instanceof ShulkerBoxScreen;
    }

    private record Frame(int key, int held, long time) {
    }
}

