package hu.montlikadani.ragemode.events;

import org.bukkit.event.EventHandler;

import hu.montlikadani.ragemode.gameUtils.GameUtils;

public final class Listeners_1_8 implements org.bukkit.event.Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onItemPickedUpEvent(org.bukkit.event.player.PlayerPickupItemEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer()))
			event.setCancelled(true);
	}
}
