package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Called when a game has started.
 */
public class RMGameStartEvent extends GameEvent {

	public RMGameStartEvent(BaseGame game) {
		super(game);
	}
}
