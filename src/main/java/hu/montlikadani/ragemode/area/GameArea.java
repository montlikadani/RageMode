package hu.montlikadani.ragemode.area;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.google.common.collect.ImmutableList;

public class GameArea {

	private String game;
	private Area area;

	private double x1, y1, z1, x2, y2, z2;

	public GameArea(String game, Area area) {
		this.game = game;
		this.area = area;

		x1 = Math.min(area.getLowLoc().getX(), area.getHighLoc().getX());
		y1 = Math.min(area.getLowLoc().getY(), area.getHighLoc().getY());
		z1 = Math.min(area.getLowLoc().getZ(), area.getHighLoc().getZ());
		x2 = Math.max(area.getLowLoc().getX(), area.getHighLoc().getX());
		y2 = Math.max(area.getLowLoc().getY(), area.getHighLoc().getY());
		z2 = Math.max(area.getLowLoc().getZ(), area.getHighLoc().getZ());
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
	public ImmutableList<Entity> getEntities() {
		return getEntities(true);
	}

	/**
	 * Gets all entities from the location.
	 * 
	 * @param ignorePlayers ignore players or not
	 * @return the list of {@link Entity}
	 */
	public ImmutableList<Entity> getEntities(boolean ignorePlayers) {
		// NOTE: don't use stream in here (memory allocation)

		List<Entity> entities = new ArrayList<>();

		for (Entity e : area.getLowLoc().getWorld().getEntities()) {
			if ((ignorePlayers && !(e instanceof org.bukkit.entity.Player)) && inArea(e.getLocation())) {
				entities.add(e);
			}
		}

		return ImmutableList.copyOf(entities);
	}

	/**
	 * Collects all blocks into a list from the area.
	 * 
	 * @return a list of {@link Block}
	 * @deprecated This method makes no sense until there is no feature to change ragemode area blocks.
	 */
	@Deprecated
	public ImmutableList<Block> getBlocks() {
		List<Block> blocks = new ArrayList<>();

		for (double x = x1; x <= x2; x++) {
			for (double z = z1; z <= z2; z++) {
				for (double y = y1; y <= y2; y++) {
					blocks.add(area.getLowLoc().getWorld().getBlockAt((int) x, (int) y, (int) z));
				}
			}
		}

		return ImmutableList.copyOf(blocks);
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

		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		return (x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2);
	}
}
