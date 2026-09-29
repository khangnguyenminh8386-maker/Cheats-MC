/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  net.minecraft.client.gui.components.CommandSuggestions
 *  net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.multiplayer.ClientSuggestionProvider
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import night.Night;
import night.commands.CommandManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CommandSuggestions.class})
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Nullable
    private ParseResults<ClientSuggestionProvider> currentParse;
    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private // Could not load outer class - annotation placement on inner may be incorrect
    @Nullable CommandSuggestions.SuggestionsList suggestions;
    @Shadow
    private boolean keepSuggestions;
    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private void updateUsageInfo(ParseResults<ClientSuggestionProvider> parseResults, Suggestions suggestionResult) {
    }

    @Inject(method={"updateCommandInfo"}, at={@At(value="RETURN")})
    private void night$updateCommandInfo(CallbackInfo ci) {
        ParseResults parse;
        String prefix;
        String raw = this.input.getValue();
        if (!raw.startsWith(prefix = Night.COMMAND_MANAGER.getPrefix())) {
            return;
        }
        StringReader reader = new StringReader(raw);
        reader.setCursor(prefix.length());
        CommandDispatcher<CommandManager> dispatcher = Night.COMMAND_MANAGER.getDispatcher();
        this.currentParse = parse = dispatcher.parse(reader, Night.COMMAND_MANAGER);
        if (this.suggestions == null || !this.keepSuggestions) {
            CompletableFuture future;
            this.pendingSuggestions = future = dispatcher.getCompletionSuggestions(parse, this.input.getCursorPosition());
            future.thenRun(() -> {
                if (future.isDone()) {
                    this.updateUsageInfo(this.currentParse, (Suggestions)future.join());
                }
            });
        }
    }
}

