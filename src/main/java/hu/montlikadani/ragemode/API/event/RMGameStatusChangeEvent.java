package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;

/**
 * Called when a game status has been changed.
 */
public class RMGameStatusChangeEvent extends GameEvent {

	private GameStatus status;

	public RMGameStatusChangeEvent(Game game, GameStatus status) {
		super(game);
		this.status = status;
	}

	/**
	 * The new game status.
	 * 
	 * @return {@link GameStatus}
	 */
	public GameStatus getStatus() {
		return status;
	}
}
