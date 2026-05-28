package com.breakinblocks.animusnv.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;

import java.util.List;

public class CrimsonWillSigilClientHelper {

    public static void addCurrentBoostTooltip(List<Component> tooltip) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        double currentSpiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler().getTotalSpiritus(SpiritusType.RAW, player);
        double spiritusMultiplier = Math.min(currentSpiritus / 4096.0, 1.0);
        double spiritusBonus = 0.20 * spiritusMultiplier;
        double totalBonus = 0.30 + spiritusBonus;

        tooltip.add(Component.literal(String.format("  • Current boost: +%.0f%%", totalBonus * 100))
            .withStyle(ChatFormatting.YELLOW));
    }
}
