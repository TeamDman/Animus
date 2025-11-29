package com.teamdman.animus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side renderer for ghost blocks shown by the Sanguine Diviner
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AltarGhostBlockRenderer {
    private static Map<BlockPos, ResourceLocation> ghostBlocks = new HashMap<>();
    private static int remainingTicks = 0;

    public static void setGhostBlocks(Map<BlockPos, ResourceLocation> blocks, int durationTicks) {
        ghostBlocks = new HashMap<>(blocks);
        remainingTicks = durationTicks;
    }

    public static void clear() {
        ghostBlocks.clear();
        remainingTicks = 0;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (ghostBlocks.isEmpty() || remainingTicks <= 0) {
            return;
        }

        // Decrement timer
        remainingTicks--;
        if (remainingTicks <= 0) {
            clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        Vec3 cameraPos = event.getCamera().getPosition();

        poseStack.pushPose();

        for (Map.Entry<BlockPos, ResourceLocation> entry : ghostBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            ResourceLocation blockId = entry.getValue();

            // Get the block from registry
            Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            if (block == null) {
                continue;
            }

            BlockState state = block.defaultBlockState();

            // Translate to block position relative to camera
            poseStack.pushPose();
            poseStack.translate(
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z
            );

            // Render translucent outline box
            renderGhostBlock(poseStack, bufferSource, state);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderGhostBlock(PoseStack poseStack, MultiBufferSource bufferSource, BlockState state) {
        // Use lines render type for wireframe
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        Matrix4f matrix = poseStack.last().pose();

        // Define translucent ghost color (light blue)
        float r = 0.3f;
        float g = 0.7f;
        float b = 1.0f;
        float a = 0.8f;  // 80% opacity for lines

        // Calculate fade based on remaining time (fade out in last 20 ticks)
        if (remainingTicks < 20) {
            a *= (remainingTicks / 20.0f);
        }

        // Render a wireframe box
        float minX = 0.0f;
        float minY = 0.0f;
        float minZ = 0.0f;
        float maxX = 1.0f;
        float maxY = 1.0f;
        float maxZ = 1.0f;

        // Draw the 12 edges of the cube
        // Bottom face edges
        addLine(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top face edges
        addLine(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Vertical edges
        addLine(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void addLine(VertexConsumer consumer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        // Calculate normal for the line (direction vector)
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Normalize
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        // Add two vertices for the line
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(dx, dy, dz).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(dx, dy, dz).endVertex();
    }
}
