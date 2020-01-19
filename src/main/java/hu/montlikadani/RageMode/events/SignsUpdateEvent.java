package hu.montlikadani.ragemode.events;

import java.util.Set;

import org.bukkit.event.Cancellable;

import hu.montlikadani.ragemode.API.event.BaseEvent;
import hu.montlikadani.ragemode.signs.SignData;

public class SignsUpdateEvent extends BaseEvent implements Cancellable {

	private Set<SignData> signs;
	private boolean cancelled = false;

	public SignsUpdateEvent(Set<SignData> signs) {
		this.signs = signs;
	}

	public Set<SignData> getSigns() {
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
