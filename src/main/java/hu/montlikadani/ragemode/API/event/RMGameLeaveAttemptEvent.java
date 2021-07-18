package hu.montlikadani.ragemode.API.event;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.managers.PlayerManager;

/**
 * Called when a player attempted to leave a game.
 */
public class RMGameLeaveAttemptEvent extends GameEvent implements Cancellable {

	private PlayerManager playerManager;
	private boolean cancelled = false;

	public RMGameLeaveAttemptEvent(BaseGame game, PlayerManager playerManager) {
		super(game);

		this.playerManager = playerManager;
	}

	/**
	 * Gets the player who left the game.
	 * 
	 * @return {@link PlayerManager}
	 */
	@NotNull
	public PlayerManager getPlayerManager() {
		return playerManager;
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
