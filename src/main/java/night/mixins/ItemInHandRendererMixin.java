/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.modules.impl.player.SwingModule;
import night.modules.impl.visuals.ShadersModule;
import night.modules.impl.visuals.ViewModelModule;
import night.utils.mixins.HandsRenderState;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemInHandRenderer.class})
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;

    @Inject(method={"submitHandsWithItems"}, at={@At(value="HEAD")})
    private void night$startHands(float partialTick, PoseStack pose, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
        HandsRenderState.renderingHands = true;
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void night$tickViewModel(CallbackInfo ci) {
        ViewModelModule vm;
        ViewModelModule viewModelModule = vm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
        if (vm == null || !vm.isToggled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (!vm.mainhandSwap.getValue()) {
            this.mainHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.mainHandItem = mc.player.getMainHandItem();
        }
        if (!vm.offhandSwap.getValue()) {
            this.offHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandItem = mc.player.getOffhandItem();
        }
    }

    @Inject(method={"submitArmWithItem"}, at={@At(value="HEAD")})
    private void night$applyViewModelTransformHead(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        boolean useMain;
        ViewModelModule vm;
        ViewModelModule viewModelModule = vm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
        if (vm == null || !vm.isToggled()) {
            return;
        }
        poseStack.pushPose();
        boolean bl = useMain = hand == InteractionHand.MAIN_HAND || vm.mode.getValue().equalsIgnoreCase("Both");
        if (useMain) {
            poseStack.translate(vm.positionMainX.getValue().floatValue(), vm.positionMainY.getValue().floatValue(), vm.positionMainZ.getValue().floatValue());
            poseStack.scale(vm.scaleMainX.getValue().floatValue(), vm.scaleMainY.getValue().floatValue(), vm.scaleMainZ.getValue().floatValue());
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(vm.rotationMainX.getValue().floatValue()));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(vm.rotationMainY.getValue().floatValue()));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(vm.rotationMainZ.getValue().floatValue()));
        } else {
            poseStack.translate(vm.positionOffX.getValue().floatValue(), vm.positionOffY.getValue().floatValue(), vm.positionOffZ.getValue().floatValue());
            poseStack.scale(vm.scaleOffX.getValue().floatValue(), vm.scaleOffY.getValue().floatValue(), vm.scaleOffZ.getValue().floatValue());
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(vm.rotationOffX.getValue().floatValue()));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(vm.rotationOffY.getValue().floatValue()));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(vm.rotationOffZ.getValue().floatValue()));
        }
    }

    @Inject(method={"submitArmWithItem"}, at={@At(value="RETURN")})
    private void night$applyViewModelTransformReturn(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        ViewModelModule vm;
        ViewModelModule viewModelModule = vm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
        if (vm == null || !vm.isToggled()) {
            return;
        }
        poseStack.popPose();
    }

    @Inject(method={"applyEatTransform"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$applyEatTransform(PoseStack poseStack, float partialTick, HumanoidArm arm, ItemStack stack, Player player, CallbackInfo ci) {
        ViewModelModule vm;
        ViewModelModule viewModelModule = vm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
        if (vm == null || !vm.isToggled()) {
            return;
        }
        float multiplier = vm.eatingMultiplier.getValue().floatValue();
        if (multiplier < 1.0f) {
            float currUsageTime = (float)player.getUseItemRemainingTicks() - partialTick + 1.0f;
            float scaledUsageTime = currUsageTime / (float)stack.getUseDuration((LivingEntity)player);
            if (scaledUsageTime < 0.8f) {
                float extraHeightOffset = Mth.abs((float)(Mth.cos((double)(currUsageTime / 4.0f * (float)Math.PI)) * 0.1f)) * multiplier;
                poseStack.translate(0.0f, extraHeightOffset, 0.0f);
            }
            float eatJiggle = 1.0f - (float)Math.pow(scaledUsageTime, 27.0);
            int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
            poseStack.translate(eatJiggle * 0.6f * (float)invert, eatJiggle * -0.5f, eatJiggle * 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)invert * eatJiggle * 90.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(eatJiggle * 10.0f));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)invert * eatJiggle * 30.0f));
            ci.cancel();
        }
        if (vm.eatAnimation.getValue()) {
            poseStack.translate(vm.eatX.getValue().floatValue(), vm.eatY.getValue().floatValue(), 0.0f);
        }
    }

    @ModifyArg(method={"renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"), index=4)
    private int night$handsOutline(int outlineColor, @Local(argsOnly=true) ItemStack stack) {
        return ItemInHandRendererMixin.computeHandsOutlineColor(outlineColor);
    }

    private static int computeHandsOutlineColor(int fallback) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return fallback;
        }
        ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shaders.isToggled() && shaders.hands.getValue()) {
            return shaders.getFillColor((Entity)mc.player);
        }
        return fallback;
    }

    @Inject(method={"submitHandsWithItems"}, at={@At(value="RETURN")})
    private void night$flushHandOutline(float partialTick, PoseStack pose, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
        HandsRenderState.renderingHands = false;
    }

    @Inject(method={"swingArm"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$swingArm(float attack, PoseStack poseStack, int invert, HumanoidArm arm, CallbackInfo info) {
        SwingModule swing = Night.MODULE_MANAGER.getModule(SwingModule.class);
        if (!swing.isToggled()) {
            return;
        }
        float xSwingPosition = swing.translateX.getValue() ? -0.4f * Mth.sin((double)(Mth.sqrt((float)attack) * (float)Math.PI)) : 0.0f;
        float ySwingPosition = swing.translateY.getValue() ? 0.2f * Mth.sin((double)(Mth.sqrt((float)attack) * ((float)Math.PI * 2))) : 0.0f;
        float zSwingPosition = swing.translateZ.getValue() ? -0.2f * Mth.sin((double)(attack * (float)Math.PI)) : 0.0f;
        poseStack.translate((float)invert * xSwingPosition, ySwingPosition, zSwingPosition);
        ItemInHandRendererMixin.night$applyItemArmAttackTransform(poseStack, arm, attack, swing);
        info.cancel();
    }

    private static void night$applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue, SwingModule swing) {
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        float ySwingRotation = swing.rotationY.getValue() ? Mth.sin((double)(attackValue * attackValue * (float)Math.PI)) : 0.0f;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)invert * (45.0f + ySwingRotation * -20.0f)));
        float xzSwingRotation = Mth.sin((double)(Mth.sqrt((float)attackValue) * (float)Math.PI));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)invert * (swing.rotationZ.getValue() ? xzSwingRotation : 0.0f) * -20.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees((swing.rotationX.getValue() ? xzSwingRotation : 0.0f) * -80.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)invert * -45.0f));
    }
}

