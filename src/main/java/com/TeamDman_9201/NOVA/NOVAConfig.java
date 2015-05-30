package com.teamdman_9201.nova;

import com.teamdman_9201.nova.enchantments.EnchantmentPow;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by TeamDman on 2015-05-30.
 */
public class NOVAConfig {
    public static Configuration config;

    public static void init(File cfg) {
        config = new Configuration(cfg);
        try {
            config.load();
            syncConfig();
        } catch (Exception e) {
            System.out.println("NOVA config file failed to load. Contact me on irc with logs.");
        } finally {
            config.save();
        }
    }

    public static void syncConfig() {
        NOVA.doLowerChat = config.get("General", "doLowerChat", false).getBoolean(false);
        NOVA.enchantPow = new EnchantmentPow(config.get("Enchantments", "enchantmentPowID", 168).getInt(), 1);
        if (config.hasChanged()) {
            config.save();
        }
    }
}
