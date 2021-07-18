package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Called when a game has been stopped.
 */
public class RMGameStopEvent extends GameEvent {

	public RMGameStopEvent(BaseGame game) {
		super(game);
	}
}
