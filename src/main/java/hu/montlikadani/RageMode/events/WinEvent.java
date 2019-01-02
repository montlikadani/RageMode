package hu.montlikadani.ragemode.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WinEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private String game;
	private boolean firstWin;
	private Player player;

	public WinEvent(String game, Player player, boolean firstWin) {
		this.game = game;
		this.player = player;
		this.firstWin = firstWin;
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

	public Player getPlayer() {
		return player;
	}

	public boolean isFirstWin() {
		return firstWin;
	}
}
