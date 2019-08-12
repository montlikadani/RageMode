package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.Titles;

public class LobbyTimer extends TimerTask {

	private String gameName;
	private int time;

	public LobbyTimer(String gameName, int time) {
		this.gameName = gameName;
		this.time = time;
	}

	public String getGame() {
		return gameName;
	}

	public void setLobbyTime(int time) {
		this.time = time;
	}

	public void addLobbyTime(int newTime) {
		time += newTime;
	}

	public int getLobbyTime() {
		return time;
	}

	public void loadTimer() {
		GameUtils.setStatus(GameStatus.WAITING);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		if (PlayerList.isGameRunning(gameName)) {
			cancel();
			return;
		}

		org.bukkit.configuration.file.FileConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();
		if (PlayerList.getPlayers().size() < conf.getInt("game.global.lobby.min-players-to-start-lobby-timer")) {
			GameUtils.setStatus(GameStatus.STOPPED);
			cancel();
			return;
		}

		List<Integer> values = conf.getIntegerList("game.global.lobby.values-to-send-start-message");
		if (values != null && !values.isEmpty()) {
			for (int val : values) {
				if (time == val) {
					GameUtils.broadcastToGame(gameName,
							RageMode.getLang().get("game.lobby.start-message", "%time%", Integer.toString(time)));
				}
			}
		}

		for (Entry<String, String> players : PlayerList.getPlayers().entrySet()) {
			Player player = Bukkit.getPlayer(UUID.fromString(players.getValue()));

			if (conf.getBoolean("titles.lobby-waiting.enable")) {
				List<Integer> titleValues = conf.getIntegerList("titles.lobby-waiting.values-to-send-start-message");
				String title = conf.getString("titles.lobby-waiting.title");
				String sTitle = conf.getString("titles.lobby-waiting.subtitle");

				title = title.replace("%time%", Integer.toString(time));
				title = title.replace("%game%", gameName);

				sTitle = sTitle.replace("%time%", Integer.toString(time));
				sTitle = sTitle.replace("%game%", gameName);

				if (title != null && sTitle != null) {
					if (titleValues != null && !titleValues.isEmpty()) {
						for (int val : titleValues) {
							if (time == val) {
								Titles.sendTitle(player,
										conf.getInt("titles.lobby-waiting.fade-in"),
										conf.getInt("titles.lobby-waiting.stay"),
										conf.getInt("titles.lobby-waiting.fade-out"),
										title, sTitle);
							}
						}
					}
				}
			}

			if (conf.getBoolean("game.global.lobby.player-level-as-time-counter"))
				player.setLevel(time);
		}

		if (time == 0) {
			cancel();

			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> new GameLoader(gameName));
		}

		time--;
	}
}
