package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameDeleteEvent extends GameEvent {

	public RMGameDeleteEvent(Game game) {
		super(game);
	}
}
