package hu.montlikadani.ragemode.area;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class GameArea {

	private String game;
	private Area area;

	public GameArea(String game, Area area) {
		this.game = game;
		this.area = area;
	}

	public String getGame() {
		return game;
	}

	/**
	 * @return {@link Area}
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * Gets all entities from the location except players.
	 * 
	 * @see #getEntities(boolean)
	 * @return the list of {@link Entity}
	 */
	public List<Entity> getEntities() {
		return getEntities(true);
	}

	/**
	 * Gets all entities from the location.
	 * 
	 * @param ignorePlayers ignore players or not
	 * @return the list of {@link Entity}
	 */
	public List<Entity> getEntities(boolean ignorePlayers) {
		return area.getLowLoc().getWorld().getEntities().stream()
				.filter(e -> ignorePlayers && !(e instanceof org.bukkit.entity.Player))
				.filter(e -> inArea(e.getLocation())).collect(Collectors.toList());
	}

	/**
	 * Converts the list of entities to {@link Stream}<br>
	 * <br>
	 * This includes {@link Player} as entity.
	 * 
	 * @param entities the list of entities
	 * @return {@link Stream}
	 */
	public Stream<Entity> getEntities(List<Entity> entities) {
		return entities == null ? Stream.empty() : entities.stream();
	}

	/**
	 * Checks if the given location is in the set of area.
	 * 
	 * @param loc {@link Location}
	 * @return true if yes
	 */
	public boolean inArea(Location loc) {
		if (loc == null || area == null) {
			return false;
		}

		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ(),
				x1 = Math.min(area.getLowLoc().getBlockX(), area.getHighLoc().getBlockX()),
				y1 = Math.min(area.getLowLoc().getBlockY(), area.getHighLoc().getBlockY()),
				z1 = Math.min(area.getLowLoc().getBlockZ(), area.getHighLoc().getBlockZ()),
				x2 = Math.max(area.getLowLoc().getBlockX(), area.getHighLoc().getBlockX()),
				y2 = Math.max(area.getLowLoc().getBlockY(), area.getHighLoc().getBlockY()),
				z2 = Math.max(area.getLowLoc().getBlockZ(), area.getHighLoc().getBlockZ());

		return (x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2);
	}
}
