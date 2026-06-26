# cv-restarts

A Paper 1.21.1 plugin for CrashVanilla that sends scheduled restart warnings to both Discord and in-game action bars.

---

## Features

- **Discord warning** at 7:50 PM UTC-4 every day
- **In-game action bar** warning at 7:50 PM UTC-4
- **5-second countdown** in action bar starting at 7:59:55 PM UTC-4
- **Server auto-stops** at 8:00 PM UTC-4 (via `/stop`)
- **Manual control** — scheduler only starts when you run `/startrestart`
- **Test command** — verify everything works without restarting the server

---

## Building

Requires **Java 21+** and **Maven 3.6+**.

```bash
./build.sh
# OR
mvn clean package
```

Output: `target/cv-restarts.jar`

---

## Installation

1. Build the jar (see above)
2. Copy `cv-restarts.jar` to your Paper server's `plugins/` folder
3. Start (or restart) your server
4. Edit `plugins/cv-restarts/config.yml` and add your Discord bot token
5. Reload config or restart server
6. When you're ready for the server to go live, run `/startrestart`

---

## Configuration (`plugins/cv-restarts/config.yml`)

```yaml
discord:
  bot-token: "YOUR_BOT_TOKEN_HERE"   # Your Discord bot token
  channel-id: "1516888533015466071"  # Hardcoded — change here if needed

timezone-offset: -4   # UTC-4
restart-time: "20:00" # 8:00 PM (informational — actual schedule is hardcoded)
warning-minutes: 10
```

### Discord Bot Setup

1. Go to https://discord.com/developers/applications
2. Create an application → add a Bot
3. Copy the **Bot Token** and paste it into `config.yml`
4. Invite the bot to your server with **Send Messages** permission in the target channel
5. Make sure the bot has permission to mention roles in channel `1516888533015466071`

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/startrestart` | `cvrestarts.admin` (op by default) | Starts the daily restart scheduler |
| `/testrestart` | `cvrestarts.admin` (op by default) | Sends Discord warning + runs 5s countdown (no restart) |

---

## Schedule (UTC-4 / EDT)

| Time | Action |
|---|---|
| 7:50:00 PM | Discord message sent + action bar "sᴇʀᴠᴇʀ ʀᴇsᴛᴀʀᴛ ɪɴ 10 ᴍɪɴᴜᴛᴇs" |
| 7:59:55 PM | 5-second countdown begins in action bar |
| 8:00:00 PM | Server stops (`/stop`) |

---

## Notes

- The scheduler **only activates** after running `/startrestart` — it will not fire on its own when the server starts.
- If you restart the server (e.g. via a restart script), you'll need to run `/startrestart` again, or add it to a startup command via a plugin like CMI or a startup script.
- The Discord bot uses the REST API directly — no extra libraries needed.
