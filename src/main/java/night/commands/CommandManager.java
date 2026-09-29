/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  lombok.Generated
 */
package night.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import lombok.Generated;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.commands.impl.ModuleCommand;
import night.core.JarClassScanner;
import night.events.SubscribeEvent;
import night.events.impl.ChatInputEvent;
import night.modules.Module;

public class CommandManager {
    private final ArrayList<Command> commands = new ArrayList();
    private String prefix = ".";
    private final CommandDispatcher<CommandManager> dispatcher = new CommandDispatcher();

    public CommandManager() {
        Night.EVENT_HANDLER.subscribe(this);
        try {
            for (Class<?> clazz : JarClassScanner.findSubtypesOf(Command.class, Night.class)) {
                if (clazz.getAnnotation(RegisterCommand.class) == null || clazz == ModuleCommand.class) continue;
                this.commands.add((Command)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            }
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
            Night.LOGGER.error("Failed to register the client's modules!", (Throwable)exception);
        }
        HashSet<String> claimedAliases = new HashSet<String>();
        for (Command command : this.commands) {
            claimedAliases.add(command.getName().toLowerCase());
            for (String alias : command.getAliases()) {
                claimedAliases.add(alias.toLowerCase());
            }
        }
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (claimedAliases.contains(module.getName().toLowerCase())) continue;
            this.commands.add(new ModuleCommand(module));
        }
        for (Command command : this.commands) {
            this.register(command);
        }
    }

    private void register(Command command) {
        ArrayList<String> aliases = new ArrayList<String>();
        aliases.add(command.getName());
        aliases.addAll(command.getAliases());
        for (String alias : aliases) {
            LiteralArgumentBuilder builder = LiteralArgumentBuilder.literal((String)alias);
            builder.executes(ctx -> {
                command.execute(new String[0]);
                return 1;
            });
            builder.then(RequiredArgumentBuilder.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).suggests((ctx, suggestionsBuilder) -> {
                String remaining = suggestionsBuilder.getRemaining();
                String[] parts = remaining.split(" ", -1);
                String partial = parts[parts.length - 1].toLowerCase();
                String[] priorArgs = Arrays.copyOf(parts, parts.length - 1);
                SuggestionsBuilder wordBuilder = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + remaining.length() - partial.length());
                for (String option : command.getSuggestions(priorArgs)) {
                    if (!option.toLowerCase().startsWith(partial)) continue;
                    wordBuilder.suggest(option);
                }
                return wordBuilder.buildFuture();
            }).executes(ctx -> {
                String args = StringArgumentType.getString((CommandContext)ctx, (String)"args");
                command.execute(args.isEmpty() ? new String[]{} : args.split(" "));
                return 1;
            }));
            this.dispatcher.register(builder);
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent event) {
        String message = event.getMessage();
        if (!message.startsWith(this.prefix)) {
            return;
        }
        event.setCancelled(true);
        this.execute(message);
    }

    public void execute(String input) {
        try {
            this.dispatcher.execute(input.substring(this.prefix.length()), this);
        }
        catch (CommandSyntaxException e) {
            Night.CHAT_MANAGER.warn("Could not find the command specified.");
        }
    }

    public Command getCommand(String name) {
        return this.commands.stream().filter(c -> c.getName().equalsIgnoreCase(name.toLowerCase()) || c.getAliases().contains(name.toLowerCase())).findFirst().orElse(null);
    }

    @Generated
    public ArrayList<Command> getCommands() {
        return this.commands;
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }

    @Generated
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Generated
    public CommandDispatcher<CommandManager> getDispatcher() {
        return this.dispatcher;
    }
}

