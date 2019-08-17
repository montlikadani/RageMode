package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

public class RMPlayerKilledEvent extends BaseEvent {

	private String game;
	private Player killer;
	private Player player;
	private String tool;

	public RMPlayerKilledEvent(String game, Player player, Player killer, String tool) {
		this.game = game;
		this.player = player;
		this.killer = killer;
		this.tool = tool;
	}

	public String getGame() {
		return game;
	}

	public Player getKiller() {
		return killer;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTool() {
		return tool;
	}
}
