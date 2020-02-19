package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class GameEvent extends BaseEvent {

	protected Game game;

	public GameEvent(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}
}
