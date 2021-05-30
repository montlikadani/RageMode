package hu.montlikadani.ragemode.area;

import java.util.ArrayList;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

/**
 * Represents the game area
 */
public class GameArea {

	private final String gameName;
	private final Area area;
	private final String name;

	private double x1, y1, z1, x2, y2, z2;

	private Location center;

	public GameArea(String gameName, Area area, String name) {
		this.gameName = gameName;
		this.area = area;
		this.name = name;

		double lowX = area.getLowLoc().getX(),
				lowY = area.getLowLoc().getY(),
				lowZ = area.getLowLoc().getZ(),
				highX = area.getHighLoc().getX(),
				highY = area.getHighLoc().getY(),
				highZ = area.getHighLoc().getZ();

		x1 = Math.min(lowX, highX);
		y1 = Math.min(lowY, highY);
		z1 = Math.min(lowZ, highZ);
		x2 = Math.max(lowX, highX);
		y2 = Math.max(lowY, highY);
		z2 = Math.max(lowZ, highZ);

		double x = (lowX + highX) / 2;
		double z = (lowZ + highZ) / 2;

		World lowLocWorld = area.getLowLoc().getWorld();

		center = new Location(lowLocWorld, x, lowLocWorld.getHighestBlockYAt((int) x, (int) z), z).clone();
	}

	/**
	 * @return the name of the game of this area
	 */
	@Nullable
	public String getGameName() {
		return gameName;
	}

	/**
	 * @return the {@link Game} instance of this area
	 */
	@Nullable
	public Game getGame() {
		return GameUtils.getGame(gameName);
	}

	/**
	 * @return {@link Area}
	 */
	@Nullable
	public Area getArea() {
		return area;
	}

	/**
	 * @return the name of this area
	 */
	@Nullable
	public String getName() {
		return name;
	}

	/**
	 * Returns the cloned center location of this area.
	 * 
	 * @return the cloned center location of this area
	 */
	@NotNull
	public Location getCenter() {
		return center;
	}

	/**
	 * Gets all entities from the location except player.
	 * 
	 * @return {@link ImmutableList} of {@link Entity}
	 */
	@NotNull
	public ImmutableList<Entity> getEntities() {
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
	@NotNull
	public ImmutableList<Entity> getEntities(EntityType... filter) {
		if (filter.length == 0) {
			return ImmutableList.of();
		}

		List<Entity> entities = new ArrayList<>();
		List<Entity> worldEntities = area.getLowLoc().getWorld().getEntities();

		for (EntityType filtered : filter) {
			for (Entity e : worldEntities) {
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
	@NotNull
	public ImmutableList<Entity> getEntities(Predicate<Entity> predicate, EntityType... filter) {
		if (filter.length == 0) {
			return ImmutableList.of();
		}

		List<Entity> entities = new ArrayList<>();
		List<Entity> worldEntities = area.getLowLoc().getWorld().getEntities();

		for (EntityType filtered : filter) {
			for (Entity e : worldEntities) {
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
	@NotNull
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
	@NotNull
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
