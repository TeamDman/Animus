package com.teamdman.animus.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

import javax.annotation.Nullable;
import java.util.UUID;

public final class AnimusRitualHelper {

    private AnimusRitualHelper() {}

    /**
     * Gets the soul network for a ritual's owner. Returns null if the network
     * doesn't exist (e.g., server not available, owner never logged in).
     */
    @Nullable
    public static ISoulNetwork getOwnerNetwork(IMasterRitualStone mrs) {
        return NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
    }

    /**
     * Gets the item handler capability at the given position, or null if none exists.
     */
    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }
}
