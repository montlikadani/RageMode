package hu.montlikadani.ragemode.events;

import java.util.List;

import org.bukkit.event.Cancellable;

import hu.montlikadani.ragemode.API.event.BaseEvent;
import hu.montlikadani.ragemode.signs.SignData;

public class SignsUpdateEvent extends BaseEvent implements Cancellable {

	private List<SignData> signs;
	private boolean cancelled = false;

	public SignsUpdateEvent(List<SignData> signs) {
		this.signs = signs;
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
