/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package night.managers;
import java.util.List;


import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.TickEvent;
import night.utils.IMinecraft;
import night.utils.minecraft.InventoryUtils;
import night.utils.system.Timer;

public class WorldManager
implements IMinecraft {
    private static final Vec3i[] SPHERE = new Vec3i[4187707];
    private static final int[] INDICES = new int[101];
    private final Map<UUID, Integer> poppedTotems = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();
    private float timerMultiplier = 1.0f;
    private final Timer placeTimer = new Timer();
    private final Set<BlockPos> reservedPlacements = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Runnable> pendingCrystalRetries = new ConcurrentHashMap<Integer, Runnable>();

    public void reservePlacement(BlockPos pos) {
        this.reservedPlacements.add(pos);
    }

    public boolean isReserved(BlockPos pos) {
        return this.reservedPlacements.contains(pos);
    }

    public Set<BlockPos> getReservedPlacements() {
        return this.reservedPlacements;
    }

    public void onCrystalAttacked(int entityId, Runnable retry) {
        this.pendingCrystalRetries.put(entityId, retry);
    }

   public WorldManager() {
      Night.EVENT_HANDLER.subscribe(this);
      final BlockPos origin = BlockPos.ZERO;
      Set<BlockPos> positions = new TreeSet<>(
         new Comparator<BlockPos>() {
            public int compare(BlockPos o, BlockPos p) {
               if (o.equals(p)) {
                  return 0;
               }

               int result = Double.compare(origin.distSqr(o), origin.distSqr(p));
               if (result == 0) {
                  result = Integer.compare(
                     Math.abs(o.getX()) + Math.abs(o.getY()) + Math.abs(o.getZ()), Math.abs(p.getX()) + Math.abs(p.getY()) + Math.abs(p.getZ())
                  );
               }

               return result == 0 ? 1 : result;
            }
         }
      );

      for (int x = origin.getX() - 100; x <= origin.getX() + 100; x++) {
         for (int z = origin.getZ() - 100; z <= origin.getZ() + 100; z++) {
            for (int y = origin.getY() - 100; y < origin.getY() + 100; y++) {
               double distance = (origin.getX() - x) * (origin.getX() - x)
                  + (origin.getZ() - z) * (origin.getZ() - z)
                  + (origin.getY() - y) * (origin.getY() - y);
               if (distance < Mth.square(100)) {
                  positions.add(new BlockPos(x, y, z));
               }
            }
         }
      }

      int i = 0;
      int currentDistance = 0;

      for (BlockPos position : positions) {
         if (Math.sqrt(origin.distSqr(position)) > currentDistance) {
            INDICES[currentDistance++] = i;
         }

         SPHERE[i++] = position;
      }
   }

    @SubscribeEvent
   public void onTick(TickEvent event) {
      this.reservedPlacements.clear();
      InventoryUtils.tickPendingRestore();
      if (mc.level != null) {
         List<Player> currentPlayers = new ArrayList<>(mc.level.players());

         for (UUID uuid : new ArrayList<>(this.poppedTotems.keySet())) {
            if (currentPlayers.stream().noneMatch(playerx -> playerx.getUUID().equals(uuid))) {
               this.poppedTotems.remove(uuid);
            }
         }

         for (Player player : currentPlayers) {
            if (player.deathTime <= 0 && player.getHealth() > 0.0F) {
               this.deadPlayers.remove(player.getUUID());
            } else if (!this.deadPlayers.contains(player.getUUID())) {
               Night.EVENT_HANDLER.post(new PlayerDeathEvent(player));
               this.deadPlayers.add(player.getUUID());
               this.poppedTotems.remove(player.getUUID());
            }
         }
      }
   }

    @SubscribeEvent
   public void onPacketReceive(PacketReceiveEvent event) {
      if (mc.level != null) {
         if (event.getPacket() instanceof ClientboundSetHeldSlotPacket packet
            && mc.player != null
            && packet.slot() != mc.player.getInventory().getSelectedSlot()) {
            event.setCancelled(true);
         } else {
            if (event.getPacket() instanceof ClientboundEntityEventPacket packet && packet.getEventId() == 35) {
               if (!(packet.getEntity(mc.level) instanceof Player player)) {
                  return;
               }

               int var12 = this.poppedTotems.getOrDefault(player.getUUID(), 0);
               this.poppedTotems.put(player.getUUID(), ++var12);
               Night.EVENT_HANDLER.post(new PlayerPopEvent(player, var12));
            }

            if (!this.pendingCrystalRetries.isEmpty() && event.getPacket() instanceof ClientboundRemoveEntitiesPacket removePacket) {
               for (int id : removePacket.getEntityIds()) {
                  Runnable retry = this.pendingCrystalRetries.remove(id);
                  if (retry != null) {
                     retry.run();
                  }
               }
            }
         }
      }
   }

    public int getRadius(double radius) {
        return INDICES[Mth.clamp((int)((int)Math.ceil(radius)), (int)0, (int)INDICES.length)];
    }

    public Vec3i getOffset(int index) {
        return SPHERE[index];
    }

    @Generated
    public Map<UUID, Integer> getPoppedTotems() {
        return this.poppedTotems;
    }

    @Generated
    public float getTimerMultiplier() {
        return this.timerMultiplier;
    }

    @Generated
    public void setTimerMultiplier(float timerMultiplier) {
        this.timerMultiplier = timerMultiplier;
    }

    @Generated
    public Timer getPlaceTimer() {
        return this.placeTimer;
    }
}

