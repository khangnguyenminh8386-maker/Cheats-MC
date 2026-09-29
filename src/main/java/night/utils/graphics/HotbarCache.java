/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.TrackingItemStackRenderState
 *  net.minecraft.client.renderer.state.gui.GuiItemRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package night.utils.graphics;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class HotbarCache {
    private static final SlotEntry[] SLOTS = new SlotEntry[10];

    public static void clear() {
        for (SlotEntry slot : SLOTS) {
            slot.clear();
        }
    }

    public static void extractItem(GuiGraphicsExtractor context, LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiRenderState renderStateContainer = ((GuiGraphicsExtractorAccessor)context).night$getGuiRenderState();
        int slotIdx = seed >= 1 && seed <= 10 ? seed - 1 : -1;
        TrackingItemStackRenderState trackingState = null;
        if (slotIdx >= 0 && slotIdx < SLOTS.length) {
            SlotEntry entry = SLOTS[slotIdx];
            if (entry.matches(stack, seed)) {
                trackingState = entry.renderState;
            } else {
                trackingState = new TrackingItemStackRenderState();
                if (mc.getItemModelResolver() != null && entity != null && entity.level() != null) {
                    mc.getItemModelResolver().updateForTopItem((ItemStackRenderState)trackingState, stack, ItemDisplayContext.GUI, entity.level(), (ItemOwner)entity, seed);
                }
                entry.set(stack, seed, trackingState);
            }
        } else {
            trackingState = new TrackingItemStackRenderState();
            if (mc.getItemModelResolver() != null && entity != null && entity.level() != null) {
                mc.getItemModelResolver().updateForTopItem((ItemStackRenderState)trackingState, stack, ItemDisplayContext.GUI, entity.level(), (ItemOwner)entity, seed);
            }
        }
        if (renderStateContainer != null && trackingState != null) {
            Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)context.pose());
            renderStateContainer.addItem(new GuiItemRenderState(pose, trackingState, x, y, (ScreenRectangle)null));
        }
    }

    static {
        for (int i = 0; i < SLOTS.length; ++i) {
            HotbarCache.SLOTS[i] = new SlotEntry();
        }
    }

    public static class SlotEntry {
        public Item item;
        public int count;
        public int damage;
        public Object components;
        public int seed;
        public TrackingItemStackRenderState renderState;

        public boolean matches(ItemStack stack, int seed) {
            if (stack == null || stack.isEmpty() || this.renderState == null) {
                return false;
            }
            if (this.renderState.isAnimated()) {
                return false;
            }
            return stack.getItem() == this.item && stack.getCount() == this.count && stack.getDamageValue() == this.damage && Objects.equals(stack.getComponents(), this.components) && this.seed == seed;
        }

        public void set(ItemStack stack, int seed, TrackingItemStackRenderState state) {
            this.item = stack.getItem();
            this.count = stack.getCount();
            this.damage = stack.getDamageValue();
            this.components = stack.getComponents();
            this.seed = seed;
            this.renderState = state;
        }

        public void clear() {
            this.item = null;
            this.count = 0;
            this.damage = 0;
            this.components = null;
            this.seed = 0;
            this.renderState = null;
        }
    }
}

