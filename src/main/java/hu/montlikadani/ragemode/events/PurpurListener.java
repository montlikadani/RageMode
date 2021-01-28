package hu.montlikadani.ragemode.events;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.area.GameAreaManager;
import net.pl3x.purpur.event.entity.EntityMoveEvent;

public class PurpurListener implements Listener {

	// Trying to prevent zombies moving outside of the game
	@EventHandler
	public void onEntityMove(EntityMoveEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE && !GameAreaManager.inArea(event.getTo())) {
			GameAreaManager.getAreaByLocation(event.getFrom()).filter(area -> area.getGame().isGameRunning())
					.ifPresent(area -> event.getEntity().teleport(event.getFrom()
							.subtract(event.getEntity().getLocation().clone().getDirection().multiply(1))));
		}
	}
}
