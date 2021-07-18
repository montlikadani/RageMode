package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Called when a player attempted to join to a game.
 */
public class RMGameJoinAttemptEvent extends GameEvent implements Cancellable {

	private Player player;
	private boolean cancelled = false;

	public RMGameJoinAttemptEvent(BaseGame game, Player player) {
		super(game);

		this.player = player;
	}

	/**
	 * Gets the player who joined.
	 * 
	 * @return {@link Player}
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Sets the cancellation state of this event. Cancelling this event will makes
	 * the player to not join to the specified game.
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
