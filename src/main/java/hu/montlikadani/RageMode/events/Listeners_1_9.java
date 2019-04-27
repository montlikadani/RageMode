package hu.montlikadani.ragemode.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class Listeners_1_9 implements Listener {

	// Prevent swap items from main hand to off hand
	@EventHandler
	public void onSwapHandItem(PlayerSwapHandItemsEvent e) {
		if (PlayerList.isPlayerPlaying(e.getPlayer().getUniqueId().toString()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onItemPickUpEvent(EntityPickupItemEvent e) {
		if (e.isCancelled() || !(e.getEntity() instanceof Player))
			return;

		if (PlayerList.isPlayerPlaying(((Player) e.getEntity()).getUniqueId().toString()))
			e.setCancelled(true);
	}
}
