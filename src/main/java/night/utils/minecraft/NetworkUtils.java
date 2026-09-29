/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerGamePacketListener
 */
package night.utils.minecraft;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import night.mixins.accessors.ClientWorldAccessor;
import night.utils.IMinecraft;

public class NetworkUtils
implements IMinecraft {
    private static Object vfpImpl;
    private static Method vfpTargetVersion;
    private static Method protocolVersionGetVersion;

    public static void sendIgnoredPacket(Packet<?> packet) {
        mc.getConnection().getConnection().send(packet, null, true);
    }

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        NetworkUtils.sendSequencedPacket(packetCreator, packet -> mc.getConnection().send(packet));
    }

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator, Consumer<Packet<ServerGamePacketListener>> sender) {
        try (BlockStatePredictionHandler prediction = ((ClientWorldAccessor)NetworkUtils.mc.level).invokeGetPendingUpdateManager().startPredicting();){
            Packet<ServerGamePacketListener> packet = packetCreator.predict(prediction.currentSequence());
            sender.accept(packet);
        }
    }

    public static boolean isLegacyProtocol() {
        try {
            Object targetVersion;
            if (vfpImpl == null) {
                vfpImpl = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus").getMethod("getImpl", new Class[0]).invoke(null, new Object[0]);
                vfpTargetVersion = Class.forName("com.viaversion.viafabricplus.api.ViaFabricPlusBase").getMethod("getTargetVersion", new Class[0]);
                protocolVersionGetVersion = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion").getMethod("getVersion", new Class[0]);
            }
            return (targetVersion = vfpTargetVersion.invoke(vfpImpl, new Object[0])) != null && (Integer)protocolVersionGetVersion.invoke(targetVersion, new Object[0]) < 768;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static interface SequencedPacketCreator {
        public Packet<ServerGamePacketListener> predict(int var1);
    }
}

