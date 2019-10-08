package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameDeleteEvent extends BaseEvent {

	private Game game;

	public RMGameDeleteEvent(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}
}
