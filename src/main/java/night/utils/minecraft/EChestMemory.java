/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 */
package night.utils.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;

public final class EChestMemory {
    private static final EChestMemory INSTANCE = new EChestMemory();
    private static boolean registered = false;
    private static NonNullList<ItemStack> items = null;
    private int trackedContainerId = -1;

    private EChestMemory() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        Night.EVENT_HANDLER.subscribe(INSTANCE);
    }

    public static boolean hasItems() {
        return items != null;
    }

    public static NonNullList<ItemStack> getItems() {
        return items;
    }

    @SubscribeEvent
   public void onPacket(PacketReceiveEvent event) {
      if (event.getPacket() instanceof ClientboundOpenScreenPacket open) {
         if (this.isLookingAtEnderChest()) {
            this.trackedContainerId = open.getContainerId();
            items = NonNullList.withSize(27, ItemStack.EMPTY);
         } else {
            this.trackedContainerId = -1;
         }
      } else if (this.trackedContainerId != -1) {
         if (!(event.getPacket() instanceof ClientboundContainerSetContentPacket content)) {
            if (event.getPacket() instanceof ClientboundContainerSetSlotPacket slot) {
               if (slot.getContainerId() != this.trackedContainerId) {
                  return;
               }

               int index = slot.getSlot();
               if (items != null && index >= 0 && index < 27) {
                  items.set(index, slot.getItem().copy());
               }
            }
         } else if (content.containerId() == this.trackedContainerId) {
            NonNullList<ItemStack> buffer = NonNullList.withSize(27, ItemStack.EMPTY);

            for (int i = 0; i < 27 && i < content.items().size(); i++) {
               buffer.set(i, content.items().get(i).copy());
            }

            items = buffer;
         }
      }
   }

    private boolean isLookingAtEnderChest() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult)) {
            return false;
        }
        BlockHitResult blockHit = (BlockHitResult)hit;
        BlockPos pos = blockHit.getBlockPos();
        return mc.level.getBlockState(pos).is(Blocks.ENDER_CHEST);
    }
}

