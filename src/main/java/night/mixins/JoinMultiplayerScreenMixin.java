/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import night.gui.components.AccountButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={JoinMultiplayerScreen.class}, priority=10000)
public abstract class JoinMultiplayerScreenMixin
extends Screen {
    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo ci) {
        int btnY;
        int btnX;
        int btnWidth = 75;
        int btnHeight = 20;
        AbstractWidget vfpWidget = null;
        for (GuiEventListener child : this.children()) {
            String text;
            if (!(child instanceof AbstractWidget)) continue;
            AbstractWidget widget = (AbstractWidget)child;
            String className = widget.getClass().getName().toLowerCase();
            String string = text = widget.getMessage() != null ? widget.getMessage().getString().toLowerCase() : "";
            if (!className.contains("viafabric") && !text.contains("viafabric") && !text.contains("viaversion")) continue;
            vfpWidget = widget;
            break;
        }
        if (vfpWidget != null) {
            btnX = vfpWidget.getX() - btnWidth - 4;
            btnY = vfpWidget.getY();
            btnHeight = vfpWidget.getHeight() > 0 ? vfpWidget.getHeight() : 20;
        } else {
            btnX = this.width - btnWidth - 6;
            btnY = 5;
        }
        this.addRenderableWidget(AccountButton.create(btnX, btnY, btnWidth, btnHeight, this));
    }
}

