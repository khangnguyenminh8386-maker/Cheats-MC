/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.utils.IMinecraft;

public class PositionUtils
implements IMinecraft {
    public static BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos(entity.getBlockX(), Mth.floor((double)(entity.getY() - Math.floor(entity.getY()) > 0.8 ? Math.floor(entity.getY()) + 1.0 : Math.floor(entity.getY()))), entity.getBlockZ());
    }

    public static AABB getRadius(Entity entity, double radius) {
        return new AABB((double)Mth.floor((double)(entity.getX() - radius)), (double)Mth.floor((double)(entity.getY() - radius)), (double)Mth.floor((double)(entity.getZ() - radius)), (double)Mth.floor((double)(entity.getX() + radius)), (double)Mth.floor((double)(entity.getY() + radius)), (double)Mth.floor((double)(entity.getZ() + radius)));
    }

    public static AABB extrapolate(Player entity, int ticks) {
        return PositionUtils.extrapolate(entity, ticks, true);
    }

    public static AABB extrapolate(Player entity, int ticks, boolean withY) {
        if (entity == null) {
            return null;
        }
        if (ticks <= 0 || PositionUtils.mc.level == null) {
            return entity.getBoundingBox();
        }
        double deltaX = entity.getX() - entity.xo;
        double deltaY = withY ? entity.getY() - entity.yo : 0.0;
        double deltaZ = entity.getZ() - entity.zo;
        double motionX = 0.0;
        double motionY = 0.0;
        double motionZ = 0.0;
        for (double i = 0.5; i <= (double)ticks; i += 0.5) {
            Vec3 fullOffset = new Vec3(deltaX * i, deltaY * i, deltaZ * i);
            if (PositionUtils.mc.level.noCollision((Entity)entity, entity.getBoundingBox().move(fullOffset))) {
                motionX = deltaX * i;
                motionY = deltaY * i;
                motionZ = deltaZ * i;
                continue;
            }
            if (!withY) break;
            Vec3 horizontalOffset = new Vec3(deltaX * i, motionY, deltaZ * i);
            if (!PositionUtils.mc.level.noCollision((Entity)entity, entity.getBoundingBox().move(horizontalOffset))) break;
            motionX = deltaX * i;
            motionZ = deltaZ * i;
        }
        return entity.getBoundingBox().move(new Vec3(motionX, motionY, motionZ));
    }
}

