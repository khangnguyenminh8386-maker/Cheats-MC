/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class GrimUtils {
    private GrimUtils() {
    }

    public static double[] getPossibleEyeHeights(Player player) {
        double[] dArray;
        float scale = (float)player.getAttributeValue(Attributes.SCALE);
        double standing = 1.62 * (double)scale;
        double sneaking = 1.27 * (double)scale;
        double swimming = 0.4 * (double)scale;
        Pose pose = player.getPose();
        switch (pose) {
            case FALL_FLYING: 
            case SPIN_ATTACK: 
            case SWIMMING: {
                double[] dArray2 = new double[3];
                dArray2[0] = swimming;
                dArray2[1] = standing;
                dArray = dArray2;
                dArray2[2] = sneaking;
                break;
            }
            case CROUCHING: {
                double[] dArray3 = new double[3];
                dArray3[0] = sneaking;
                dArray3[1] = standing;
                dArray = dArray3;
                dArray3[2] = swimming;
                break;
            }
            default: {
                double[] dArray4 = new double[3];
                dArray4[0] = standing;
                dArray4[1] = sneaking;
                dArray = dArray4;
                dArray4[2] = swimming;
            }
        }
        return dArray;
    }

    public static Vec3[] getPossibleEyePositions(Player player) {
        Vec3 basePos = player.position();
        double[] heights = GrimUtils.getPossibleEyeHeights(player);
        Vec3[] result = new Vec3[heights.length];
        for (int i = 0; i < heights.length; ++i) {
            result[i] = basePos.add(0.0, heights[i], 0.0);
        }
        return result;
    }
}

