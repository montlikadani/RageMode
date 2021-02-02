package hu.montlikadani.ragemode.events;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import io.papermc.paper.event.entity.EntityMoveEvent;

public class PaperListener implements Listener {

	@EventHandler
	public void onEntityMove(EntityMoveEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE) {
			GameAreaManager.getAreaByLocation(event.getFrom()).filter(area -> area.getGame().isGameRunning())
					.ifPresent(area -> {
						if (!GameAreaManager.inArea(event.getTo())) {
							event.getEntity().teleport(event.getFrom()
									.subtract(event.getEntity().getLocation().clone().getDirection().multiply(1)));
							return;
						}

						m: for (PressureMine mines : GameListener.PRESSUREMINES) {
							for (Location mineLoc : mines.getMines()) {
								if (mineLoc.getBlock().getType() == event.getTo().getBlock().getType()) {
									GameListener.explodeMine(org.bukkit.Bukkit.getPlayer(mines.getOwner()),
											event.getTo());
									break m;
								}
							}
						}
					});
		}
	}
}
