package fr.noe.simpleJobs;

import java.util.HashMap;
import java.util.UUID;

public class PlayerProfile {

    private final UUID playerUUID;
    private JobType currentJob;
    private final HashMap<JobType, Double> jobXpMap;

    public PlayerProfile(UUID playerUUID)
    {
        this.playerUUID = playerUUID;
        this.currentJob = JobType.JOBLESS;
        this.jobXpMap = new HashMap<>();
        for(JobType type: JobType.values())
        {
            jobXpMap.put(type,0.0);
        }
    }

    public UUID getPlayerUUID()
    {
        return playerUUID;
    }

    public JobType getCurrentJob()
    {
        return currentJob;
    }

    public void setCurrentJob(JobType currentJob) {
        this.currentJob = currentJob;
    }

    public double getXp(JobType job)
    {
        return jobXpMap.getOrDefault(job,0.0);
    }

    public void addXp(JobType job, double amount){
        double currentXp = getXp(job);
        jobXpMap.put(job,currentXp + amount);
    }

    public int getLevel(JobType job){
        return (int) (getXp(job) / 100) + 1;
    }
}
