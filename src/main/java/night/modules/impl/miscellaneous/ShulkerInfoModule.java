/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package night.modules.impl.miscellaneous;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import night.Night;
import night.modules.Module;
import night.modules.RegisterModule;
import night.utils.graphics.Renderer2D;

@RegisterModule(name="ShulkerInfo", description="Renders a preview of the contents of the shulker you are hovering.", category=Module.Category.MISCELLANEOUS)
public class ShulkerInfoModule
extends Module {
    public void renderInfo(GuiGraphicsExtractor context, int x, int y, ItemStack itemStack) {
        try {
            BlockItem blockItem;
            Object object;
            ItemContainerContents component = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
            Item item = itemStack.getItem();
            Color color = Color.WHITE;
            if (item instanceof BlockItem && (object = (blockItem = (BlockItem)item).getBlock()) instanceof ShulkerBoxBlock) {
                ShulkerBoxBlock shulker = (ShulkerBoxBlock)object;
                try {
                    color = new Color(shulker.getColor().getTextureDiffuseColor());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            Renderer2D.renderQuad(context, x += 4, y -= 62 + Night.FONT_MANAGER.getHeight(), x + 164, y + 60 + Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 200));
            Renderer2D.renderOutline(context, x, y, x + 164, y + 60 + Night.FONT_MANAGER.getHeight(), color);
            Night.FONT_MANAGER.drawTextWithShadow(context, itemStack.getHoverName().getString(), x + 3, y + 3, Color.WHITE);
            int row = 0;
            int i = 0;
            for (ItemStack stack : component.nonEmptyItemCopyStream().toList()) {
                int offsetX = x + 1 + i * 18;
                int offsetY = y + Night.FONT_MANAGER.getHeight() + 5 + row * 18;
                context.item(stack, offsetX, offsetY);
                context.itemDecorations(ShulkerInfoModule.mc.font, stack, offsetX, offsetY);
                if (++i < 9) continue;
                i = 0;
                ++row;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean hasItems(ItemStack itemStack) {
        ItemContainerContents component = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
        return component != null && !component.nonEmptyItemCopyStream().toList().isEmpty();
    }
}

