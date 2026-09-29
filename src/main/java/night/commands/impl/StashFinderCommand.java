/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.List;
import java.util.Map;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.impl.visuals.StashFinderModule;
import night.modules.impl.visuals.stashfinder.StashWebhook;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="stashfinder", aliases={"stash"}, tag="StashFinder", description="Configures StashFinder's Discord webhook and controls scan state.", syntax="<webhook <url> | userid <id> | test | reset>")
public class StashFinderCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("webhook", "userid", "test", "reset");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.messageSyntax();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "webhook": {
                if (args.length < 2) {
                    Night.CHAT_MANAGER.tagged("Current webhook: " + String.valueOf(ChatUtils.getPrimary()) + (StashWebhook.getWebhookUrl().isBlank() ? "None" : StashWebhook.getWebhookUrl()), this.getTag());
                    return;
                }
                String url = args[1];
                StashWebhook.setWebhookUrl(url);
                StashFinderModule module = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (module != null) {
                    module.webhookUrl.setValue(url);
                }
                Night.CHAT_MANAGER.tagged("Webhook URL updated.", this.getTag());
                break;
            }
            case "userid": {
                if (args.length < 2) {
                    Night.CHAT_MANAGER.tagged("Current User ID: " + String.valueOf(ChatUtils.getPrimary()) + (StashWebhook.getUserId().isBlank() ? "None" : StashWebhook.getUserId()), this.getTag());
                    return;
                }
                String id = args[1];
                StashWebhook.setUserId(id);
                StashFinderModule module = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (module != null) {
                    module.userId.setValue(id);
                }
                Night.CHAT_MANAGER.tagged("User ID updated (will be pinged on stash finds).", this.getTag());
                break;
            }
            case "test": {
                String id;
                StashFinderModule module = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                String url = module != null && !module.webhookUrl.getValue().isBlank() ? module.webhookUrl.getValue() : StashWebhook.getWebhookUrl();
                String string = id = module != null && !module.userId.getValue().isBlank() ? module.userId.getValue() : StashWebhook.getUserId();
                if (url.isBlank()) {
                    Night.CHAT_MANAGER.tagged("No webhook URL configured. Use " + String.valueOf(ChatUtils.getPrimary()) + ".stashfinder webhook <url>" + String.valueOf(ChatUtils.getSecondary()) + " first.", this.getTag());
                    break;
                }
                StashWebhook.send(0, 0, "Test Dimension", Map.of("Chests", 25, "Shulkers", 8), url, id);
                Night.CHAT_MANAGER.tagged("Test webhook dispatched.", this.getTag());
                break;
            }
            case "reset": {
                StashFinderModule module = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (module == null) break;
                module.resetCurrentWorldStore();
                Night.CHAT_MANAGER.tagged("Scan history cleared for this world. All chunks will be re-evaluated.", this.getTag());
                break;
            }
            default: {
                this.messageSyntax();
            }
        }
    }
}

