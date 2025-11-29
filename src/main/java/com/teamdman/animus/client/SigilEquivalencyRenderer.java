package com.teamdman.animus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer for Sigil of Equivalency block preview
 * Shows which blocks would be replaced when looking at a block with the sigil
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SigilEquivalencyRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        // Check if player is holding the sigil
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (!mainHand.is(AnimusItems.SIGIL_EQUIVALENCY.get()) && !offHand.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
            return;
        }

        // Get the block the player is looking at
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHit.getBlockPos();
        Level level = mc.level;
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.isAir()) {
            return;
        }

        // Find all matching blocks in radius
        int radius = AnimusConfig.sigils.sigilEquivalencyRadius.get();
        List<BlockPos> matchingBlocks = findMatchingBlocksInRadius(level, targetPos, targetState.getBlock(), radius);

        if (matchingBlocks.isEmpty()) {
            return;
        }

        // Render preview
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        poseStack.pushPose();

        for (BlockPos pos : matchingBlocks) {
            poseStack.pushPose();
            poseStack.translate(
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z
            );

            renderPreviewBlock(poseStack, bufferSource);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static List<BlockPos> findMatchingBlocksInRadius(Level level, BlockPos center, Block targetBlock, int radius) {
        List<BlockPos> matches = new ArrayList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();

        // Start flood-fill from center
        queue.add(center);
        visited.add(center);

        // Neighbor offsets (6 cardinal directions - only blocks that share a face)
        BlockPos[] neighbors = new BlockPos[] {
            new BlockPos(1, 0, 0),   // East
            new BlockPos(-1, 0, 0),  // West
            new BlockPos(0, 1, 0),   // Up
            new BlockPos(0, -1, 0),  // Down
            new BlockPos(0, 0, 1),   // South
            new BlockPos(0, 0, -1)   // North
        };

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Check if within radius from center
            if (current.distManhattan(center) > radius) {
                continue;
            }

            // Check if this block matches
            BlockState state = level.getBlockState(current);
            if (state.getBlock() == targetBlock) {
                matches.add(current.immutable());

                // Add neighbors to queue
                for (BlockPos offset : neighbors) {
                    BlockPos neighbor = current.offset(offset.getX(), offset.getY(), offset.getZ());

                    // Only process if not visited and within radius
                    if (!visited.contains(neighbor) && neighbor.distManhattan(center) <= radius) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return matches;
    }

    private static void renderPreviewBlock(PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        // Purple/magenta color for the preview
        float r = 0.8f;
        float g = 0.2f;
        float b = 0.8f;
        float a = 0.6f;

        // Slightly offset the box to make it visible outside the block
        float offset = 0.002f;
        float minX = -offset;
        float minY = -offset;
        float minZ = -offset;
        float maxX = 1.0f + offset;
        float maxY = 1.0f + offset;
        float maxZ = 1.0f + offset;

        // Draw the 12 edges of the cube
        // Bottom face
        addLine(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top face
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
