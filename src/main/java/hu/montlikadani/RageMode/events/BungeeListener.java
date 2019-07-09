package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class BungeeListener implements Listener {

	private String game;

	public BungeeListener(String game) {
		this.game = game;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		GameUtils.joinPlayer(event.getPlayer(), game);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent ev) {
		GameUtils.kickPlayer(ev.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent e) {
		GameUtils.kickPlayer(e.getPlayer());
	}
}
