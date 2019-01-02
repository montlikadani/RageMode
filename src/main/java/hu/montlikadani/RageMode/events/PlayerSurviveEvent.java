package hu.montlikadani.ragemode.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSurviveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private String game;
	private Player player;

	public PlayerSurviveEvent(String game, Player player) {
		this.game = game;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public String getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}
}
