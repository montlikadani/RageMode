package hu.montlikadani.ragemode.area;

import java.util.ArrayList;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Represents the game area
 */
public class GameArea {

	private Game game;
	private Area area;

	private double x1, y1, z1, x2, y2, z2;

	public GameArea(Game game, Area area) {
		this.game = game;
		this.area = area;

		x1 = Math.min(area.getLowLoc().getX(), area.getHighLoc().getX());
		y1 = Math.min(area.getLowLoc().getY(), area.getHighLoc().getY());
		z1 = Math.min(area.getLowLoc().getZ(), area.getHighLoc().getZ());
		x2 = Math.max(area.getLowLoc().getX(), area.getHighLoc().getX());
		y2 = Math.max(area.getLowLoc().getY(), area.getHighLoc().getY());
		z2 = Math.max(area.getLowLoc().getZ(), area.getHighLoc().getZ());
	}

	/**
	 * @return {@link Game}
	 * @return
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * @return {@link Area}
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * Gets all entities from the location except player.
	 * 
	 * @return {@link ImmutableList} of {@link Entity}
	 */
	public ImmutableList<Entity> getEntities() {
		// NOTE: don't use stream in here (slowest)

		List<Entity> entities = new ArrayList<>();

		for (Entity e : area.getLowLoc().getWorld().getEntities()) {
			if (inArea(e.getLocation()) && !(e instanceof Player)) {
				entities.add(e);
			}
		}

		return ImmutableList.copyOf(entities);
	}

	/**
	 * Gets the given entities from the location from the filter array.
	 * 
	 * @param filter the array of entities
	 * @return {@link ImmutableList} of {@link Entity}
	 */
	public ImmutableList<Entity> getEntities(EntityType... filter) {
		if (filter.length == 0) {
			return ImmutableList.of();
		}

		List<Entity> entities = new ArrayList<>();

		for (EntityType filtered : filter) {
			for (Entity e : area.getLowLoc().getWorld().getEntities()) {
				if (filtered == e.getType() && inArea(e.getLocation())) {
					entities.add(e);
				}
			}
		}

		return ImmutableList.copyOf(entities);
	}

	/**
	 * Gets the given entities from the location from the filter array with the
	 * given predicate.
	 * 
	 * @param predicate the test condition to filter
	 * @param filter    the array of entities
	 * @return {@link ImmutableList} of {@link Entity}
	 */
	public ImmutableList<Entity> getEntities(Predicate<Entity> predicate, EntityType... filter) {
		if (filter.length == 0) {
			return ImmutableList.of();
		}

		List<Entity> entities = new ArrayList<>();

		for (EntityType filtered : filter) {
			for (Entity e : area.getLowLoc().getWorld().getEntities()) {
				if (filtered == e.getType() && inArea(e.getLocation()) && predicate.test(e)) {
					entities.add(e);
				}
			}
		}

		return ImmutableList.copyOf(entities);
	}

	/**
	 * Collects all players from this location.
	 * 
	 * @return {@link ImmutableList} of {@link Player}
	 */
	public ImmutableList<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (Entity e : area.getLowLoc().getWorld().getEntities()) {
			if (inArea(e.getLocation()) && e instanceof Player) {
				players.add((Player) e);
			}
		}

		return ImmutableList.copyOf(players);
	}

	/**
	 * Collects all blocks into a list from the area.
	 * 
	 * @return {@link ImmutableList} of {@link Block}
	 * @deprecated This method makes no sense until there is no feature to change
	 *             ragemode area blocks.
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
		if (loc == null || loc.getWorld() != area.getHighLoc().getWorld()) {
			return false;
		}

		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		return (x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2);
	}
}
