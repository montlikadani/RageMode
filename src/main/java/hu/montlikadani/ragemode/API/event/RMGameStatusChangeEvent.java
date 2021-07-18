package hu.montlikadani.ragemode.API.event;

import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.GameStatus;

/**
 * Called when a game status has been changed.
 */
public class RMGameStatusChangeEvent extends GameEvent {

	private GameStatus status;

	public RMGameStatusChangeEvent(BaseGame game, GameStatus status) {
		super(game);

		this.status = status;
	}

	/**
	 * The new game status.
	 * 
	 * @return {@link GameStatus}
	 */
	@NotNull
	public GameStatus getStatus() {
		return status;
	}
}
