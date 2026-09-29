/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.RenderOverlayEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="ArrowTracers", description="Renders arrows towards players who are off screen.", category=Module.Category.VISUALS)
public class ArrowTracersModule
extends Module {
    public NumberSetting width = new NumberSetting("Width", "The width of the arrows being rendered.", Float.valueOf(4.0f), Float.valueOf(0.5f), Float.valueOf(15.0f));
    public NumberSetting height = new NumberSetting("Height", "The height of the arrows being rendered.", Float.valueOf(8.0f), Float.valueOf(0.5f), Float.valueOf(15.0f));
    public NumberSetting distance = new NumberSetting("Distance", "The distance that will be between the crosshair and the arrows.", 45, 5, 200);
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
    public BooleanSetting onScreen = new BooleanSetting("OnScreen", "Renders the arrow tracers even for players who are on screen.", false);
    public ModeSetting alpha = new ModeSetting("Alpha", "The alpha that will be used in the arrow rendering.", "Fade", new String[]{"Default", "Fade"});
    public NumberSetting fadeDistance = new NumberSetting("FadeDistance", "The distance at which the arrows will start fading.", new ModeSetting.Visibility(this.alpha, "Fade"), (Number)Float.valueOf(100.0f), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(200.0f));
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the arrows", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultOutlineColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), new ColorSetting.Color(new Color(0, 0, 0), true, false));

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        if (ArrowTracersModule.mc.player == null || ArrowTracersModule.mc.level == null) {
            return;
        }
        GuiGraphicsExtractor context = event.getContext();
        float cx = (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        float cy = (float)mc.getWindow().getGuiScaledHeight() / 2.0f;
        float dist = this.distance.getValue().floatValue();
        float arrowWidth = this.width.getValue().floatValue();
        float arrowHeight = this.height.getValue().floatValue();
        Vec3 pos = EntityUtils.getRenderPos((Entity)ArrowTracersModule.mc.player, event.getTickDelta());
        for (Player player : ArrowTracersModule.mc.level.players()) {
            if (player == ArrowTracersModule.mc.player || EntityUtils.isGhost((Entity)player) || EntityUtils.isBot(player) && this.antiBot.getValue() || !this.onScreen.getValue() && Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            Vec3 playerPos = EntityUtils.getRenderPos((Entity)player, event.getTickDelta());
            double diffX = playerPos.x - pos.x;
            double diffZ = playerPos.z - pos.z;
            double targetYaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
            double deltaYaw = Mth.wrapDegrees((double)(targetYaw - (double)ArrowTracersModule.mc.player.getYRot()));
            int a = (int)Mth.clamp((float)(255.0f - 255.0f / this.fadeDistance.getValue().floatValue() * ArrowTracersModule.mc.player.distanceTo((Entity)player)), (float)80.0f, (float)255.0f);
            Color fill = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtils.getColor(this.fillColor.getColor(), a) : this.fillColor.getColor();
            Color outline = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtils.getColor(this.outlineColor.getColor(), a) : this.outlineColor.getColor();
            context.pose().pushMatrix();
            context.pose().translate(cx, cy);
            context.pose().rotate((float)Math.toRadians(deltaYaw));
            context.pose().translate(-cx, -cy);
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer2D.renderArrow(context, cx, cy - dist, arrowWidth, arrowHeight, fill);
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer2D.renderArrowOutline(context, cx, cy - dist, arrowWidth, arrowHeight, outline);
            }
            context.pose().popMatrix();
        }
    }
}

