/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import java.awt.Color;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IFreecamModule;
import night.modules.impl.core.IgnoreNakedModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="Tracers", description="Renders a line showing where other players are located.", category=Module.Category.VISUALS)
public class TracersModule
extends Module {
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the tracers color.", "Distance", new String[]{"Distance", "Custom"});
    public ColorSetting color = new ColorSetting("Color", "The color used for the fill rendering.", ColorUtils.getDefaultOutlineColor());

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        Vec3 cameraPos;
        boolean isFreecam;
        if (this.getNull()) {
            return;
        }
        if (TracersModule.mc.player == null) {
            return;
        }
        IFreecamModule freecam = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
        boolean bl = isFreecam = freecam != null && freecam.isToggled();
        if (isFreecam) {
            float pitch = freecam.getFreePitch();
            float yaw = freecam.getFreeYaw();
            float f = pitch * ((float)Math.PI / 180);
            float g = -yaw * ((float)Math.PI / 180);
            float h = Mth.cos((double)g);
            float i = Mth.sin((double)g);
            float j = Mth.cos((double)f);
            float k = Mth.sin((double)f);
            Vec3 forward = new Vec3((double)(i * j), (double)(-k), (double)(h * j));
            cameraPos = new Vec3(freecam.getFreeX(), freecam.getFreeY(), freecam.getFreeZ()).add(forward);
        } else {
            Vec3 cam = TracersModule.mc.gameRenderer.mainCamera().position();
            Vec3 forward = Vec3.directionFromRotation((float)TracersModule.mc.gameRenderer.mainCamera().xRot(), (float)TracersModule.mc.gameRenderer.mainCamera().yRot());
            cameraPos = cam.add(forward);
        }
        for (Player player : TracersModule.mc.level.players()) {
            if (player == TracersModule.mc.player && !isFreecam || EntityUtils.isGhost((Entity)player) || EntityUtils.isBot(player) && this.antiBot.getValue() || Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() && EntityUtils.isNaked(player)) continue;
            Vec3 playerPos = EntityUtils.getRenderPos((Entity)player, event.getTickDelta());
            Renderer3D.renderLine(Renderer3D.TRACER_DEBUG_LINES, event.getMatrices(), cameraPos, playerPos, this.getColor(player, cameraPos));
        }
    }

    private Color getColor(Player player, Vec3 origin) {
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return Night.FRIEND_MANAGER.getDefaultFriendColor(this.color.getColor().getAlpha());
        }
        if (this.mode.getValue().equals("Custom")) {
            return this.color.getColor();
        }
        float maxDistance = 80.0f;
        double distSq = origin.distanceToSqr(player.getX(), player.getY(), player.getZ());
        float distance = Mth.clamp((float)((float)Math.sqrt(distSq)), (float)0.0f, (float)maxDistance);
        return new Color((maxDistance - distance) / maxDistance, 1.0f - (maxDistance - distance) / maxDistance, 0.0f, (float)this.color.getColor().getAlpha() / 255.0f);
    }
}

