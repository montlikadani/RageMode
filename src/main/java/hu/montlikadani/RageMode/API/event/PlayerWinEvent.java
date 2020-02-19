package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

public class PlayerWinEvent extends GameEvent {

	private Player winner;

	public PlayerWinEvent(Game game, Player winner) {
		super(game);
		this.winner = winner;
	}

	public Player getWinner() {
		return winner;
	}
}
