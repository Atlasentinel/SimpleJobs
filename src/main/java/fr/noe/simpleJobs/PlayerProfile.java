package fr.noe.simpleJobs;

import java.util.HashMap;
import java.util.UUID;

public class PlayerProfile {
    private final UUID playerUUID;
    private JobType currentJob;
    private final HashMap<JobType, Double> jobXpMap;
    private boolean showXpMessage;

    public PlayerProfile(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.currentJob = JobType.CHOMEUR;
        this.jobXpMap = new HashMap<>();
        this.showXpMessage = true;
        for (JobType type : JobType.values()) {
            jobXpMap.put(type, 0.0);
        }
    }

    public void resetXp(JobType job){
        jobXpMap.put(job, 0.0);
    }

    public void resetAllXp(){
        for(JobType type : JobType.values()){
            jobXpMap.put(type, 0.0);
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public JobType getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(JobType currentJob) {
        this.currentJob = currentJob;
    }

    public boolean isShowXpMessage() {
        return showXpMessage;
    }

    public void setShowXpMessage(boolean showXpMessage) {
        this.showXpMessage = showXpMessage;
    }

    public double getXp(JobType job) {
        return jobXpMap.getOrDefault(job, 0.0);
    }

    public void addXp(JobType job, double amount) {
        jobXpMap.put(job, getXp(job) + amount);
    }

    public int getLevel(JobType job) {
        return (int) (getXp(job) / 100) + 1;
    }

    public float getXpProgress(JobType job) {
        return (float) ((getXp(job) % 100) / 100.0);
    }
}