package hu.montlikadani.ragemode.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import hu.montlikadani.ragemode.gameUtils.GameUtils;

public final class Listeners_1_9 implements org.bukkit.event.Listener {

	// Prevent swap items from main hand to off hand
	@EventHandler(ignoreCancelled = true)
	public void onSwapHandItem(PlayerSwapHandItemsEvent e) {
		if (GameUtils.isPlayerPlaying(e.getPlayer()))
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onItemPickUpEvent(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player && GameUtils.isPlayerPlaying((Player) e.getEntity()))
			e.setCancelled(true);
	}
}
