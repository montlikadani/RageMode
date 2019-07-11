package hu.montlikadani.ragemode.gameLogic;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameStartEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.BossMessenger;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameLoader {

	private String gameName;

	private Configuration conf;
	private GameTimer gameTimer;

	public GameLoader(String gameName) {
		this.gameName = gameName;
		conf = RageMode.getInstance().getConfiguration();

		checkTeleport();

		GameStartEvent gameStartEvent = new GameStartEvent(gameName, PlayerList.getPlayersInGame(gameName));
		Bukkit.getPluginManager().callEvent(gameStartEvent);

		PlayerList.setGameRunning(gameName);
		GameUtils.setStatus(GameStatus.RUNNING);
		setInventories();

		int time = !conf.getArenasCfg().isSet("arenas." + gameName + ".gametime")
				? conf.getCfg().getInt("game.global.defaults.gametime") < 0 ? 5 * 60
						: conf.getCfg().getInt("game.global.defaults.gametime") * 60
				: GetGames.getGameTime(gameName) * 60;

		gameTimer = new GameTimer(gameName, time);
		gameTimer.loadModules();
		Timer t = new Timer();
		t.scheduleAtFixedRate(gameTimer, 0, 60 * 20L);

		SignCreator.updateAllSigns(gameName);

		List<String> players = Arrays.asList(PlayerList.getPlayersInGame(gameName));
		for (String player : players) {
			Player p = Bukkit.getPlayer(UUID.fromString(player));

			if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
				String bossMessage = conf.getCfg().getString("bossbar-messages.join.message");

				if (bossMessage != null && !bossMessage.equals("")) {
					bossMessage = bossMessage.replace("%game%", gameName);
					bossMessage = bossMessage.replace("%player%", p.getName());
					bossMessage = RageMode.getLang().colors(bossMessage);

					if (conf.getArenasCfg().isSet("arenas." + gameName + ".bossbar")) {
						if (conf.getArenasCfg().getBoolean("arenas." + gameName + ".bossbar"))
							new BossMessenger(gameName).sendBossBar(bossMessage, p,
									BarStyle.valueOf(conf.getCfg().getString("bossbar-messages.join.style")),
									BarColor.valueOf(conf.getCfg().getString("bossbar-messages.join.color")));
					} else {
						if (conf.getCfg().getBoolean("game.global.defaults.bossbar"))
							new BossMessenger(gameName).sendBossBar(bossMessage, p,
									BarStyle.valueOf(conf.getCfg().getString("bossbar-messages.join.style")),
									BarColor.valueOf(conf.getCfg().getString("bossbar-messages.join.color")));
					}
				}
			}

			String actionbarMsg = conf.getCfg().getString("actionbar-messages.join.message");
			if (actionbarMsg != null && !actionbarMsg.equals("")) {
				actionbarMsg = actionbarMsg.replace("%game%", gameName);
				actionbarMsg = actionbarMsg.replace("%player%", p.getName());
				actionbarMsg = RageMode.getLang().colors(actionbarMsg);

				if (conf.getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
					if (conf.getArenasCfg().getBoolean("arenas." + gameName + ".actionbar"))
						ActionBar.sendActionBar(p, actionbarMsg);
				} else if (conf.getCfg().getBoolean("game.global.defaults.actionbar"))
					ActionBar.sendActionBar(p, actionbarMsg);
			}
		}
	}

	private void checkTeleport() {
		GameSpawnGetter gameSpawnGetter = GameUtils.getGameSpawnByName(gameName);
		if (gameSpawnGetter.isGameReady()) {
			teleportPlayersToGameSpawns();
		} else {
			GameUtils.broadcastToGame(gameName, RageMode.getLang().get("game.not-set-up"));
			String[] players = PlayerList.getPlayersInGame(gameName);
			for (String player : players) {
				Player thisPlayer = Bukkit.getPlayer(UUID.fromString(player));
				PlayerList.removePlayer(thisPlayer);
			}
		}
	}

	private void teleportPlayersToGameSpawns() {
		String[] players = PlayerList.getPlayersInGame(gameName);
		for (int i = 0; i < players.length; i++) {
			Player player = Bukkit.getPlayer(UUID.fromString(players[i]));
			Location location = GameUtils.getGameSpawnByName(gameName).getSpawnLocations().get(i);
			player.teleport(location);
		}
	}

	private void setInventories() {
		String[] players = PlayerList.getPlayersInGame(gameName);
		for (String playerUUID : players) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));

			org.bukkit.inventory.PlayerInventory inv = player.getInventory();

			// Removing the lobby items
			inv.clear();

			YamlConfiguration f = conf.getCfg();
			inv.setItem(f.getInt("items.rageBow.slot"), RageBow.getItem());
			inv.setItem(f.getInt("items.rageKnife.slot"), RageKnife.getItem());
			inv.setItem(f.getInt("items.combatAxe.slot"), CombatAxe.getItem());
			inv.setItem(f.getInt("items.rageArrow.slot"), RageArrow.getItem());
			inv.setItem(f.getInt("items.grenade.slot"), Grenade.getItem());
		}
	}

	/*public static List<Entity> getEntities(Player player) {
		List<Entity> entitys = new ArrayList<>();
		for (Entity e : player.getNearbyEntities(5, 5, 5)) {
			if (e instanceof LivingEntity) {
				if (getLookingAt(player, (LivingEntity) e))
					entitys.add(e);
			}
		}

		return entitys;
	}*/

	public static boolean getLookingAt(Player player, LivingEntity livingEntity) {
		Location eye = player.getEyeLocation();
		Vector toEntity = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());

		return dot > 0.99D;
	}
}
