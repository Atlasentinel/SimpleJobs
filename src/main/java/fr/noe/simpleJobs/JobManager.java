package fr.noe.simpleJobs;

import java.util.HashMap;
import java.util.UUID;

public class JobManager {
    private final HashMap<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerProfile getProfile(UUID uuid)
    {
        if(!profiles.containsKey(uuid))
        {
            profiles.put(uuid, new PlayerProfile(uuid));
        }
        return profiles.get(uuid);
    }
}
