package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameStartEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.BossUtils;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.scoreboard.ScoreBoardHolder;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameLoader {

	private String gameName;
	private List<Location> gameSpawns = new ArrayList<>();
	public static BukkitTask task, task2;

	private Configuration conf;
	private GameTimer gameTimer;

	public GameLoader(String gameName) {
		this.gameName = gameName;
		conf = RageMode.getInstance().getConfiguration();

		GameStartEvent gameStartEvent = new GameStartEvent(gameName);
		Bukkit.getPluginManager().callEvent(gameStartEvent);

		PlayerList.setGameRunning(gameName);
		PlayerList.setStatus(GameStatus.RUNNING);
		checkTeleport();
		setInventories();

		List<String> players = Arrays.asList(PlayerList.getPlayersInGame(gameName));
		setGameTab(players);
		setGameScoreboard(players);

		int time = 0;
		if (!conf.getArenasCfg().isSet("arenas." + gameName + ".gametime")) {
			if (conf.getCfg().getInt("game.global.defaults.gametime") > 0)
				time = conf.getCfg().getInt("game.global.defaults.gametime") * 60;
			else
				time = 300;
		} else
			time = conf.getArenasCfg().getInt("arenas." + gameName + ".gametime") * 60;
		gameTimer = new GameTimer(gameName, time);
		gameTimer.startCounting();

		SignCreator.updateAllSigns(gameName);

		String bossMessage = conf.getCfg().getString("bossbar-messages.join.message");
		if (bossMessage != null && !bossMessage.equals("")) {
			for (String player : players) {
				bossMessage = bossMessage.replace("%game%", gameName);
				bossMessage = bossMessage.replace("%player%", Bukkit.getPlayer(UUID.fromString(player)).getName());
				bossMessage = RageMode.getLang().colors(bossMessage);

				if (conf.getArenasCfg().isSet("arenas." + gameName + ".bossbar")) {
					if (conf.getArenasCfg().getBoolean("arenas." + gameName + ".bossbar"))
						new BossUtils(bossMessage).sendBossBar(gameName, player,
								BarStyle.valueOf(conf.getCfg().getString("bossbar-messages.join.style")));
				} else {
					if (conf.getCfg().getBoolean("game.global.defaults.bossbar"))
						new BossUtils(bossMessage).sendBossBar(gameName, player,
								BarStyle.valueOf(conf.getCfg().getString("bossbar-messages.join.style")));
				}
			}
		}

		String actionbarMsg = conf.getCfg().getString("actionbar-messages.join.message");
		if (actionbarMsg != null && !actionbarMsg.equals("")) {
			for (String player : players) {
				actionbarMsg = actionbarMsg.replace("%game%", gameName);
				actionbarMsg = actionbarMsg.replace("%player%", Bukkit.getPlayer(UUID.fromString(player)).getName());
				actionbarMsg = RageMode.getLang().colors(actionbarMsg);

				if (conf.getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
					if (conf.getArenasCfg().getBoolean("arenas." + gameName + ".actionbar"))
						ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), actionbarMsg);
				} else if (conf.getCfg().getBoolean("game.global.defaults.actionbar"))
					ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), actionbarMsg);
			}
		}
	}

	private void checkTeleport() {
		GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(gameName);
		if (gameSpawnGetter.isGameReady()) {
			gameSpawns = gameSpawnGetter.getSpawnLocations();
			teleportPlayersToGameSpawns();
		} else {
			GameBroadcast.broadcastToGame(gameName, RageMode.getLang().get("game.not-set-up"));
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
			Location location = gameSpawns.get(i);
			player.teleport(location);
		}
	}

	private void setInventories() {
		String[] players = PlayerList.getPlayersInGame(gameName);
		for (String playerUUID : players) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			FileConfiguration f = conf.getCfg();
			player.getInventory().setItem(f.getInt("items.rageBow.slot"), RageBow.getRageBow());
			player.getInventory().setItem(f.getInt("items.rageKnife.slot"), RageKnife.getRageKnife());
			player.getInventory().setItem(f.getInt("items.combatAxe.slot"), CombatAxe.getCombatAxe());
			player.getInventory().setItem(f.getInt("items.rageArrow.slot"), RageArrow.getRageArrow());
			player.getInventory().setItem(f.getInt("items.grenade.slot"), Grenade.getGrenade());
		}
	}

	private void setGameScoreboard(List<String> listPlayers) {
		if (!conf.getCfg().getBoolean("game.global.scoreboard.enable")) return;

		String[] players = PlayerList.getPlayersInGame(gameName);
		for (String playerUUID : players) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			ScoreBoard gameBoard = new ScoreBoard(listPlayers, false);

			task = Bukkit.getScheduler().runTaskTimerAsynchronously(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					String boardTitle = conf.getCfg().getString("game.global.scoreboard.title");
					if (boardTitle != null && !boardTitle.equals(""))
						gameBoard.setTitle(RageMode.getLang().colors(boardTitle));

					List<String> rows = conf.getCfg().getStringList("game.global.scoreboard.content");
					if (rows != null && !rows.isEmpty()) {
						int rowMax = rows.size();

						for (String row : rows) {
							if (row.trim().equals("")) {
								for (int i = 0; i <= rowMax; i++) {
									row = row + " ";
								}
							}

							row = Utils.setPlaceholders(row, player);
							row = row.replace("%game-time%", Integer.toString(gameTimer.getGameTime()));

							gameBoard.setLine(row, rowMax);
							rowMax--;

							gameBoard.setScoreBoard();
							gameBoard.addToScoreBoards(gameName, true);

							ScoreBoardHolder sHolder = gameBoard.getScoreboards().get(player);
							sHolder.setOldKdLine(row);
							sHolder.setOldPointsLine(row);
						}
					}
				}
			}, 3 * 20L, 3 * 20L);
		}
	}

	private void setGameTab(List<String> listPlayers) {
		if (!conf.getCfg().getBoolean("game.global.tablist.list.enable")) return;

		String[] players = PlayerList.getPlayersInGame(gameName);
		for (String playerUUID : players) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			TabTitles gameTab = new TabTitles(listPlayers);

			task2 = Bukkit.getScheduler().runTaskTimerAsynchronously(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					List<String> tabHeader = conf.getCfg().getStringList("game.global.tablist.list.header");
					List<String> tabFooter = conf.getCfg().getStringList("game.global.tablist.list.footer");

					String he = "";
					String fo = "";
					int s = 0;
					for (String line : tabHeader) {
						s++;
						if (s > 1)
							he = he + "\n\u00a7r";

						he = he + line;
					}
					s = 0;
					for (String line : tabFooter) {
						s++;
						if (s > 1)
							fo = fo + "\n\u00a7r";

						fo = fo + line;
					}

					he = Utils.setPlaceholders(he, player);
					he = he.replace("%game-time%", Integer.toString(gameTimer.getGameTime()));

					fo = Utils.setPlaceholders(fo, player);
					fo = fo.replace("%game-time%", Integer.toString(gameTimer.getGameTime()));

					gameTab.sendTabTitle(he, fo);

					gameTab.addToTabList(gameName, true);
				}
			}, 3 * 20L, 3 * 20L);
		}
	}
}
