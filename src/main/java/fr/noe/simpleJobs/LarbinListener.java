package fr.noe.simpleJobs;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class LarbinListener implements Listener {

    private final LarbinManager larbinManager;
    private final JobManager jobManager;
    private final SimpleJobs plugin;

    public LarbinListener(JobManager jobManager, LarbinManager larbinManager, SimpleJobs plugin)
    {
        this.jobManager = jobManager;
        this.larbinManager = larbinManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        Player player = event.getPlayer();

        if(!(event.getRightClicked() instanceof Villager)) return;

        Villager villager = (Villager) event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if(isTameItem(itemInHand)) {
            larbinManager.tamerLarbin(player, villager.getLocation());
            villager.remove();
            event.setCancelled(true);

            itemInHand.setAmount(itemInHand.getAmount() - 1);

            return;
        }

        if(!larbinManager.isOwner(player, villager)) {
            player.sendMessage("§cCe larbin ne vous appartient pas !");
            return;
        }

        larbinManager.openInventory(player, villager);
        event.setCancelled(true);
    }

    @EventHandler
    public void onLarbinDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager)) return;
        Villager villager = (Villager) event.getEntity();

        NamespacedKey ownerKey = new NamespacedKey(plugin, "larbin_owner");
        if (!villager.getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING)) return;

        org.bukkit.inventory.Inventory inv = larbinManager.getInventory(villager);
        if (inv == null) return;

        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                villager.getWorld().dropItemNaturally(villager.getLocation(), item);
            }
        }

        larbinManager.removeInventory(villager);
    }

    public Boolean isTameItem(ItemStack itemStack)
    {
        return itemStack.getType() == Material.PINK_TULIP;
    }

}
