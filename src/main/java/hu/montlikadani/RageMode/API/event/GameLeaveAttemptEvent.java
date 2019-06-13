package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

public class GameLeaveAttemptEvent extends BaseEvent {

	private String game;
	private Player player;

	public GameLeaveAttemptEvent(Player player, String game) {
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
