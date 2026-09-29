/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.commands;

import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.commands.RegisterCommand;
import night.utils.IMinecraft;

public abstract class Command
implements IMinecraft {
    private String name;
    private String tag;
    private final String description;
    private final String syntax;
    private final List<String> aliases;

    public Command() {
        RegisterCommand annotation = this.getClass().getAnnotation(RegisterCommand.class);
        this.name = annotation.name();
        this.tag = annotation.tag().isEmpty() ? annotation.name() : annotation.tag();
        this.description = annotation.description();
        this.syntax = annotation.syntax();
        this.aliases = Arrays.asList(annotation.aliases());
    }

    public abstract void execute(String[] var1);

    public List<String> getSuggestions(String[] args) {
        return List.of();
    }

    protected List<String> moduleNames() {
        return Night.MODULE_MANAGER.getModules().stream().map(m -> m.getName().toLowerCase()).toList();
    }

    public void messageSyntax() {
        Night.CHAT_MANAGER.info(this.name + " " + this.syntax);
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public String getSyntax() {
        return this.syntax;
    }

    @Generated
    public List<String> getAliases() {
        return this.aliases;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
    }

    @Generated
    public void setTag(String tag) {
        this.tag = tag;
    }
}

