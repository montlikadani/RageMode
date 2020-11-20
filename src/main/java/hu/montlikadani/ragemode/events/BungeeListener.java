package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class BungeeListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		if (!RageMode.getInstance().getGames().isEmpty()) {
			GameUtils.joinPlayer(event.getPlayer(),
					GameUtils.getGame(RageMode.getInstance().getGames().get(0).getName()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent ev) {
		GameUtils.kickPlayer(ev.getPlayer());
		GameUtils.kickSpectator(ev.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent e) {
		GameUtils.kickPlayer(e.getPlayer());
		GameUtils.kickSpectator(e.getPlayer());
	}
}
