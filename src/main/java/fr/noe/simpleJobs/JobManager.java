package fr.noe.simpleJobs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class JobManager {
    private final SimpleJobs plugin;
    private final HashMap<UUID, PlayerProfile> profiles = new HashMap<>();
    private final File configFile;
    private FileConfiguration dataConfig;

    public JobManager(SimpleJobs plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    public PlayerProfile getProfile(UUID uuid) {
        if (!profiles.containsKey(uuid)) {
            profiles.put(uuid, new PlayerProfile(uuid));
        }
        return profiles.get(uuid);
    }

    public void loadData() {
        if (!configFile.exists()) {
            return;
        }
        dataConfig = YamlConfiguration.loadConfiguration(configFile);
        if (dataConfig.getConfigurationSection("players") == null) {
            return;
        }
        for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerProfile profile = new PlayerProfile(uuid);

                String jobStr = dataConfig.getString("players." + uuidStr + ".job", "JOBLESS");
                profile.setCurrentJob(JobType.valueOf(jobStr));

                boolean showMsg = dataConfig.getBoolean("players." + uuidStr + ".show-messages", true);
                profile.setShowXpMessage(showMsg);

                if (dataConfig.getConfigurationSection("players." + uuidStr + ".xp") != null) {
                    for (String jobTypeStr : dataConfig.getConfigurationSection("players." + uuidStr + ".xp").getKeys(false)) {
                        JobType type = JobType.valueOf(jobTypeStr);
                        double xp = dataConfig.getDouble("players." + uuidStr + ".xp." + jobTypeStr);
                        profile.addXp(type, xp);
                    }
                }
                profiles.put(uuid, profile);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Erreur de chargement pour le joueur UUID: " + uuidStr);
            }
        }
    }

    public void saveData() {
        dataConfig = new YamlConfiguration();
        for (PlayerProfile profile : profiles.values()) {
            String path = "players." + profile.getPlayerUUID().toString();
            dataConfig.set(path + ".job", profile.getCurrentJob().name());
            dataConfig.set(path + ".show-messages", profile.isShowXpMessage());
            for (JobType type : JobType.values()) {
                dataConfig.set(path + ".xp." + type.name(), profile.getXp(type));
            }
        }
        try {
            dataConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les donnees dans data.yml !");
        }
    }
}