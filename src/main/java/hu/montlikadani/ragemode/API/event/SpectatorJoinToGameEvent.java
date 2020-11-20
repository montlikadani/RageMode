package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a spectator joined into a game.
 */
public class SpectatorJoinToGameEvent extends GameEvent {

	private Player player;

	public SpectatorJoinToGameEvent(Game game, Player player) {
		super(game);
		this.player = player;
	}

	/**
	 * The spectator player who joined.
	 * 
	 * @return {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}
}
