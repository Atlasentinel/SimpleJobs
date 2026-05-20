    package fr.noe.simpleJobs;

    import org.bukkit.plugin.java.JavaPlugin;

    public final class SimpleJobs extends JavaPlugin {
        private JobManager jobManager;

        @Override
        public void onEnable() {
            this.jobManager = new JobManager(this);
            getServer().getPluginManager().registerEvents(new JobListener(jobManager), this);
            getLogger().info("Le plugin de metiers SimpleJobs vient de s'activer !");
        }

        @Override
        public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            if (!(sender instanceof org.bukkit.entity.Player)) return true;
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            PlayerProfile profile = jobManager.getProfile(player.getUniqueId());

            if(args[0].equalsIgnoreCase("info")){
                if(args.length == 1){
                    player.sendMessage("§6=== A propos de vos métiers ===");
                    for(JobType type : JobType.values())
                    {
                        if(type == JobType.JOBLESS) continue;

                        int level = profile.getLevel(type);
                        double currentXp = profile.getXp(type);
                        double xpForNextLevel = level * 100;
                        double xpInCurrentLevel = currentXp % 100;

                        String indicator = (profile.getCurrentJob() == type) ? "§a▶ " : "§7  ";
                        player.sendMessage(indicator + "§e" + type.name() + " §7: Niveau §f" + level + " §7(§f" + xpInCurrentLevel + "§7/§f" + xpForNextLevel + " §7XP)");
                    }
                    return true;
                }
            }

            if (label.equalsIgnoreCase("job")) {

                if (args.length == 0) {
                    player.sendMessage("§6=== Liste des Métiers ===");
                    for (JobType type : JobType.values()) {
                        player.sendMessage("§e- " + type.name());
                    }
                    player.sendMessage("§7Utilise /job set <metier> pour en choisir un.");
                    return true;
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("show")) {
                    try{
                        profile.setShowXpMessage(!profile.isShowXpMessage());
                    } catch (IllegalArgumentException e){
                        player.sendMessage("§cCe métier n'existe pas !");
                    }
                    return true;
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                    try {
                        JobType newJob = JobType.valueOf(args[1].toUpperCase());
                        profile.setCurrentJob(newJob);
                        player.sendMessage("§aTu es maintenant : §6" + newJob.name());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cCe métier n'existe pas !");
                    }
                    return true;
                }
                player.sendMessage("§eUtilise : /job set <MINEUR|BUCHERON|etc.>");
            }
            player.sendMessage("§eTon métier actuel : §6" + profile.getCurrentJob().name());
            return true;
        }
        @Override
        public void onDisable() {
            jobManager.saveData();
            getLogger().info("Le plugin de metiers SimpleJobs s'est desactive !");
        }
    }
