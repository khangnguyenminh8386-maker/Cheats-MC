/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.WebBlock
 */
package night.modules.impl.movement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.WebBlock;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="FastWeb", description="Allows you to move quickly through webs.", category=Module.Category.MOVEMENT)
public class FastWebModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to speed you through webs.", "Normal", new String[]{"Normal", "Ignore", "Strong"});
    public NumberSetting speed = new NumberSetting("Speed", "The speed at which you will fall through webs.", new ModeSetting.Visibility(this.mode, "Normal"), (Number)3.0, (Number)0.1, (Number)10.0);
    public NumberSetting horizontal = new NumberSetting("Horizontal", "The speed at which you will move horizontally.", new ModeSetting.Visibility(this.mode, "Strong"), (Number)2.5, (Number)0.1, (Number)5.0);
    public NumberSetting vertical = new NumberSetting("Vertical", "The speed at which you will move vertically.", new ModeSetting.Visibility(this.mode, "Strong"), (Number)2.5, (Number)0.1, (Number)5.0);
    public BooleanSetting sneak = new BooleanSetting("Sneak", "Only bypasses web slowdown when sneaking.", new ModeSetting.Visibility(this.mode, "Normal"), false);
    public BooleanSetting grim = new BooleanSetting("Grim", "Includes bypasses for the Grim anticheat.", false);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (FastWebModule.mc.player == null || FastWebModule.mc.level == null) {
            return;
        }
        if (FastWebModule.mc.options.keyShift.isDown() || !this.sneak.getValue()) {
            if (this.mode.getValue().equals("Normal") && !FastWebModule.mc.player.onGround() && EntityUtils.isInWeb((Entity)FastWebModule.mc.player)) {
                FastWebModule.mc.player.setDeltaMovement(FastWebModule.mc.player.getDeltaMovement().x, FastWebModule.mc.player.getDeltaMovement().y - this.speed.getValue().doubleValue(), FastWebModule.mc.player.getDeltaMovement().z);
            }
            if (this.grim.getValue()) {
                for (BlockPos position : this.getWebs()) {
                    mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, position, Direction.DOWN));
                    mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, position, Direction.DOWN));
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    public List<BlockPos> getWebs() {
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int x = 2; x > -2; --x) {
            for (int y = 2; y > -2; --y) {
                for (int z = 2; z > -2; --z) {
                    BlockPos position = FastWebModule.mc.player.blockPosition().offset(x, y, z);
                    if (!(FastWebModule.mc.level.getBlockState(position).getBlock() instanceof WebBlock)) continue;
                    blocks.add(position);
                }
            }
        }
        return blocks;
    }
}

