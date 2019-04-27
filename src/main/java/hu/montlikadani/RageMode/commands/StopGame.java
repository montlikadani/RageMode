package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameStopEvent;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameLogic.Reward.Reward;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.MySQLThread;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class StopGame extends RmCommand {

	public StopGame(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.stopgame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length >= 2) {
			if (PlayerList.isGameRunning(args[1])) {
				String game = args[1];
				String[] players = PlayerList.getPlayersInGame(game);

				RageScores.calculateWinner(game, players);

				if (players != null) {
					int i = 0;
					int imax = players.length;

					while (i < imax) {
						if (players[i] != null) {
							PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[i])));
							PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
						}
						i++;
					}
				}

				for (Player key : PlayerList.specPlayer.values()) {
					PlayerList.removeSpectatorPlayer(key);
				}

				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));

				RageScores.removePointsForPlayers(players);
				PlayerList.setGameNotRunning(game);
				PlayerList.setStatus(GameStatus.STOPPED);
				SignCreator.updateAllSigns(game);
			} else
				sendMessage(sender, RageMode.getLang().get("game.not-running"));
		} else
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return;
	}

	public static void stopGame(String game) {
		boolean winnervalid = false;
		if (PlayerList.isGameRunning(game)) {
			PlayerList.setStatus(GameStatus.GAMEFREEZE);

			String[] players = PlayerList.getPlayersInGame(game);

			GameStopEvent gameStopEvent = new GameStopEvent(game, players);
			Bukkit.getPluginManager().callEvent(gameStopEvent);

			String winnerUUID = RageScores.calculateWinner(game, players);
			if (winnerUUID != null) {
				if (UUID.fromString(winnerUUID) != null) {
					if (Bukkit.getPlayer(UUID.fromString(winnerUUID)) != null) {
						winnervalid = true;
						Player winner = Bukkit.getPlayer(UUID.fromString(winnerUUID));
						for (String playerUUID : players) {
							Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
							YamlConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();
							String wonTitle = conf.getString("titles.player-won.title");
							String wonSubtitle = conf.getString("titles.player-won.subtitle");

							String youWonTitle = conf.getString("titles.you-won.title");
							String youWonSubtitle = conf.getString("titles.you-won.subtitle");

							wonTitle = wonTitle.replace("%winner%", winner.getName());
							wonTitle = replaceVariables(wonTitle, winnerUUID);

							youWonTitle = replaceVariables(youWonTitle, winnerUUID);

							wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
							wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);

							youWonSubtitle = replaceVariables(youWonSubtitle, winnerUUID);

							if (wonTitle != null && wonSubtitle != null)
								Titles.sendTitle(player, conf.getInt("titles.player-won.fade-in"), conf.getInt("titles.player-won.stay"),
										conf.getInt("titles.player-won.fade-out"), wonTitle, wonSubtitle);

							if (youWonTitle != null && youWonSubtitle != null)
								Titles.sendTitle(winner, conf.getInt("titles.you-won.fade-in"), conf.getInt("titles.you-won.stay"),
										conf.getInt("titles.you-won.fade-out"), youWonTitle, youWonSubtitle);

							if (conf.getBoolean("game.global.switch-gamemode-to-spectator-at-end-of-game") && player != winner)
								player.setGameMode(GameMode.SPECTATOR);

							PlayerList.removePlayerSynced(player);
						}
					}
				}
			}
			if (!winnervalid) {
				for (String playerUUID : players) {
					Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
					GameUtils.broadcastToGame(game, RageMode.getLang().get("game.no-won"));
					player.setGameMode(GameMode.SPECTATOR);

					PlayerList.removePlayerSynced(player);
				}
			}

			if (!EventListener.waitingGames.containsKey(game))
				EventListener.waitingGames.put(game, true);
			else {
				EventListener.waitingGames.remove(game);
				EventListener.waitingGames.put(game, true);
			}

			final boolean winner = winnervalid;
			final String gameName = game;
			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					Reward reward = new Reward("end-game", game);
					if (winner)
						reward.executeRewards(Bukkit.getPlayer(UUID.fromString(winnerUUID)), true);
					else {
						for (String playerUUID : players) {
							reward.executeRewards(Bukkit.getPlayer(UUID.fromString(playerUUID)), false);
						}
					}

					finishStopping(gameName);
					PlayerList.setStatus(GameStatus.STOPPED);

					if (EventListener.waitingGames.containsKey(gameName))
						EventListener.waitingGames.remove(gameName);
				}
			}, 200);
		}
	}

	private static void finishStopping(String game) {
		if (PlayerList.isGameRunning(game)) {
			final String[] players = PlayerList.getPlayersInGame(game);
			int f = 0;
			int fmax = players.length;
			List<PlayerPoints> lPP = new ArrayList<>();
			while (f < fmax) {
				if (RageScores.getPlayerPoints(players[f]) != null) {
					final PlayerPoints pP = RageScores.getPlayerPoints(players[f]);
					lPP.add(pP);

					if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("mysql") && !lPP.isEmpty()) {
						Thread sthread = new Thread(new MySQLThread(pP));
						sthread.start();
					}

					Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
						try {
							RuntimeRPPManager.updatePlayerEntry(pP);
						} catch (IndexOutOfBoundsException e) {}

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> {
							for (String playerUUID : players) {
								HoloHolder.updateHolosForPlayer(Bukkit.getPlayer(UUID.fromString(playerUUID)));
							}
						});
					});
				}
				f++;
			}
			if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("yaml") && !lPP.isEmpty()) {
				Thread thread = new Thread(YAMLStats.createPlayersStats(lPP));
				thread.start();
			}

			if (players != null) {
				int i = 0;
				int imax = players.length;

				while (i < imax) {
					if (players[i] != null) {
						PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[i])));
						PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
					}
					i++;
				}
			}
			for (Player key : PlayerList.specPlayer.values()) {
				PlayerList.removeSpectatorPlayer(key);
			}
			RageScores.removePointsForPlayers(players);

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
			PlayerList.setGameNotRunning(game);
			SignCreator.updateAllSigns(game);
		}
	}

	public static void stopAllGames() {
		RageMode.logConsole("Searching games to stop...");

		String[] games = GetGames.getGameNames();

		int i = 0;
		int imax = games.length;

		while (i < imax) {
			if (games[i] != null && PlayerList.isGameRunning(games[i])) {

				RageMode.logConsole("Stopping " + games[i] + " ...");

				String[] players = PlayerList.getPlayersInGame(games[i]);
				RageScores.calculateWinner(games[i], players);

				if (players != null) {
					int n = 0;
					int nmax = players.length;

					while (n < nmax) {
						if (players[n] != null) {
							PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[n])));
							PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[n])));
						}
						n++;
					}
				}
				for (Player key : PlayerList.specPlayer.values()) {
					PlayerList.removeSpectatorPlayer(key);
				}
				RageScores.removePointsForPlayers(players);

				PlayerList.setGameNotRunning(games[i]);
				PlayerList.setStatus(GameStatus.STOPPED);

				RageMode.logConsole(games[i] + " has been stopped.");
			}
			i++;
		}
	}

	private static String replaceVariables(String s, String uuid) {
		PlayerPoints score = RageScores.getPlayerPoints(uuid);

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(score.getPoints()));
		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(score.getKills()));
		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(score.getDeaths()));
		return s;
	}
}
