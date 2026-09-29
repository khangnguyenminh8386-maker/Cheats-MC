/*
 * Decompiled with CFR 0.152.
 */
package night.pingbypass;

import night.Night;

public class PingBypassFlags {
    public static volatile boolean tolerateRegistryErrors = false;
    public static volatile boolean suppressEncoderErrors = false;
    public static volatile boolean suppressAllDisconnects = false;
    public static volatile boolean proxyForwardingActive = false;
    public static volatile boolean clientOnGround = true;
    public static volatile boolean clientHorizontalCollision = false;

    public static boolean isPingBypassActive() {
        return proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer();
    }
}

