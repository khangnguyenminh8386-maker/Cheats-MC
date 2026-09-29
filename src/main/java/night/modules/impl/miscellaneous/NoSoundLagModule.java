/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  net.minecraft.core.Holder
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundLevelEventPacket
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 */
package night.modules.impl.miscellaneous;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="NoSoundLag", description="Prevents lagging caused by a large amount of sounds being played.", category=Module.Category.MISCELLANEOUS)
public class NoSoundLagModule
extends Module {
    public BooleanSetting armor = new BooleanSetting("Armor", "Prevents lagging caused by armor sounds.", true);
    public BooleanSetting withers = new BooleanSetting("Withers", "Prevents lagging caused by wither sounds.", true);
    public BooleanSetting ghasts = new BooleanSetting("Ghasts", "Prevents lagging caused by ghast sounds.", true);
    public BooleanSetting piston = new BooleanSetting("Piston", "Prevents lagging caused by piston sounds.", true);
    public BooleanSetting dispenser = new BooleanSetting("Dispenser", "Prevents lagging caused by dispenser sounds.", true);
    public static final Set<Holder<SoundEvent>> ARMOR_SOUNDS = Sets.newHashSet(new Holder[]{SoundEvents.ARMOR_EQUIP_GENERIC, SoundEvents.ARMOR_EQUIP_ELYTRA, SoundEvents.ARMOR_EQUIP_DIAMOND, SoundEvents.ARMOR_EQUIP_IRON, SoundEvents.ARMOR_EQUIP_GOLD, SoundEvents.ARMOR_EQUIP_CHAIN, SoundEvents.ARMOR_EQUIP_LEATHER});
    public static final Set<SoundEvent> WITHER_SOUNDS = Sets.newHashSet(new SoundEvent[]{SoundEvents.WITHER_AMBIENT, SoundEvents.WITHER_DEATH, SoundEvents.WITHER_BREAK_BLOCK, SoundEvents.WITHER_HURT, SoundEvents.WITHER_SPAWN, SoundEvents.WITHER_SHOOT});
    public Set<SoundEvent> GHAST_SOUNDS = Sets.newHashSet(new SoundEvent[]{SoundEvents.GHAST_AMBIENT, SoundEvents.GHAST_DEATH, SoundEvents.GHAST_HURT, SoundEvents.GHAST_SCREAM, SoundEvents.GHAST_SHOOT, SoundEvents.GHAST_WARN});
    public static final Set<SoundEvent> PISTON_SOUNDS = Sets.newHashSet(new SoundEvent[]{SoundEvents.PISTON_EXTEND, SoundEvents.PISTON_CONTRACT});
    public static final Set<SoundEvent> DISPENSER_SOUNDS = Sets.newHashSet(new SoundEvent[]{SoundEvents.DISPENSER_DISPENSE, SoundEvents.DISPENSER_FAIL, SoundEvents.DISPENSER_LAUNCH});

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket packet2 = (ClientboundSoundPacket)packet;
            if (this.armor.getValue() && ARMOR_SOUNDS.contains(packet2.getSound()) || this.withers.getValue() && WITHER_SOUNDS.contains(packet2.getSound().value()) || this.ghasts.getValue() && this.GHAST_SOUNDS.contains(packet2.getSound().value()) || this.piston.getValue() && PISTON_SOUNDS.contains(packet2.getSound().value()) || this.dispenser.getValue() && DISPENSER_SOUNDS.contains(packet2.getSound().value())) {
                event.setCancelled(true);
            }
        }
        if ((packet = event.getPacket()) instanceof ClientboundLevelEventPacket) {
            ClientboundLevelEventPacket levelEvent = (ClientboundLevelEventPacket)packet;
            if (this.dispenser.getValue() && (levelEvent.getType() == 1000 || levelEvent.getType() == 1001 || levelEvent.getType() == 1002)) {
                event.setCancelled(true);
            }
        }
    }
}

