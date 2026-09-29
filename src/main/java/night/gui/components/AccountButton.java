/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package night.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.gui.screens.account.AccountManagerScreen;

public class AccountButton {
    public static Button create(int x, int y, int width, int height, Screen parent) {
        return Button.builder((Component)Component.literal((String)"Accounts"), button -> {
            if (Minecraft.getInstance() != null) {
                Minecraft.getInstance().gui.setScreen((Screen)new AccountManagerScreen(parent));
            }
        }).bounds(x, y, width, height).build();
    }
}

