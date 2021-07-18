package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;

/**
 * Called when a spectator player has left a game.
 */
public class SpectatorLeaveGameEvent extends GameEvent {

	private Player player;

	public SpectatorLeaveGameEvent(BaseGame game, Player player) {
		super(game);

		this.player = player;
	}

	/**
	 * The spectator player who left the game.
	 * 
	 * @return {@link Player}
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}
}
