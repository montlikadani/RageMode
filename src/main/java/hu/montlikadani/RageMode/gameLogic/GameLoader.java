package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.scoreboard.ScoreBoard;
import hu.montlikadani.ragemode.scoreboard.ScoreBoardHolder;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.tablist.TabTitles;

public class GameLoader {

	private String gameName;
	private List<Location> gameSpawns = new ArrayList<>();

	public GameLoader(String gameName) {
		this.gameName = gameName;

		PlayerList.setGameRunning(gameName);
		checkTeleport();
		setInventories();

		List<String> players = Arrays.asList(PlayerList.getPlayersInGame(gameName));

		@SuppressWarnings("deprecation")
		ScoreBoard gameBoard = new ScoreBoard(players, true);
		TabTitles gameTab = new TabTitles(3, ChatColor.GOLD + "RageMode", ChatColor.YELLOW + "0 / 0 " + ChatColor.GREEN + "K/D" + ChatColor.RESET
				+ "\n" + ChatColor.YELLOW + "0 " + ChatColor.GREEN + "Points");

		gameBoard.setTitle(ChatColor.GOLD + "RageMode" + ChatColor.RESET);
		String kdLine = ChatColor.YELLOW + "0 / 0 " + ChatColor.GREEN + "K/D" + ChatColor.RESET;
		gameBoard.setLine(kdLine, 0);
		String pointsLine = ChatColor.YELLOW + "0 " + ChatColor.GREEN + "Points" + ChatColor.RESET;
		gameBoard.setLine(pointsLine, 1);
		gameBoard.setScoreBoard();
		gameBoard.addToScoreBoards(gameName, true);
		gameTab.addToTabList(gameName, true);
		for (String player : players) {
			ScoreBoardHolder sHolder = gameBoard.getScoreboards().get(Bukkit.getPlayer(UUID.fromString(player)));
			sHolder.setOldKdLine(kdLine);
			sHolder.setOldPointsLine(pointsLine);
		}
		new GameTimer(gameName);
		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable"))
			SignCreator.updateAllSigns(gameName);

		String ver = RageMode.getVer();
		String initialMessage = ChatColor.DARK_AQUA + "Welcome to " + ChatColor.DARK_PURPLE + gameName + ChatColor.DARK_AQUA + " game!";
		if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".bossbar")
				&& RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".bossbar")) {
			if (!(ver.equals("1_8_R1") || ver.equals("1_8_R2") || ver.equals("1_8_R3"))) {
				for (String player : players) {
					BossBar boss = Bukkit.createBossBar(initialMessage, BarColor.RED, BarStyle.SOLID);
					boss.addPlayer(Bukkit.getPlayer(UUID.fromString(player)));

					for (int i = 1; i <= 6; ++i) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
							@Override
							public void run() {
								if (boss.getProgress() >= 0.2D)
									boss.setProgress(boss.getProgress() - 0.2D);
								else
									boss.removeAll();
							}
						}, (long) (20 * i));
					}
				}
			} else
				RageMode.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
		} else {
			if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("settings.global.bossbar")) {
				if (!(ver.equals("1_8_R1") || ver.equals("1_8_R2") || ver.equals("1_8_R3"))) {
					for (String player : players) {
						BossBar boss = Bukkit.createBossBar(initialMessage, BarColor.RED, BarStyle.SOLID);
						boss.addPlayer(Bukkit.getPlayer(UUID.fromString(player)));

						for (int i = 1; i <= 6; ++i) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
								@Override
								public void run() {
									if (boss.getProgress() >= 0.2D)
										boss.setProgress(boss.getProgress() - 0.2D);
									else
										boss.removeAll();
								}
							}, (long) (20 * i));
						}
					}
				} else
					RageMode.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			}
		}

		for (String player : players) {
			if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
				if (RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".actionbar"))
					ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), initialMessage);
			} else if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.actionbar"))
				ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(player)), initialMessage);
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
			FileConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();
			player.getInventory().setItem(conf.getInt("items.rageBow.slot"), RageBow.getRageBow());
			player.getInventory().setItem(conf.getInt("items.rageKnife.slot"), RageKnife.getRageKnife());
			player.getInventory().setItem(conf.getInt("items.combatAxe.slot"), CombatAxe.getCombatAxe());
			player.getInventory().setItem(conf.getInt("items.rageArrow.slot"), RageArrow.getRageArrow());
			player.getInventory().setItem(conf.getInt("items.grenade.slot"), Grenade.getGrenade());
		}
	}
}
