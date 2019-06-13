package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class BungeeListener implements Listener {

	private String game;

	public BungeeListener(String game) {
		this.game = game;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerListPing(ServerListPingEvent event) {
		event.setMaxPlayers(GetGames.getMaxPlayers(game));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		GameUtils.joinPlayer(event.getPlayer(), game);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent ev) {
		GameUtils.leavePlayer(ev.getPlayer());
	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {
		GameUtils.leavePlayer(e.getPlayer());
	}
}
