package hu.montlikadani.ragemode.events;

import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

import hu.montlikadani.ragemode.gameUtils.GameUtils;

public final class Listener_1_13_R2 implements org.bukkit.event.Listener {

	@org.bukkit.event.EventHandler
	public void onPlayerDiscoverRecipe(PlayerRecipeDiscoverEvent event) {
		if (GameUtils.isPlayerPlaying(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
}
