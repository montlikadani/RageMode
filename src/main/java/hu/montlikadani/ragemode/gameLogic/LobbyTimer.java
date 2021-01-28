package hu.montlikadani.ragemode.gameLogic;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
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

	public void increaseLobbyTime(int newTime) {
		time += newTime;
	}

	public int getLobbyTime() {
		return time;
	}

	public void beginScheduling() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		Set<PlayerManager> players = game.getPlayers();
		if (game.isGameRunning()) {
			// Set level counter back to 0
			players.forEach(p -> p.getPlayer().setLevel(0));
			cancel();
			return;
		}

		if (players.size() < game.minPlayers) {
			game.setStatus(GameStatus.WAITING);
			players.forEach(p -> p.getPlayer().setLevel(0));
			cancel();
			return;
		}

		for (int value : ConfigValues.getLobbyBeginTimes()) {
			if (time == value) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.lobby.start-message", "%time%", value));
				break;
			}
		}

		if (ConfigValues.isPlayerLevelAsTimeCounter()) {
			players.forEach(pm -> pm.getPlayer().setLevel(time));
		}

		if (ConfigValues.isLobbyTitle()) {
			for (int value : ConfigValues.getLobbyTitleBeginTimes()) {
				if (time == value) {
					String title = ConfigValues.getLobbyTitle(),
							sTitle = ConfigValues.getLobbySubTitle(),
							times = ConfigValues.getLobbyTitleTime();

					title = title.replace("%time%", Integer.toString(value));
					title = title.replace("%game%", game.getName());

					sTitle = sTitle.replace("%time%", Integer.toString(value));
					sTitle = sTitle.replace("%game%", game.getName());

					for (PlayerManager pm : players) {
						GameUtils.sendTitleMessages(pm.getPlayer(), title, sTitle, times);
					}

					break;
				}
			}
		}

		if (time == 0) {
			cancel();
			players.forEach(pl -> pl.getPlayer().setLevel(0));
			new GameLoader(game).startGame();
			return;
		}

		time--;
	}
}
