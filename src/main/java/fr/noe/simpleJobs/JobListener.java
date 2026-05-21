package fr.noe.simpleJobs;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class JobListener implements Listener {
    private final JobManager jobManager;
    private final SimpleJobs plugin;

    private static final Map<Class<? extends Entity>, double[]> ENTITY_XP = Map.of(
            org.bukkit.entity.Zombie.class,      new double[]{0.3,  1.0},
            org.bukkit.entity.Drowned.class,     new double[]{0.32, 1.2},
            org.bukkit.entity.Skeleton.class,    new double[]{0.5,  1.5},
            org.bukkit.entity.Creeper.class,     new double[]{1.0,  3.0},
            org.bukkit.entity.Spider.class,      new double[]{0.3,  0.8},
            org.bukkit.entity.CaveSpider.class,  new double[]{0.35, 0.82},
            org.bukkit.entity.Enderman.class,    new double[]{2.0,  3.0},
            org.bukkit.entity.Endermite.class,   new double[]{1.5,  2.0},
            org.bukkit.entity.EnderDragon.class, new double[]{4.5,  10.5}
    );

    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public JobListener(JobManager jobManager, SimpleJobs plugin) {
        this.jobManager = jobManager;
        this.plugin = plugin;
    }

    // ==================== PASSAGE NIVEAU ===============

    private void checkLevelUp(Player player, PlayerProfile profile, JobType job, int levelBefore) {
        int levelAfter = profile.getLevel(job);
        if (levelAfter <= levelBefore) return;

        String basePath = "level-rewards." + job.name();
        if (!plugin.getConfig().contains(basePath)) return;

        int every = plugin.getConfig().getInt(basePath + ".default-every", 5);

        for (int lvl = levelBefore + 1; lvl <= levelAfter; lvl++) {
            String itemPath;

            if (plugin.getConfig().contains(basePath + ".overrides." + lvl)) {
                itemPath = basePath + ".overrides." + lvl;
            } else if (lvl % every == 0) {
                itemPath = basePath + ".default-item";
            } else {
                continue;
            }

            org.bukkit.inventory.ItemStack item = buildItem(itemPath);
            if (item == null) continue;

            player.getInventory().addItem(item);
            String displayName = plugin.getConfig().getString(itemPath + ".name", item.getType().name());
            player.sendMessage("§6🎁 Niveau " + lvl + " atteint ! Vous recevez : §e" + displayName);
        }
    }

    private org.bukkit.inventory.ItemStack buildItem(String path) {
        String materialName = plugin.getConfig().getString(path + ".material");
        if (materialName == null) return null;

        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Material invalide dans config.yml : " + materialName);
            return null;
        }

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Nom custom
        String name = plugin.getConfig().getString(path + ".name");
        if (name != null) {
            meta.setDisplayName(name);
        }

        // Enchantements
        if (plugin.getConfig().getConfigurationSection(path + ".enchantments") != null) {
            for (String enchName : plugin.getConfig().getConfigurationSection(path + ".enchantments").getKeys(false)) {
                try {
                    org.bukkit.enchantments.Enchantment ench = org.bukkit.enchantments.Enchantment.getByName(enchName);
                    if (ench != null) {
                        int level = plugin.getConfig().getInt(path + ".enchantments." + enchName, 1);
                        meta.addEnchant(ench, level, true); // true = ignore les restrictions
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Enchantement invalide dans config.yml : " + enchName);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }
    // ===================== BOSSBAR =====================

    private void updateBossBar(Player player, PlayerProfile profile) {
        JobType job = profile.getCurrentJob();

        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), uuid ->
                plugin.getServer().createBossBar("", BarColor.GREEN, BarStyle.SOLID)
        );

        if (job == JobType.CHOMEUR) {
            bar.setVisible(false);
            return;
        }

        bar.setColor(getColorForJob(job));
        bar.setStyle(BarStyle.SEGMENTED_10);
        bar.setProgress(profile.getXpProgress(job));
        bar.setTitle("§6" + job.name() + " §7— Niveau §f" + profile.getLevel(job));
        bar.setVisible(true);

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
    }

    public void setJob(Player player, JobType newJob)
    {
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        profile.setCurrentJob(newJob);
        updateBossBar(player, profile);
    }

    private BarColor getColorForJob(JobType job) {
        return switch (job) {
            case MINEUR      -> BarColor.PINK;
            case BUCHERON    -> BarColor.WHITE;
            case CHASSEUR    -> BarColor.RED;
            case CULTIVATEUR -> BarColor.YELLOW;
            case EXPLORATEUR -> BarColor.BLUE;
            case INGENIEUR   -> BarColor.PURPLE;
            default          -> BarColor.WHITE;
        };
    }

    // ===================== UTILITAIRE =====================

    private double randomXp(double min, double max) {
        return Math.round(ThreadLocalRandom.current().nextDouble(min, max) * 10.0) / 10.0;
    }

    // ===================== EVENTS =====================

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        updateBossBar(player, profile);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BossBar bar = bossBars.remove(event.getPlayer().getUniqueId());
        if (bar != null) bar.removeAll();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        Material material = event.getBlock().getType();

        if (profile.getCurrentJob() == JobType.BUCHERON) {
            if (Tag.SAPLINGS.isTagged(material)) {
                double xp = randomXp(0.5, 1.3);
                int levelBefore = profile.getLevel(JobType.BUCHERON);
                profile.addXp(JobType.BUCHERON, xp);
                updateBossBar(player, profile);
                checkLevelUp(player, profile, JobType.BUCHERON, levelBefore);
                if (profile.isShowXpMessage()) {
                    player.sendMessage("§a+" + xp + " XP Bûcheron ! (Niveau " + profile.getLevel(JobType.BUCHERON) + ")");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        Material material = event.getBlock().getType();

        if (profile.getCurrentJob() == JobType.CHOMEUR) {
            if (material == Material.STONE && profile.isShowXpMessage()) {
                player.sendMessage("§a+0 XP CHÔMEUR ! Trouve toi un travail !");
            }
            return;
        }

        if (profile.getCurrentJob() == JobType.BUCHERON) {
            if (Tag.LOGS.isTagged(material) || material == Material.SUGAR_CANE || Tag.FLOWERS.isTagged(material)) {
                double xp = randomXp(0.5, 1.3);
                int levelBefore = profile.getLevel(JobType.BUCHERON);
                profile.addXp(JobType.BUCHERON, xp);
                updateBossBar(player, profile);
                checkLevelUp(player, profile, JobType.BUCHERON, levelBefore);
                if (profile.isShowXpMessage()) {
                    player.sendMessage("§a+" + xp + " XP Bûcheron ! (Niveau " + profile.getLevel(JobType.BUCHERON) + ")");
                }
            }
        }

        if (profile.getCurrentJob() == JobType.MINEUR) {
            if (material == Material.STONE || material == Material.COAL_ORE || material == Material.IRON_ORE || material == Material.DIAMOND_ORE) {
                double xp = randomXp(0.2, 1.5);
                int levelBefore = profile.getLevel(JobType.MINEUR);
                profile.addXp(JobType.MINEUR, xp);
                updateBossBar(player, profile);
                checkLevelUp(player, profile, JobType.MINEUR, levelBefore);
                if (profile.isShowXpMessage()) {
                    player.sendMessage("§a+" + xp + " XP Mineur ! (Niveau " + profile.getLevel(JobType.MINEUR) + ")");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        if (profile.getCurrentJob() != JobType.CHASSEUR) return;

        double[] range = null;
        for (Map.Entry<Class<? extends Entity>, double[]> entry : ENTITY_XP.entrySet()) {
            if (entry.getKey().isInstance(event.getEntity())) {
                range = entry.getValue();
                break;
            }
        }
        if (range == null) return;

        double xp = randomXp(range[0], range[1]);
        int levelBefore = profile.getLevel(JobType.CHASSEUR);
        profile.addXp(JobType.CHASSEUR, xp);
        updateBossBar(player, profile);
        checkLevelUp(player, profile, JobType.CHASSEUR, levelBefore);
        if (profile.isShowXpMessage()) {
            player.sendMessage("§a+" + xp + " XP Chasseur ! (Niveau " + profile.getLevel(JobType.CHASSEUR) + ")");
        }
    }
}