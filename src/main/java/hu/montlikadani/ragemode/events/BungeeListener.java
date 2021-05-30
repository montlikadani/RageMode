package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public final class BungeeListener implements org.bukkit.event.Listener {

	private final RageMode plugin;

	public BungeeListener(RageMode plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		if (!plugin.getGames().isEmpty()) {
			GameUtils.joinPlayer(event.getPlayer(), plugin.getGames().get(0));
		}
	}
}
