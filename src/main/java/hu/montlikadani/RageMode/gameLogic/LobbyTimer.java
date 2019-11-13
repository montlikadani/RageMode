package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class LobbyTimer extends TimerTask {

	private Game game;
	private int time;

	public LobbyTimer(Game game, int time) {
		this.game = game;
		this.time = time;
	}

	public Game getGame() {
		return game;
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
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		if (game.isGameRunning()) {
			cancel();
			return;
		}

		hu.montlikadani.ragemode.config.ConfigValues fc = RageMode.getInstance().getConfiguration().getCV();
		if (game.getPlayers().size() < fc.getMinPlayers()) {
			GameUtils.setStatus(game.getName(), null);
			cancel();
			return;
		}

		List<Integer> values = fc.getLobbyTimeMsgs();
		for (int val : values) {
			if (time == val) {
				GameUtils.broadcastToGame(game.getName(),
						RageMode.getLang().get("game.lobby.start-message", "%time%", Integer.toString(time)));
				break;
			}
		}

		for (PlayerManager pm : game.getPlayersFromList()) {
			Player player = pm.getPlayer();

			if (fc.isLobbyTitle()) {
				String title = fc.getLobbyTitle();
				String sTitle = fc.getLobbySubTitle();

				if (title != null && sTitle != null) {
					title = title.replace("%time%", Integer.toString(time));
					title = title.replace("%game%", game.getName());

					sTitle = sTitle.replace("%time%", Integer.toString(time));
					sTitle = sTitle.replace("%game%", game.getName());

					List<Integer> titleValues = fc.getLobbyTitleStartMsgs();
					for (int val : titleValues) {
						if (time == val) {
							String[] split = fc.getLobbyTitleTime().split(", ");
							if (split.length == 3) {
								Titles.sendTitle(player, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
										Integer.parseInt(split[2]), title, sTitle);
							} else {
								Titles.sendTitle(player, 20, 30, 20, title, sTitle);
							}

							break;
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
					() -> new GameLoader(game));
		}

		time--;
	}
}
