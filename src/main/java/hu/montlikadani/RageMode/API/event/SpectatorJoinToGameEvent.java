package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

public class SpectatorJoinToGameEvent extends BaseEvent {

	private String game;
	private Player player;

	public SpectatorJoinToGameEvent(String game, Player player) {
		this.game = game;
		this.player = player;
	}

	public String getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}
}
