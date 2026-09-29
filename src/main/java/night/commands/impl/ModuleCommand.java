/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.Block
 */
package night.commands.impl;

import java.awt.Color;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.Module;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.chat.ChatUtils;
import night.utils.color.ColorUtils;
import night.utils.input.KeyboardUtils;
import night.utils.minecraft.IdentifierUtils;
import night.utils.miscellaneous.ListUtils;

@RegisterCommand(name="module", tag="Module", description="Allows you to view and change this module's settings.", syntax="<[setting]> <[...]> | <list|reset>")
public class ModuleCommand
extends Command {
    private final Module module;

    public ModuleCommand(Module module) {
        this.module = module;
        this.setName(module.getName().toLowerCase());
        this.setTag(module.getName());
    }

    @Override
   public List<String> getSuggestions(String[] args) {
      if (args.length == 0) {
         List<String> names = new ArrayList<>();
         names.add("list");
         names.add("reset");

         for (Setting setting : this.module.getSettings()) {
            if (!(setting instanceof CategorySetting)) {
               names.add(setting.getName().toLowerCase());
            }
         }

         return names;
      } else if (args.length == 1) {
         Setting uncastedSetting = this.module.getSetting(args[0]);
         if (uncastedSetting == null) {
            return List.of();
         }

         return switch (uncastedSetting) {
            case BooleanSetting ignored -> List.of("true", "false", "reset");
            case NumberSetting ignored -> List.of("reset");
            case ModeSetting setting -> {
               List<String> modes = new ArrayList<>(setting.getModes());
               modes.add("reset");
               modes.add("list");
               yield modes;
            }
            case StringSetting ignored -> List.of("force-reset");
            case BindSetting ignored -> List.of("reset");
            case ColorSetting ignored -> List.of("red", "green", "blue", "alpha", "sync", "rainbow", "code", "reset");
            case WhitelistSetting ignored -> List.of("add", "del", "clear", "list");
            default -> List.of();
         };
      } else {
         if (args.length == 2 && this.module.getSetting(args[0]) instanceof ColorSetting) {
            String channel = args[1].toLowerCase();
            if (channel.equals("sync") || channel.equals("rainbow")) {
               return List.of("true", "false");
            }
         }

         return List.of();
      }
   }

    @Override
   public void execute(String[] args) {
      if (args.length >= 1) {
         if (args[0].equalsIgnoreCase("list")) {
            if (this.module.getSettings().isEmpty()) {
               Night.CHAT_MANAGER.tagged("This module currently has no registered settings.", this.module.getName(), "module-cmd-" + this.getName() + "-list");
            } else {
               StringBuilder builder = new StringBuilder();
               int index = 0;

               for (Setting setting : this.module.getSettings()) {
                  if (!(setting instanceof CategorySetting)) {
                     builder.append(ChatUtils.getSecondary())
                        .append(setting.getName())
                        .append(ChatUtils.getPrimary())
                        .append(" [")
                        .append(ChatUtils.getSecondary());
                     if (setting instanceof BooleanSetting) {
                        builder.append(((BooleanSetting)setting).getValue());
                     }

                     if (setting instanceof NumberSetting) {
                        builder.append(((NumberSetting)setting).getValue());
                     }

                     if (setting instanceof ModeSetting) {
                        builder.append(((ModeSetting)setting).getValue());
                     }

                     if (setting instanceof StringSetting) {
                        builder.append(((StringSetting)setting).getValue());
                     }

                     if (setting instanceof BindSetting) {
                        builder.append(KeyboardUtils.getKeyName(((BindSetting)setting).getValue()).toUpperCase());
                     }

                     if (setting instanceof WhitelistSetting) {
                        builder.append("...");
                     }

                     if (setting instanceof ColorSetting) {
                        builder.append("RGBA(")
                           .append(((ColorSetting)setting).getColor().getRed())
                           .append(ChatUtils.getPrimary())
                           .append(", ")
                           .append(ChatUtils.getSecondary())
                           .append(((ColorSetting)setting).getColor().getGreen())
                           .append(ChatUtils.getPrimary())
                           .append(", ")
                           .append(ChatUtils.getSecondary())
                           .append(((ColorSetting)setting).getColor().getBlue())
                           .append(ChatUtils.getPrimary())
                           .append(", ")
                           .append(ChatUtils.getSecondary())
                           .append(((ColorSetting)setting).getColor().getAlpha())
                           .append(")")
                           .append(ChatUtils.getPrimary())
                           .append(",")
                           .append(ChatUtils.getSecondary())
                           .append(" SyRa(")
                           .append(((ColorSetting)setting).isSync())
                           .append(ChatUtils.getPrimary())
                           .append(", ")
                           .append(ChatUtils.getSecondary())
                           .append(((ColorSetting)setting).isRainbow())
                           .append(")");
                     }

                     builder.append(ChatUtils.getPrimary())
                        .append("]")
                        .append(ChatUtils.getSecondary())
                        .append(index + 1 == this.module.getSettings().size() ? "" : ", ");
                     index++;
                  }
               }

               Night.CHAT_MANAGER
                  .message(
                     ChatUtils.getSecondary()
                        + this.module.getName()
                        + " "
                        + ChatUtils.getPrimary()
                        + "["
                        + ChatUtils.getSecondary()
                        + this.module.getSettings().size()
                        + ChatUtils.getPrimary()
                        + "]: "
                        + ChatUtils.getSecondary()
                        + builder,
                     "module-cmd-" + this.getName() + "-list"
                  );
            }
         } else if (args[0].equalsIgnoreCase("reset")) {
            this.module.resetValues();
            Night.CHAT_MANAGER.tagged("Successfully reset all of the module's settings.", this.module.getName(), "module-cmd-" + this.getName());
         } else {
            Setting uncastedSetting = this.module.getSetting(args[0]);
            if (uncastedSetting == null) {
               Night.CHAT_MANAGER.tagged("Could not find the setting specified.", this.module.getName(), "module-cmd-" + this.getName());
               return;
            }

            args = Arrays.copyOfRange(args, 1, args.length);
            switch (uncastedSetting) {
               case BooleanSetting setting:
                  if (setting == this.module.chatNotify) {
                     Night.CHAT_MANAGER
                        .tagged(
                           "This setting is the module's main ChatNotify setting. Please use the "
                              + ChatUtils.getPrimary()
                              + "chatnotify"
                              + ChatUtils.getSecondary()
                              + " command instead.",
                           this.module.getName(),
                           this.getName()
                        );
                     return;
                  }

                  if (setting == this.module.drawn) {
                     Night.CHAT_MANAGER
                        .tagged(
                           "This setting is the module's main Drawn setting. Please use the "
                              + ChatUtils.getPrimary()
                              + "drawn"
                              + ChatUtils.getSecondary()
                              + " command instead.",
                           this.module.getName(),
                           this.getName()
                        );
                     return;
                  }

                  if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("reset")) {
                        setting.resetValue();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else {
                        setting.setValue(Boolean.parseBoolean(args[0]));
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully set "
                                 + ChatUtils.getPrimary()
                                 + setting.getName()
                                 + ChatUtils.getSecondary()
                                 + " to "
                                 + ChatUtils.getPrimary()
                                 + setting.getValue()
                                 + ChatUtils.getSecondary()
                                 + ".",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                  }
                  break;
               case NumberSetting setting:
                  if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("reset")) {
                        setting.resetValue();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else {
                        try {
                           switch (setting.getType()) {
                              case LONG:
                                 setting.setValue(Long.parseLong(args[0]));
                                 break;
                              case DOUBLE:
                                 setting.setValue(Double.parseDouble(args[0]));
                                 break;
                              case FLOAT:
                                 setting.setValue(Float.parseFloat(args[0]));
                                 break;
                              default:
                                 setting.setValue(Integer.parseInt(args[0]));
                           }

                           Night.CHAT_MANAGER
                              .tagged(
                                 "Successfully set "
                                    + ChatUtils.getPrimary()
                                    + setting.getName()
                                    + ChatUtils.getSecondary()
                                    + " to "
                                    + ChatUtils.getPrimary()
                                    + setting.getValue()
                                    + ChatUtils.getSecondary()
                                    + ".",
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        } catch (NumberFormatException exception) {
                           Night.CHAT_MANAGER
                              .tagged(
                                 "Please input a valid "
                                    + ChatUtils.getPrimary()
                                    + setting.getType().name().toLowerCase()
                                    + ChatUtils.getSecondary()
                                    + " number.",
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        }
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                  }
                  break;
               case ModeSetting setting:
                  if (args.length >= 1) {
                     if ((!args[0].equalsIgnoreCase("reset") || !setting.getModes().stream().noneMatch("reset"::equalsIgnoreCase))
                        && !args[0].equalsIgnoreCase("force-reset")) {
                        if ((!args[0].equalsIgnoreCase("list") || !setting.getModes().stream().noneMatch("list"::equalsIgnoreCase))
                           && !args[0].equalsIgnoreCase("force-list")) {
                           if (args[0].equalsIgnoreCase("reset") && setting.getModes().stream().anyMatch("reset"::equalsIgnoreCase)) {
                              Night.CHAT_MANAGER
                                 .info(
                                    "If you would like to reset this setting's value, write \""
                                       + ChatUtils.getPrimary()
                                       + "force-reset"
                                       + ChatUtils.getSecondary()
                                       + "\" instead."
                                 );
                           }

                           if (args[0].equalsIgnoreCase("list") && setting.getModes().stream().anyMatch("list"::equalsIgnoreCase)) {
                              Night.CHAT_MANAGER
                                 .info(
                                    "If you would like to view a list of valid values for this setting, write \""
                                       + ChatUtils.getPrimary()
                                       + "force-list"
                                       + ChatUtils.getSecondary()
                                       + "\" instead."
                                 );
                           }

                           StringBuilder builder = new StringBuilder();
                           int index = 0;

                           for (String str : args) {
                              builder.append(str).append(index + 1 == args.length ? "" : " ");
                              index++;
                           }

                           if (setting.getModes().stream().anyMatch(builder.toString()::equalsIgnoreCase)) {
                              setting.setValue(setting.getModes().get(ListUtils.getIndex(setting.getModes(), builder.toString())));
                              Night.CHAT_MANAGER
                                 .tagged(
                                    "Successfully set "
                                       + ChatUtils.getPrimary()
                                       + setting.getName()
                                       + ChatUtils.getSecondary()
                                       + " to "
                                       + ChatUtils.getPrimary()
                                       + setting.getValue()
                                       + ChatUtils.getSecondary()
                                       + ".",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           } else {
                              Night.CHAT_MANAGER.tagged("Please input a valid value for this setting.", this.module.getName(), "module-cmd-" + this.getName());
                           }
                        } else if (setting.getModes().isEmpty()) {
                           Night.CHAT_MANAGER
                              .tagged(
                                 ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " currently has no values registered.",
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        } else {
                           StringBuilder modesString = new StringBuilder();
                           int index = 0;

                           for (String str : setting.getModes()) {
                              modesString.append(ChatUtils.getSecondary()).append(str).append(index + 1 == setting.getModes().size() ? "" : ", ");
                              index++;
                           }

                           Night.CHAT_MANAGER
                              .tagged(
                                 ChatUtils.getSecondary()
                                    + setting.getName()
                                    + " "
                                    + ChatUtils.getPrimary()
                                    + "["
                                    + ChatUtils.getSecondary()
                                    + setting.getModes().size()
                                    + ChatUtils.getPrimary()
                                    + "]: "
                                    + ChatUtils.getSecondary()
                                    + modesString,
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        }
                     } else {
                        setting.resetValue();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset|list>");
                  }
                  break;
               case StringSetting setting:
                  if (args.length >= 1) {
                     if (args[0].equalsIgnoreCase("force-reset")) {
                        setting.setValue(setting.getDefaultValue());
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else {
                        if (args[0].equalsIgnoreCase("reset")) {
                           Night.CHAT_MANAGER
                              .info(
                                 "If you would like to reset this setting's value, write \""
                                    + ChatUtils.getPrimary()
                                    + "force-reset"
                                    + ChatUtils.getSecondary()
                                    + "\" instead."
                              );
                        }

                        StringBuilder builder = new StringBuilder();
                        int index = 0;

                        for (String str : args) {
                           builder.append(str).append(index + 1 == args.length ? "" : " ");
                           index++;
                        }

                        setting.setValue(builder.toString());
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully set "
                                 + ChatUtils.getPrimary()
                                 + setting.getName()
                                 + ChatUtils.getSecondary()
                                 + " to "
                                 + ChatUtils.getPrimary()
                                 + setting.getValue()
                                 + ChatUtils.getSecondary()
                                 + ".",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                  }
                  break;
               case BindSetting setting:
                  if (uncastedSetting == this.module.bind) {
                     Night.CHAT_MANAGER
                        .tagged(
                           "This setting is the module's main toggle keybind. Please use the "
                              + ChatUtils.getPrimary()
                              + "bind"
                              + ChatUtils.getSecondary()
                              + " command instead.",
                           this.module.getName(),
                           "module-cmd-" + this.getName()
                        );
                     return;
                  }

                  if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("reset")) {
                        setting.resetValue();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else {
                        int key = 0;

                        try {
                           key = KeyboardUtils.getKeyNumber(args[0]);
                        } catch (IllegalArgumentException var22) {
                        }

                        setting.setValue(key);
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully set "
                                 + ChatUtils.getPrimary()
                                 + setting.getName()
                                 + ChatUtils.getSecondary()
                                 + " to "
                                 + ChatUtils.getPrimary()
                                 + KeyboardUtils.getKeyName(setting.getValue()).toUpperCase()
                                 + ChatUtils.getSecondary()
                                 + ".",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                  }
                  break;
               case ColorSetting setting:
                  if (args.length == 2) {
                     int parsedValue = 0;
                     if (args[0].equalsIgnoreCase("red")
                        || args[0].equalsIgnoreCase("green")
                        || args[0].equalsIgnoreCase("blue")
                        || args[0].equalsIgnoreCase("alpha")) {
                        try {
                           parsedValue = Math.clamp(Integer.parseInt(args[1]), 0, 255);
                        } catch (NumberFormatException ignored) {
                           Night.CHAT_MANAGER
                              .tagged(
                                 "Please input a valid number for the " + ChatUtils.getPrimary() + args[0].toLowerCase() + ChatUtils.getSecondary() + " value.",
                                 this.module.getName()
                              );
                           return;
                        }
                     }

                     String valueName = "";
                     String newValue = "";
                     if (args[0].equalsIgnoreCase("red")) {
                        setting.setColor(
                           new Color(
                              parsedValue,
                              setting.getValue().getColor().getGreen(),
                              setting.getValue().getColor().getBlue(),
                              setting.getValue().getColor().getAlpha()
                           )
                        );
                        valueName = "red";
                        newValue = String.valueOf(setting.getValue().getColor().getRed());
                     } else if (args[0].equalsIgnoreCase("green")) {
                        setting.setColor(
                           new Color(
                              setting.getValue().getColor().getRed(),
                              parsedValue,
                              setting.getValue().getColor().getBlue(),
                              setting.getValue().getColor().getAlpha()
                           )
                        );
                        valueName = "green";
                        newValue = String.valueOf(setting.getValue().getColor().getGreen());
                     } else if (args[0].equalsIgnoreCase("blue")) {
                        setting.setColor(
                           new Color(
                              setting.getValue().getColor().getRed(),
                              setting.getValue().getColor().getGreen(),
                              parsedValue,
                              setting.getValue().getColor().getAlpha()
                           )
                        );
                        valueName = "blue";
                        newValue = String.valueOf(setting.getValue().getColor().getBlue());
                     } else if (args[0].equalsIgnoreCase("alpha")) {
                        setting.setColor(
                           new Color(
                              setting.getValue().getColor().getRed(),
                              setting.getValue().getColor().getGreen(),
                              setting.getValue().getColor().getBlue(),
                              parsedValue
                           )
                        );
                        valueName = "alpha";
                        newValue = String.valueOf(setting.getValue().getColor().getAlpha());
                     } else if (args[0].equalsIgnoreCase("sync")) {
                        setting.setSync(Boolean.parseBoolean(args[1]));
                        valueName = "sync";
                        newValue = String.valueOf(setting.isSync());
                     } else if (args[0].equalsIgnoreCase("rainbow")) {
                        setting.setRainbow(Boolean.parseBoolean(args[1]));
                        valueName = "rainbow";
                        newValue = String.valueOf(setting.isRainbow());
                     } else if (args[0].equalsIgnoreCase("code")) {
                        if (!ColorUtils.isValidColorCode(args[1])) {
                           Night.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), "module-cmd-" + this.getName());
                           return;
                        }

                        try {
                           Color decoded = Color.decode((args[1].startsWith("#") ? "" : "#") + args[1]);
                           setting.setColor(new Color(decoded.getRed(), decoded.getGreen(), decoded.getBlue()));
                        } catch (NumberFormatException exception) {
                           Night.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), "module-cmd-" + this.getName());
                           return;
                        }

                        valueName = "color";
                        newValue = "rgba("
                           + setting.getValue().getColor().getRed()
                           + ", "
                           + setting.getValue().getColor().getGreen()
                           + ", "
                           + setting.getValue().getColor().getBlue()
                           + ", "
                           + setting.getValue().getColor().getAlpha()
                           + ")";
                     } else {
                        Night.CHAT_MANAGER
                           .info(
                              this.module.getName().toLowerCase()
                                 + " "
                                 + setting.getName().toLowerCase()
                                 + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"
                           );
                     }

                     Night.CHAT_MANAGER
                        .tagged(
                           "Successfully set the "
                              + ChatUtils.getPrimary()
                              + valueName
                              + ChatUtils.getSecondary()
                              + " value to "
                              + ChatUtils.getPrimary()
                              + newValue
                              + ChatUtils.getSecondary()
                              + ".",
                           this.module.getName(),
                           "module-cmd-" + this.getName()
                        );
                  } else if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("reset")) {
                        setting.resetValue();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else {
                        Night.CHAT_MANAGER
                           .info(
                              this.module.getName().toLowerCase()
                                 + " "
                                 + setting.getName().toLowerCase()
                                 + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"
                           );
                     }
                  } else {
                     Night.CHAT_MANAGER
                        .info(
                           this.module.getName().toLowerCase()
                              + " "
                              + setting.getName().toLowerCase()
                              + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"
                        );
                  }
                  break;
               case WhitelistSetting setting:
                  if (args.length == 2) {
                     if (args[0].equalsIgnoreCase("add")) {
                        if (setting.getType() == WhitelistSetting.Type.ITEMS) {
                           Item item = IdentifierUtils.getItem(args[1]);
                           if (item == null) {
                              Night.CHAT_MANAGER.tagged("Please input a valid item ID.", this.module.getName(), "module-cmd-" + this.getName());
                              return;
                           }

                           if (setting.isWhitelistContains(item)) {
                              Night.CHAT_MANAGER
                                 .tagged(
                                    ChatUtils.getPrimary()
                                       + item.getName(item.getDefaultInstance()).getString()
                                       + ChatUtils.getSecondary()
                                       + " is already on the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           } else {
                              setting.add(item);
                              Night.CHAT_MANAGER
                                 .tagged(
                                    "Successfully added "
                                       + ChatUtils.getPrimary()
                                       + item.getName(item.getDefaultInstance()).getString()
                                       + ChatUtils.getSecondary()
                                       + " to the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           }
                        } else if (setting.getType() == WhitelistSetting.Type.BLOCKS) {
                           Block block = IdentifierUtils.getBlock(args[1]);
                           if (block == null) {
                              Night.CHAT_MANAGER.tagged("Please input a valid block ID.", this.module.getName(), "module-cmd-" + this.getName());
                              return;
                           }

                           if (setting.isWhitelistContains(block)) {
                              Night.CHAT_MANAGER
                                 .tagged(
                                    ChatUtils.getPrimary() + block.getName().getString() + ChatUtils.getSecondary() + " is already on the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           } else {
                              setting.add(block);
                              Night.CHAT_MANAGER
                                 .tagged(
                                    "Successfully added "
                                       + ChatUtils.getPrimary()
                                       + block.getName().getString()
                                       + ChatUtils.getSecondary()
                                       + " to the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           }
                        } else {
                           Night.CHAT_MANAGER.error("Something went wrong while detecting the setting's type.");
                        }
                     } else if (args[0].equalsIgnoreCase("del")) {
                        Item item;
                        if (setting.getType() == WhitelistSetting.Type.ITEMS
                           && (item = IdentifierUtils.getItem(args[1])) != null
                           && setting.isWhitelistContains(item)) {
                           setting.remove(item);
                           Night.CHAT_MANAGER
                              .tagged(
                                 "Successfully removed "
                                    + ChatUtils.getPrimary()
                                    + item.getName(item.getDefaultInstance()).getString()
                                    + ChatUtils.getSecondary()
                                    + " from the whitelist.",
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        } else {
                           Block block;
                           if (setting.getType() == WhitelistSetting.Type.BLOCKS
                              && (block = IdentifierUtils.getBlock(args[1])) != null
                              && setting.isWhitelistContains(block)) {
                              setting.remove(block);
                              Night.CHAT_MANAGER
                                 .tagged(
                                    "Successfully removed "
                                       + ChatUtils.getPrimary()
                                       + block.getName().getString()
                                       + ChatUtils.getSecondary()
                                       + " from the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           } else {
                              Night.CHAT_MANAGER
                                 .tagged(
                                    ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is not on the whitelist.",
                                    this.module.getName(),
                                    "module-cmd-" + this.getName()
                                 );
                           }
                        }
                     } else {
                        Night.CHAT_MANAGER
                           .info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <list|clear>");
                     }
                  } else if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("clear")) {
                        setting.getWhitelist().clear();
                        Night.CHAT_MANAGER
                           .tagged(
                              "Successfully cleared the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " whitelist.",
                              this.module.getName(),
                              "module-cmd-" + this.getName()
                           );
                     } else if (args[0].equalsIgnoreCase("list")) {
                        if (setting.getWhitelist().isEmpty()) {
                           Night.CHAT_MANAGER
                              .tagged(
                                 "There are currently no " + (setting.getType() == WhitelistSetting.Type.ITEMS ? "items" : "blocks") + " on the whitelist.",
                                 this.module.getName(),
                                 "module-cmd-" + this.getName()
                              );
                        } else {
                           StringBuilder whitelist = new StringBuilder();
                           int index = 0;

                           for (Object object : setting.getWhitelist()) {
                              whitelist.append(ChatUtils.getSecondary());
                              switch (object) {
                                 case Block block:
                                    whitelist.append(block.getName().getString());
                                    break;
                                 case Item item:
                                    whitelist.append(item.getName(item.getDefaultInstance()).getString());
                                    break;
                                 default:
                                    whitelist.append("Invalid");
                              }

                              whitelist.append(ChatUtils.getPrimary()).append(index + 1 == setting.getWhitelist().size() ? "" : ", ");
                              index++;
                           }

                           Night.CHAT_MANAGER
                              .message(
                                 ChatUtils.getSecondary()
                                    + setting.getName()
                                    + " "
                                    + ChatUtils.getPrimary()
                                    + "["
                                    + ChatUtils.getSecondary()
                                    + setting.getWhitelist().size()
                                    + ChatUtils.getPrimary()
                                    + "]: "
                                    + ChatUtils.getSecondary()
                                    + whitelist,
                                 "module-cmd-" + this.getName()
                              );
                        }
                     } else {
                        Night.CHAT_MANAGER
                           .info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                     }
                  } else {
                     Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                  }
                  break;
               default:
                  this.messageSyntax();
            }
         }
      } else {
         this.messageSyntax();
      }
   }
}

