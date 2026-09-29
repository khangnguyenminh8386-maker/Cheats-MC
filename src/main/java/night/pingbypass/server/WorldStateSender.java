/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.Connection
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import night.pingbypass.server.WorldStateReplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldStateSender.class);

    private WorldStateSender() {
    }

    public static void sendWorld(ClientLevel world, LocalPlayer player, Connection toClient) {
        LOGGER.info("Sending world state to reconnecting client...");
        try {
            if (world == null || player == null || toClient == null) {
                LOGGER.warn("Cannot send world state: null parameter(s)");
                return;
            }
            WorldStateReplay.replay(toClient, world.registryAccess());
            LOGGER.info("World state sent successfully.");
        }
        catch (Exception e) {
            LOGGER.error("Error sending world state to client", (Throwable)e);
        }
    }
}

