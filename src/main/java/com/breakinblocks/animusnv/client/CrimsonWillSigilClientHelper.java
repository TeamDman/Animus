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

        double currentWill = NeoVitaeAPI.getInstance().getPlayerWillHandler().getTotalSpiritus(SpiritusType.RAW, player);
        double willMultiplier = Math.min(currentWill / 4096.0, 1.0);
        double willBonus = 0.20 * willMultiplier;
        double totalBonus = 0.30 + willBonus;

        tooltip.add(Component.literal(String.format("  • Current boost: +%.0f%%", totalBonus * 100))
            .withStyle(ChatFormatting.YELLOW));
    }
}
