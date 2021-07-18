package hu.montlikadani.ragemode.events;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;

import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import io.papermc.paper.event.entity.EntityMoveEvent;

public final class PaperListener implements org.bukkit.event.Listener {

	private boolean isNewMethodsSupported = false;

	public PaperListener() {
		try {
			EntityMoveEvent.class.getDeclaredMethod("hasExplicitlyChangedBlock");
			isNewMethodsSupported = true;
		} catch (NoSuchMethodException ex) {
		}
	}

	@EventHandler
	public void onEntityMove(EntityMoveEvent event) {
		if (event.getEntityType() != EntityType.ZOMBIE
				|| (isNewMethodsSupported && !event.hasExplicitlyChangedBlock())) {
			return;
		}

		GameAreaManager.getAreaByLocation(event.getFrom()).filter(area -> area.getGame().isRunning())
				.ifPresent(area -> {
					m: for (PressureMine mines : GameListener.PRESSUREMINES) {
						for (Location mineLoc : mines.getMines()) {
							if (mineLoc.getBlock().getType() == event.getTo().getBlock().getType()) {
								GameListener.explodeMine(event.getTo());
								break m;
							}
						}
					}
				});
	}
}
