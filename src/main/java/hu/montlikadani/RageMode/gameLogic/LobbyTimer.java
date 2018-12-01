package hu.montlikadani.RageMode.gameLogic;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;

import hu.montlikadani.RageMode.RageMode;

public class LobbyTimer {

	private String gameName;
	private int secondsRemaining;
	private Timer t;

	public LobbyTimer(String gameName) {
		this.gameName = gameName;

		getSecondsToWait();
		sendTimerMessages();
	}

	private void getSecondsToWait() {
		if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".lobbydelay")) {
			if (RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobbydelay") > 0)
				secondsRemaining = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.lobbydelay");
			else
				secondsRemaining = 30;
		}

		if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName + ".lobbydelay"))
			secondsRemaining = RageMode.getInstance().getConfiguration().getArenasCfg().getInt("arenas." + gameName + ".lobbydelay");
	}

	private void sendTimerMessages() {
		t = new Timer();

		int totalTimerMillis = ((int) (((secondsRemaining * 1000) + 5000) / 10000)) * (10000);
		if (totalTimerMillis == 0)
			totalTimerMillis = 10000;
		final int timeMillisForLoop = totalTimerMillis;

		t.scheduleAtFixedRate(new TimerTask() {
			private int totalMessagesBeforeTen = timeMillisForLoop / 10000;

			public void run() {
				if (totalMessagesBeforeTen > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
					for (int i = 0; i < playerUUIDs.length; i++) {
						Bukkit.getPlayer(UUID.fromString(playerUUIDs[i])).sendMessage(RageMode.getLang().get("game.lobby-message", "%time%",
								Integer.toString(totalMessagesBeforeTen * 10)));
					}
					totalMessagesBeforeTen--;
					if (totalMessagesBeforeTen == 0) {
						this.cancel();
						startTimer();
					}
				} else if (PlayerList.getPlayersInGame(gameName).length < 2)
					this.cancel();
			}
		}, 0, 10000);
	}

	private void startTimer() {
		t.scheduleAtFixedRate(new TimerTask() {
			private int timesToSendMessage = 9;

			@Override
			public void run() {
				if (timesToSendMessage > 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					String[] playerUUIDs = PlayerList.getPlayersInGame(gameName);
					for (int i = 0; i < playerUUIDs.length; i++) {
						Bukkit.getPlayer(UUID.fromString(playerUUIDs[i])).sendMessage(RageMode.getLang().get("game.lobby-message", "%time%",
								Integer.toString(timesToSendMessage)));
					}
					timesToSendMessage--;
				} else if (timesToSendMessage == 0 && PlayerList.getPlayersInGame(gameName).length >= 2) {
					this.cancel();
					RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
						@Override
						public void run() {
							new GameLoader(gameName);
						}
					});
				} else
					this.cancel();
			}
		}, 0, 1000);
	}
}
