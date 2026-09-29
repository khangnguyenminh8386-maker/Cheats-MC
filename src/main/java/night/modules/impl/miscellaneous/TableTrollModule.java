/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.level.block.Blocks
 */
package night.modules.impl.miscellaneous;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.WorldUtils;

public class TableTrollModule
extends Module {
    public NumberSetting limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed each group.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between each group of placements.", 0, 0, 20);
    private int ticks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        int bps = 0;
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(5.0) && bps <= this.limit.getValue().intValue(); ++i) {
            BlockPos position = TableTrollModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if (!WorldUtils.isPlaceable(position) || TableTrollModule.mc.level.getBlockState(position.below()).canBeReplaced() || TableTrollModule.mc.level.getBlockState(position.below()).getBlock() == Blocks.ENDER_CHEST) continue;
            WorldUtils.placeBlock(position, WorldUtils.getDirection(position, false), InteractionHand.MAIN_HAND, false, false);
            this.ticks = this.delay.getValue().intValue();
            ++bps;
        }
    }
}

