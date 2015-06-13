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
        NOVA.doLowerChat = config.get("General", "Lowercase Incoming Messages", false).getBoolean
                (false);
        if  (NOVA.enchantPow==null)
        NOVA.enchantPow = new EnchantmentPow(config.get("Enchantments", "Pow", 168).getInt(), 1);

        NOVA.disabledRituals.put("ritualSol", config.get("Ritual Blacklist", "Ritual of Sol", false).getBoolean());
        NOVA.disabledRituals.put("ritualLuna", config.get("Ritual Blacklist", "Ritual of Luna",false).getBoolean());
        NOVA.disabledRituals.put("ritualUncreate", config.get("Ritual Blacklist","Ritual of Uncreation", false).getBoolean());

        NOVA.ritualCosts.put("upkeepSol",config.get("Ritual Costs", "Sol Upkeep",10).getInt());
        NOVA.ritualCosts.put("upkeepLuna",config.get("Ritual Costs", "Luna Upkeep",20).getInt());
        NOVA.ritualCosts.put("upkeepUncreate",config.get("Ritual Costs", "Uncreation Upkeep",10).getInt());

        NOVA.ritualCosts.put("initSol",config.get("Ritual Costs", "Sol Init",1000).getInt());
        NOVA.ritualCosts.put("initLuna",config.get("Ritual Costs", "Luna Init",10000).getInt());
        NOVA.ritualCosts.put("initUncreate",config.get("Ritual Costs", "Uncreation Init",50000).getInt());

        config.save();
    }
}
