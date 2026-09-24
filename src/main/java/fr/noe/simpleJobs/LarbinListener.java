package fr.noe.simpleJobs;

import net.kyori.adventure.text.Component;
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
    private final Material CHOOSEN_ITEM = Material.PINK_TULIP;

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

        if (itemInHand.getType() == Material.ENDER_PEARL) {
            if (!larbinManager.isOwner(player, villager)) {
                player.sendMessage("§cCe larbin ne vous appartient pas !");
                return;
            }
            larbinManager.levelUpLarbin(player, villager);
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            event.setCancelled(true);
            return;
        }

        if(!larbinManager.isOwner(player, villager)) {
            larbinManager.larbinSpeak(player, villager, "Je n'ai pas d'ordre à recevoir de vous !");
            player.sendMessage("§cCe larbin ne vous appartient pas !");
            return;
        }

        if(player.isSneaking()){
            if (larbinManager.isStay(villager)) {
                larbinManager.setFollow(villager);
                larbinManager.larbinSpeak(player, villager, "Je vous suis maître !");
            } else {
                larbinManager.setStay(villager);
                larbinManager.larbinSpeak(player, villager, "Je vous attend ici maître !");
            }
        } else {
            larbinManager.openInventory(player, villager);
            event.setCancelled(true);
        }
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
        return itemStack.getType() == CHOOSEN_ITEM;
    }

}
