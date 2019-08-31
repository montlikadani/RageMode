package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.gameLogic.Game;

public class Listeners_1_8 implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onItemPickedUpEvent(org.bukkit.event.player.PlayerPickupItemEvent event) {
		if (Game.isPlayerPlaying(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}
}
