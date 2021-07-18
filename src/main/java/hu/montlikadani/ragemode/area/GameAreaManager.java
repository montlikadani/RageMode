package hu.montlikadani.ragemode.area;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public final class GameAreaManager {

	private static final Map<String, GameArea> GAMEAREAS = new java.util.HashMap<>();

	public static Map<String, GameArea> getGameAreas() {
		return GAMEAREAS;
	}

	private GameAreaManager() {
	}

	public static void load() {
		GAMEAREAS.clear();

		final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

		if (!plugin.getConfiguration().getAreasFile().exists()) {
			return;
		}

		org.bukkit.configuration.ConfigurationSection section = plugin.getConfiguration().getAreasCfg()
				.getConfigurationSection("areas");
		if (section == null) {
			return;
		}

		for (String name : section.getKeys(false)) {
			String gameName = section.getString(name + ".game", "");

			if (!GameUtils.isGameExist(gameName)) {
				continue;
			}

			World world = plugin.getServer().getWorld(section.getString(name + ".world", ""));
			if (world == null) {
				continue;
			}

			Location loc1 = new Location(world, section.getDouble(name + ".loc1.x"),
					section.getDouble(name + ".loc1.y"), section.getDouble(name + ".loc1.z"));
			Location loc2 = new Location(world, section.getDouble(name + ".loc2.x"),
					section.getDouble(name + ".loc2.y"), section.getDouble(name + ".loc2.z"));

			GAMEAREAS.put(name, new GameArea(gameName, new Area(loc1, loc2), name));
		}
	}

	/**
	 * Checks if the area exist with the given name.
	 * 
	 * @param name area name
	 * @return true if the map contains the key
	 */
	public static boolean isAreaExist(@NotNull String name) {
		return GAMEAREAS.containsKey(name);
	}

	/**
	 * Remove all entities from the given game. The game type should be set to
	 * {@link GameType#APOCALYPSE}.
	 * 
	 * @param game {@link BaseGame}
	 */
	public static void removeEntitiesFromGame(@NotNull BaseGame game) {
		if (game == null || game.getGameType() != GameType.APOCALYPSE) {
			return;
		}

		for (GameArea area : GAMEAREAS.values()) {
			if (area.getGameName().equalsIgnoreCase(game.getName())) {
				area.getEntities().forEach(Entity::remove);
			}
		}
	}

	/**
	 * Gets an area by given location.
	 * 
	 * @param loc {@link Location}
	 * @return {@link GameArea}
	 */
	@NotNull
	public static Optional<GameArea> getAreaByLocation(@NotNull Location loc) {
		if (loc != null) {
			for (GameArea area : GAMEAREAS.values()) {
				if (area.inArea(loc)) {
					return Optional.of(area);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks if the given location is in the set of area.
	 * 
	 * @param loc {@link Location}
	 * @return true if yes
	 */
	public static boolean inArea(@NotNull Location loc) {
		return getAreaByLocation(loc).isPresent();
	}

	/**
	 * Retrieves all the game area from the given location.
	 * 
	 * @param loc {@link Location}
	 * @return an immutable list of {@link GameArea}
	 */
	@NotNull
	public static ImmutableList<GameArea> getAreasByLocation(@NotNull Location loc) {
		List<GameArea> list = new java.util.ArrayList<>();

		if (loc != null) {
			for (GameArea area : GAMEAREAS.values()) {
				if (area.inArea(loc)) {
					list.add(area);
				}
			}
		}

		return ImmutableList.copyOf(list);
	}

	/**
	 * Gets the area instance by game.
	 * 
	 * @param game {@link BaseGame}
	 * @return {@link GameArea}
	 */
	@org.jetbrains.annotations.Nullable
	public static GameArea getAreaByGame(@NotNull BaseGame game) {
		if (game == null) {
			return null;
		}

		for (GameArea area : GAMEAREAS.values()) {
			if (area.getGameName().equalsIgnoreCase(game.getName())) {
				return area;
			}
		}

		return null;
	}

	/**
	 * Removes all the areas by game object if exist.
	 * 
	 * @param game the {@link BaseGame} from where to remove
	 */
	public static void removeAreaByGame(@NotNull BaseGame game) {
		if (game == null) {
			return;
		}

		for (GameArea area : GAMEAREAS.values()) {
			if (game.getName().equalsIgnoreCase(area.getGameName())) {
				GAMEAREAS.remove(area.getName());
			}
		}
	}
}
