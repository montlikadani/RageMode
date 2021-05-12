package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a player attempt to respawn before teleporting.
 */
public class RMPlayerPreRespawnEvent extends GameEvent implements Cancellable {

	private Player player;
	private boolean cancel = false;

	public RMPlayerPreRespawnEvent(Game game, Player player) {
		super(game);

		this.player = player;
	}

	/**
	 * Gets the player who's attempting to respawn.
	 * 
	 * @return {@link Player}
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
