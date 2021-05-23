package hu.montlikadani.ragemode.API.event;

import java.util.Set;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.signs.SignData;

/**
 * Called when some ragemode sign(s) is updated for a specific game.
 */
public class RMSignsUpdateEvent extends BaseEvent implements Cancellable {

	private Set<SignData> signs;
	private boolean cancelled = false;

	public RMSignsUpdateEvent(Set<SignData> signs) {
		this.signs = signs;
	}

	/**
	 * @return the set of {@link SignData}
	 */
	@NotNull
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
