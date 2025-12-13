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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer for Sigil of Equivalency block preview
 * Shows which blocks would be replaced when looking at a block with the sigil
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
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

        // Check if player is holding the sigil and get the stack
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack sigilStack = null;

        if (mainHand.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
            sigilStack = mainHand;
        } else if (offHand.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
            sigilStack = offHand;
        } else {
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

        // Get radius from the item
        int radius = getRadius(sigilStack);
        net.minecraft.core.Direction clickedFace = blockHit.getDirection();
        List<BlockPos> matchingBlocks = findMatchingBlocksInRadius(level, targetPos, targetState.getBlock(), radius, clickedFace);

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

    private static List<BlockPos> findMatchingBlocksInRadius(Level level, BlockPos center, Block targetBlock, int radius, net.minecraft.core.Direction clickedFace) {
        List<BlockPos> matches = new ArrayList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();

        // Maximum blocks for a plane (square area)
        int maxBlocks = (radius * 2 + 1) * (radius * 2 + 1);

        // Start flood-fill from center
        queue.add(center);
        visited.add(center);

        // Determine which neighbors to use based on clicked face
        BlockPos[] neighbors;
        if (clickedFace == net.minecraft.core.Direction.UP || clickedFace == net.minecraft.core.Direction.DOWN) {
            // Horizontal plane - only check cardinal directions on XZ plane
            neighbors = new BlockPos[] {
                new BlockPos(1, 0, 0),   // East
                new BlockPos(-1, 0, 0),  // West
                new BlockPos(0, 0, 1),   // South
                new BlockPos(0, 0, -1)   // North
            };
        } else if (clickedFace == net.minecraft.core.Direction.NORTH || clickedFace == net.minecraft.core.Direction.SOUTH) {
            // North/South vertical plane - check on XY plane
            neighbors = new BlockPos[] {
                new BlockPos(1, 0, 0),   // East
                new BlockPos(-1, 0, 0),  // West
                new BlockPos(0, 1, 0),   // Up
                new BlockPos(0, -1, 0)   // Down
            };
        } else {
            // East/West vertical plane - check on ZY plane
            neighbors = new BlockPos[] {
                new BlockPos(0, 0, 1),   // South
                new BlockPos(0, 0, -1),  // North
                new BlockPos(0, 1, 0),   // Up
                new BlockPos(0, -1, 0)   // Down
            };
        }

        while (!queue.isEmpty() && matches.size() < maxBlocks) {
            BlockPos current = queue.poll();

            // Check if on the same plane and within radius
            if (!isOnSamePlaneAndInRadius(current, center, radius, clickedFace)) {
                continue;
            }

            // Check if this block matches
            BlockState state = level.getBlockState(current);
            if (state.getBlock() == targetBlock) {
                matches.add(current.immutable());

                // Add neighbors to queue
                for (BlockPos offset : neighbors) {
                    BlockPos neighbor = current.offset(offset.getX(), offset.getY(), offset.getZ());

                    // Only process if not visited and on same plane within radius
                    if (!visited.contains(neighbor) && isOnSamePlaneAndInRadius(neighbor, center, radius, clickedFace)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return matches;
    }

    private static boolean isOnSamePlaneAndInRadius(BlockPos pos, BlockPos center, int radius, net.minecraft.core.Direction clickedFace) {
        int dx = Math.abs(pos.getX() - center.getX());
        int dy = Math.abs(pos.getY() - center.getY());
        int dz = Math.abs(pos.getZ() - center.getZ());

        // Check plane constraint and 2D distance
        if (clickedFace == net.minecraft.core.Direction.UP || clickedFace == net.minecraft.core.Direction.DOWN) {
            // Horizontal plane - must be same Y, check X and Z distance
            return pos.getY() == center.getY() && Math.max(dx, dz) <= radius;
        } else if (clickedFace == net.minecraft.core.Direction.NORTH || clickedFace == net.minecraft.core.Direction.SOUTH) {
            // North/South plane - must be same Z, check X and Y distance
            return pos.getZ() == center.getZ() && Math.max(dx, dy) <= radius;
        } else {
            // East/West plane - must be same X, check Y and Z distance
            return pos.getX() == center.getX() && Math.max(dy, dz) <= radius;
        }
    }

    private static int getRadius(ItemStack stack) {
        // TODO: Use data components for custom radius in future
        return AnimusConfig.sigils.sigilEquivalencyRadius.get();
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
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(dx, dy, dz);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(dx, dy, dz);
    }
}
