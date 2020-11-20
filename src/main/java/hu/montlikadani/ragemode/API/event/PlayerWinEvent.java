package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

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
	 * The winner player.
	 * 
	 * @return {@link Player}
	 */
	public Player getWinner() {
		return winner;
	}
}
