package hu.montlikadani.ragemode.gameLogic;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ForceStart;

public class LobbyTimer {

	private String gameName;
	private int time;
	private Timer t;

	public LobbyTimer(String gameName, int time) {
		this.gameName = gameName;
		this.time = time;

		PlayerList.setStatus(GameStatus.WAITING);
	}

	public String getGame() {
		return gameName;
	}

	public int getLobbyTime() {
		return time;
	}

	public void sendTimerMessages() {
		t = new Timer();

		int totalTimerMillis = ((int) (((time * 1000) + 5000) / 10000)) * (10000);
		if (totalTimerMillis == 0)
			totalTimerMillis = 10000;
		final int timeMillisForLoop = totalTimerMillis;

		t.scheduleAtFixedRate(new TimerTask() {
			private int totalMessagesBeforeTen = timeMillisForLoop / 10000;

			public void run() {
				if (totalMessagesBeforeTen > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					if (ForceStart.toStopTask.contains(gameName)) {
						cancel();
						ForceStart.toStopTask.remove(gameName);
					} else {
						String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
						for (int i = 0; i < playerUUIDs.length; i++) {
							Bukkit.getPlayer(UUID.fromString(playerUUIDs[i])).sendMessage(RageMode.getLang().get("game.lobby.start-message", "%time%",
									Integer.toString(totalMessagesBeforeTen * 10)));

							setLevelCounter(playerUUIDs[i], totalMessagesBeforeTen * 10);
						}

						totalMessagesBeforeTen--;

						if (totalMessagesBeforeTen == 0) {
							cancel();
							startTimer();
						}
					}
				} else if (PlayerList.getPlayersInGame(gameName).length < 2)
					cancel();
			}
		}, 0, 10000);
	}

	private void startTimer() {
		t.scheduleAtFixedRate(new TimerTask() {
			private int timesToSendMessage = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobby.time-to-send-message");

			@Override
			public void run() {
				if (timesToSendMessage > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					if (ForceStart.toStopTask.contains(gameName)) {
						cancel();
						ForceStart.toStopTask.remove(gameName);
					} else {
						String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
						for (int i = 0; i < playerUUIDs.length; i++) {
							Bukkit.getPlayer(UUID.fromString(playerUUIDs[i])).sendMessage(RageMode.getLang().get("game.lobby.start-message", "%time%",
									Integer.toString(timesToSendMessage)));

							setLevelCounter(playerUUIDs[i], timesToSendMessage);
						}
						timesToSendMessage--;
					}
				} else if (timesToSendMessage == 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					cancel();
					String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
					for (int i = 0; i < playerUUIDs.length; i++) {
						Bukkit.getPlayer(UUID.fromString(playerUUIDs[i])).getInventory().clear();

						setLevelCounter(playerUUIDs[i], 0);
					}

					RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
						@Override
						public void run() {
							new GameLoader(gameName);
						}
					});
				} else
					cancel();
			}
		}, 0, 1000);
	}

	private void setLevelCounter(String player, int timer) {
		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.lobby.player-level-as-time-counter"))
			Bukkit.getPlayer(UUID.fromString(player)).setLevel(timer);
	}
}
