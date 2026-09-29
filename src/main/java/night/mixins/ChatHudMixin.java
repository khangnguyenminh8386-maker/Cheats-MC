/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.gui.components.ChatComponent$ChatGraphicsAccess
 *  net.minecraft.client.gui.components.ChatComponent$DisplayMode
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.client.multiplayer.chat.GuiMessageSource
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.ARGB
 *  org.joml.Matrix3x2f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package night.mixins;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import night.Night;
import night.managers.ChatManager;
import night.modules.impl.core.CommandsModule;
import night.modules.impl.miscellaneous.BetterChatModule;
import night.utils.text.FormattingUtils;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={ChatComponent.class})
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Inject(method={"addMessageToDisplayQueue"}, at={@At(value="TAIL")})
    private void night$trackAnimation(GuiMessage message, CallbackInfo ci) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (module.isToggled() && module.animation.getValue() && !this.trimmedMessages.isEmpty()) {
            module.getAnimationMap().put(this.trimmedMessages.getFirst(), System.currentTimeMillis());
        }
    }

    @Inject(method={"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$preventChatOverGui(GuiGraphicsExtractor graphics, Font font, int ticks, int mouseX, int mouseY, ChatComponent.DisplayMode displayMode, boolean isFocused, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.screen() != null && !(mc.gui.screen() instanceof ChatScreen) && !ChatManager.isInsideHudExtractChat) {
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"}, at={@At(value="TAIL")})
    private void night$cleanupAnimations(CallbackInfo ci) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (module.isToggled() && module.animation.getValue()) {
            module.getAnimationMap().entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > (long)module.delay.getValue().intValue());
        }
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;updatePose(Ljava/util/function/Consumer;)V"))
    private void night$applyOffset(ChatComponent.ChatGraphicsAccess instance, Consumer<Matrix3x2f> updater) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        int offset = module.isToggled() ? module.offset.getValue().intValue() : 0;
        instance.updatePose(pose -> {
            updater.accept((Matrix3x2f)pose);
            if (offset != 0) {
                pose.translate((float)offset, 0.0f);
            }
        });
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal=0))
    private Object night$textOpacity(OptionInstance<?> instance) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (module.isToggled()) {
            return module.textAlpha.getValue().doubleValue() / 255.0;
        }
        return instance.get();
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal=1))
    private Object night$backgroundOpacity(OptionInstance<?> instance) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (module.isToggled()) {
            return switch (module.background.getValue()) {
                case "Clear" -> 0.0;
                case "Custom" -> (double)module.color.getColor().getAlpha() / 255.0;
                default -> instance.get();
            };
        }
        return instance.get();
    }

    @Redirect(method={"lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/ARGB;black(F)I"))
    private static int night$backgroundColor(float alpha) {
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (module.isToggled() && module.background.getValue().equalsIgnoreCase("Custom")) {
            return ARGB.color((int)Math.round(alpha * 255.0f), (int)(module.color.getColor().getRGB() & 0xFFFFFF));
        }
        return ARGB.black((float)alpha);
    }

    @ModifyArgs(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"))
    private void addMessage(Args args, Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag indicator) {
        String rawText;
        boolean isSender;
        String selfName;
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (!module.isToggled()) {
            return;
        }
        Component processed = message;
        Minecraft mc = Minecraft.getInstance();
        if (module.highlight.getValue() && mc.player != null && (selfName = mc.player.getGameProfile().name()) != null && !selfName.isEmpty() && !(isSender = ChatHudMixin.night$isSender(rawText = message.getString(), selfName)) && ChatHudMixin.night$containsWord(rawText.toLowerCase(), selfName.toLowerCase())) {
            if (module.highlightSound.getValue()) {
                mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
            processed = ChatHudMixin.night$highlightMessage(processed, selfName, module.highlightColor.getColor());
        }
        if (module.timestamps.getValue()) {
            processed = Component.literal((String)"").append((Component)Component.literal((String)(String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + module.opening.getValue() + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).primaryWatermarkColor.getValue())) + new SimpleDateFormat("HH:mm").format(new Date()) + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + module.closing.getValue() + String.valueOf(ChatFormatting.RESET) + " "))).append(processed);
        }
        args.set(1, (Object)processed);
    }

    private static boolean night$isSender(String rawText, String selfName) {
        String senderPart;
        int closeAngle;
        if (rawText == null || selfName == null || selfName.isEmpty()) {
            return false;
        }
        String lower = rawText.toLowerCase();
        String lowerName = selfName.toLowerCase();
        if (lower.startsWith("you whisper to") || lower.startsWith("to ")) {
            return true;
        }
        int sepIdx = -1;
        if (rawText.startsWith("<") && (closeAngle = rawText.indexOf(62)) != -1) {
            sepIdx = closeAngle;
        }
        if (sepIdx == -1) {
            String[] separators;
            for (String sep : separators = new String[]{": ", " \u00bb ", " > ", " -> ", ":"}) {
                int idx = rawText.indexOf(sep);
                if (idx == -1 || sepIdx != -1 && idx >= sepIdx) continue;
                sepIdx = idx;
            }
        }
        return sepIdx != -1 && ChatHudMixin.night$containsWord((senderPart = rawText.substring(0, sepIdx)).toLowerCase(), lowerName);
    }

    private static boolean night$containsWord(String text, String word) {
        if (text == null || word == null) {
            return false;
        }
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            boolean endOk;
            boolean startOk = idx == 0 || !ChatHudMixin.night$isWordChar(text.charAt(idx - 1));
            boolean bl = endOk = idx + word.length() == text.length() || !ChatHudMixin.night$isWordChar(text.charAt(idx + word.length()));
            if (startOk && endOk) {
                return true;
            }
            idx += word.length();
        }
        return false;
    }

    private static boolean night$isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static Component night$highlightMessage(Component original, String name, Color color) {
        TextColor highlightColor = TextColor.fromRgb((int)(color.getRGB() & 0xFFFFFF));
        Style highlightStyle = Style.EMPTY.withColor(highlightColor);
        MutableComponent result = Component.empty();
        original.visit((style, text) -> {
            if (text == null || text.isEmpty()) {
                return Optional.empty();
            }
            String lowerText = text.toLowerCase();
            String lowerName = name.toLowerCase();
            int cursor = 0;
            while (cursor < text.length()) {
                boolean endOk;
                int idx = lowerText.indexOf(lowerName, cursor);
                if (idx == -1) {
                    result.append((Component)Component.literal((String)text.substring(cursor)).withStyle(style));
                    break;
                }
                boolean startOk = idx == 0 || !ChatHudMixin.night$isWordChar(text.charAt(idx - 1));
                boolean bl = endOk = idx + lowerName.length() == text.length() || !ChatHudMixin.night$isWordChar(text.charAt(idx + lowerName.length()));
                if (startOk && endOk) {
                    if (idx > cursor) {
                        result.append((Component)Component.literal((String)text.substring(cursor, idx)).withStyle(style));
                    }
                    result.append((Component)Component.literal((String)text.substring(idx, idx + lowerName.length())).withStyle(highlightStyle));
                    cursor = idx + lowerName.length();
                    continue;
                }
                result.append((Component)Component.literal((String)text.substring(cursor, idx + 1)).withStyle(style));
                cursor = idx + 1;
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }
}

