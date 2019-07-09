package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class GameLeaveAttemptEvent extends BaseEvent implements Cancellable {

	private String game;
	private Player player;
	private boolean cancelled = false;

	public GameLeaveAttemptEvent(Player player, String game) {
		this.game = game;
		this.player = player;
	}

	public String getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
