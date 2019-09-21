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
		GameUtils.setStatus(gameName, GameStatus.WAITING);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		if (Game.isGameRunning(gameName)) {
			cancel();
			return;
		}

		hu.montlikadani.ragemode.config.ConfigValues fc = RageMode.getInstance().getConfiguration().getCV();
		if (Game.getPlayers().size() < fc.getMinPlayers()) {
			GameUtils.setStatus(gameName, null);
			cancel();
			return;
		}

		List<Integer> values = fc.getLobbyTimeMsgs();
		if (values != null && !values.isEmpty()) {
			for (int val : values) {
				if (time == val) {
					GameUtils.broadcastToGame(gameName,
							RageMode.getLang().get("game.lobby.start-message", "%time%", Integer.toString(time)));
				}
			}
		}

		for (Entry<String, String> players : Game.getPlayers().entrySet()) {
			Player player = Bukkit.getPlayer(UUID.fromString(players.getValue()));

			if (fc.isLobbyTitle()) {
				List<Integer> titleValues = fc.getLobbyTitleStartMsgs();
				String title = fc.getLobbyTitle();
				String sTitle = fc.getLobbySubTitle();

				if (title != null && sTitle != null) {
					title = title.replace("%time%", Integer.toString(time));
					title = title.replace("%game%", gameName);

					sTitle = sTitle.replace("%time%", Integer.toString(time));
					sTitle = sTitle.replace("%game%", gameName);

					if (titleValues != null && !titleValues.isEmpty()) {
						for (int val : titleValues) {
							if (time == val) {
								String[] split = fc.getLobbyTitleTime().split(", ");
								if (split.length == 3) {
									Titles.sendTitle(player, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
											Integer.parseInt(split[2]), title, sTitle);
								}
							}
						}
					}
				}
			}

			if (fc.isPlayerLevelAsTimeCounter())
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
