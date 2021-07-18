package hu.montlikadani.ragemode.API.event;

import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

public class GameEvent extends BaseEvent {

	protected BaseGame game;

	public GameEvent(BaseGame game) {
		this.game = game;
	}

	/**
	 * Returns the game of this event.
	 * 
	 * @return {@link BaseGame}
	 */
	@Nullable
	public BaseGame getGame() {
		return game;
	}
}
