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
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameStopEvent;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
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
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class StopGame extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.stopgame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
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

				PlayerList.setGameNotRunning(game);
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
		if (PlayerList.isGameRunning(game)) {
			final String[] players = PlayerList.getPlayersInGame(game);

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

							if (player != winner) {
								Titles.sendTitle(player, conf.getInt("titles.player-won.fade-in"), conf.getInt("titles.player-won.stay"),
										conf.getInt("titles.player-won.fade-out"), wonTitle, wonSubtitle);

								if (conf.getBoolean("game.global.switch-gamemode-to-spectator-at-end-of-game"))
									player.setGameMode(GameMode.SPECTATOR);
							}

							if (player == winner)
								Titles.sendTitle(winner, conf.getInt("titles.you-won.fade-in"), conf.getInt("titles.you-won.stay"),
										conf.getInt("titles.you-won.fade-out"), youWonTitle, youWonSubtitle);

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

			GameUtils.setStatus(GameStatus.GAMEFREEZE);

			final Player winner = winnerUUID != null ? Bukkit.getPlayer(UUID.fromString(winnerUUID)) : null;
			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					GameUtils.setStatus(GameStatus.STOPPED);

					if (EventListener.waitingGames.containsKey(game))
						EventListener.waitingGames.remove(game);

					Reward reward = new Reward("end-game", game);
					for (String playerUUID : players) {
						Utils.clearPlayerInventory(Bukkit.getPlayer(UUID.fromString(playerUUID)));

						if (winner != null) {
							if (Bukkit.getPlayer(UUID.fromString(playerUUID)) == winner) {
								Utils.clearPlayerInventory(winner);

								reward.rewardForWinner(winner);
							} else
								reward.rewardForPlayers(winner);
						}
					}

					finishStopping(game);
				}
			}, RageMode.getInstance().getConfiguration().getCfg()
					.getInt("game.global.game-freeze-time-at-end-game") * 20);
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
			if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("yaml")) {
				Thread thread = new Thread(YAMLStats.createPlayersStats(lPP));
				thread.start();
			}

			if (players != null) {
				int i = 0;
				int imax = players.length;

				while (i < imax) {
					if (players[i] != null)
						PlayerList.removePlayer(Bukkit.getPlayer(UUID.fromString(players[i])));
					i++;
				}
			}
			for (Player key : PlayerList.specPlayer.values()) {
				PlayerList.removeSpectatorPlayer(key);
			}

			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));
			PlayerList.setGameNotRunning(game);
			GameUtils.runCommands(game, "stop");
			SignCreator.updateAllSigns(game);
			if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game-stop.restart-server")) {
				try {
					Class.forName("org.spigotmc.SpigotConfig");

					Bukkit.spigot().restart();
				} catch (ClassNotFoundException e) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
				}
			} else if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game-stop.stop-server"))
				Bukkit.shutdown();
		}
	}

	public static void stopAllGames() {
		RageMode.logConsole("[RageMode] Searching games to stop...");

		String[] games = GetGames.getGameNames();
		if (games == null)
			return;

		int i = 0;
		int imax = games.length;

		while (i < imax) {
			if (games[i] != null && PlayerList.isGameRunning(games[i])) {

				RageMode.logConsole("[RageMode] Stopping " + games[i] + " ...");

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

				PlayerList.setGameNotRunning(games[i]);
				GameUtils.setStatus(GameStatus.STOPPED);

				RageMode.logConsole("[RageMode] " + games[i] + " has been stopped.");
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