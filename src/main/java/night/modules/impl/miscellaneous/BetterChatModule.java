/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.util.FormattedCharSequence
 */
package night.modules.impl.miscellaneous;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;

@RegisterModule(name="BetterChat", description="Improves the default minecraft chat.", category=Module.Category.MISCELLANEOUS)
public class BetterChatModule
extends Module {
    public BooleanSetting noIndicators = new BooleanSetting("NoIndicators", "Removes indicators from the chat.", false);
    public NumberSetting offset = new NumberSetting("Offset", "The offset for the chat on the X axis.", 0, -100, 100);
    public BooleanSetting animation = new BooleanSetting("Animation", "Adds in an animation when adding new messages.", false);
    public NumberSetting delay = new NumberSetting("Delay", "The delay of the animation.", new BooleanSetting.Visibility(this.animation, true), (Number)200, (Number)0, (Number)300);
    public NumberSetting textAlpha = new NumberSetting("TextAlpha", "The alpha of the chat text.", 255, 0, 255);
    public BooleanSetting timestamps = new BooleanSetting("Timestamps", "Adds timestamps to every chat message.", true);
    public StringSetting opening = new StringSetting("Opening", "The symbol that will be placed before the timestamp text.", new BooleanSetting.Visibility(this.timestamps, true), "<");
    public StringSetting closing = new StringSetting("Closing", "The symbol that will be placed after the timestamp text.", new BooleanSetting.Visibility(this.timestamps, true), ">");
    public ModeSetting background = new ModeSetting("Background", "The background mode for the chat.", "Default", new String[]{"Default", "Clear", "Custom"});
    public ColorSetting color = new ColorSetting("Color", "The color of the chat background.", new ModeSetting.Visibility(this.background, "Custom"), new ColorSetting.Color(new Color(0, 0, 0, 127), false, false));
    public BooleanSetting highlight = new BooleanSetting("Highlight", "Highlights your name in incoming chat messages.", true);
    public ColorSetting highlightColor = new ColorSetting("HighlightColor", "The color used to highlight your name in chat.", new BooleanSetting.Visibility(this.highlight, true), new ColorSetting.Color(new Color(85, 255, 255), false, false));
    public BooleanSetting highlightSound = new BooleanSetting("HighlightSound", "Plays a ding notification sound when your name is mentioned in chat.", new BooleanSetting.Visibility(this.highlight, true), true);
    private final Map<GuiMessage.Line, Long> animationMap = new HashMap<GuiMessage.Line, Long>();

    public Long getAnimationStart(FormattedCharSequence content) {
        for (Map.Entry<GuiMessage.Line, Long> entry : this.animationMap.entrySet()) {
            if (entry.getKey().content() != content) continue;
            return entry.getValue();
        }
        return null;
    }

    @Generated
    public Map<GuiMessage.Line, Long> getAnimationMap() {
        return this.animationMap;
    }
}

