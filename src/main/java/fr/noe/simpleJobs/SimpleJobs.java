package fr.noe.simpleJobs;

import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleJobs extends JavaPlugin {
    private JobManager jobManager;
    private JobListener jobListener;
    private LarbinManager larbinManager;
    private LarbinListener larbinListener;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.jobManager = new JobManager(this);
        this.larbinManager = new LarbinManager(this);
        this.jobListener = new JobListener(jobManager, this);
        this.larbinListener = new LarbinListener(jobManager, larbinManager, this);
        getServer().getPluginManager().registerEvents(jobListener, this);
        getServer().getPluginManager().registerEvents(larbinListener, this);
        getLogger().info("Le plugin de metiers SimpleJobs vient de s'activer !");
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player)) return true;
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        PlayerProfile profile = jobManager.getProfile(player.getUniqueId());

        if (!label.equalsIgnoreCase("job")) return true;

        // /job
        if (args.length == 0) {
            player.sendMessage("§6=== Liste des Métiers ===");
            for (JobType type : JobType.values()) {
                player.sendMessage("§e- " + type.name());
            }
            player.sendMessage("§7Utilise /job set <metier> pour en choisir un.");
            return true;
        }

        // /job info
        if (args[0].equalsIgnoreCase("info")) {
            player.sendMessage("§6=== A propos de vos métiers ===");
            for (JobType type : JobType.values()) {
                if (type == JobType.CHOMEUR) continue;
                int level = profile.getLevel(type);
                double xpInCurrentLevel = profile.getXp(type) % 100;
                double xpForNextLevel = level * 100;
                String indicator = (profile.getCurrentJob() == type) ? "§a▶ " : "§7  ";
                player.sendMessage(indicator + "§e" + type.name() + " §7: Niveau §f" + level + " §7(§f" + xpInCurrentLevel + "§7/§f" + xpForNextLevel + " §7XP)");
            }
            return true;
        }

        // /job show
        if (args[0].equalsIgnoreCase("show")) {
            profile.setShowXpMessage(!profile.isShowXpMessage());
            player.sendMessage(profile.isShowXpMessage() ? "§aMessages XP activés." : "§7Messages XP désactivés.");
            return true;
        }

        // /job set <metier>
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage("§eUtilise : /job set <MINEUR|BUCHERON|etc.>");
                return true;
            }
            try {
                JobType newJob = JobType.valueOf(args[1].toUpperCase());
                jobListener.setJob(player, newJob);
                player.sendMessage("§aTu es maintenant : §6" + newJob.name());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cCe métier n'existe pas !");
            }
            return true;
        }

        // /job givexp <joueur|all> <metier> <montant>
        if (args[0].equalsIgnoreCase("givexp")) {
            if (!player.hasPermission("simplejobs.givexp")) {
                player.sendMessage("§cVous n'avez pas la permission de faire ça !");
                return true;
            }
            if (args.length < 4) {
                player.sendMessage("§eUtilise : /job givexp <joueur|all> <metier> <montant>");
                return true;
            }

            try {
                JobType job = JobType.valueOf(args[2].toUpperCase());
                double amount = Double.parseDouble(args[3]);
                if (amount <= 0) {
                    player.sendMessage("§cLe montant doit être positif !");
                    return true;
                }

                if (args[1].equalsIgnoreCase("all")) {
                    for (org.bukkit.entity.Player target : getServer().getOnlinePlayers()) {
                        PlayerProfile targetProfile = jobManager.getProfile(target.getUniqueId());
                        targetProfile.addXp(job, amount);
                        jobListener.setJob(target, targetProfile.getCurrentJob());
                        target.sendMessage("§aVous avez reçu §f" + amount + " XP §a" + job.name() + " !");
                    }
                    player.sendMessage("§aVous avez donné §f" + amount + " XP §a" + job.name() + " à tous les joueurs.");
                } else {
                    org.bukkit.entity.Player target = getServer().getPlayerExact(args[1]);
                    if (target == null) {
                        player.sendMessage("§cJoueur introuvable ou non connecté !");
                        return true;
                    }
                    PlayerProfile targetProfile = jobManager.getProfile(target.getUniqueId());
                    targetProfile.addXp(job, amount);
                    jobListener.setJob(target, targetProfile.getCurrentJob());
                    player.sendMessage("§aVous avez donné §f" + amount + " XP §a" + job.name() + " à §f" + target.getName() + ".");
                    target.sendMessage("§aVous avez reçu §f" + amount + " XP §a" + job.name() + " !");
                }

            } catch (IllegalArgumentException e) {
                player.sendMessage("§cMétier invalide ou le montant n'est pas valide !");
            }
            return true;
        }

        // /job reset <joueur|all> [metier]
        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                player.sendMessage("§eUtilise : /job reset <joueur|all> [metier]");
                return true;
            }

            // Cible all ou joueur spécifique
            boolean targetAll = args[1].equalsIgnoreCase("all");
            org.bukkit.entity.Player target = null;

            if (!targetAll) {
                target = getServer().getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage("§cJoueur introuvable ou non connecté !");
                    return true;
                }
            }

            // Avec ou sans métier précisé
            if (args.length >= 3) {
                try {
                    JobType job = JobType.valueOf(args[2].toUpperCase());
                    if (targetAll) {
                        if (!player.hasPermission("simplejobs.reset.all")) {
                            player.sendMessage("§cVous n'avez pas la permission de faire ça !");
                            return true;
                        }
                        jobManager.resetAllPlayers(job);
                        player.sendMessage("§aXP " + job.name() + " réinitialisée pour tous les joueurs.");
                    } else {
                        jobManager.resetPlayerJob(target.getUniqueId(), job);
                        player.sendMessage("§aXP " + job.name() + " réinitialisée pour " + target.getName() + ".");
                        jobListener.setJob(target, jobManager.getProfile(target.getUniqueId()).getCurrentJob());
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cCe métier n'existe pas !");
                }
            } else {
                if (targetAll && !player.hasPermission("simplejobs.reset.all")) {
                    jobManager.resetAllPlayersAll();
                    player.sendMessage("§aToute l'XP réinitialisée pour tous les joueurs.");
                } else {
                    jobManager.resetPlayerAll(target.getUniqueId());
                    player.sendMessage("§aToute l'XP réinitialisée pour " + target.getName() + ".");
                    jobListener.setJob(target, jobManager.getProfile(target.getUniqueId()).getCurrentJob());
                }
            }
            return true;
        }


        // Commande inconnue
        player.sendMessage("§eTon métier actuel : §6" + profile.getCurrentJob().name());
        return true;
    }

    @Override
    public void onDisable() {
        jobManager.saveData();
        getLogger().info("Le plugin de metiers SimpleJobs s'est desactive !");
    }
}