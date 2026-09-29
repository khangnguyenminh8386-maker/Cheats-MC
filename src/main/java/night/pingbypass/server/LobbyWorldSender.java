/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundGameEventPacket
 *  net.minecraft.network.protocol.game.ClientboundLoginPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
 *  net.minecraft.network.protocol.game.CommonPlayerSpawnInfo
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.PositionMoveRotation
 *  net.minecraft.world.entity.player.Abilities
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.dimension.BuiltinDimensionTypes
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobbyWorldSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyWorldSender.class);

    private LobbyWorldSender() {
    }

    public static void sendLobbyWorld(Connection toClient, RegistryAccess registryManager) {
        LOGGER.info("Sending lobby world to client...");
        try {
            Registry dimRegistry = registryManager.lookupOrThrow(Registries.DIMENSION_TYPE);
            Holder.Reference overworldType = dimRegistry.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            HashSet dimensionIds = Sets.newHashSet(new ResourceKey[]{Level.OVERWORLD, Level.NETHER, Level.END});
            CommonPlayerSpawnInfo spawnInfo = new CommonPlayerSpawnInfo((Holder)overworldType, Level.OVERWORLD, 0L, GameType.SPECTATOR, null, false, true, Optional.empty(), 0, 63);
            LobbyWorldSender.send(toClient, new ClientboundLoginPacket(1337, false, (Set)dimensionIds, 1, 16, 16, false, true, false, spawnInfo, false, false));
            LobbyWorldSender.send(toClient, new ClientboundPlayerAbilitiesPacket(LobbyWorldSender.createLobbyAbilities()));
            LobbyWorldSender.send(toClient, new ClientboundSetHeldSlotPacket(0));
            LobbyWorldSender.send(toClient, new ClientboundPlayerPositionPacket(1, new PositionMoveRotation(new Vec3(0.0, 240.0, 0.0), Vec3.ZERO, 0.0f, 0.0f), Set.of()));
            LobbyWorldSender.send(toClient, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0f));
            LOGGER.info("Lobby world sent successfully.");
        }
        catch (Exception e) {
            LOGGER.error("Failed to send lobby world", (Throwable)e);
        }
    }

    private static void send(Connection connection, Packet<?> packet) {
        if (connection.isConnected()) {
            connection.send(packet);
        }
    }

    private static Abilities createLobbyAbilities() {
        Abilities abilities = new Abilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.invulnerable = true;
        return abilities;
    }
}

