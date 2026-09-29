/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMineEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="BreakHighlight", description="Renders blocks that are being mined by other players.", category=Module.Category.VISUALS)
public class BreakHighlightModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the mine esp.", "Outline", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting name = new BooleanSetting("Name", "Renders the name of the player mining the block.", true);
    public ColorSetting nameColor = new ColorSetting("NameColor", "The color used for the name text rendering.", new BooleanSetting.Visibility(this.name, true), new ColorSetting.Color(Color.WHITE, false, false));
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around the breaker name text.", new BooleanSetting.Visibility(this.name, true), false);
    public NumberSetting scale = new NumberSetting("Scale", "The scale of the name text.", new BooleanSetting.Visibility(this.name, true), (Number)30, (Number)10, (Number)100);
    private final Map<Integer, Mine> mineMap = new HashMap<Integer, Mine>();

    @SubscribeEvent
   public void onPlayerMine(PlayerMineEvent event) {
      if (!this.getNull() && event.getActorID() != mc.player.getId()) {
         if (mc.level != null) {
            Block targetBlock = mc.level.getBlockState(event.getPosition()).getBlock();
            if (!targetBlock.equals(Blocks.AIR)) {
               Entity entity = mc.level.getEntity(event.getActorID());
               String playerName = entity instanceof Player player ? player.getName().getString() : "Player";
               float breakTime = entity instanceof Player player ? WorldUtils.getBreakTime(player, mc.level.getBlockState(event.getPosition())) : 1000.0F;
               BreakHighlightModule.Mine mine = new BreakHighlightModule.Mine(
                  event.getPosition(), targetBlock, breakTime, System.currentTimeMillis(), playerName
               );
               if (!this.mineMap.containsKey(event.getActorID())) {
                  this.mineMap.put(event.getActorID(), mine);
               } else if (!this.mineMap.get(event.getActorID()).pos.equals(event.getPosition())) {
                  this.mineMap.replace(event.getActorID(), mine);
               }
            }
         }
      }
   }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.getNull() || this.mineMap.isEmpty()) {
            return;
        }
        this.mineMap.entrySet().removeIf(e -> this.clearMine((Integer)e.getKey(), (Mine)e.getValue()));
        this.mineMap.forEach((id, mine) -> {
            float progressDelta = Easing.toDelta(mine.time, (int)mine.breakTime);
            AABB box = new AABB(mine.pos).deflate(0.5).inflate((double)progressDelta / 2.0);
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(event.getMatrices(), box, this.fillColor.getColor());
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(event.getMatrices(), box, this.outlineColor.getColor());
            }
            if (this.name.getValue() && mine.playerName != null) {
                Vec3 center = Vec3.atCenterOf((Vec3i)mine.pos);
                int progressPercent = Math.min(100, Math.max(0, (int)(progressDelta * 100.0f)));
                String text = mine.playerName + "\n" + progressPercent + "%";
                Renderer3D.renderCenteredScaledText(event.getMatrices(), text, center.x, center.y, center.z, this.scale.getValue().intValue(), false, this.nameColor.getColor(), this.glow.getValue());
            }
        });
    }

    private boolean clearMine(int id, Mine mine) {
        if (BreakHighlightModule.mc.level == null) {
            return true;
        }
        if (!BreakHighlightModule.mc.level.getBlockState(mine.pos).getBlock().equals(mine.block)) {
            return true;
        }
        if ((float)(System.currentTimeMillis() - mine.time) > mine.breakTime + 3000.0f) {
            return true;
        }
        Entity entity = BreakHighlightModule.mc.level.getEntity(id);
        return entity != null && Math.sqrt(entity.distanceToSqr(Vec3.atCenterOf((Vec3i)mine.pos))) > 8.0;
    }

    private static class Mine {
        private final BlockPos pos;
        private final Block block;
        private final float breakTime;
        private final long time;
        private final String playerName;

        @Generated
        public Mine(BlockPos pos, Block block, float breakTime, long time, String playerName) {
            this.pos = pos;
            this.block = block;
            this.breakTime = breakTime;
            this.time = time;
            this.playerName = playerName;
        }
    }
}

