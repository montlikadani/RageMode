package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameStopEvent;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
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
			sender.sendMessage(RageMode.getLang().get("no-permission"));
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
							PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
							PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[i])));
						}
						i++;
					}
				}
				GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));

				RageScores.removePointsForPlayers(players);
				PlayerList.setGameNotRunning(game);
				SignCreator.updateAllSigns(game);
			} else
				sender.sendMessage(RageMode.getLang().get("game.not-running"));
		} else
			sender.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return;
	}

	public static void stopGame(String game) {
		boolean winnervalid = false;
		if (PlayerList.isGameRunning(game)) {
			PlayerList.setStatus(GameStatus.GAMEFREEZE);

			GameStopEvent gameStopEvent = new GameStopEvent(game);
			Bukkit.getPluginManager().callEvent(gameStopEvent);

			String[] players = PlayerList.getPlayersInGame(game);
			String winnerUUID = RageScores.calculateWinner(game, players);
			if (winnerUUID != null) {
				if (UUID.fromString(winnerUUID) != null) {
					if (Bukkit.getPlayer(UUID.fromString(winnerUUID)) != null) {
						winnervalid = true;
						Player winner = Bukkit.getPlayer(UUID.fromString(winnerUUID));
						for (String playerUUID : players) {
							Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
							FileConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();
							String title = conf.getString("titles.player-won.title");
							String subtitle = conf.getString("titles.player-won.subtitle");
							String title2 = conf.getString("titles.you-won.title");
							String subtitle2 = conf.getString("titles.you-won.subtitle");

							title = title.replace("%winner%", winner.getName());
							title = replaceVariables(title, winnerUUID);

							title2 = replaceVariables(title2, winnerUUID);

							subtitle = subtitle.replace("%winner%", winner.getName());
							subtitle = replaceVariables(subtitle, winnerUUID);

							subtitle2 = replaceVariables(subtitle2, winnerUUID);

							if (title != null && !title.equals("") && subtitle != null && !subtitle.equals(""))
								Titles.sendTitle(player, conf.getInt("titles.player-won.fade-in"), conf.getInt("titles.player-won.stay"),
										conf.getInt("titles.player-won.fade-out"), title, subtitle);

							if (title2 != null && !title2.equals("") && subtitle2 != null && !subtitle2.equals(""))
								Titles.sendTitle(winner, conf.getInt("titles.you-won.fade-in"), conf.getInt("titles.you-won.stay"),
										conf.getInt("titles.you-won.fade-out"), title2, subtitle2);

							if (conf.getBoolean("game.global.switch-gamemode-to-spectator-at-end-of-game"))
								player.setGameMode(GameMode.SPECTATOR);

							PlayerList.removePlayerSynced(player);
							PlayerList.setStatus(GameStatus.STOPPED);
						}
					}
				}
			}
			if (winnervalid == false) {
				for (String playerUUID : players) {
					Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
					GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.no-won"));
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

			final String gameName = game;
			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					finishStopping(gameName);
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

					if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("mysql")) {
						Thread sthread = new Thread(new MySQLThread(pP));
						sthread.start();
					}

					Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), new Runnable() {
						@Override
						public void run() {
							RuntimeRPPManager.updatePlayerEntry(pP);
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {

								@Override
								public void run() {
									for (String playerUUID : players) {
										HoloHolder.updateHolosForPlayer(Bukkit.getPlayer(UUID.fromString(playerUUID)));
									}
								}
							});
						}
					});
				}
				f++;
			}
			Thread thread = new Thread(YAMLStats.createPlayersStats(lPP));
			thread.start();

			if (players != null) {
				int i = 0;
				int imax = players.length;

				while (i < imax) {
					if (players[i] != null) {
						PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
						PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[i])));
					}
					i++;
				}
			}
			RageScores.removePointsForPlayers(players);

			GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
			PlayerList.setGameNotRunning(game);
			SignCreator.updateAllSigns(game);
		}
	}

	public static void stopAllGames() {
		RageMode.logConsole(Level.INFO, "Searching games to stop...");

		String[] games = GetGames.getGameNames();

		int i = 0;
		int imax = games.length;

		while (i < imax) {
			if (PlayerList.isGameRunning(games[i])) {

				RageMode.logConsole(Level.INFO, "Stopping " + games[i] + " ...");

				String[] players = PlayerList.getPlayersInGame(games[i]);
				RageScores.calculateWinner(games[i], players);

				if (players != null) {
					int n = 0;
					int nmax = players.length;

					while (n < nmax) {
						if (players[n] != null) {
							PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[n])));
							PlayerList.removePlayerSynced(Bukkit.getPlayer(UUID.fromString(players[n])));
						}
						n++;
					}
				}
				RageScores.removePointsForPlayers(players);
				PlayerList.setStatus(GameStatus.STOPPED);
				PlayerList.setGameNotRunning(games[i]);
				SignCreator.updateAllSigns(games[i]);

				RageMode.logConsole(Level.INFO, games[i] + " has been stopped.");
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
