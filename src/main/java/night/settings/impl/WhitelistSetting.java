/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.level.block.Block
 */
package night.settings.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import night.settings.Setting;

public class WhitelistSetting
extends Setting {
    private final Type type;
    private final Set<Object> whitelist = new HashSet<Object>();

    public WhitelistSetting(String name, String description, Type type) {
        super(name, name, description, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String name, String tag, String description, Type type) {
        super(name, tag, description, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String name, String description, Setting.Visibility visibility, Type type) {
        super(name, name, description, visibility);
        this.type = type;
    }

    public WhitelistSetting(String name, String tag, String description, Setting.Visibility visibility, Type type) {
        super(name, tag, description, visibility);
        this.type = type;
    }

    public void add(Object object) {
        this.whitelist.add(object);
    }

    public void remove(Object id) {
        this.whitelist.remove(id);
    }

    public boolean isWhitelistContains(Object object) {
        return this.whitelist.contains(object);
    }

    public List<String> getWhitelistIds() {
        return this.whitelist.stream().map(object -> {
            if (object instanceof Item) {
                Item item = (Item)object;
                return BuiltInRegistries.ITEM.getKey(item).toString();
            }
            if (object instanceof Block) {
                Block block = (Block)object;
                return BuiltInRegistries.BLOCK.getKey(block).toString();
            }
            if (object instanceof Potion) {
                Potion potion = (Potion)object;
                return BuiltInRegistries.POTION.getKey(potion).toString();
            }
            return null;
        }).toList();
    }

    public List<Potion> getWhitelistedPotions() {
        return this.whitelist.stream().filter(o -> o instanceof Potion).map(o -> (Potion)o).toList();
    }

    public void clear() {
        this.whitelist.clear();
    }

    @Generated
    public Type getType() {
        return this.type;
    }

    @Generated
    public Set<Object> getWhitelist() {
        return this.whitelist;
    }

    public static enum Type {
        ITEMS,
        BLOCKS,
        POTIONS;

    }
}

