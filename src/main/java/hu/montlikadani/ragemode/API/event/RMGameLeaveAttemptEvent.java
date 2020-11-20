package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a player attempted to leave a game.
 */
public class RMGameLeaveAttemptEvent extends GameEvent implements Cancellable {

	private Player player;
	private boolean cancelled = false;

	public RMGameLeaveAttemptEvent(Game game, Player player) {
		super(game);
		this.player = player;
	}

	/**
	 * Gets the player who left the game.
	 * 
	 * @return {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
