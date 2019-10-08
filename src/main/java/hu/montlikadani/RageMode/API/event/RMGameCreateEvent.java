package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameCreateEvent extends BaseEvent {

	private Game game;
	private int maxPlayers;

	public RMGameCreateEvent(Game game, int maxPlayers) {
		this.game = game;
		this.maxPlayers = maxPlayers;
	}

	public Game getGame() {
		return game;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}
}
