package hu.montlikadani.ragemode.area;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class GameArea {

	private String game;
	private Area area;

	private int x1, y1, z1, x2, y2, z2;

	public GameArea(String game, Area area) {
		this.game = game;
		this.area = area;

		x1 = Math.min(area.getLowLoc().getBlockX(), area.getHighLoc().getBlockX());
		y1 = Math.min(area.getLowLoc().getBlockY(), area.getHighLoc().getBlockY());
		z1 = Math.min(area.getLowLoc().getBlockZ(), area.getHighLoc().getBlockZ());
		x2 = Math.max(area.getLowLoc().getBlockX(), area.getHighLoc().getBlockX());
		y2 = Math.max(area.getLowLoc().getBlockY(), area.getHighLoc().getBlockY());
		z2 = Math.max(area.getLowLoc().getBlockZ(), area.getHighLoc().getBlockZ());
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
	 * Collects all blocks into a list from the area.
	 * 
	 * @return an unmodifiable list of {@link Block}
	 * @deprecated This method makes no sense until there is no feature to change ragemode area blocks.
	 */
	@Deprecated
	public List<Block> getBlocks() {
		List<Block> blocks = new java.util.ArrayList<>();
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				for (int y = y1; y <= y2; y++) {
					blocks.add(area.getLowLoc().getWorld().getBlockAt(x, y, z));
				}
			}
		}

		return Collections.unmodifiableList(blocks);
	}

	/**
	 * Checks if the given location is in the set of area.
	 * 
	 * @param loc {@link Location}
	 * @return true if yes
	 */
	public boolean inArea(Location loc) {
		if (loc == null) {
			return false;
		}

		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		return (x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2);
	}
}
