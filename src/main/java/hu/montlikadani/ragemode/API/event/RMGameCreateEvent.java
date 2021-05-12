package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a game has been created.
 */
public class RMGameCreateEvent extends GameEvent {

	private int maxPlayers;
	private int minPlayers;

	public RMGameCreateEvent(Game game, int maxPlayers, int minPlayers) {
		super(game);

		this.maxPlayers = maxPlayers;
		this.minPlayers = minPlayers;
	}

	/**
	 * Gets the maximum players.
	 * 
	 * @return the maximum players
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * Gets the minimum players.
	 * 
	 * @return the minimum players
	 */
	public int getMinPlayers() {
		return minPlayers;
	}
}
