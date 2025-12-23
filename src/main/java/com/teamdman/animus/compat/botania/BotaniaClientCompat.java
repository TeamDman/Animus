package com.teamdman.animus.compat.botania;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.BotaniaCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.forge.CapabilityUtil;

/**
 * Client-side Botania compatibility for Animus
 * Handles WandHUD rendering for our Botania-integrated blocks
 */
@OnlyIn(Dist.CLIENT)
public class BotaniaClientCompat {

    private static final ResourceLocation WAND_HUD_CAP_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "wand_hud");

    /**
     * Initialize client-side Botania compatibility
     * Call this from FMLClientSetupEvent
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.register(BotaniaClientCompat.class);
    }

    /**
     * Attach WandHUD capability to our block entities
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();

        // Attach WandHUD for Diabolical Fungi
        if (be instanceof BlockEntityDiabolicalFungi fungi) {
            event.addCapability(WAND_HUD_CAP_ID,
                CapabilityUtil.makeProvider(BotaniaForgeClientCapabilities.WAND_HUD,
                    new DiabolicalFungiWandHud(fungi)));
        }

        // Attach WandHUD for Rune of Unleashed Nature
        if (be instanceof BlockEntityRuneUnleashedNature rune) {
            event.addCapability(WAND_HUD_CAP_ID,
                CapabilityUtil.makeProvider(BotaniaForgeClientCapabilities.WAND_HUD,
                    new RuneUnleashedNatureWandHud(rune)));
        }
    }

    /**
     * WandHUD implementation for Diabolical Fungi
     * Shows current mana and max mana similar to other generating flowers
     */
    @OnlyIn(Dist.CLIENT)
    public static class DiabolicalFungiWandHud implements WandHUD {
        private final BlockEntityDiabolicalFungi fungi;

        public DiabolicalFungiWandHud(BlockEntityDiabolicalFungi fungi) {
            this.fungi = fungi;
        }

        @Override
        public void renderHUD(GuiGraphics guiGraphics, Minecraft minecraft) {
            String name = new ItemStack(fungi.getBlockState().getBlock()).getHoverName().getString();
            int currentMana = fungi.getMana();
            int maxMana = fungi.getMaxMana();

            // Use Botania's built-in mana HUD drawing
            // Color: 0x660099 (purple/dark red for blood magic theme)
            BotaniaAPIClient.instance().drawSimpleManaHUD(guiGraphics, 0x8B0000, currentMana, maxMana, name);
        }
    }

    /**
     * WandHUD implementation for Rune of Unleashed Nature
     * Shows current mana buffer and active state
     */
    @OnlyIn(Dist.CLIENT)
    public static class RuneUnleashedNatureWandHud implements WandHUD {
        private final BlockEntityRuneUnleashedNature rune;

        public RuneUnleashedNatureWandHud(BlockEntityRuneUnleashedNature rune) {
            this.rune = rune;
        }

        @Override
        public void renderHUD(GuiGraphics guiGraphics, Minecraft minecraft) {
            String name = new ItemStack(rune.getBlockState().getBlock()).getHoverName().getString();

            // Show active state in the name
            if (rune.isActive()) {
                name = name + " (Active)";
            }

            int currentMana = rune.getCurrentMana();
            int maxMana = rune.getMaxMana();

            // Use Botania's built-in mana HUD drawing
            // Color: 0x4B0082 (indigo/purple for nature theme)
            BotaniaAPIClient.instance().drawSimpleManaHUD(guiGraphics, 0x4B0082, currentMana, maxMana, name);
        }
    }
}
