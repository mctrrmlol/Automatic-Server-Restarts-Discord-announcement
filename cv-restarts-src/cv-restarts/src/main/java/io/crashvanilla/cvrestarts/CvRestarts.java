package io.crashvanilla.cvrestarts;

import io.crashvanilla.cvrestarts.commands.StartRestartCommand;
import io.crashvanilla.cvrestarts.commands.TestRestartCommand;
import io.crashvanilla.cvrestarts.discord.DiscordBot;
import io.crashvanilla.cvrestarts.scheduler.RestartScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class CvRestarts extends JavaPlugin {

    private static CvRestarts instance;
    private DiscordBot discordBot;
    private RestartScheduler restartScheduler;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize Discord bot
        String token = getConfig().getString("discord.bot-token", "");
        if (token.isEmpty() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            getLogger().warning("Discord bot token is not set in config.yml! Discord messages will be disabled.");
            discordBot = null;
        } else {
            discordBot = new DiscordBot(this, token);
            discordBot.connect();
        }

        // Initialize scheduler (not started yet — requires /startrestart)
        restartScheduler = new RestartScheduler(this);

        // Register commands
        getCommand("startrestart").setExecutor(new StartRestartCommand(this));
        getCommand("testrestart").setExecutor(new TestRestartCommand(this));

        getLogger().info("cv-restarts enabled! Use /startrestart to activate the automatic restart timer.");
    }

    @Override
    public void onDisable() {
        if (restartScheduler != null) {
            restartScheduler.cancel();
        }
        if (discordBot != null) {
            discordBot.shutdown();
        }
        getLogger().info("cv-restarts disabled.");
    }

    public static CvRestarts getInstance() {
        return instance;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public RestartScheduler getRestartScheduler() {
        return restartScheduler;
    }
}
