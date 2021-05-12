package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a player wins a game.
 */
public class PlayerWinEvent extends GameEvent {

	private Player winner;

	public PlayerWinEvent(Game game, Player winner) {
		super(game);

		this.winner = winner;
	}

	/**
	 * The winner player who won the involved game.
	 * <p>
	 * This may not necessarily return as a {@code null} value, it may occur in
	 * cases where a sudden kick has occurred to the player.
	 * 
	 * @return {@link Player}
	 */
	@Nullable
	public Player getWinner() {
		return winner;
	}
}
