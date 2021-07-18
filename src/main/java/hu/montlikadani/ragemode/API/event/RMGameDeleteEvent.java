package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Called when a game has been deleted.
 */
public class RMGameDeleteEvent extends GameEvent {

	public RMGameDeleteEvent(BaseGame game) {
		super(game);
	}
}
