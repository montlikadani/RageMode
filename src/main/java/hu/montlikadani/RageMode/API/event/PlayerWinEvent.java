package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

public class PlayerWinEvent extends BaseEvent {

	private Game game;
	private Player winner;

	public PlayerWinEvent(Game game, Player winner) {
		this.game = game;
		this.winner = winner;
	}

	public Game getGame() {
		return game;
	}

	public Player getWinner() {
		return winner;
	}
}
