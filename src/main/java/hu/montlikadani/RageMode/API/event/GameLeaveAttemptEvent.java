package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameLeaveAttemptEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private String game;
	private final Player player;

	public GameLeaveAttemptEvent(Player player, String game) {
		this.game = game;
		this.player = player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public String getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
