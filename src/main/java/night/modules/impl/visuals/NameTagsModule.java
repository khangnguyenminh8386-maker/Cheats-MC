/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package night.modules.impl.visuals;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;


import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IFreecamModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.modules.impl.visuals.LogoutSpotModule;
import night.modules.impl.visuals.PopChamsModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import org.joml.Quaternionfc;

@RegisterModule(name="NameTags", description="Replaces the default Minecraft NameTag with a more visible and customizable one.", category=Module.Category.VISUALS)
public class NameTagsModule
extends Module {
    private static final Identifier LOGO_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/gui/logo.png");
    public BooleanSetting gameMode = new BooleanSetting("GameMode", "Renders the player's gamemode.", false);
    public BooleanSetting ping = new BooleanSetting("Ping", "Renders the player's latency to the server.", true);
    public BooleanSetting entityId = new BooleanSetting("EntityID", "Renders the player's entity ID.", false);
    public BooleanSetting health = new BooleanSetting("Health", "Renders the player's health and absorption.", true);
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the amount of totems that the player has popped.", true);
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having nametags rendered for them.", false);
    public BooleanSetting nightCheck = new BooleanSetting("Cheats MCCheck", "Adds an indicator next to the name of other Cheats MC users.", true);
    public BooleanSetting self = new BooleanSetting("Self", "Also renders a nametag above your own player.", false);
    public BooleanSetting items = new BooleanSetting("Items", "Renders the items that the player is wearing or holding.", true);
    public BooleanSetting enchantments = new BooleanSetting("Enchantments", "Renders the enchantments of the player's items.", false);
    public BooleanSetting durability = new BooleanSetting("Durability", "Renders the durability of the player's items.", true);
    public BooleanSetting itemName = new BooleanSetting("ItemName", "Renders the name of the item that the player is currently holding.", true);
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around nametag text.", false);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the nametag rendering.", 30, 10, 100);
    public ModeSetting border = new ModeSetting("Border", "The border that will surround the text.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.border, "Fill", "Both"), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.border, "Outline", "Both"), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
    private static final Map<ItemEnchantments, List<String>> ENCHANT_CACHE = new ConcurrentHashMap<ItemEnchantments, List<String>>();
    private static final Map<Holder<Enchantment>, String> NAME_CACHE = new ConcurrentHashMap<Holder<Enchantment>, String>();

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post event) {
        if (NameTagsModule.mc.level == null || NameTagsModule.mc.player == null) {
            return;
        }
        PoseStack matrices = event.getMatrices();
        for (Player player : NameTagsModule.mc.level.players().stream().sorted(Comparator.comparing(p -> Float.valueOf(-NameTagsModule.mc.player.distanceTo((Entity)p)))).toList()) {
            boolean freecam = ((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled();
            if (player == NameTagsModule.mc.player && (!this.self.getValue() || NameTagsModule.mc.options.getCameraType().isFirstPerson() && !freecam) || this.antiBot.getValue() && EntityUtils.isBot(player)) continue;
            if (player instanceof RemotePlayer) {
                RemotePlayer ghost = (RemotePlayer)player;
                if (Night.MODULE_MANAGER.getModule(PopChamsModule.class) != null && Night.MODULE_MANAGER.getModule(PopChamsModule.class).isGhost((Entity)ghost) || Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) != null && Night.MODULE_MANAGER.getModule(LogoutSpotModule.class).isGhost((Entity)ghost)) continue;
            }
            if (!Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            double x = Mth.lerp((double)event.getTickDelta(), (double)player.xo, (double)player.getX());
            double y = Mth.lerp((double)event.getTickDelta(), (double)player.yo, (double)player.getY()) + (double)(player.isShiftKeyDown() ? 1.9f : 2.1f);
            double z = Mth.lerp((double)event.getTickDelta(), (double)player.zo, (double)player.getZ());
            Vec3 vec3d = new Vec3(x - NameTagsModule.mc.gameRenderer.mainCamera().position().x, y - NameTagsModule.mc.gameRenderer.mainCamera().position().y, z - NameTagsModule.mc.gameRenderer.mainCamera().position().z);
            float distance = (float)Math.sqrt(NameTagsModule.mc.gameRenderer.mainCamera().position().distanceToSqr(x, y, z));
            float scaling = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * distance;
            if ((double)distance <= 8.0) {
                scaling = 0.0245f;
            }
            matrices.pushPose();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.mulPose((Quaternionfc)NameTagsModule.mc.gameRenderer.mainCamera().rotation());
            matrices.scale(scaling, -scaling, scaling);
            boolean isNight = this.nightCheck.getValue() && Night.NIGHT_USER_MANAGER != null && Night.NIGHT_USER_MANAGER.isNightUser(player);
            Object text = player.getName().getString();
            if (this.gameMode.getValue()) {
                text = (String)text + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
            }
            if (this.ping.getValue()) {
                text = (String)text + " " + EntityUtils.getLatency(player) + "ms";
            }
            if (this.entityId.getValue()) {
                text = (String)text + " " + player.getId();
            }
            if (this.health.getValue()) {
                int hp = Math.round(player.getHealth() + player.getAbsorptionAmount());
                text = (String)text + " " + String.valueOf(ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount())) + hp + String.valueOf(ChatFormatting.RESET);
            }
            int pops = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
            if (this.totemPops.getValue() && pops > 0) {
                text = (String)text + " " + String.valueOf(ColorUtils.getTotemColor(pops)) + "-" + pops;
            }
            int textWidth = Night.FONT_MANAGER.getWidth((String)text);
            int fontHeight = Night.FONT_MANAGER.getHeight();
            int logoSize = isNight ? fontHeight - 1 : 0;
            int logoSpacing = isNight ? 3 : 0;
            int totalWidth = textWidth + logoSize + logoSpacing;
            if (this.border.getValue().equalsIgnoreCase("Fill") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderQuad(matrices, (float)(-totalWidth) / 2.0f - 2.0f, -fontHeight - 1, (float)totalWidth / 2.0f + 2.0f, 1.0f, this.fillColor.getColor());
            }
            if (this.border.getValue().equalsIgnoreCase("Outline") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderOutline(matrices, (float)(-totalWidth) / 2.0f - 2.0f, -fontHeight - 1, (float)totalWidth / 2.0f + 2.0f, 1.0f, this.outlineColor.getColor());
            }
            float startX = (float)(-totalWidth) / 2.0f;
            if (isNight) {
                Renderer3D.renderTexture(matrices, LOGO_ID, startX, (float)(-fontHeight) + 0.5f, startX + (float)logoSize, 0.5f, -1);
                startX += (float)(logoSize + logoSpacing);
            }
            Color nameColor = Night.MODULE_MANAGER.getModule(FakePlayerModule.class).isToggled() && Night.MODULE_MANAGER.getModule(FakePlayerModule.class).getPlayer() == player ? new Color(225, 0, 70) : (player.isShiftKeyDown() ? new Color(255, 170, 0) : (Night.FRIEND_MANAGER.contains(player.getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : Color.WHITE));
            this.drawText(matrices, (String)text, (int)startX, -fontHeight, nameColor);
            float highestY = -fontHeight - 2;
            if (this.items.getValue()) {
                ItemStack[] playerItems = new ItemStack[6];
                List[] itemEnchants = new List[6];
                int maxEnchantsCount = 0;
                boolean hasAnyItem = false;
                for (int i = 0; i < 6; ++i) {
                    List<String> enchList;
                    ItemStack stack;
                    playerItems[i] = stack = this.getItem(player, i);
                    if (stack.isEmpty()) continue;
                    hasAnyItem = true;
                    if (!this.enchantments.getValue()) continue;
                    itemEnchants[i] = enchList = this.getCachedEnchantments(stack);
                    if (enchList.size() <= maxEnchantsCount) continue;
                    maxEnchantsCount = enchList.size();
                }
                if (hasAnyItem) {
                    int stackX;
                    ItemStack stack;
                    int i;
                    float itemRowY;
                    float maxEnchantsHeight = (float)maxEnchantsCount * 4.2f;
                    float itemContentHeight = Math.max(16.0f, maxEnchantsHeight);
                    highestY = itemRowY = (float)(-fontHeight) - 4.0f - itemContentHeight;
                    for (i = 0; i < 6; ++i) {
                        stack = playerItems[i];
                        if (stack == null || stack.isEmpty()) continue;
                        stackX = -54 + i * 18 + 1;
                        matrices.pushPose();
                        matrices.translate((float)(stackX + 8), itemRowY + 8.0f, -0.5f);
                        matrices.scale(16.0f, -16.0f, 1.0E-4f);
                        Renderer3D.renderItem(matrices, stack, (ItemOwner)player);
                        matrices.popPose();
                    }
                    for (i = 0; i < 6; ++i) {
                        stack = playerItems[i];
                        if (stack == null || stack.isEmpty()) continue;
                        stackX = -54 + i * 18 + 1;
                        if (this.durability.getValue() && stack.isDamageableItem()) {
                            float green = (float)(stack.getMaxDamage() - stack.getDamageValue()) / (float)stack.getMaxDamage();
                            float red = 1.0f - green;
                            String durText = Math.round((float)(stack.getMaxDamage() - stack.getDamageValue()) * 100.0f / (float)stack.getMaxDamage()) + "%";
                            float durWidth = (float)Night.FONT_MANAGER.getWidth(durText) * 0.5f;
                            matrices.pushPose();
                            matrices.translate((float)(stackX + 8) - durWidth / 2.0f, itemRowY - 5.5f, 1.0f);
                            matrices.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(matrices, durText, 0.0f, 0.0f, new Color(red, green, 0.0f));
                            matrices.popPose();
                        }
                        if (stack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                            matrices.pushPose();
                            matrices.translate((float)(stackX + 1), itemRowY + 1.0f, 1.0f);
                            matrices.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(matrices, "God", 0.0f, 0.0f, new Color(255, 125, 255));
                            matrices.popPose();
                        }
                        if (stack.getCount() != 1) {
                            String count = String.valueOf(stack.getCount());
                            float countWidth = Night.FONT_MANAGER.getWidth(count);
                            matrices.pushPose();
                            matrices.translate((float)(stackX + 16) - countWidth, itemRowY + 7.0f, 1.0f);
                            matrices.scale(1.0f, 1.0f, 1.0f);
                            this.drawText(matrices, count, 0.0f, 0.0f, Color.WHITE);
                            matrices.popPose();
                        }
                        if (!this.enchantments.getValue() || itemEnchants[i] == null || itemEnchants[i].isEmpty()) continue;
                        List enchList = itemEnchants[i];
                        float enchLineStep = 4.2f;
                        for (int enchIndex = 0; enchIndex < enchList.size(); ++enchIndex) {
                            String str = (String)enchList.get(enchIndex);
                            float enchY = itemRowY + 8.0f + (float)enchIndex * enchLineStep;
                            matrices.pushPose();
                            matrices.translate((float)(stackX + 1), enchY, 1.0f);
                            matrices.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(matrices, str, 0.0f, 0.0f, Color.WHITE);
                            matrices.popPose();
                        }
                    }
                    if (this.durability.getValue()) {
                        highestY = itemRowY - 6.0f;
                    }
                }
            }
            if (this.itemName.getValue() && !player.getMainHandItem().isEmpty()) {
                String itemText = player.getMainHandItem().getHoverName().getString();
                float itemTextWidth = (float)Night.FONT_MANAGER.getWidth(itemText) * 0.5f;
                float nameY = highestY - (float)fontHeight * 0.5f - 2.0f;
                matrices.pushPose();
                matrices.translate(-itemTextWidth / 2.0f, nameY, 1.0f);
                matrices.scale(0.5f, 0.5f, 1.0f);
                this.drawText(matrices, itemText, 0.0f, 0.0f, Color.WHITE);
                matrices.popPose();
            }
            matrices.popPose();
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
        }
    }

    private void drawText(PoseStack matrices, String text, float x, float y, Color color) {
        if (this.glow.getValue()) {
            Night.FONT_MANAGER.drawText(matrices, text, x, y, color, true);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(matrices, text, x, y, color, false);
        }
    }

   private List<String> getCachedEnchantments(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         ItemEnchantments component = stack.get(DataComponents.ENCHANTMENTS);
         return component != null && !component.isEmpty() ? ENCHANT_CACHE.computeIfAbsent(component, c -> {
            List<String> list = new ArrayList<>(c.size());

            for (Entry<Holder<Enchantment>> entry : c.entrySet()) {
               String shortName = NAME_CACHE.computeIfAbsent(entry.getKey(), NameTagsModule::parseShortName);
               int level = entry.getIntValue();
               list.add(level > 1 ? shortName + " " + level : shortName);
            }

            return list;
         }) : Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

    private static String parseShortName(Holder<Enchantment> holder) {
        String id;
        String reg = holder.getRegisteredName();
        int colon = reg.indexOf(58);
        String string = id = colon != -1 ? reg.substring(colon + 1) : reg;
        String shortName = id.startsWith("protection") ? "Pro" : (id.startsWith("blast_protection") ? "Bla" : (id.startsWith("fire_protection") ? "Fir" : (id.startsWith("projectile_protection") ? "Proj" : (id.startsWith("unbreaking") ? "Unb" : (id.startsWith("mending") ? "Men" : (id.startsWith("thorns") ? "Tho" : (id.startsWith("respiration") ? "Res" : (id.startsWith("aqua_affinity") ? "Aqu" : (id.startsWith("depth_strider") ? "Dep" : (id.startsWith("feather_falling") ? "Fea" : (id.startsWith("soul_speed") ? "Soul" : (id.startsWith("swift_sneak") ? "Sne" : (id.startsWith("sharpness") ? "Sha" : (id.startsWith("smite") ? "Smi" : (id.startsWith("bane_of_arthropods") ? "Boa" : (id.startsWith("knockback") ? "Kb" : (id.startsWith("fire_aspect") ? "FA" : (id.startsWith("looting") ? "Loot" : (id.startsWith("sweeping_edge") ? "Swp" : (id.startsWith("efficiency") ? "Eff" : (id.startsWith("silk_touch") ? "Silk" : (id.startsWith("fortune") ? "Fort" : (id.startsWith("power") ? "Pow" : (id.startsWith("punch") ? "Pun" : (id.startsWith("flame") ? "Fla" : (id.startsWith("infinity") ? "Inf" : (id.startsWith("loyalty") ? "Loy" : (id.startsWith("impaling") ? "Imp" : (id.startsWith("riptide") ? "Rip" : (id.startsWith("channeling") ? "Chan" : (id.startsWith("multishot") ? "Multi" : (id.startsWith("quick_charge") ? "Quick" : (id.startsWith("piercing") ? "Pier" : (id.startsWith("density") ? "Den" : (id.startsWith("breach") ? "Bre" : (id.startsWith("wind_burst") ? "Wind" : (id.startsWith("vanishing_curse") ? "Van" : (id.startsWith("binding_curse") ? "Bind" : (id.length() > 3 ? id.substring(0, 3) : id)))))))))))))))))))))))))))))))))))))));
        return shortName.substring(0, 1).toUpperCase() + shortName.substring(1);
    }

    private ItemStack getItem(Player player, int index) {
        return switch (index) {
            case 0 -> player.getMainHandItem();
            case 1 -> player.getItemBySlot(EquipmentSlot.HEAD);
            case 2 -> player.getItemBySlot(EquipmentSlot.CHEST);
            case 3 -> player.getItemBySlot(EquipmentSlot.LEGS);
            case 4 -> player.getItemBySlot(EquipmentSlot.FEET);
            case 5 -> player.getOffhandItem();
            default -> ItemStack.EMPTY;
        };
    }
}

