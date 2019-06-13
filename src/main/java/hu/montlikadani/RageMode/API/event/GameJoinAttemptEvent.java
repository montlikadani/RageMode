package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class GameJoinAttemptEvent extends BaseEvent implements Cancellable {

	private String game;
	private Player player;
	private boolean cancelled = false;

	public GameJoinAttemptEvent(Player player, String game) {
		this.game = game;
		this.player = player;
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
}
