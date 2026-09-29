/*
 * Decompiled with CFR 0.152.
 */
package night.utils.minecraft;

import java.util.Optional;

public final class BaritoneUtils {
    private BaritoneUtils() {
    }

    public static Object getPrimaryBaritone() {
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
            return provider.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(provider, new Object[0]);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static boolean isAvailable() {
        return BaritoneUtils.getPrimaryBaritone() != null;
    }

    public static boolean isActive() {
        Object baritone = BaritoneUtils.getPrimaryBaritone();
        if (baritone == null) {
            return false;
        }
        try {
            Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(baritone, new Object[0]);
            if (((Boolean)pathingBehavior.getClass().getMethod("isPathing", new Class[0]).invoke(pathingBehavior, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable pathingBehavior) {
            // empty catch block
        }
        try {
            Object customGoalProcess = baritone.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(baritone, new Object[0]);
            if (((Boolean)customGoalProcess.getClass().getMethod("isActive", new Class[0]).invoke(customGoalProcess, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable customGoalProcess) {
            // empty catch block
        }
        try {
            Object elytraProcess = baritone.getClass().getMethod("getElytraProcess", new Class[0]).invoke(baritone, new Object[0]);
            if (elytraProcess != null && ((Boolean)elytraProcess.getClass().getMethod("isActive", new Class[0]).invoke(elytraProcess, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable elytraProcess) {
            // empty catch block
        }
        try {
            Object proc;
            Object pcm = baritone.getClass().getMethod("getPathingControlManager", new Class[0]).invoke(baritone, new Object[0]);
            Optional mostRecent = (Optional)pcm.getClass().getMethod("mostRecentInControl", new Class[0]).invoke(pcm, new Object[0]);
            if (mostRecent.isPresent() && ((Boolean)(proc = mostRecent.get()).getClass().getMethod("isActive", new Class[0]).invoke(proc, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }
}

