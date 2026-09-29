/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="TextESP", description="Renders text ESP on the world.", category=Module.Category.VISUALS)
public class TextESPModule
extends Module {
    public BooleanSetting items = new BooleanSetting("Items", "Renders text ESP on item entities.", true);
    public ModeSetting itemListMode = new ModeSetting("Mode", "WhiteList = only listed items. BlackList = items NOT in list. All = render on every item.", new BooleanSetting.Visibility(this.items, true), "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting itemWhitelist = new WhitelistSetting("Whitelist", "Items the WhiteList/BlackList mode compares against.", new BooleanSetting.Visibility(this.items, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Renders text ESP on pearl entities.", true);
    public BooleanSetting chorus = new BooleanSetting("Chorus", "Renders text ESP on chorus sounds.", true);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 30, 10, 100);
    public ColorSetting color = new ColorSetting("Color", "The color that will be used for the text ESP rendering.", new ColorSetting.Color(Color.WHITE, false, false));
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around text ESP.", false);
    private final ArrayList<Chorus> chorusList = new ArrayList();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (this.getNull()) {
            return;
        }
        ArrayList<Chorus> arrayList = this.chorusList;
        synchronized (arrayList) {
            ClientboundSoundPacket packet;
            SoundEvent sound;
            Packet<?> packet2 = event.getPacket();
            if (packet2 instanceof ClientboundSoundPacket && ((sound = (SoundEvent)(packet = (ClientboundSoundPacket)packet2).getSound().value()) == SoundEvents.CHORUS_FRUIT_TELEPORT || sound == SoundEvents.ENDERMAN_TELEPORT)) {
                this.chorusList.add(new Chorus(this, mc.getSoundManager().getSoundEvent(sound.location()).getSubtitle().getString(), new Vec3(packet.getX(), packet.getY(), packet.getZ())));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.getNull()) {
            return;
        }
        for (Entity e : TextESPModule.mc.level.entitiesForRendering()) {
            ThrownEnderpearl pearl;
            Vec3 pos;
            if (!Renderer3D.isFrustumVisible(e.getBoundingBox())) continue;
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity)e;
                if (this.items.getValue() && this.itemAllowed(item.getItem().getItem())) {
                    pos = EntityUtils.getRenderPos((Entity)item, event.getTickDelta());
                    String s = item.getName().getString() + (String)(item.getItem().getCount() > 1 ? " x" + item.getItem().getCount() : "");
                    Renderer3D.renderScaledText(event.getMatrices(), s, pos.x, pos.y, pos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
                    continue;
                }
            }
            if (!(e instanceof ThrownEnderpearl) || (pearl = (ThrownEnderpearl)e).getOwner() == null || !this.pearls.getValue()) continue;
            pos = EntityUtils.getRenderPos((Entity)pearl, event.getTickDelta());
            Renderer3D.renderScaledText(event.getMatrices(), pearl.getOwner().getName().getString(), pos.x, pos.y, pos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
        }
        if (this.chorus.getValue()) {
            ArrayList<Chorus> arrayList = this.chorusList;
            synchronized (arrayList) {
                this.chorusList.removeIf(c -> System.currentTimeMillis() - c.time >= 1500L);
                for (Chorus c2 : this.chorusList) {
                    Renderer3D.renderScaledText(event.getMatrices(), c2.subtitle, c2.pos.x, c2.pos.y, c2.pos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
                }
            }
        }
    }

    private boolean itemAllowed(Item item) {
        boolean listed = this.itemWhitelist.isWhitelistContains(item);
        return switch (this.itemListMode.getValue()) {
            case "WhiteList" -> listed;
            case "BlackList" -> {
                if (!listed) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private class Chorus {
        private final String subtitle;
        private final Vec3 pos;
        private final long time;

        public Chorus(TextESPModule textESPModule, String subtitle, Vec3 pos) {
            Objects.requireNonNull(textESPModule);
            this.subtitle = subtitle;
            this.pos = pos;
            this.time = System.currentTimeMillis();
        }
    }
}

