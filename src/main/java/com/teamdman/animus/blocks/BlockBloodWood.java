package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blood Wood block - example of a simple block port
 * Original: 1.12.2 Material.WOOD based block
 * Ported: 1.20.1 BlockBehaviour.Properties based block
 */
public class BlockBloodWood extends Block {
    public BlockBloodWood() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0F)
            .sound(SoundType.WOOD)
            // Blood wood is non-flammable
        );
    }
}
