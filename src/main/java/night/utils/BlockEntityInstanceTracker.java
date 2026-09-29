package night.utils;

import java.util.ArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Passive counterpart of Alek's global BlockFinder registration. */
public final class BlockEntityInstanceTracker {
    private static BlockEntityInstanceTracker instance;
    private final ArrayList<BlockEntity> instances = new ArrayList<>();

    private BlockEntityInstanceTracker() {
    }

    public static void initialize() {
        instance = new BlockEntityInstanceTracker();
    }

    public static void constructed(BlockEntity entity) {
        if (instance != null) {
            instance.instances.add(entity);
        }
    }

    public static void removed(BlockEntity entity) {
        if (instance != null) {
            instance.instances.remove(entity);
        }
    }

    public static void localPlayerConstructed() {
        if (instance != null) {
            instance.instances.clear();
        }
    }

    public static ArrayList<BlockEntity> snapshot() {
        return new ArrayList<>(instance.instances);
    }
}
