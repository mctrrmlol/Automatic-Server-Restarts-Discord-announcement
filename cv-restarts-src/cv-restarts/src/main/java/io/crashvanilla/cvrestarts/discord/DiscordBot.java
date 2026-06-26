package io.crashvanilla.cvrestarts.discord;

import io.crashvanilla.cvrestarts.CvRestarts;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight Discord integration using the Discord REST API directly.
 * No external library needed — keeps the jar small and dependency-free.
 */
public class DiscordBot {

    private final CvRestarts plugin;
    private final String token;
    private final String channelId;
    private boolean connected = false;

    public DiscordBot(CvRestarts plugin, String token) {
        this.plugin = plugin;
        this.token = token;
        this.channelId = plugin.getConfig().getString("discord.channel-id", "1516888533015466071");
    }

    public void connect() {
        // Validate token by fetching bot info
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create("https://discord.com/api/v10/users/@me").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bot " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    connected = true;
                    plugin.getLogger().info("[Discord] Bot connected successfully.");
                } else {
                    plugin.getLogger().warning("[Discord] Failed to connect. HTTP " + code + ". Check your bot token.");
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Connection error: " + e.getMessage());
            }
        });
    }

    /**
     * Sends the 10-minute restart warning message to the configured channel.
     */
    public void sendRestartWarning() {
        sendMessage(
            "\uD83D\uDD52 Server restart is in __**10 MINUTES**__!\n\n" +
            " \u2705 **IP Address:** `crashvanilla.qzz.io`\n\n" +
            " <@&1516888532403097743>"
        );
    }

    /**
     * Sends a custom message to the configured Discord channel.
     */
    public void sendMessage(String content) {
        if (!connected) {
            plugin.getLogger().warning("[Discord] Not connected — skipping message send.");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create("https://discord.com/api/v10/channels/" + channelId + "/messages").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bot " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Escape the content for JSON
                String escaped = content
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");

                String json = "{\"content\":\"" + escaped + "\"}";
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 201) {
                    plugin.getLogger().info("[Discord] Restart warning sent successfully.");
                } else {
                    plugin.getLogger().warning("[Discord] Failed to send message. HTTP " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Error sending message: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
