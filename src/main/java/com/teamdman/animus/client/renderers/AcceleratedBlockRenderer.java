package com.teamdman.animus.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.Map;

/**
 * Renders acceleration multiplier text above accelerated blocks
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AcceleratedBlockRenderer {
    private static final int RENDER_DISTANCE = 32; // Only render within 32 blocks

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) {
            return;
        }

        // Get accelerated blocks from temporal dominance sigil
        Map<BlockPos, ItemSigilTemporalDominance.AccelerationState> acceleratedBlocks =
            ItemSigilTemporalDominance.getAcceleratedBlocks();

        if (acceleratedBlocks.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();
        Font font = mc.font;

        poseStack.pushPose();

        for (Map.Entry<BlockPos, ItemSigilTemporalDominance.AccelerationState> entry : acceleratedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            ItemSigilTemporalDominance.AccelerationState state = entry.getValue();

            // Only render if in the same dimension
            if (!state.dimension.equals(level.dimension())) {
                continue;
            }

            // Check distance
            double distanceSq = pos.distToCenterSqr(cameraPos);
            if (distanceSq > RENDER_DISTANCE * RENDER_DISTANCE) {
                continue;
            }

            // Calculate position (center of block, slightly above)
            double x = pos.getX() + 0.5 - cameraPos.x;
            double y = pos.getY() + 1.2 - cameraPos.y; // Floating above the block
            double z = pos.getZ() + 0.5 - cameraPos.z;

            // Get multiplier and color
            int multiplier = state.getSpeedMultiplier();
            String text = multiplier + "x";
            int color = getColorForLevel(state.level);

            // Render the text
            renderFloatingText(poseStack, bufferSource, font, text, x, y, z, color, event.getPartialTick());
        }

        poseStack.popPose();
    }

    /**
     * Render floating text at a specific position
     */
    private static void renderFloatingText(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                          String text, double x, double y, double z, int color, float partialTick) {
        poseStack.pushPose();

        // Translate to position
        poseStack.translate(x, y, z);

        // Rotate to face the camera
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

        // Scale
        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        // Get the matrix
        Matrix4f matrix = poseStack.last().pose();

        // Calculate text width for centering
        float width = font.width(text);
        float xOffset = -width / 2.0f;

        // Render background (dark background for visibility)
        int backgroundColor = 0x40000000; // Semi-transparent black
        font.drawInBatch(text, xOffset, 0, color, false, matrix, bufferSource,
            Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }

    /**
     * Get color based on acceleration level
     * Level 1 (2x): Green
     * Level 2 (4x): Yellow-Green
     * Level 3 (8x): Yellow
     * Level 4 (16x): Orange
     * Level 5 (32x): Red
     */
    private static int getColorForLevel(int level) {
        return switch (level) {
            case 1 -> new Color(0, 255, 0).getRGB();        // Green - 2x
            case 2 -> new Color(128, 255, 0).getRGB();      // Yellow-Green - 4x
            case 3 -> new Color(255, 255, 0).getRGB();      // Yellow - 8x
            case 4 -> new Color(255, 128, 0).getRGB();      // Orange - 16x
            case 5 -> new Color(255, 0, 0).getRGB();        // Red - 32x
            default -> new Color(255, 255, 255).getRGB();   // White - fallback
        };
    }
}
