package com.teamdman.animus.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.teamdman.animus.client.AcceleratedBlocksClientData;
import com.teamdman.animus.network.AcceleratedBlocksSyncPayload.AccelerationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.Map;

/**
 * Renders acceleration multiplier text above accelerated blocks
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = "animus")
public class AcceleratedBlockRenderer {
    private static final int RENDER_DISTANCE = 32; // Only render within 32 blocks

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) {
            return;
        }

        // Get accelerated blocks from client-side data (synced from server)
        Map<BlockPos, AccelerationData> acceleratedBlocks = AcceleratedBlocksClientData.getAcceleratedBlocks();

        if (acceleratedBlocks.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        Font font = mc.font;

        for (Map.Entry<BlockPos, AccelerationData> entry : acceleratedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            AccelerationData state = entry.getValue();

            // Only render if in the same dimension
            if (!state.dimension().equals(level.dimension())) {
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

            // Get multiplier, remaining time, and color
            int multiplier = state.getSpeedMultiplier();
            long remainingTicks = Math.max(0, state.expiryTime() - level.getGameTime());
            double remainingSeconds = remainingTicks / 20.0;
            String text = String.format("%dx * %.1fs", multiplier, remainingSeconds);
            int color = getColorForLevel(state.level());

            // Render the text
            renderFloatingText(poseStack, bufferSource, font, text, x, y, z, color);
        }

        // Disable depth test and flush buffer to render text on top
        RenderSystem.disableDepthTest();
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    /**
     * Render floating text at a specific position (matching Minecraft's name tag approach)
     */
    private static void renderFloatingText(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                          String text, double x, double y, double z, int color) {
        poseStack.pushPose();

        // Translate to position
        poseStack.translate(x, y, z);

        // Rotate to face the camera (use entityRenderDispatcher's orientation for consistency)
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // Scale - match Minecraft's name tag: positive X/Z, negative Y only
        poseStack.scale(0.025F, -0.025F, 0.025F);

        // Get the matrix
        Matrix4f matrix = poseStack.last().pose();

        // Calculate text width for centering
        float halfWidth = -font.width(text) / 2.0f;

        // Ensure color has full alpha
        int colorWithAlpha = color | 0xFF000000;

        // Render background pass (semi-transparent, see-through)
        int backgroundColor = (int)(0.25F * 255.0F) << 24; // 25% opacity black background
        font.drawInBatch(text, halfWidth, 0, 0x20FFFFFF, false, matrix, bufferSource,
            Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);

        // Render foreground pass (solid color on top)
        font.drawInBatch(text, halfWidth, 0, colorWithAlpha, false, matrix, bufferSource,
            Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

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
