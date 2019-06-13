package hu.montlikadani.ragemode.gameLogic;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.Titles;

public class LobbyTimer extends TimerTask {

	private String gameName;
	private int time;

	public static HashMap<String, TimerTask> map = new HashMap<>();

	public LobbyTimer(String gameName, int time) {
		this.gameName = gameName;
		this.time = time;
	}

	public String getGame() {
		return gameName;
	}

	public int getLobbyTime() {
		return time;
	}

	public void loadTimer() {
		GameUtils.setStatus(GameStatus.WAITING);
		// Put the timer to map to cancel when use force start
		if (map.containsKey(gameName)) {
			cancel();
			map.remove(gameName);
		}
		map.put(gameName, this);

		new Timer().scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		if (PlayerList.isGameRunning(gameName)) {
			cancel();
			return;
		}
		String[] playersInGame = PlayerList.getPlayersInGame(gameName);
		org.bukkit.configuration.file.YamlConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();
		if (playersInGame.length < conf.getInt("game.global.lobby.min-players-to-start-lobby-timer")) {
			cancel();
			return;
		}

		Player player = Bukkit.getPlayer(UUID.fromString(playersInGame[playersInGame.length - 1]));

		List<Integer> values = conf.getIntegerList("game.global.lobby.values-to-send-start-message");
		if (values != null && !values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				if (time == values.get(i)) {
					GameUtils.broadcastToGame(gameName,
							RageMode.getLang().get("game.lobby.start-message", "%time%", Integer.toString(time)));
				}
			}
		}

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
					for (int i = 0; i < titleValues.size(); i++) {
						if (time == titleValues.get(i)) {
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

		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("game.global.lobby.player-level-as-time-counter"))
			player.setLevel(time);

		if (time == 0) {
			cancel();
			map.remove(gameName);

			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> new GameLoader(gameName));
		}

		time--;
	}
}
