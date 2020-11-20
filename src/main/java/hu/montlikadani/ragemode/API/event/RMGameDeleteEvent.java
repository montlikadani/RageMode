package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a game has been deleted.
 */
public class RMGameDeleteEvent extends GameEvent {

	public RMGameDeleteEvent(Game game) {
		super(game);
	}
}
