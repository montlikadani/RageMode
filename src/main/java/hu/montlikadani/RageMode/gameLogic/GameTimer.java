package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;

public class GameTimer extends TimerTask {

	@Deprecated private TabTitles gameTab = null;
	@Deprecated private ScoreBoard gameBoard = null;
	@Deprecated private ScoreTeam scoreTeam = null;

	private ActionMessengers ac = null;

	private Game game;
	private int time;

	public GameTimer(Game game, int time) {
		this.game = game;
		this.time = time;
	}

	public Game getGame() {
		return game;
	}

	public int getGameTime() {
		return time;
	}

	/**
	 * Moved to {@link ActionMessengers}
	 * @return {@link TabTitles}
	 */
	@Deprecated
	public TabTitles getTabTitles() {
		return gameTab;
	}

	/**
	 * Moved to {@link ActionMessengers}
	 * @return {@link ScoreBoard}
	 */
	@Deprecated
	public ScoreBoard getScoreboard() {
		return gameBoard;
	}

	/**
	 * Moved to {@link ActionMessengers}
	 * @return {@link ScoreTeam}
	 */
	@Deprecated
	public ScoreTeam getScoreTeam() {
		return scoreTeam;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning()) {
				// TODO: fix IllegalPluginAccessException when calling new event
				//GameUtils.setStatus(game.getName(), null);
				cancel();
				return;
			}

			if (game.getPlayers().size() < 2) {
				GameUtils.stopGame(game.getName());
				cancel();
				return;
			}

			String tFormat = Utils.getFormattedTime(time);

			// Broadcast time message should be in this place, before counting
			for (int val : ConfigValues.getGameEndBcs()) {
				if (time == val) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", tFormat));
					break;
				}
			}

			if (ac == null) {
				ac = new ActionMessengers(game, game.getPlayersFromList());
			}

			ac.setScoreboard();
			ac.setTabList();
			ac.setTeam();

			if (time == 0) {
				cancel();
				GameUtils.stopGame(game.getName());
			}

			time--;
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.stopGame(game.getName());
			return;
		}
	}
}
