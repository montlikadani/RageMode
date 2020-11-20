package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a game has been stopped.
 */
public class RMGameStopEvent extends GameEvent {

	public RMGameStopEvent(Game game) {
		super(game);
	}
}
