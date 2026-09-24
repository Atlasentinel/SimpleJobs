package fr.noe.simpleJobs;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class LarbinManager {

    private final SimpleJobs plugin;
    private final HashMap<UUID, Villager> larbins;
    private final ProtocolManager protocolManager;
    private final HashMap<UUID, Inventory> larbinInventories = new HashMap<>();
    private final HashSet<UUID> stayingLarbins= new HashSet<>();
    private final HashMap<UUID, Integer> larbinLevels = new HashMap<>();
    private static final String SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc3OTM5NjMzNDc5MSwKICAicHJvZmlsZUlkIiA6ICIwYjRjZjE4NzY4YWQ0ODFlOWNlNTFlZjE1OWE1ODk5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBaWRqbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81N2RkYzYwNjQzOTM4ODM5NDRhMjgzOGRkOGU0YjAzYTBlNzlkYWJlMzcyZTk2NmQ3YzNlNDljZDE1NTliOWM5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTlhYmEwMmVmMDVlYzZhYTRkNDJkYjhlZTQzNzk2ZDZjZDUwZTRiMjk1NGFiMjlmMGNhZWI4NWY5NmJmNTJhMSIKICAgIH0KICB9Cn0=";
    private static final String SKIN_SIGNATURE = "g64S5vvh6igspp7rcWMs8aXv2gEw9uTeCnRUh9EE94cxY5N72R2doj0T3cXlzuiifYkS3u4U3v2HQMMdGAcJPYCyui9gwwdDZTYzQzFVJMECjXM9TN6MNRyiHWeCh8aziFPmKj9MN5xXKQvW8cFivdQ4qnUV2HckyyRGViY+6Kw06w9IJx/x82CTSTkCyP8b99kTFeH5CyWf8UTQKtclHyYEhYl/7UOft5KFCR301uQ591N/tsie1F657vrMFFe8RBMkO+fIvzPfnP/weRH1yyipPM9GjZL3hPQNvHR3waw/1c8wSdHf26iTwasYYibd7JFpkHrUN5ixg5dJCSSIRAJGi4e8h6j63hx8Yq4Z4VaHbqR4s3nkgrrSOuZ7MUkBSCCST7tsafXZ3WGZBdkHrF1pkVEUrtB2vdDYmLtnxHrZNhd+WPbAY07uqw7KhEddVcQnoz6uv+Ju6OTuIEMbwY3tTPjaRVLOHUoZ7Nd+gRtiSmfeyHiAbbiD59DT3aVK9p8UDFixvOhIgMMJt53UnoGpcmFUcJGfLv+7Y7ENWkHS2EekPDmmHWYzJxhG52pFhYamfm9p1ZLt1MAro85EfGr9ENOePcJ7dPrcZIGSh+pC32aM5df7TSTW0NcX08IIMgLYNz9NkCMWuQJcMAd+gwvKVsWp5YlvrqlKKmcCFow=";

    public LarbinManager(SimpleJobs plugin)
    {
        this.plugin = plugin;
        this.larbins = new HashMap<>();
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Villager> entry : larbins.entrySet()) {
                Villager _larbin = entry.getValue();
                Player owner = plugin.getServer().getPlayer(entry.getKey());

                if (owner == null || !owner.isOnline()) continue;
                if (!_larbin.isValid()) continue;

                if (stayingLarbins.contains(_larbin.getUniqueId())) continue;

                double distance = _larbin.getLocation().distance(owner.getLocation());
                if (distance > 20) {
                    _larbin.teleport(owner.getLocation());
                    continue;
                }

                if (distance > 3) {
                    _larbin.getPathfinder().moveTo(owner, 1.2);
                }
            }
        }, 0L, 10L);

    }

    private void spawnLarbin(Player player, Location location){
        World world = location.getWorld();
        Villager larbin = (Villager) world.spawnEntity(location, EntityType.VILLAGER);

        larbin.customName(Component.text("§cEsclave de " + player.getName()));
        larbin.setCustomNameVisible(true);
        larbin.setProfession(Villager.Profession.NONE);
        larbin.setCanPickupItems(false);
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

    private int getInventorySize(Villager larbin) {
        int level = larbinLevels.getOrDefault(larbin.getUniqueId(), 1);
        return switch (level) {
            case 1 -> 9;
            case 2 -> 18;
            case 3 -> 27;
            case 4 -> 36;
            case 5 -> 45;
            case 6 -> 54;
            default -> 9;
        };
    }

    public void setStay(Villager larbin){
        larbin.setAI(false);
        stayingLarbins.add(larbin.getUniqueId());
    }

    public void larbinSpeak(Player player, Villager larbin, String message) {
        player.sendMessage(Component.text("§6[Larbin] §f" + message));
    }

    public Boolean isStay(Villager larbin){
        return stayingLarbins.contains(larbin.getUniqueId());
    }

    public void setFollow(Villager larbin){
        larbin.setAI(true);
        stayingLarbins.remove(larbin.getUniqueId());
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
                uuid -> Bukkit.createInventory(player, getInventorySize(larbin), Component.text("§6Inventaire du Larbin"))
        );
        player.openInventory(inv);
    }

    public void levelUpLarbin(Player player, Villager larbin) {
        int currentLevel = larbinLevels.getOrDefault(larbin.getUniqueId(), 1);
        if (currentLevel >= 6) {
            larbinSpeak(player, larbin, "Je suis déjà au maximum de mes capacités !");
            return;
        }

        int newLevel = currentLevel + 1;
        larbinLevels.put(larbin.getUniqueId(), newLevel);

        Inventory oldInv = larbinInventories.get(larbin.getUniqueId());

        Inventory newInv = Bukkit.createInventory(
                null, getInventorySize(larbin),
                Component.text("§6Inventaire du Larbin")
        );

        if (oldInv != null) {
            for (int i = 0; i < oldInv.getSize(); i++) {
                newInv.setItem(i, oldInv.getItem(i));
            }
        }

        larbinInventories.put(larbin.getUniqueId(), newInv);
        larbinSpeak(player, larbin, "Je me sens plus fort ! Niveau " + newLevel);
    }

    public void tamerLarbin(Player player, Location location) {
        spawnLarbin(player, location);
    }

}
