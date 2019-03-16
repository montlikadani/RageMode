package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameJoinAttemptEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private String game;
	private final Player player;
	private boolean cancelled = false;

	public GameJoinAttemptEvent(Player player, String game) {
		this.game = game;
		this.player = player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public String getGame() {
		return game;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Player getPlayer() {
		return player;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
