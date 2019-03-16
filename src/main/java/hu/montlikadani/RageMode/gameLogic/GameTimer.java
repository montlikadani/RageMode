package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.gameUtils.ActionBar;
import hu.montlikadani.ragemode.gameUtils.BossUtils;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;

public class GameTimer {

	private String gameName;
	private int time;
	private Timer t;

	public GameTimer(String gameName, int time) {
		this.gameName = gameName;
		this.time = time;
	}

	public String getGame() {
		return gameName;
	}

	public int getGameTime() {
		return time;
	}

	public void startCounting() {
		t = new Timer();

		int totalTimerMillis = ((int) (((time * 1000) + 5000) / 10000)) * (10000);
		if (totalTimerMillis == 0)
			totalTimerMillis = 10000;
		final int timeMillisForLoop = totalTimerMillis;

		t.scheduleAtFixedRate(new TimerTask() {
			private int totalMessages = timeMillisForLoop / 60000;
			private int secondsRemaining = timeMillisForLoop / 1000;
			private int pointer = 0;
			// private int totalTimesLooped = 0;
			// private final long startTimeMillis = System.currentTimeMillis();

			public void run() {
				if (totalMessages > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {

					// long timeMillisNow = System.currentTimeMillis();
					// long timeMillisPassed = timeMillisNow - startTimeMillis;

					// if (fullMinute % 1 == 0) {
					// String[] playerUUIDs =
					// PlayerList.getPlayersInGame(gameName);
					// for (int i = 0; i < playerUUIDs.length; i++) {
					// Bukkit.getPlayer(UUID.fromString(playerUUIDs[i]))
					// .sendMessage(ConstantHolder.RAGEMODE_PREFIX +
					// ChatColor.BLUE
					// + "This round will end in " + ChatColor.YELLOW
					// + Integer.toString(totalMessages) + ChatColor.BLUE + "
					// minutes.");
					// }
					// totalMessages--;
					// }
					/*double minutes = Math.floor(secondsRemaining / 60);
					double seconds = secondsRemaining % 60;*/
					secondsRemaining--;
					/*String minutesString;
					String secondsString;
					if (minutes < 10)
						minutesString = "0" + Integer.toString((int) minutes);
					else
						minutesString = Integer.toString((int) minutes);
					secondsString = (seconds < 10) ? "0" + Integer.toString((int) seconds) : Integer.toString((int) seconds);*/

					if (secondsRemaining == 0) {
						cancel();
						RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
							@Override
							public void run() {
								StopGame.stopGame(gameName);
							}
						});
					}

					if (secondsRemaining % 2 == 0) {
						String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
						List<PlayerPoints> playerPoints = new ArrayList<>();
						for (String playerUUID : playerUUIDs) {
							PlayerPoints points = RageScores.getPlayerPoints(playerUUID);
							if (points != null)
								playerPoints.add(points);
						}

						if (playerPoints == null || playerPoints.isEmpty())
							return;

						Collections.sort(playerPoints);

						if (playerPoints.size() > pointer) {
							PlayerPoints points = playerPoints.get(pointer);

							for (String playerUUID : playerUUIDs) {
								String bossMessage = RageMode.getInstance().getConfiguration().getCfg().getString("bossbar-messages.player-actions.message");
								if (bossMessage != null && !bossMessage.equals("")) {
									bossMessage = bossMessage.replace("%pointer%", Integer.toString(pointer + 1));
									bossMessage = bossMessage.replace("%player%", Bukkit.getPlayer(UUID.fromString(points.getPlayerUUID())).getName());
									bossMessage = bossMessage.replace("%points%", Integer.toString(points.getPoints()));
									bossMessage = bossMessage.replace("%kills%", Integer.toString(points.getKills()));
									bossMessage = bossMessage.replace("%deaths%", Integer.toString(points.getDeaths()));
									bossMessage = RageMode.getLang().colors(bossMessage);

									if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".bossbar")) {
										if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.defaults.bossbar"))
											new BossUtils(bossMessage).sendBossBar(gameName, playerUUID,
													BarStyle.valueOf(RageMode.getInstance().getConfiguration().getCfg().getString("bossbar-messages.player-actions.style")));
									} else if (RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".bossbar"))
										new BossUtils(bossMessage).sendBossBar(gameName, playerUUID,
												BarStyle.valueOf(RageMode.getInstance().getConfiguration().getCfg().getString("bossbar-messages.player-actions.style")));
								}

								String actionbarMsg = RageMode.getInstance().getConfiguration().getCfg().getString("actionbar-messages.player-actions.message");
								if (actionbarMsg != null && !actionbarMsg.equals("")) {
									actionbarMsg = actionbarMsg.replace("%pointer%", Integer.toString(pointer + 1));
									actionbarMsg = actionbarMsg.replace("%player%", Bukkit.getPlayer(UUID.fromString(points.getPlayerUUID())).getName());
									actionbarMsg = actionbarMsg.replace("%points%", Integer.toString(points.getPoints()));
									actionbarMsg = actionbarMsg.replace("%kills%", Integer.toString(points.getKills()));
									actionbarMsg = actionbarMsg.replace("%deaths%", Integer.toString(points.getDeaths()));
									actionbarMsg = RageMode.getLang().colors(actionbarMsg);

									if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".actionbar")) {
										if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.defaults.actionbar"))
											ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(playerUUID)), actionbarMsg);
									} else if (RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + gameName + ".actionbar"))
										ActionBar.sendActionBar(Bukkit.getPlayer(UUID.fromString(playerUUID)), actionbarMsg);
								}
							}
							pointer++;
						} else {
							if (pointer >= PlayerList.getPlayersInGame(gameName).length)
								pointer = 0;
						}
					}
				} else {
					if (PlayerList.getPlayersInGame(gameName).length < 0) {
						cancel();
						return;
					}

					cancel();
					RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
						@Override
						public void run() {
							StopGame.stopGame(gameName);
						}
					});
				}
			}
		}, 0, 1000);
	}
}
