package hu.montlikadani.ragemode.API.event;

import java.util.Set;

import org.bukkit.event.Cancellable;

import hu.montlikadani.ragemode.signs.SignData;

/**
 * Called when one of the ragemode signs is updated.
 */
public class RMSignsUpdateEvent extends BaseEvent implements Cancellable {

	private Set<SignData> signs;
	private boolean cancelled = false;

	public RMSignsUpdateEvent(Set<SignData> signs) {
		this.signs = signs;
	}

	/**
	 * Gets the set of added signs.
	 * 
	 * @return {@link SignData}
	 */
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
