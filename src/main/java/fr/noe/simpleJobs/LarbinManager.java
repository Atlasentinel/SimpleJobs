package fr.noe.simpleJobs;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class LarbinManager {

    private final SimpleJobs plugin;
    private final HashMap<UUID, Villager> larbins;
    private final ProtocolManager protocolManager;
    private final HashMap<UUID, org.bukkit.inventory.Inventory> larbinInventories = new HashMap<>();
    private static final String SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc3OTM5NjMzNDc5MSwKICAicHJvZmlsZUlkIiA6ICIwYjRjZjE4NzY4YWQ0ODFlOWNlNTFlZjE1OWE1ODk5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBaWRqbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81N2RkYzYwNjQzOTM4ODM5NDRhMjgzOGRkOGU0YjAzYTBlNzlkYWJlMzcyZTk2NmQ3YzNlNDljZDE1NTliOWM5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTlhYmEwMmVmMDVlYzZhYTRkNDJkYjhlZTQzNzk2ZDZjZDUwZTRiMjk1NGFiMjlmMGNhZWI4NWY5NmJmNTJhMSIKICAgIH0KICB9Cn0=";
    private static final String SKIN_SIGNATURE = "g64S5vvh6igspp7rcWMs8aXv2gEw9uTeCnRUh9EE94cxY5N72R2doj0T3cXlzuiifYkS3u4U3v2HQMMdGAcJPYCyui9gwwdDZTYzQzFVJMECjXM9TN6MNRyiHWeCh8aziFPmKj9MN5xXKQvW8cFivdQ4qnUV2HckyyRGViY+6Kw06w9IJx/x82CTSTkCyP8b99kTFeH5CyWf8UTQKtclHyYEhYl/7UOft5KFCR301uQ591N/tsie1F657vrMFFe8RBMkO+fIvzPfnP/weRH1yyipPM9GjZL3hPQNvHR3waw/1c8wSdHf26iTwasYYibd7JFpkHrUN5ixg5dJCSSIRAJGi4e8h6j63hx8Yq4Z4VaHbqR4s3nkgrrSOuZ7MUkBSCCST7tsafXZ3WGZBdkHrF1pkVEUrtB2vdDYmLtnxHrZNhd+WPbAY07uqw7KhEddVcQnoz6uv+Ju6OTuIEMbwY3tTPjaRVLOHUoZ7Nd+gRtiSmfeyHiAbbiD59DT3aVK9p8UDFixvOhIgMMJt53UnoGpcmFUcJGfLv+7Y7ENWkHS2EekPDmmHWYzJxhG52pFhYamfm9p1ZLt1MAro85EfGr9ENOePcJ7dPrcZIGSh+pC32aM5df7TSTW0NcX08IIMgLYNz9NkCMWuQJcMAd+gwvKVsWp5YlvrqlKKmcCFow=";

    public LarbinManager(SimpleJobs plugin)
    {
        this.plugin = plugin;
        this.larbins = new HashMap<>();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    private void spawnLarbin(Player player, Location location){
        World world = location.getWorld();
        Villager larbin = (Villager) world.spawnEntity(location, EntityType.VILLAGER);

        larbin.customName(Component.text("§cEsclave de " + player.getName()));
        larbin.setCustomNameVisible(true);
        larbin.setAI(false);
        larbin.setInvulnerable(true);
        this.larbins.put(player.getUniqueId(), larbin);

        NamespacedKey ownerKey = new NamespacedKey(plugin, "larbin_owner");
        larbin.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    public boolean isOwner(Player player, Villager villager) {
        NamespacedKey ownerKey = new NamespacedKey(plugin, "larbin_owner");
        if (!villager.getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING)) return false;

        String ownerUUID = villager.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        return ownerUUID.equals(player.getUniqueId().toString());
    }

    public Inventory getInventory(Villager larbin) {
        return larbinInventories.get(larbin.getUniqueId());
    }

    public void removeInventory(Villager larbin) {
        larbinInventories.remove(larbin.getUniqueId());
    }

    public void openInventory(Player player, Villager larbin){
        Inventory inv = larbinInventories.computeIfAbsent(
                larbin.getUniqueId(),
                uuid -> Bukkit.createInventory(player, 54, Component.text("§6Inventaire du Larbin"))
        );
        player.openInventory(inv);
    }

    public void tamerLarbin(Player player, Location location) {
        spawnLarbin(player, location);
    }

}
