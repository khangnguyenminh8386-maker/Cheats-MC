/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CraftingScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package night.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.modules.impl.core.NoMiddleClickModule;
import night.modules.impl.miscellaneous.ShulkerInfoModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.InventoryControlModule;
import night.modules.impl.player.ChestStealerModule;
import night.utils.IMinecraft;
import night.utils.minecraft.InventoryUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={AbstractContainerScreen.class})
public abstract class HandledScreenMixin
extends Screen
implements IMinecraft {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected AbstractContainerMenu menu;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected int imageWidth;
    private Slot night$lastDragSlot = null;

    @Shadow
    protected abstract void slotClicked(Slot var1, int var2, int var3, ContainerInput var4);

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"getTooltipFromContainerItem"}, at={@At(value="RETURN")}, cancellable=true)
    private void night$trimShulkerName(ItemStack itemStack, CallbackInfoReturnable<List<Component>> cir) {
        ShulkerInfoModule shulkerInfoModule = Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
        if (shulkerInfoModule == null || !shulkerInfoModule.isToggled() || !shulkerInfoModule.hasItems(itemStack)) {
            return;
        }
        List lines = (List)cir.getReturnValue();
        if (lines == null || lines.size() <= 1) {
            return;
        }
        cir.setReturnValue(new ArrayList(lines.subList(1, lines.size())));
    }

    @ModifyArgs(method={"extractTooltip"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
    private void night$pushVanillaTooltipBelowShulkerInfo(Args args) {
        ShulkerInfoModule shulkerInfoModule = Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
        if (shulkerInfoModule == null || !shulkerInfoModule.isToggled()) {
            return;
        }
        if (this.hoveredSlot == null || this.hoveredSlot.getItem().isEmpty() || !shulkerInfoModule.hasItems(this.hoveredSlot.getItem())) {
            return;
        }
        int mouseY = (Integer)args.get(4);
        args.set(4, (Object)(mouseY + 20));
    }

    @Inject(method={"mouseDragged"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$dragClick(MouseButtonEvent event, double dragX, double dragY, CallbackInfoReturnable<Boolean> info) {
        boolean shiftDown;
        InventoryControlModule module = Night.MODULE_MANAGER.getModule(InventoryControlModule.class);
        if (!module.isToggled() || !module.dragClick.getValue()) {
            return;
        }
        boolean bl = shiftDown = InputConstants.isKeyDown((Window)mc.getWindow(), (int)340) || InputConstants.isKeyDown((Window)mc.getWindow(), (int)344);
        if (event.button() != 0 || !shiftDown) {
            return;
        }
        if (this.hoveredSlot == null || this.hoveredSlot.getItem().isEmpty() || this.hoveredSlot == this.night$lastDragSlot) {
            return;
        }
        this.night$lastDragSlot = this.hoveredSlot;
        this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ContainerInput.QUICK_MOVE);
        info.setReturnValue(true);
        info.cancel();
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")})
    private void night$dragClickReset(MouseButtonEvent event, CallbackInfoReturnable<Boolean> info) {
        this.night$lastDragSlot = null;
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
   private void night$chestStealerButtons(CallbackInfo ci) {
      if (Night.MODULE_MANAGER != null) {
         ChestStealerModule module = Night.MODULE_MANAGER.getModule(ChestStealerModule.class);
         if (module != null && module.isToggled() && module.buttonMode.getValue()) {
            boolean isInventoryScreen = (Object)this instanceof InventoryScreen;
            boolean isCraftingScreen = (Object)this instanceof CraftingScreen;
            if (isInventoryScreen) {
               int x = this.leftPos + this.imageWidth - 60;
               int y = this.topPos - 22;
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), b -> module.triggerDrop()).bounds(x, y, 60, 18).build());
            } else if (isCraftingScreen) {
               int btnW = 46;
               int btnH = 18;
               int gap = 3;
               int x = this.leftPos + this.imageWidth - btnW;
               int y = this.topPos - 22;
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), b -> module.triggerDrop()).bounds(x, y, btnW, btnH).build());
               x -= btnW + gap;
               this.addRenderableWidget(Button.builder(Component.literal("Dump"), b -> module.triggerDump()).bounds(x, y, btnW, btnH).build());
               x -= btnW + gap;
               this.addRenderableWidget(Button.builder(Component.literal("Steal"), b -> module.triggerSteal()).bounds(x, y, btnW, btnH).build());
            } else {
               int x = this.leftPos + this.imageWidth + 4;
               int y = this.topPos;
               this.addRenderableWidget(Button.builder(Component.literal("Steal"), b -> module.triggerSteal()).bounds(x, y, 60, 20).build());
               this.addRenderableWidget(Button.builder(Component.literal("Dump"), b -> module.triggerDump()).bounds(x, y + 22, 60, 20).build());
               this.addRenderableWidget(Button.builder(Component.literal("Drop"), b -> module.triggerDrop()).bounds(x, y + 44, 60, 20).build());
            }
         }
      }
   }

    @Redirect(method={"extractSlot"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack night$grimSwapVisual(Slot slot) {
        ItemStack elytra;
        ItemStack real = slot.getItem();
        if (Night.MODULE_MANAGER == null) {
            return real;
        }
        ElytraFlyModule elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        int parkedSlot = elytraFly.getGrimParkedSlot();
        if (parkedSlot == -1) {
            return real;
        }
        if (slot.index == 6 && (elytra = elytraFly.getGrimHiddenElytra()) != null) {
            return elytra;
        }
        if (slot.index == InventoryUtils.indexToSlot(parkedSlot)) {
            return elytraFly.getGrimParkedDisplaced();
        }
        return real;
    }

    @Inject(method={"slotClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onSlotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type, CallbackInfo info) {
        NoMiddleClickModule module;
        if (type == ContainerInput.CLONE && Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && module.isToggled()) {
            info.cancel();
        }
    }
}

