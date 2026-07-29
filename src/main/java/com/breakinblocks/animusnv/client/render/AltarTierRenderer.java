package com.breakinblocks.animusnv.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
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
import com.breakinblocks.animusnv.util.AltarTierLookup;
import com.breakinblocks.neovitae.client.render.NeoVitaeRenderer;
import com.breakinblocks.neovitae.client.render.RenderResizableCuboid;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.registry.AltarComponent;
import com.breakinblocks.neovitae.common.registry.AltarTier;
import com.breakinblocks.neovitae.common.structure.NVMultiblock;
import com.breakinblocks.neovitae.common.tag.NVTags;

import java.util.List;
import java.util.Optional;

/**
 * Renders holographic previews of altar tier upgrade blocks.
 * Shows the next tier's structure when a player holds a Sanguine Diviner and looks at an altar.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class AltarTierRenderer {

    private static final Identifier BLANK_RUNE = Identifier.fromNamespaceAndPath("neovitae", "block/rune_blank");
    private static final Identifier STONE_BRICKS = Identifier.withDefaultNamespace("block/stone_bricks");
    private static final Identifier GLOWSTONE = Identifier.withDefaultNamespace("block/glowstone");
    private static final Identifier BLOODSTONE = Identifier.fromNamespaceAndPath("neovitae", "block/bloodstone_brick");
    private static final Identifier HELLFORGED = Identifier.fromNamespaceAndPath("neovitae", "block/hellforged_block");
    private static final Identifier CRYSTALLIZED_SPIRITUS = Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "block/crystallized_spiritus_block");

    private static final int GHOST_COLOR_RUNE = 0xAAFF6666;
    private static final int GHOST_COLOR_PILLAR = 0xAA66FF66;
    private static final int GHOST_COLOR_CAP = 0xAA6666FF;
    private static final int GHOST_COLOR_CRYSTAL = 0xAAFF66FF;
    private static final int FULL_BRIGHT = 0x00F000F0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        Level level = player.level();

        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(AnimusItems.SANGUINE_DIVINER.get())) {
            heldItem = player.getOffhandItem();
            if (!heldItem.is(AnimusItems.SANGUINE_DIVINER.get())) {
                return;
            }
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos altarPos = blockHit.getBlockPos();
        BlockEntity be = level.getBlockEntity(altarPos);

        if (!(be instanceof AraVitaeTile altar)) {
            return;
        }

        int currentTier = altar.getTier();
        Optional<AltarTier> nextTier = AltarTierLookup.findNextTier(level.registryAccess(), currentTier);
        if (nextTier.isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Vec3 eyePos = event.getLevelRenderState().cameraRenderState.pos;
        RenderType ghostSheet = Sheets.translucentBlockSheet();

        renderNextTierComponents(poseStack, buffers, ghostSheet, eyePos, altarPos, nextTier.get(), level);

        buffers.endBatch(ghostSheet);
    }

    private static void renderNextTierComponents(PoseStack poseStack, MultiBufferSource buffers, RenderType ghostSheet,
                                                   Vec3 eyePos, BlockPos altarPos, AltarTier nextTier, Level level) {
        VertexConsumer buffer = buffers.getBuffer(ghostSheet);

        List<AltarComponent> components = nextTier.components();

        for (AltarComponent component : components) {
            BlockPos componentPos = altarPos.offset(component.pos());

            BlockState existingState = level.getBlockState(componentPos);
            if (!existingState.isAir() && !existingState.canBeReplaced()) {
                if (isValidBlock(component, existingState, level)) {
                    continue;
                }
            }

            poseStack.pushPose();

            double minX = componentPos.getX() - eyePos.x;
            double minY = componentPos.getY() - eyePos.y;
            double minZ = componentPos.getZ() - eyePos.z;

            poseStack.translate(minX, minY, minZ);

            Identifier textureRL = getComponentTexture(component, level);
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
                    .lookupOrThrow(Registries.BLOCK)
                    .getValue(component.material().id());
            return block != null && state.is(block);
        }
    }

    /**
     * Picks a texture to use for the holographic ghost of an altar component slot.
     *
     * Animus retains explicit textures for the vanilla NV capstone/structural tags so the
     * preview looks familiar in the default layout. For any other component (pack-custom
     * tags, exact-block references), the texture is derived from the first datapack-valid
     * block's particle icon; see {@link NVMultiblock#getDisplayStates}.
     */
    private static Identifier getComponentTexture(AltarComponent component, Level level) {
        if (component.material().tag()) {
            Identifier tagId = component.material().id();
            if (tagId.equals(NVTags.Blocks.RUNES.location())) return BLANK_RUNE;
            if (tagId.equals(NVTags.Blocks.PILLARS.location())) return STONE_BRICKS;
            if (tagId.equals(NVTags.Blocks.T3_CAPSTONES.location())) return GLOWSTONE;
            if (tagId.equals(NVTags.Blocks.T4_CAPSTONES.location())) return BLOODSTONE;
            if (tagId.equals(NVTags.Blocks.T5_CAPSTONES.location())) return HELLFORGED;
            if (tagId.equals(NVTags.Blocks.T6_CAPSTONES.location())) return CRYSTALLIZED_SPIRITUS;
        }

        List<BlockState> displayStates = NVMultiblock.getDisplayStates(component, level.registryAccess());
        if (!displayStates.isEmpty()) {
            BlockState state = displayStates.get(0);
            TextureAtlasSprite particle = Minecraft.getInstance()
                    .getModelManager()
                    .getBlockStateModelSet()
                    .getParticleMaterial(state)
                    .sprite();
            return particle.contents().name();
        }

        return STONE_BRICKS;
    }

    private static int getComponentColor(AltarComponent component) {
        if (component.material().tag()) {
            Identifier tagId = component.material().id();
            if (tagId.equals(NVTags.Blocks.T6_CAPSTONES.location())) return GHOST_COLOR_CRYSTAL;
            if (tagId.equals(NVTags.Blocks.T3_CAPSTONES.location())
                    || tagId.equals(NVTags.Blocks.T4_CAPSTONES.location())
                    || tagId.equals(NVTags.Blocks.T5_CAPSTONES.location())) {
                return GHOST_COLOR_CAP;
            }
        }
        if (component.isUpgrade()) {
            return GHOST_COLOR_RUNE;
        }
        return GHOST_COLOR_PILLAR;
    }

    private static NeoVitaeRenderer.Model3D getBlockModel(Identifier textureRL) {
        NeoVitaeRenderer.Model3D model = new NeoVitaeRenderer.Model3D();
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(textureRL);
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
