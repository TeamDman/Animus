package com.teamdman.animus.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.breakinblocks.neovitae.client.render.NeoVitaeRenderer;
import com.breakinblocks.neovitae.client.render.RenderResizableCuboid;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.BloodAltarTile;
import com.breakinblocks.neovitae.common.registry.AltarComponent;
import com.breakinblocks.neovitae.common.structure.NVMultiblock;
import com.breakinblocks.neovitae.common.tag.NVTags;

import java.util.List;

/**
 * Renders holographic previews of altar tier upgrade blocks.
 * Shows the next tier's structure when a player holds a Sanguine Diviner and looks at an altar.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class AltarTierRenderer {

    // Texture resource locations for different block types
    private static final ResourceLocation BLANK_RUNE = ResourceLocation.fromNamespaceAndPath("neovitae", "block/rune_blank");
    private static final ResourceLocation STONE_BRICKS = ResourceLocation.withDefaultNamespace("block/stone_bricks");
    private static final ResourceLocation GLOWSTONE = ResourceLocation.withDefaultNamespace("block/glowstone");
    private static final ResourceLocation BLOODSTONE = ResourceLocation.fromNamespaceAndPath("neovitae", "block/bloodstone_brick");
    private static final ResourceLocation HELLFORGED = ResourceLocation.fromNamespaceAndPath("neovitae", "block/hellforged_block");
    private static final ResourceLocation CRYSTALLIZED_DEMON_WILL = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "block/crystallized_demon_will_block");

    // Ghost effect colors - translucent with different tints
    private static final int GHOST_COLOR_RUNE = 0xAAFF6666;      // Red tint for runes
    private static final int GHOST_COLOR_PILLAR = 0xAA66FF66;   // Green tint for pillars
    private static final int GHOST_COLOR_CAP = 0xAA6666FF;      // Blue tint for caps
    private static final int GHOST_COLOR_CRYSTAL = 0xAAFF66FF;  // Purple tint for crystals
    private static final int FULL_BRIGHT = 0x00F000F0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        Level level = player.level();

        // Check if player is holding Sanguine Diviner
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(AnimusItems.SANGUINE_DIVINER.get())) {
            heldItem = player.getOffhandItem();
            if (!heldItem.is(AnimusItems.SANGUINE_DIVINER.get())) {
                return;
            }
        }

        // Check if player is looking at a Blood Altar
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos altarPos = blockHit.getBlockPos();
        BlockEntity be = level.getBlockEntity(altarPos);

        if (!(be instanceof BloodAltarTile altar)) {
            return;
        }

        int currentTier = altar.getTier();
        int nextTier = currentTier + 1;

        // Check if there's a next tier to show
        if (nextTier >= NVMultiblock.TIER_LIST.length || NVMultiblock.TIER_LIST[nextTier] == null) {
            return;
        }

        // Get buffer source and render
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();

        renderNextTierComponents(poseStack, buffers, altarPos, nextTier, level);

        RenderSystem.disableDepthTest();
        buffers.endBatch();
    }

    private static void renderNextTierComponents(PoseStack poseStack, MultiBufferSource buffers,
                                                   BlockPos altarPos, int nextTier, Level level) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 eyePos = camera.getPosition();
        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());

        List<AltarComponent> components = NVMultiblock.TIER_LIST[nextTier].components();

        for (AltarComponent component : components) {
            BlockPos componentPos = altarPos.offset(component.pos());

            // Only render if the position is air or replaceable
            BlockState existingState = level.getBlockState(componentPos);
            if (!existingState.isAir() && !existingState.canBeReplaced()) {
                // Check if the block is already valid for this component
                if (isValidBlock(component, existingState, level)) {
                    continue;
                }
            }

            poseStack.pushPose();

            double minX = componentPos.getX() - eyePos.x;
            double minY = componentPos.getY() - eyePos.y;
            double minZ = componentPos.getZ() - eyePos.z;

            poseStack.translate(minX, minY, minZ);

            ResourceLocation textureRL = getComponentTexture(component);
            int color = getComponentColor(component);
            NeoVitaeRenderer.Model3D model = getBlockModel(textureRL);

            RenderResizableCuboid.INSTANCE.renderCube(
                    model, poseStack, buffer, color, FULL_BRIGHT, OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }
    }

    private static boolean isValidBlock(AltarComponent component, BlockState state, Level level) {
        if (component.material().tag()) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, component.material().id());
            return state.is(tag);
        } else {
            Block block = level.registryAccess()
                    .registryOrThrow(Registries.BLOCK)
                    .get(ResourceKey.create(Registries.BLOCK, component.material().id()));
            return block != null && state.is(block);
        }
    }

    private static ResourceLocation getComponentTexture(AltarComponent component) {
        ResourceLocation materialId = component.material().id();

        // Check for specific materials
        if (materialId.equals(NVTags.Blocks.RUNES.location())) {
            return BLANK_RUNE;
        } else if (materialId.equals(NVTags.Blocks.PILLARS.location())) {
            return STONE_BRICKS;
        } else if (materialId.equals(NVTags.Blocks.T3_CAPSTONES.location())) {
            return GLOWSTONE;
        } else if (materialId.equals(NVTags.Blocks.T4_CAPSTONES.location())) {
            return BLOODSTONE;
        } else if (materialId.equals(NVTags.Blocks.T5_CAPSTONES.location())) {
            return HELLFORGED;
        } else if (materialId.equals(NVTags.Blocks.T6_CAPSTONES.location())) {
            return CRYSTALLIZED_DEMON_WILL;
        } else if (materialId.getPath().contains("bloodstone")) {
            return BLOODSTONE;
        }

        // Default to stone bricks
        return STONE_BRICKS;
    }

    private static int getComponentColor(AltarComponent component) {
        ResourceLocation materialId = component.material().id();

        if (component.isUpgrade()) {
            return GHOST_COLOR_RUNE;
        } else if (materialId.equals(NVTags.Blocks.PILLARS.location())) {
            return GHOST_COLOR_PILLAR;
        } else if (materialId.equals(NVTags.Blocks.T3_CAPSTONES.location()) ||
                   materialId.equals(NVTags.Blocks.T4_CAPSTONES.location()) ||
                   materialId.equals(NVTags.Blocks.T5_CAPSTONES.location())) {
            return GHOST_COLOR_CAP;
        } else if (materialId.equals(NVTags.Blocks.T6_CAPSTONES.location())) {
            return GHOST_COLOR_CRYSTAL;  // Purple tint for T6 crystallized demon will
        }

        return GHOST_COLOR_PILLAR;
    }

    private static NeoVitaeRenderer.Model3D getBlockModel(ResourceLocation textureRL) {
        NeoVitaeRenderer.Model3D model = new NeoVitaeRenderer.Model3D();
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(textureRL);
        model.setTexture(sprite);
        model.minX = 0;
        model.minY = 0;
        model.minZ = 0;
        model.maxX = 1;
        model.maxY = 1;
        model.maxZ = 1;
        return model;
    }
}
