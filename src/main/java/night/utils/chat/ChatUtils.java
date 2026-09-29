/*
 * Decompiled with CFR 0.152.
 */
package night.utils.chat;

import night.Night;
import night.modules.impl.core.CommandsModule;
import night.utils.text.FormattingUtils;

public class ChatUtils {
    public static Object getPrimary() {
        return FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).primaryMessageColor.getValue());
    }

    public static Object getSecondary() {
        return FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryMessageColor.getValue());
    }
}

