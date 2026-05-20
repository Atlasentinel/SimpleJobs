package fr.noe.simpleJobs;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class JobListener implements Listener {
    private final JobManager jobManager;

    private static final Map<Class<? extends Entity>, double[]> ENTITY_XP = Map.of(
            org.bukkit.entity.Zombie.class,   new double[]{0.3, 1.0},
            org.bukkit.entity.Skeleton.class, new double[]{0.5, 1.5},
            org.bukkit.entity.Creeper.class,  new double[]{1.0, 3.0},
            org.bukkit.entity.Spider.class,   new double[]{0.3, 0.8},
            org.bukkit.entity.CaveSpider.class, new double[]{0.35, 0.82},
            org.bukkit.entity.Enderman.class, new double[]{2.0, 3.0}
    );

    public JobListener(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    //Casser des blocks
    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        Material material = event.getBlock().getType();


        double rawBucherXp = ThreadLocalRandom.current().nextDouble(0.5, 1.3);
        double randomBucherXp = Math.round(rawBucherXp * 10.0) / 10.0;

        double rawDiggerXp = ThreadLocalRandom.current().nextDouble(0.2, 1.5);
        double randomDiggerXp = Math.round(rawDiggerXp * 10.0) / 10.0;

        //Métier CHÔMEUR
        if (profile.getCurrentJob() == JobType.JOBLESS) {
            if (material == Material.STONE && profile.isShowXpMessage()) {
                player.sendMessage("§a+0 XP CHÔMEUR ! Trouve toi un travail !");
            }
        }

        //Métier BUCHERON
        if (profile.getCurrentJob() == JobType.BUCHERON) {
            if (org.bukkit.Tag.LOGS.isTagged(material)) {
                profile.addXp(JobType.BUCHERON, randomBucherXp);
                if (profile.isShowXpMessage()) {
                    player.sendMessage("§a +" + randomBucherXp + " XP Bûcheron ! (Niveau " + profile.getLevel(JobType.BUCHERON) + ")");
                }
            }
        }

        //Métier MINEUR
        if (profile.getCurrentJob() == JobType.MINEUR) {
            if (material == Material.STONE || material == Material.COAL_ORE || material == Material.IRON_ORE || material == Material.DIAMOND_ORE) {
                profile.addXp(JobType.MINEUR, randomDiggerXp);
                if (profile.isShowXpMessage()) {
                    player.sendMessage("§a+" + randomDiggerXp + " XP Mineur ! (Niveau " + profile.getLevel(JobType.MINEUR) + ")");
                }
            }
        }
    }

    //Tuer des entités
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        if (profile.getCurrentJob() != JobType.CHASSEUR) return;

        double[] range = null;
        for (Map.Entry<Class<? extends org.bukkit.entity.Entity>, double[]> entry : ENTITY_XP.entrySet()) {
            if (entry.getKey().isInstance(event.getEntity())) {
                range = entry.getValue();
                break;
            }
        }
        if (range == null) return;

        double xp = Math.round(ThreadLocalRandom.current().nextDouble(range[0], range[1]) * 10.0) / 10.0;
        profile.addXp(JobType.CHASSEUR, xp);

        if (profile.isShowXpMessage()) {
            player.sendMessage("§a+" + xp + " XP Chasseur ! (Niveau " + profile.getLevel(JobType.CHASSEUR) + ")");
        }
    }
}