package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameCreateEvent extends GameEvent {

	private int maxPlayers;
	private int minPlayers;

	public RMGameCreateEvent(Game game, int maxPlayers, int minPlayers) {
		super(game);
		this.maxPlayers = maxPlayers;
		this.minPlayers = minPlayers;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}
}
