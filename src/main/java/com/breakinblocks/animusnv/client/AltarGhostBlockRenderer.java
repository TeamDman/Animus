package com.breakinblocks.animusnv.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.Matrix4f;

import com.breakinblocks.animusnv.Constants;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class AltarGhostBlockRenderer {
    private static final float LINE_WIDTH = 2.0f;

    private static Map<BlockPos, Identifier> ghostBlocks = new HashMap<>();
    private static int remainingTicks = 0;

    public static void setGhostBlocks(Map<BlockPos, Identifier> blocks, int durationTicks) {
        ghostBlocks = new HashMap<>(blocks);
        remainingTicks = durationTicks;
    }

    public static void clear() {
        ghostBlocks.clear();
        remainingTicks = 0;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event) {
        if (ghostBlocks.isEmpty() || remainingTicks <= 0) {
            return;
        }

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

        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        RenderType lines = RenderTypes.lines();

        poseStack.pushPose();

        for (Map.Entry<BlockPos, Identifier> entry : ghostBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            Identifier blockId = entry.getValue();

            Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
            if (block == null || block == Blocks.AIR) {
                continue;
            }

            BlockState state = block.defaultBlockState();

            poseStack.pushPose();
            poseStack.translate(
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z
            );

            renderGhostBlock(poseStack, bufferSource, lines, state);

            poseStack.popPose();
        }

        poseStack.popPose();

        bufferSource.endBatch(lines);
    }

    private static void renderGhostBlock(PoseStack poseStack, MultiBufferSource bufferSource, RenderType lines, BlockState state) {
        VertexConsumer consumer = bufferSource.getBuffer(lines);
        Matrix4f matrix = poseStack.last().pose();

        float r = 0.3f;
        float g = 0.7f;
        float b = 1.0f;
        float a = 0.8f;

        // Fade out in last 20 ticks
        if (remainingTicks < 20) {
            a *= (remainingTicks / 20.0f);
        }

        float minX = 0.0f;
        float minY = 0.0f;
        float minZ = 0.0f;
        float maxX = 1.0f;
        float maxY = 1.0f;
        float maxZ = 1.0f;

        addLine(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        addLine(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        addLine(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void addLine(VertexConsumer consumer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(LINE_WIDTH);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(LINE_WIDTH);
    }
}
