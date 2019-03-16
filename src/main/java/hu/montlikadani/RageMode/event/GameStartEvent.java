package hu.montlikadani.ragemode.API.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private String game;

	public GameStartEvent(String game) {
		this.game = game;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public String getGame() {
		return game;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
