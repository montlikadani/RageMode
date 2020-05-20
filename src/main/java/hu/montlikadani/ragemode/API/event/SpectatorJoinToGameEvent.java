package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

public class SpectatorJoinToGameEvent extends GameEvent {

	private Player player;

	public SpectatorJoinToGameEvent(Game game, Player player) {
		super(game);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
