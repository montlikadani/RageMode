package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

public class SpectatorLeaveFromGameEvent extends BaseEvent {

	private String game;
	private Player player;

	public SpectatorLeaveFromGameEvent(String game, Player player) {
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
