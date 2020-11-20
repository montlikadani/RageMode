package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a game has started.
 */
public class RMGameStartEvent extends GameEvent {

	public RMGameStartEvent(Game game) {
		super(game);
	}
}
