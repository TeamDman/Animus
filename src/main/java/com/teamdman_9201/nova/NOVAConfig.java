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

        NOVA.ritualData.put("ritualSol", config.get("Ritual Blacklist", "Ritual of Sol", false).getBoolean()?1:0);
        NOVA.ritualData.put("ritualLuna", config.get("Ritual Blacklist", "Ritual of Luna",false).getBoolean()?1:0);
        NOVA.ritualData.put("ritualUncreate", config.get("Ritual Blacklist","Ritual of Uncreation", false).getBoolean()?1:0);
        NOVA.ritualData.put("ritualEntropy", config.get("Ritual Blacklist","Ritual of Entropy",false).getBoolean()?1:0);

        NOVA.ritualData.put("levelSol",config.get("Ritual Levels","Sol Level",1).getInt());
        NOVA.ritualData.put("levelLuna",config.get("Ritual Levels","Luna Level",2).getInt());
        NOVA.ritualData.put("levelUncreate",config.get("Ritual Levels","Uncreation Level",2).getInt());
        NOVA.ritualData.put("levelEntropy",config.get("Ritual Levels","Entropy Level",1).getInt());

        NOVA.ritualData.put("upkeepSol",config.get("Ritual Costs", "Sol Upkeep",10).getInt());
        NOVA.ritualData.put("upkeepLuna",config.get("Ritual Costs", "Luna Upkeep",20).getInt());
        NOVA.ritualData.put("upkeepUncreate",config.get("Ritual Costs", "Uncreation Upkeep",10).getInt());
        NOVA.ritualData.put("upkeepEntropy",config.get("Ritual Costs", "Entropy Upkeep",10).getInt());

        NOVA.ritualData.put("initSol",config.get("Ritual Costs", "Sol Init",1000).getInt());
        NOVA.ritualData.put("initLuna",config.get("Ritual Costs", "Luna Init",10000).getInt());
        NOVA.ritualData.put("initUncreate",config.get("Ritual Costs", "Uncreation Init",50000).getInt());
        NOVA.ritualData.put("initEntropy",config.get("Ritual Costs", "Entropy Init", 1000).getInt());

        config.save();
    }
}
