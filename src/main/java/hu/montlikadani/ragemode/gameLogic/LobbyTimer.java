package hu.montlikadani.ragemode.gameLogic;

import java.util.Timer;
import java.util.TimerTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.modules.TitleSender;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Utils;

public final class LobbyTimer {

	private BaseGame game;

	private int time = 200;
	private TimerTask timerTask;

	public LobbyTimer(BaseGame game) {
		this.game = game;
	}

	public BaseGame getGame() {
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

	public void beginScheduling(BaseGame game) {
		if (game != null) {
			this.game = game;
		}

		if (timerTask != null) {
			timerTask.cancel();
		}

		if (this.game == null) {
			throw new IllegalArgumentException("Game cannot be null for lobby timer.");
		}

		setLobbyTime(this.game.getGameLobby().lobbyTime);

		new Timer().scheduleAtFixedRate(timerTask = new TimerTask() {
			@Override
			public void run() {
				perform();
			}
		}, 0, 60 * 20L);
	}

	private void setPlayerLevel(int level) {
		if (ConfigValues.isPlayerLevelAsTimeCounter()) {
			for (PlayerManager pm : game.getPlayers()) {
				org.bukkit.entity.Player player = pm.getPlayer();

				if (player != null) {
					player.setLevel(level);
				}
			}
		}
	}

	private void perform() {
		if (game.getPlayers().size() < game.minPlayers) {
			game.setStatus(GameStatus.WAITING);
			setPlayerLevel(0); // Set level counter back to 0
			timerTask.cancel();
			return;
		}

		if (game.isRunning()) {
			setPlayerLevel(0);
			timerTask.cancel();
			return;
		}

		for (int value : ConfigValues.getLobbyBeginTimes()) {
			if (time == value) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.lobby.start-message", "%time%", value));
				break;
			}
		}

		for (PlayerManager pm : game.getPlayers()) {
			pm.tickMovement();
		}

		setPlayerLevel(time);

		for (int value : ConfigValues.getLobbyTitleBeginTimes()) {
			if (time == value) {
				ConfigValues.TitleSettings lobbyTitleSettings = ConfigValues.getTitleSettings()
						.get(ConfigValues.TitleSettings.TitleType.LOBBY_WAITING);

				if (lobbyTitleSettings != null) {
					String title = lobbyTitleSettings.getTitle(), subTitle = lobbyTitleSettings.getSubTitle(),
							stringTime = Integer.toString(time);

					title = title.replace("%time%", stringTime);
					subTitle = subTitle.replace("%game%", game.getName());

					title = Utils.colors(title.replace("%game%", game.getName()));
					subTitle = Utils.colors(subTitle.replace("%time%", stringTime));

					int[] times = lobbyTitleSettings.getTimes();

					for (PlayerManager pm : game.getPlayers()) {
						TitleSender.sendTitle(pm.getPlayer(), times[0], times[1], times[2], title, subTitle);
					}
				}

				break;
			}
		}

		if (time == 0) {
			timerTask.cancel();
			setPlayerLevel(0);
			new GameLoader(game).startGame();
			return;
		}

		time--;
	}
}
