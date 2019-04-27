package hu.montlikadani.ragemode.API.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private String game;
	private String[] players;

	public GameStartEvent(String game, String[] players) {
		this.game = game;
		this.players = players;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public String getGame() {
		return game;
	}

	public String[] getPlayers() {
		return players;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
