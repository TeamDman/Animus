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
            .ignitedByLava()
        );
    }

    // Note: Flammability is now handled through BlockBehaviour.Properties or data packs
    // In 1.12.2 this was: Blocks.FIRE.setFireInfo(this, 5, 5);
    // In 1.20.1, implement FlammableBlockRegistry or use tags
}
