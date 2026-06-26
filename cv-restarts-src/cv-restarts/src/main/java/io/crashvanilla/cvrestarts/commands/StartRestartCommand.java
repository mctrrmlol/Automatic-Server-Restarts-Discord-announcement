package io.crashvanilla.cvrestarts.commands;

import io.crashvanilla.cvrestarts.CvRestarts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartRestartCommand implements CommandExecutor {

    private final CvRestarts plugin;

    public StartRestartCommand(CvRestarts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("cvrestarts.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (plugin.getRestartScheduler().isActive()) {
            sender.sendMessage(Component.text("[cv-restarts] The restart scheduler is already running!", NamedTextColor.YELLOW));
            return true;
        }

        plugin.getRestartScheduler().start();
        sender.sendMessage(Component.text("[cv-restarts] Restart scheduler activated! Daily restart at 8:00 PM UTC-4.", NamedTextColor.GREEN));
        return true;
    }
}
