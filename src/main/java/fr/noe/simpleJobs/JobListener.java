package fr.noe.simpleJobs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class JobListener implements Listener {
    private final JobManager jobManager;

    public JobListener(JobManager jobManager)
    {
        this.jobManager = jobManager;
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());
        Material material = event.getBlock().getType();

        if(profile.getCurrentJob() == JobType.JOBLESS)
        {
            if(material == Material.STONE){
                player.sendMessage("§a+0 XP CHÔMEUR ! Trouve toi un travail !");
            }
        }

        if(profile.getCurrentJob() == JobType.BUCHERON)
        {
            if(org.bukkit.Tag.LOGS.isTagged(material))
            {
                profile.addXp(JobType.BUCHERON, 10.0);
                player.sendMessage("§a+10 XP Bûcheron ! (Niveau " + profile.getLevel(JobType.BUCHERON) + ")");
            }
        }

        if(profile.getCurrentJob() == JobType.MINEUR)
        {
            if(material == Material.STONE || material == Material.COAL_ORE || material == Material.IRON_ORE || material == Material.DIAMOND_ORE){
                profile.addXp(JobType.MINEUR, 10.0);
                player.sendMessage("§a+10 XP Mineur ! (Niveau " + profile.getLevel(JobType.MINEUR) + ")");
            }
        }
    }
}
