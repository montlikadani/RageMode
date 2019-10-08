package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

public class SpectatorJoinToGameEvent extends BaseEvent {

	private Game game;
	private Player player;

	public SpectatorJoinToGameEvent(Game game, Player player) {
		this.game = game;
		this.player = player;
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}
}
