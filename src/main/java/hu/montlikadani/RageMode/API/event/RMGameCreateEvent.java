package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameCreateEvent extends BaseEvent {

	private Game game;
	private int maxPlayers;
	private int minPlayers;

	public RMGameCreateEvent(Game game, int maxPlayers, int minPlayers) {
		this.game = game;
		this.maxPlayers = maxPlayers;
		this.minPlayers = minPlayers;
	}

	public Game getGame() {
		return game;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}
}
