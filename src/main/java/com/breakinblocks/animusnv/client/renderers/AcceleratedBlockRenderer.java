package com.breakinblocks.animusnv.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.breakinblocks.animusnv.client.AcceleratedBlocksClientData;
import com.breakinblocks.animusnv.network.AcceleratedBlocksSyncPayload.AccelerationData;
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

import com.breakinblocks.animusnv.Constants;

import java.awt.Color;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class AcceleratedBlockRenderer {
    private static final int RENDER_DISTANCE = 32;

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

            if (!state.dimension().equals(level.dimension())) {
                continue;
            }

            double distanceSq = pos.distToCenterSqr(cameraPos);
            if (distanceSq > RENDER_DISTANCE * RENDER_DISTANCE) {
                continue;
            }

            double x = pos.getX() + 0.5 - cameraPos.x;
            double y = pos.getY() + 1.2 - cameraPos.y;
            double z = pos.getZ() + 0.5 - cameraPos.z;

            int multiplier = state.getSpeedMultiplier();
            long remainingTicks = Math.max(0, state.expiryTime() - level.getGameTime());
            double remainingSeconds = remainingTicks / 20.0;
            String text = String.format("%dx * %.1fs", multiplier, remainingSeconds);
            int color = getColorForLevel(state.level());

            renderFloatingText(poseStack, bufferSource, font, text, x, y, z, color);
        }

        RenderSystem.disableDepthTest();
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    private static void renderFloatingText(PoseStack poseStack, MultiBufferSource bufferSource, Font font,
                                          String text, double x, double y, double z, int color) {
        poseStack.pushPose();

        poseStack.translate(x, y, z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        poseStack.scale(0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float halfWidth = -font.width(text) / 2.0f;

        int colorWithAlpha = color | 0xFF000000;

        int backgroundColor = (int)(0.25F * 255.0F) << 24;
        font.drawInBatch(text, halfWidth, 0, 0x20FFFFFF, false, matrix, bufferSource,
            Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);

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
