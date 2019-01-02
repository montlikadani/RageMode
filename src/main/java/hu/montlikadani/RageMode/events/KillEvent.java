package hu.montlikadani.ragemode.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private String game;
	private Player killer;
	private Player victim;
	private boolean firstKill;

	public KillEvent(String game, Player killer, Player victim, boolean firstKill) {
		this.game = game;
		this.killer = killer;
		this.victim = victim;
		this.firstKill = firstKill;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public String getGame() {
		return game;
	}

	public Player getKiller() {
		return killer;
	}

	public Player getVictim() {
		return victim;
	}

	public boolean isFirstKill() {
		return firstKill;
	}
}
