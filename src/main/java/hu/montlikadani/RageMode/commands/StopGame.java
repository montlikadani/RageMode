package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameStopEvent;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.Reward.Reward;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.MySQLThread;
import hu.montlikadani.ragemode.statistics.SQLThread;
import hu.montlikadani.ragemode.statistics.YAMLStats;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class StopGame extends ICommand {

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.stopgame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];

			if (Game.isGameRunning(game)) {
				RageScores.calculateWinner(game, Game.getPlayersFromList());

				for (Map.Entry<String, String> players : Game.getPlayers().entrySet()) {
					Player player = Bukkit.getPlayer(UUID.fromString(players.getValue()));

					player.removeMetadata("killedWith", RageMode.getInstance());
					Game.removePlayer(player);
				}

				for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
						.hasNext();) {
					Player pl = Bukkit.getPlayer(it.next().getKey());
					Game.removeSpectatorPlayer(pl);
				}

				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));

				Game.setGameNotRunning(game);
				GameUtils.setStatus(GameStatus.STOPPED);
				SignCreator.updateAllSigns(game);
			} else
				sendMessage(sender, RageMode.getLang().get("game.not-running"));
		} else
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return false;
	}

	public static void stopGame(final String game) {
		boolean winnervalid = false;
		if (Game.isGameRunning(game)) {
			final List<String> players = Game.getPlayersFromList();

			GameStopEvent gameStopEvent = new GameStopEvent(game, players);
			Utils.callEvent(gameStopEvent);

			String winnerUUID = RageScores.calculateWinner(game, players);
			if (winnerUUID != null) {
				if (UUID.fromString(winnerUUID) != null) {
					if (Bukkit.getPlayer(UUID.fromString(winnerUUID)) != null) {
						winnervalid = true;
						Player winner = Bukkit.getPlayer(UUID.fromString(winnerUUID));

						for (String playersUUID : players) {
							Player player = Bukkit.getPlayer(UUID.fromString(playersUUID));
							hu.montlikadani.ragemode.config.ConfigValues cv = RageMode.getInstance().getConfiguration()
									.getCV();
							String wonTitle = cv.getWonTitle();
							String wonSubtitle = cv.getWonSubTitle();

							String youWonTitle = cv.getYouWonTitle();
							String youWonSubtitle = cv.getYouWonSubTitle();

							wonTitle = wonTitle.replace("%winner%", winner.getName());
							wonTitle = replaceVariables(wonTitle, winnerUUID);

							youWonTitle = replaceVariables(youWonTitle, winnerUUID);

							wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
							wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);

							youWonSubtitle = replaceVariables(youWonSubtitle, winnerUUID);

							String[] split = null;
							if (player != winner) {
								split = cv.getWonTitleTime().split(", ");
								if (split.length == 3) {
									Titles.sendTitle(player, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
											Integer.parseInt(split[2]), wonTitle, wonSubtitle);
								}

								if (cv.isSwitchGMForPlayers())
									player.setGameMode(GameMode.SPECTATOR);
							} else {
								split = cv.getYouWonTitleTime().split(", ");
								if (split.length == 3) {
									Titles.sendTitle(winner, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
											Integer.parseInt(split[2]), youWonTitle, youWonSubtitle);
								}
							}

							Game.removePlayerSynced(player);
						}
					}
				}
			}
			if (!winnervalid) {
				for (String playerUUID : players) {
					Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
					GameUtils.broadcastToGame(game, RageMode.getLang().get("game.no-won"));
					// Why?
					Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
							() -> player.setGameMode(GameMode.SPECTATOR));

					Game.removePlayerSynced(player);
				}
			}

			if (!EventListener.waitingGames.containsKey(game))
				EventListener.waitingGames.put(game, true);
			else {
				EventListener.waitingGames.remove(game);
				EventListener.waitingGames.put(game, true);
			}

			GameUtils.setStatus(GameStatus.GAMEFREEZE);

			final Player winner = winnerUUID != null ? Bukkit.getPlayer(UUID.fromString(winnerUUID)) : null;
			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					GameUtils.setStatus(GameStatus.STOPPED);

					if (EventListener.waitingGames.containsKey(game))
						EventListener.waitingGames.remove(game);

					if (RageMode.getInstance().getConfiguration().getCV().isRewardEnabled()) {
						Reward reward = new Reward(game);

						for (String playerUUID : players) {
							Utils.clearPlayerInventory(Bukkit.getPlayer(UUID.fromString(playerUUID)));

							if (winner != null) {
								if (Bukkit.getPlayer(UUID.fromString(playerUUID)) == winner) {
									Utils.clearPlayerInventory(winner);

									reward.rewardForWinner(winner);
								} else
									reward.rewardForPlayers(winner, Bukkit.getPlayer(UUID.fromString(playerUUID)));
							}
						}
					}

					finishStopping(game);
				}
			}, RageMode.getInstance().getConfiguration().getCV().getGameFreezeTime() * 20);
		}
	}

	private static void finishStopping(String game) {
		if (Game.isGameRunning(game)) {
			List<PlayerPoints> lPP = new ArrayList<>();
			List<String> players = Game.getPlayersFromList();

			String stats = RageMode.getInstance().getConfiguration().getCV().getStatistics();
			for (String playersUUID : players) {
				if (playersUUID != null && RageScores.getPlayerPoints(playersUUID) != null) {
					final PlayerPoints pP = RageScores.getPlayerPoints(playersUUID);
					lPP.add(pP);

					Thread th = null;
					if (stats.equals("mysql")) {
						th = new Thread(new MySQLThread(pP));
					} else if (stats.equals("sql") || stats.equals("sqlite")) {
						th = new Thread(new SQLThread(pP));
					}

					if (th != null) {
						th.start();
					}

					Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
						RuntimeRPPManager.updatePlayerEntry(pP);

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () ->
							HoloHolder.updateHolosForPlayer(Bukkit.getPlayer(UUID.fromString(playersUUID))));
					});
				}
			}

			if (stats.equals("yaml")) {
				Thread thread = new Thread(YAMLStats.createPlayersStats(lPP));
				thread.start();
			}

			for (String playersUUID : players) {
				if (playersUUID != null) {
					GameUtils.sendActionBarMessages(Bukkit.getPlayer(UUID.fromString(playersUUID)), game, "stop");
					RageScores.removePointsForPlayer(playersUUID);
					Game.removePlayer(Bukkit.getPlayer(UUID.fromString(playersUUID)));
				}
			}

			for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
					.hasNext();) {
				Player pl = Bukkit.getPlayer(it.next().getKey());
				Game.removeSpectatorPlayer(pl);
			}

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
			Game.setGameNotRunning(game);
			GameUtils.runCommandsForAll(game, "stop");
			SignCreator.updateAllSigns(game);
			if (RageMode.getInstance().getConfiguration().getCV().isRestartServerEnabled()) {
				try {
					Class.forName("org.spigotmc.SpigotConfig");

					Bukkit.spigot().restart();
				} catch (ClassNotFoundException e) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
				}
			} else if (RageMode.getInstance().getConfiguration().getCV().isStopServerEnabled())
				Bukkit.shutdown();
		}
	}

	public static void stopAllGames() {
		Debug.logConsole("Searching games to stop...");

		String[] games = GetGames.getGameNames();
		if (games == null)
			return;

		int i = 0;
		int imax = games.length;

		while (i < imax) {
			if (games[i] != null && Game.isGameRunning(games[i])) {

				Debug.logConsole("Stopping " + games[i] + " ...");

				Game.getPlayersFromList()
						.forEach(uuids -> Game.removePlayer(Bukkit.getPlayer(UUID.fromString(uuids))));

				for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
						.hasNext();) {
					Player pl = Bukkit.getPlayer(it.next().getKey());
					Game.removeSpectatorPlayer(pl);
				}

				Game.setGameNotRunning(games[i]);
				GameUtils.setStatus(GameStatus.STOPPED);

				Debug.logConsole(games[i] + " has been stopped.");
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
