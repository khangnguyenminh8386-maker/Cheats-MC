/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectUtil
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.ItemInstance
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.AbstractFurnaceBlock
 *  net.minecraft.world.level.block.AnvilBlock
 *  net.minecraft.world.level.block.BarrelBlock
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.CrafterBlock
 *  net.minecraft.world.level.block.CraftingTableBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.EnderChestBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.RespawnAnchorBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.WallSignBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult.Type;


import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.impl.combat.CrystalPlacementHelper;
import night.modules.impl.core.RotationsModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.S2CBlockRenderPacket;
import night.utils.IMinecraft;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.miscellaneous.RenderPosition;
import night.utils.rotations.RotationUtils;

public class WorldUtils
implements IMinecraft {
    public static Set<Block> RIGHT_CLICKABLE_BLOCKS = Sets.newHashSet(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.SHULKER_BOX, Blocks.BARREL, Blocks.RESPAWN_ANCHOR, Blocks.CRAFTER, (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.WHITE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.ORANGE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.YELLOW), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIME), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PINK), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GRAY), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.CYAN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PURPLE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLUE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BROWN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GREEN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.RED), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLACK), Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.BELL, Blocks.OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.BIRCH_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.STONE_BUTTON, Blocks.COMPARATOR, Blocks.REPEATER, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LEVER, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.BEACON, (Block)Blocks.BED.pick(DyeColor.BLACK), (Block)Blocks.BED.pick(DyeColor.BLUE), (Block)Blocks.BED.pick(DyeColor.BROWN), (Block)Blocks.BED.pick(DyeColor.CYAN), (Block)Blocks.BED.pick(DyeColor.GRAY), (Block)Blocks.BED.pick(DyeColor.GREEN), (Block)Blocks.BED.pick(DyeColor.LIGHT_BLUE), (Block)Blocks.BED.pick(DyeColor.LIGHT_GRAY), (Block)Blocks.BED.pick(DyeColor.LIME), (Block)Blocks.BED.pick(DyeColor.MAGENTA), (Block)Blocks.BED.pick(DyeColor.ORANGE), (Block)Blocks.BED.pick(DyeColor.PINK), (Block)Blocks.BED.pick(DyeColor.PURPLE), (Block)Blocks.BED.pick(DyeColor.RED), (Block)Blocks.BED.pick(DyeColor.WHITE), (Block)Blocks.BED.pick(DyeColor.YELLOW), Blocks.FURNACE, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE, Blocks.DRAGON_EGG, Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE, Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CAKE, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.CARTOGRAPHY_TABLE, Blocks.GRINDSTONE, Blocks.LECTERN, Blocks.LOOM, Blocks.STONECUTTER, Blocks.SMITHING_TABLE});
    private static final ItemStack NETHERITE_PICKAXE = new ItemStack((ItemLike)Items.NETHERITE_PICKAXE);
    private static final Map<BlockPos, Long> pendingAirPlaces = new ConcurrentHashMap<BlockPos, Long>();
    private static final Map<BlockPos, PredictedPlacement> predictedPlacements = new ConcurrentHashMap<BlockPos, PredictedPlacement>();
    private static final long PREDICTION_TIMEOUT_MS = 1000L;
    public static volatile long lastInteractablePlaceTime = 0L;
    private static final String[] FACING_NAMES = new String[]{"South", "South West", "West", "North West", "North", "North East", "East", "South East"};
    private static final String[] FACING_AXES = new String[]{"+Z", "-X +Z", "-X", "-X -Z", "-Z", "+X -Z", "+X", "+X +Z"};

    public static boolean isInteractable(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        Block block = state.getBlock();
        if (RIGHT_CLICKABLE_BLOCKS.contains(block)) {
            return true;
        }
        return block instanceof BaseEntityBlock || block instanceof ShulkerBoxBlock || block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AnvilBlock || block instanceof CraftingTableBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock || block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof AbstractFurnaceBlock || block instanceof BedBlock || block instanceof SignBlock || block instanceof WallSignBlock || block instanceof CrafterBlock || block instanceof RespawnAnchorBlock || block instanceof BarrelBlock;
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, boolean rotate, boolean crystalDestruction) {
        return WorldUtils.placeBlock(position, direction, hand, rotate ? "Normal" : "None", crystalDestruction, false);
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, boolean rotate, boolean crystalDestruction, boolean render) {
        return WorldUtils.placeBlock(position, direction, hand, null, rotate ? "Normal" : "None", crystalDestruction, render);
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, Runnable runnable, boolean rotate, boolean crystalDestruction, boolean render) {
        return WorldUtils.placeBlock(position, direction, hand, runnable, rotate ? "Normal" : "None", crystalDestruction, render);
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, String rotateMode, boolean crystalDestruction) {
        return WorldUtils.placeBlock(position, direction, hand, rotateMode, crystalDestruction, false);
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, String rotateMode, boolean crystalDestruction, boolean render) {
        return WorldUtils.placeBlock(position, direction, hand, null, rotateMode, crystalDestruction, render);
    }

    public static void predictPlacement(BlockPos position, Block block) {
        if (position == null || block == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (predictedPlacements.size() > 256) {
            predictedPlacements.entrySet().removeIf(e -> now - ((PredictedPlacement)e.getValue()).time() >= 1000L);
        }
        predictedPlacements.put(position.immutable(), new PredictedPlacement(block, now));
    }

    public static void clearPredictedPlacement(BlockPos position) {
        if (position != null) {
            predictedPlacements.remove(position);
        }
    }

    public static void clearPredictedPlacements() {
        predictedPlacements.clear();
    }

    public static BlockState getPredictedCollisionState(BlockPos position) {
        if (predictedPlacements.isEmpty()) {
            return null;
        }
        PredictedPlacement pending = predictedPlacements.get(position);
        if (pending == null) {
            return null;
        }
        if (System.currentTimeMillis() - pending.time() >= 1000L) {
            predictedPlacements.remove(position);
            return null;
        }
        return pending.block().defaultBlockState();
    }

    public static void airPlaceBlock(BlockPos position, InteractionHand hand, String rotateMode, boolean render) {
        RenderPosition renderPosition;
        boolean shouldRotate;
        if (position == null || WorldUtils.mc.player == null || WorldUtils.mc.level == null || mc.getConnection() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long cooldownMs = Math.max(50L, (long)Night.SERVER_MANAGER.getPing() * 2L);
        Long lastAttempt = pendingAirPlaces.get(position);
        if (lastAttempt != null) {
            if (now - lastAttempt < cooldownMs) {
                return;
            }
            pendingAirPlaces.remove(position);
        }
        if (pendingAirPlaces.size() > 256) {
            pendingAirPlaces.entrySet().removeIf(e -> now - (Long)e.getValue() >= cooldownMs);
        }
        pendingAirPlaces.put(position, now);
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0);
        boolean bl = shouldRotate = rotateMode != null && !rotateMode.equalsIgnoreCase("None") && !rotateMode.equalsIgnoreCase("false");
        if (shouldRotate) {
            float[] rotations = RotationUtils.getRotations(hitVec);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
            }
        }
        if (render && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(position))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
        BlockHitResult result = new BlockHitResult(hitVec, Direction.UP, position, false);
        NetworkUtils.sendSequencedPacket(seq -> new ServerboundUseItemOnPacket(hand, result, seq));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
    }

    public static boolean placeBlock(BlockPos position, Direction direction, InteractionHand hand, Runnable runnable, String rotateMode, boolean crystalDestruction, boolean render) {
        RenderPosition renderPosition;
        ClientPlayerEntityAccessor accessor;
        Object object;
        boolean sneak;
        boolean shouldRotate;
        boolean isInter;
        if (position == null || direction == null) {
            return false;
        }
        BlockPos offsetPosition = position.relative(direction);
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return false;
        }
        boolean bl = isInter = WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(offsetPosition)) || WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(position));
        if (isInter) {
            lastInteractablePlaceTime = System.currentTimeMillis();
        }
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)offsetPosition).add((double)(-direction.getStepX()) * 0.45, (double)(-direction.getStepY()) * 0.45, (double)(-direction.getStepZ()) * 0.45);
        Night.WORLD_MANAGER.reservePlacement(position);
        float prevYaw = Night.ROTATION_MANAGER.getServerYaw();
        float prevPitch = Night.ROTATION_MANAGER.getServerPitch();
        float yaw = (float)RotationUtils.getYRotToVec((Entity)WorldUtils.mc.player, hitVec);
        float pitch = (float)RotationUtils.getXRotToVec((Entity)WorldUtils.mc.player, hitVec);
        boolean bl2 = shouldRotate = rotateMode != null && !rotateMode.equalsIgnoreCase("None") && !rotateMode.equalsIgnoreCase("false");
        if (shouldRotate) {
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(yaw, pitch);
            } else {
                Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
            }
        }
        if (crystalDestruction) {
            Direction finalDirection = direction;
            Runnable retry = () -> WorldUtils.placeBlock(position, finalDirection, hand, runnable, rotateMode, crystalDestruction, render);
            if (WorldUtils.destroyCrystals(position, WorldUtils.mc.player.getEyePosition(), hitVec, retry, rotateMode)) {
                return false;
            }
        }
        if (runnable != null) {
            runnable.run();
        }
        boolean bl3 = sneak = (WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(offsetPosition)) || WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(position))) && !WorldUtils.mc.player.isShiftKeyDown();
        if (sneak) {
            Input real = WorldUtils.mc.player.input != null ? WorldUtils.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            Input sneakInput = new Input(real.forward(), real.backward(), real.left(), real.right(), real.jump(), true, real.sprint());
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(sneakInput));
            object = WorldUtils.mc.player;
            if (object instanceof ClientPlayerEntityAccessor) {
                accessor = (ClientPlayerEntityAccessor)object;
                accessor.setLastSentInput(sneakInput);
            }
        }
        BlockHitResult blockHitResult = new BlockHitResult(hitVec, direction.getOpposite(), offsetPosition, false);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(hand, blockHitResult, sequence));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
        if (shouldRotate && !Night.ROTATION_MANAGER.isBatchingRotations() && Night.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) {
            Night.ROTATION_MANAGER.packetRotate(prevYaw, prevPitch);
        }
        Night.WORLD_MANAGER.getPlaceTimer().reset();
        if (sneak) {
            Input real = WorldUtils.mc.player.input != null ? WorldUtils.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(real));
            object = WorldUtils.mc.player;
            if (object instanceof ClientPlayerEntityAccessor) {
                accessor = (ClientPlayerEntityAccessor)object;
                accessor.setLastSentInput(real);
            }
        }
        if (render && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(position))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null) {
                ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CBlockRenderPacket(position)));
                for (Connection conn : Night.PROXY_SERVER.getConnections()) {
                    if (!conn.isConnected()) continue;
                    conn.send((Packet)packet);
                }
            }
        }
        return true;
    }

    public static boolean isPlaceable(BlockPos position) {
        return WorldUtils.isPlaceable(position, false);
    }

    public static boolean isPlaceable(BlockPos position, boolean excludeSelf) {
        boolean selfCollides;
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return false;
        }
        if (WorldUtils.mc.level.isOutsideBuildHeight(position)) {
            return false;
        }
        if (!WorldUtils.mc.level.getBlockState(position).canBeReplaced()) {
            return false;
        }
        boolean isFloorBlock = (double)position.getY() < Math.floor(WorldUtils.mc.player.getBoundingBox().minY + 0.01);
        boolean playerCenterInside = new AABB(position).contains(WorldUtils.mc.player.position().x, WorldUtils.mc.player.getBoundingBox().minY + 0.1, WorldUtils.mc.player.position().z);
        boolean isFeetLevel = !isFloorBlock && position.getY() == (int)Math.floor(WorldUtils.mc.player.getBoundingBox().minY);
        double selfDeflate = isFeetLevel ? 0.05 : 0.25;
        boolean bl = selfCollides = !excludeSelf && !isFloorBlock && (playerCenterInside || !EntityUtils.isPhased((Entity)WorldUtils.mc.player) && WorldUtils.mc.player.getBoundingBox().deflate(selfDeflate).intersects(new AABB(position)));
        if (selfCollides) {
            return false;
        }
        return WorldUtils.mc.level.getEntities((Entity)null, new AABB(position), entity -> true).stream().noneMatch(entity -> !(entity instanceof EndCrystal) && !(entity instanceof ExperienceOrb) && !(entity instanceof ItemEntity) && !(entity instanceof AbstractArrow) && (!entity.equals((Object)WorldUtils.mc.player) || !excludeSelf && !isFloorBlock && selfCollides) && !EntityUtils.isGhost(entity));
    }

    public static boolean isCrystalPlaceable(BlockPos position) {
        if (WorldUtils.mc.level == null) {
            return false;
        }
        if (!WorldUtils.mc.level.getBlockState(position).canBeReplaced()) {
            return false;
        }
        return WorldUtils.mc.level.getEntities((Entity)null, new AABB(position), entity -> true).stream().noneMatch(entity -> !(entity instanceof EndCrystal) && !(entity instanceof ExperienceOrb) && !(entity instanceof ItemEntity) && !(entity instanceof AbstractArrow) && !EntityUtils.isGhost(entity));
    }

    public static boolean isInstantBreakable(BlockPos position) {
        if (WorldUtils.mc.level == null) {
            return false;
        }
        BlockState state = WorldUtils.mc.level.getBlockState(position);
        if (state.isAir() || state.canBeReplaced()) {
            return false;
        }
        return state.getDestroySpeed((BlockGetter)WorldUtils.mc.level, position) == 0.0f;
    }

    public static void instantBreak(BlockPos position, Direction side) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, position, side != null ? side : Direction.UP));
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, position, side != null ? side : Direction.UP));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    public static Direction getDirection(BlockPos position) {
        return WorldUtils.getDirection(position, null, false);
    }

    public static Direction getDirection(BlockPos position, boolean strictDirection) {
        return WorldUtils.getDirection(position, null, strictDirection);
    }

    public static BlockPos findSupportBlock(BlockPos targetPos, List<BlockPos> placedPositions, boolean strictDirection, int maxDistance) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null || targetPos == null) {
            return null;
        }
        for (int d = 1; d <= maxDistance; ++d) {
            for (Direction dir : Direction.values()) {
                Direction supportDir;
                BlockPos candidate = targetPos.relative(dir, d);
                if (!WorldUtils.mc.player.isWithinBlockInteractionRange(candidate, 0.0) || !WorldUtils.isPlaceable(candidate, false) || (supportDir = WorldUtils.getDirection(candidate, placedPositions, strictDirection)) == null) continue;
                return candidate;
            }
        }
        return null;
    }

    private static boolean isVisibleBlock(BlockPos hitPos, BlockPos offset, BlockPos position) {
        if (hitPos == null) {
            return false;
        }
        return hitPos.equals((Object)offset) || hitPos.equals((Object)position);
    }

    public static List<Direction> getDirections(BlockPos position, List<BlockPos> exceptions, boolean strictDirection) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return Collections.emptyList();
        }
        Vec3 eye = WorldUtils.mc.player.getEyePosition();
        ArrayList<DirectionCandidate> candidates = new ArrayList<DirectionCandidate>();
        ArrayList fallbacks = new ArrayList();
        for (Direction direction : Direction.values()) {
            BlockPos offset = position.relative(direction);
            BlockState state = WorldUtils.mc.level.getBlockState(offset);
            if (state.canBeReplaced() && (exceptions == null || !exceptions.contains(offset)) || !state.getFluidState().isEmpty() || !WorldUtils.mc.player.isWithinBlockInteractionRange(offset, 0.0)) continue;
            Vec3 hit = Vec3.atCenterOf((Vec3i)position).relative(direction, 0.5);
            if (strictDirection) {
                List<Direction> strictDirections;
                if (direction == Direction.DOWN ? eye.y < (double)offset.getY() + 1.0 : (direction == Direction.UP ? eye.y > (double)offset.getY() : !(strictDirections = WorldUtils.getStrictDirections(eye, Vec3.atCenterOf((Vec3i)offset))).contains(direction.getOpposite()))) continue;
                Direction face = direction.getOpposite();
                int[] faceSteps = new int[]{face.getStepX(), face.getStepY(), face.getStepZ()};
                double[][] lateralRatios = new double[][]{{0.5, 0.5}, {0.25, 0.5}, {0.75, 0.5}, {0.5, 0.25}, {0.5, 0.75}};
                boolean faceVisible = false;
                for (double[] ratio : lateralRatios) {
                    BlockHitResult blockHit;
                    double[] p = new double[]{offset.getX(), offset.getY(), offset.getZ()};
                    int lateral = 0;
                    for (int axis = 0; axis < 3; ++axis) {
                        int n = axis;
                        p[n] = p[n] + (faceSteps[axis] != 0 ? 0.5 + (double)faceSteps[axis] * 0.48 : ratio[lateral++]);
                    }
                    BlockHitResult clip = WorldUtils.mc.level.clip(new ClipContext(eye, new Vec3(p[0], p[1], p[2]), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
                    if (clip.getType() != HitResult.Type.BLOCK || !(clip instanceof BlockHitResult) || !(blockHit = clip).getBlockPos().equals((Object)offset) || blockHit.getDirection() != face) continue;
                    faceVisible = true;
                    break;
                }
                if (!faceVisible) continue;
            }
            double distSq = eye.distanceToSqr(hit);
            if (RIGHT_CLICKABLE_BLOCKS.contains(state.getBlock())) {
                distSq += 20.0;
            }
            if (direction == Direction.DOWN) {
                distSq -= 50.0;
            }
            candidates.add(new DirectionCandidate(direction, distSq));
        }
        candidates.sort(Comparator.comparingDouble(c -> c.distSq));
        ArrayList<Direction> result = new ArrayList<Direction>();
        for (DirectionCandidate candidate : candidates) {
            result.add(candidate.direction);
        }
        return result;
    }

    public static Direction getDirection(BlockPos position, List<BlockPos> exceptions, boolean strictDirection) {
        List<Direction> dirs = WorldUtils.getDirections(position, exceptions, strictDirection);
        return dirs.isEmpty() ? null : dirs.get(0);
    }

    public static boolean destroyCrystals(BlockPos position) {
        return WorldUtils.destroyCrystals(position, null, null, null, "None");
    }

   public static boolean destroyCrystals(BlockPos position, Vec3 from, Vec3 to, Runnable retry, String rotateMode) {
      AABB searchArea = from != null && to != null ? new AABB(from, to).inflate(0.5) : new AABB(position);

      List<Entity> surroundingCrystals;
      try {
         surroundingCrystals = mc.level
            .getEntities(
               (Entity)null,
               searchArea,
               entityx -> entityx instanceof EndCrystal
                  && (
                     from == null
                        || to == null
                        || new AABB(position).intersects(entityx.getBoundingBox())
                        || entityx.getBoundingBox().clip(from, to).isPresent()
                  )
            );
      } catch (ConcurrentModificationException exception) {
         return false;
      }

      if (surroundingCrystals.isEmpty()) {
         return false;
      }

      boolean shouldRotate = rotateMode != null && !rotateMode.equalsIgnoreCase("None") && !rotateMode.equalsIgnoreCase("false");

      for (Entity entity : surroundingCrystals) {
         if (entity.isAlive() && !entity.isRemoved() && !(mc.player.getEyePosition().distanceToSqr(entity.position()) > 36.0)) {
            if (shouldRotate) {
               float[] rots = RotationUtils.getRotations(entity.getBoundingBox().getCenter());
               Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
            }

            mc.player.connection.send(new ServerboundAttackPacket(entity.getId()));
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            if (retry != null) {
               Night.WORLD_MANAGER.onCrystalAttacked(entity.getId(), retry);
            }
         }
      }

      return true;
   }

    public static Vec3 getHitVector(BlockPos position, Direction direction) {
        return Vec3.atCenterOf((Vec3i)position).add((double)direction.getStepX() / 2.0, (double)direction.getStepY() / 2.0, (double)direction.getStepZ() / 2.0);
    }

    public static Direction getClosestDirection(BlockPos position, boolean strictDirection) {
        if (strictDirection) {
            if (WorldUtils.mc.player.getY() >= (double)position.getY()) {
                return Direction.UP;
            }
            BlockHitResult result = WorldUtils.mc.level.clip(new ClipContext(WorldUtils.mc.player.getEyePosition(), Vec3.atCenterOf((Vec3i)position), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
            if (result == null || result.getType() != HitResult.Type.BLOCK || result.getDirection() == null) {
                return WorldUtils.getClosestDirection(position);
            }
            return result.getDirection();
        }
        return WorldUtils.getClosestDirection(position);
    }

    private static Direction getClosestDirection(BlockPos position) {
        Direction closestDirection = null;
        Vec3 offsetPosition = null;
        for (Direction direction : Direction.values()) {
            Vec3 newOffset = WorldUtils.getHitVector(position, direction);
            if (closestDirection == null) {
                closestDirection = direction;
                offsetPosition = newOffset;
                continue;
            }
            if (!(WorldUtils.mc.player.distanceToSqr(newOffset) < WorldUtils.mc.player.distanceToSqr(offsetPosition))) continue;
            closestDirection = direction;
            offsetPosition = newOffset;
        }
        return closestDirection;
    }

    public static List<Direction> getStrictDirections(Vec3 eyePos, Vec3 blockPos) {
        ArrayList<Direction> directions = new ArrayList<Direction>();
        double differenceX = eyePos.x - blockPos.x;
        double differenceY = eyePos.y - blockPos.y;
        double differenceZ = eyePos.z - blockPos.z;
        if (differenceY > 0.5) {
            directions.add(Direction.UP);
        } else if (differenceY < -0.5) {
            directions.add(Direction.DOWN);
        }
        if (differenceX > 0.5) {
            directions.add(Direction.EAST);
        } else if (differenceX < -0.5) {
            directions.add(Direction.WEST);
        }
        if (differenceZ > 0.5) {
            directions.add(Direction.SOUTH);
        } else if (differenceZ < -0.5) {
            directions.add(Direction.NORTH);
        }
        return directions;
    }

    public static HitResult getRaytraceTarget(float yaw, float pitch, double x, double y, double z) {
        AABB box;
        Vec3 multipliedVector;
        EntityHitResult entityHitResult;
        Vec3 rotationVector = new Vec3((double)(Mth.sin((double)(-yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)))), (double)(-Mth.sin((double)(pitch * ((float)Math.PI / 180)))), (double)(Mth.cos((double)(-yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)))));
        BlockHitResult result = WorldUtils.mc.level.clip(new ClipContext(new Vec3(x, y, z), new Vec3(x + rotationVector.x * 5.0, y + rotationVector.y * 5.0, z + rotationVector.z * 5.0), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        Vec3 vec3d = new Vec3(x, y + (double)WorldUtils.mc.player.getEyeHeight(WorldUtils.mc.player.getPose()), z);
        double distance = 25.0;
        if (result != null) {
            distance = result.getLocation().distanceToSqr(vec3d);
        }
        if ((entityHitResult = ProjectileUtil.getEntityHitResult((Entity)WorldUtils.mc.player, (Vec3)vec3d, (Vec3)(multipliedVector = vec3d.add(rotationVector.x * 5.0, rotationVector.y * 5.0, rotationVector.z * 5.0)), (AABB)(box = new AABB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3).expandTowards(rotationVector.scale(5.0)).inflate(1.0, 1.0, 1.0)), entity -> !entity.isSpectator() && entity.isPickable(), (double)distance)) != null && (vec3d.distanceToSqr(entityHitResult.getLocation()) < distance || result == null) && entityHitResult.getEntity() instanceof LivingEntity) {
            return entityHitResult;
        }
        return result;
    }

    public static boolean canSee(Entity entity) {
        if (entity == null || WorldUtils.mc.player == null || WorldUtils.mc.level == null) {
            return false;
        }
        AABB box = entity.getBoundingBox();
        Vec3 eye = new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ());
        if (WorldUtils.canSee(eye, box.getCenter())) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(box.getCenter().x, box.maxY - 0.1, box.getCenter().z))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(box.getCenter().x, box.minY + 0.1, box.getCenter().z))) {
            return true;
        }
        double minX = box.minX + 0.1;
        double maxX = box.maxX - 0.1;
        double minY = box.minY + 0.1;
        double maxY = box.maxY - 0.1;
        double minZ = box.minZ + 0.1;
        double maxZ = box.maxZ - 0.1;
        if (WorldUtils.canSee(eye, new Vec3(minX, maxY, minZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(maxX, maxY, minZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(minX, maxY, maxZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(maxX, maxY, maxZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(minX, minY, minZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(maxX, minY, minZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(minX, minY, maxZ))) {
            return true;
        }
        if (WorldUtils.canSee(eye, new Vec3(maxX, minY, maxZ))) {
            return true;
        }
        return WorldUtils.canSeeBlock(BlockPos.containing((double)box.getCenter().x, (double)(box.minY - 0.5), (double)box.getCenter().z));
    }

    public static boolean canSee(Vec3 from, Vec3 to) {
        return WorldUtils.mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSee(BlockPos position) {
        return WorldUtils.canSee((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5);
    }

    public static boolean canSee(Vec3 vec3d) {
        return WorldUtils.canSee(vec3d.x, vec3d.y, vec3d.z);
    }

    public static boolean canSee(double x, double y, double z) {
        return WorldUtils.mc.level.clip(new ClipContext(new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ()), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSeeBlock(BlockPos position) {
        BlockHitResult blockHit;
        BlockHitResult blockHit2;
        if (WorldUtils.mc.player == null || WorldUtils.mc.level == null) {
            return false;
        }
        Vec3 eye = new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ());
        BlockHitResult result = WorldUtils.mc.level.clip(new ClipContext(eye, Vec3.atCenterOf((Vec3i)position), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        if (result.getType() == HitResult.Type.MISS || result instanceof BlockHitResult && (blockHit2 = result).getBlockPos().equals((Object)position)) {
            return true;
        }
        Vec3 topCenter = new Vec3((double)position.getX() + 0.5, (double)position.getY() + 1.0, (double)position.getZ() + 0.5);
        BlockHitResult topResult = WorldUtils.mc.level.clip(new ClipContext(eye, topCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        if (topResult.getType() == HitResult.Type.MISS || topResult instanceof BlockHitResult && (blockHit = topResult).getBlockPos().equals((Object)position)) {
            return true;
        }
        return CrystalPlacementHelper.isPlacementVisible(position);
    }

    public static boolean canBreak(BlockPos ... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> WorldUtils.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f);
    }

    public static boolean isReplaceable(BlockPos ... pos) {
        return Arrays.stream(pos).allMatch(blockPos -> WorldUtils.mc.level.getBlockState(blockPos).canBeReplaced());
    }

    public static Block getBlock(BlockPos pos) {
        return WorldUtils.mc.level.getBlockState(pos).getBlock();
    }

    public static int getNetherPosition(int position) {
        return WorldUtils.mc.player.level().dimension() == Level.NETHER ? position * 8 : position / 8;
    }

    public static String getMovementDirection(Direction direction) {
        if (direction.getName().equalsIgnoreCase("North")) {
            return "-Z";
        }
        if (direction.getName().equalsIgnoreCase("East")) {
            return "+X";
        }
        if (direction.getName().equalsIgnoreCase("South")) {
            return "+Z";
        }
        if (direction.getName().equalsIgnoreCase("West")) {
            return "-X";
        }
        return "N/A";
    }

    public static String getFacingName(float yaw) {
        return FACING_NAMES[WorldUtils.getFacingOctant(yaw)];
    }

    public static String getFacingAxes(float yaw) {
        return FACING_AXES[WorldUtils.getFacingOctant(yaw)];
    }

    private static int getFacingOctant(float yaw) {
        float angle = yaw % 360.0f;
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return Math.round(angle / 45.0f) % 8;
    }

    public static float getBreakTime(Player player, BlockState blockState) {
        if (player == null) {
            return 0.0f;
        }
        float speed = NETHERITE_PICKAXE.getItem().getDestroySpeed(NETHERITE_PICKAXE, blockState) + 26.0f;
        return 1.0f / (speed / blockState.getBlock().defaultDestroyTime() / 30.0f) * 50.0f;
    }

    public static double getBreakDelta(BlockState blockState, int slot) {
        boolean ignoreAir;
        if (slot == -1) {
            return 0.0;
        }
        float speed = WorldUtils.mc.player.getInventory().getItem(slot).getItem().getDestroySpeed(WorldUtils.mc.player.getInventory().getItem(slot), blockState);
        if (speed > 1.0f) {
            ItemStack stack = WorldUtils.mc.player.getInventory().getItem(slot);
            int efficiency = EnchantmentHelper.getItemEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), (ItemInstance)stack);
            if (efficiency > 0 && !stack.isEmpty()) {
                speed += (float)(efficiency * efficiency + 1);
            }
        }
        if (MobEffectUtil.hasDigSpeed((LivingEntity)WorldUtils.mc.player)) {
            speed *= 1.0f + (float)(MobEffectUtil.getDigSpeedAmplification((LivingEntity)WorldUtils.mc.player) + 1) * 0.2f;
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            speed *= (switch (WorldUtils.mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        SpeedMineModule speedMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        boolean ignoreLiquid = speedMine != null && speedMine.isToggled() && speedMine.liquidCheck.getValue();
        boolean bl = ignoreAir = speedMine != null && speedMine.isToggled() && speedMine.airCheck.getValue();
        if (!ignoreLiquid && WorldUtils.mc.player.isEyeInFluid(FluidTags.WATER) && EnchantmentHelper.getEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY), (LivingEntity)WorldUtils.mc.player) <= 0) {
            speed /= 5.0f;
        }
        if (!ignoreAir && !WorldUtils.mc.player.onGround()) {
            speed /= 5.0f;
        }
        return speed / blockState.getBlock().defaultDestroyTime() / (float)(!blockState.requiresCorrectToolForDrops() || WorldUtils.mc.player.getInventory().getItem(slot).isCorrectToolForDrops(blockState) ? 30 : 100);
    }

    public static float getMineSpeed(BlockState state, int slot) {
        boolean ignoreAir;
        if (WorldUtils.mc.player == null) {
            return 0.0f;
        }
        float speed = WorldUtils.mc.player.getInventory().getItem(slot).getItem().getDestroySpeed(WorldUtils.mc.player.getInventory().getItem(slot), state);
        if (speed > 1.0f) {
            ItemStack stack = WorldUtils.mc.player.getInventory().getItem(slot);
            int efficiency = EnchantmentHelper.getItemEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), (ItemInstance)stack);
            if (efficiency > 0 && !stack.isEmpty()) {
                speed += (float)(StrictMath.pow(efficiency, 2.0) + 1.0);
            }
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.HASTE)) {
            speed *= 1.0f + (float)(WorldUtils.mc.player.getEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            speed *= (float)Math.pow(0.3f, WorldUtils.mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() + 1);
        }
        SpeedMineModule speedMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        boolean ignoreLiquid = speedMine != null && speedMine.isToggled() && speedMine.liquidCheck.getValue();
        boolean bl = ignoreAir = speedMine != null && speedMine.isToggled() && speedMine.airCheck.getValue();
        if (!ignoreLiquid && WorldUtils.mc.player.isEyeInFluid(FluidTags.WATER)) {
            speed *= (float)WorldUtils.mc.player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!ignoreAir && !WorldUtils.mc.player.onGround()) {
            speed /= 5.0f;
        }
        speed = speed < 0.0f ? 0.0f : speed;
        return speed / state.getBlock().defaultDestroyTime() / (float)(!state.requiresCorrectToolForDrops() || WorldUtils.mc.player.getInventory().getItem(slot).isCorrectToolForDrops(state) ? 30 : 100);
    }

    public static boolean blocksMovement(BlockState state) {
        return state.getBlock() != Blocks.COBWEB && state.getBlock() != Blocks.BAMBOO_SAPLING && !state.canBeReplaced();
    }

    public static boolean equals(BlockPos x, BlockPos y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals((Object)y);
    }

    public static String getDimension() {
        return WorldUtils.mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
    }

    public static List<Player> getCollisions(BlockPos pos) {
        ArrayList<Player> collisions = new ArrayList<Player>();
        for (Player player : WorldUtils.mc.level.players()) {
            if (player == null || player.isRemoved() || EntityUtils.isGhost((Entity)player) || !player.getBoundingBox().intersects(new AABB(pos))) continue;
            collisions.add(player);
        }
        return collisions;
    }

    private record PredictedPlacement(Block block, long time) {
    }

    private record DirectionCandidate(Direction direction, double distSq) {
    }
}

