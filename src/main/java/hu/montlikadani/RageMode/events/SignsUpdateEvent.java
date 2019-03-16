package hu.montlikadani.ragemode.events;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import hu.montlikadani.ragemode.signs.SignData;

public class SignsUpdateEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private List<SignData> signs;
	private boolean cancelled = false;

	public SignsUpdateEvent(List<SignData> signs) {
		this.signs = signs;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public List<SignData> getSigns() {
		return signs;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
}
