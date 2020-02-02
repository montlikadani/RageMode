package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
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
		List<PlayerManager> list = game.getPlayersFromList();
		if (game.isGameRunning()) {
			// Set level counter back to 0
			list.forEach(p -> p.getPlayer().setLevel(0));
			cancel();
			return;
		}

		if (game.getPlayers().size() < GetGames.getMinPlayers(game.getName())) {
			GameUtils.setStatus(game.getName(), null);
			cancel();
			return;
		}

		List<Integer> values = ConfigValues.getLobbyTimeMsgs();
		for (int val : values) {
			if (time == val) {
				GameUtils.broadcastToGame(game,
						RageMode.getLang().get("game.lobby.start-message", "%time%", time));
				break;
			}
		}

		for (PlayerManager pm : list) {
			Player player = pm.getPlayer();

			if (ConfigValues.isLobbyTitle()) {
				String title = ConfigValues.getLobbyTitle();
				String sTitle = ConfigValues.getLobbySubTitle();

				title = title.replace("%time%", Integer.toString(time));
				title = title.replace("%game%", game.getName());

				sTitle = sTitle.replace("%time%", Integer.toString(time));
				sTitle = sTitle.replace("%game%", game.getName());

				List<Integer> titleValues = ConfigValues.getLobbyTitleStartMsgs();
				for (int val : titleValues) {
					if (time == val) {
						String[] split = ConfigValues.getLobbyTitleTime().split(", ");
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

			if (ConfigValues.isPlayerLevelAsTimeCounter()) {
				player.setLevel(time);
			}
		}

		if (time == 0) {
			cancel();
			list.forEach(pl -> pl.getPlayer().setLevel(0));

			GameLoader loder = new GameLoader(game);
			loder.startGame();
		}

		time--;
	}
}
