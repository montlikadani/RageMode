package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

public class PlayerWinEvent extends BaseEvent {

	private String game;
	private Player winner;

	public PlayerWinEvent(String game, Player winner) {
		this.game = game;
		this.winner = winner;
	}

	public String getGame() {
		return game;
	}

	public Player getWinner() {
		return winner;
	}
}
