package io.crashvanilla.cvrestarts.commands;

import io.crashvanilla.cvrestarts.CvRestarts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TestRestartCommand implements CommandExecutor {

    private final CvRestarts plugin;

    public TestRestartCommand(CvRestarts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("cvrestarts.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("[cv-restarts] Running test: sending Discord warning + starting countdown...", NamedTextColor.AQUA));

        // Send Discord warning
        if (plugin.getDiscordBot() != null && plugin.getDiscordBot().isConnected()) {
            plugin.getDiscordBot().sendRestartWarning();
            sender.sendMessage(Component.text("[cv-restarts] Discord warning sent!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("[cv-restarts] Discord bot not connected — skipping Discord message.", NamedTextColor.YELLOW));
        }

        // Fire the in-game warning message too
        plugin.getRestartScheduler().fireWarning();

        // After 5 seconds, start the countdown (test mode = no actual restart)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sender.sendMessage(Component.text("[cv-restarts] Starting 5-second countdown (TEST — server will NOT restart)...", NamedTextColor.AQUA));
            plugin.getRestartScheduler().startCountdown(true);
        }, 5 * 20L); // 5 second delay

        return true;
    }
}
