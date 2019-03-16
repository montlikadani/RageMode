package hu.montlikadani.ragemode.API.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStopEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private String game;

	public GameStopEvent(String game) {
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
