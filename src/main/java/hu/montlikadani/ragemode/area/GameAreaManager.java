package hu.montlikadani.ragemode.area;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class GameAreaManager {

	private static final Map<String, GameArea> GAMEAREAS = new HashMap<>();

	public static Map<String, GameArea> getGameAreas() {
		return GAMEAREAS;
	}

	public static void load() {
		GAMEAREAS.clear();

		if (!RageMode.getInstance().getConfiguration().getAreasFile().exists()) {
			return;
		}

		FileConfiguration c = RageMode.getInstance().getConfiguration().getAreasCfg();
		if (!c.isConfigurationSection("areas")) {
			return;
		}

		for (String area : c.getConfigurationSection("areas").getKeys(false)) {
			String path = "areas." + area + ".";
			String game = c.getString(path + "game", "");
			if (game.isEmpty() || !GameUtils.isGameWithNameExists(game)) {
				continue;
			}

			World world = Bukkit.getWorld(c.getString(path + "world", ""));
			if (world == null) {
				continue;
			}

			Location loc1 = new Location(world, c.getDouble(path + "loc1.x", 0), c.getDouble(path + "loc1.y", 0),
					c.getDouble(path + "loc1.z", 0));
			Location loc2 = new Location(world, c.getDouble(path + "loc2.x", 0), c.getDouble(path + "loc2.y", 0),
					c.getDouble(path + "loc2.z", 0));
			GAMEAREAS.put(area, new GameArea(game, new Area(loc1, loc2)));
		}
	}

	/**
	 * Checks if the given area name is exists.
	 * @param name area name
	 * @return true if yes
	 */
	public static boolean isAreaExist(String name) {
		return GAMEAREAS.containsKey(name);
	}

	/**
	 * Remove all entities from the given game. If the game type is not
	 * {@link GameType#APOCALYPSE} it will returns.
	 * @param game {@link Game}
	 */
	public static void removeEntitiesFromGame(Game game) {
		if (game == null || game.getGameType() != GameType.APOCALYPSE) {
			return;
		}

		for (GameArea area : GAMEAREAS.values()) {
			if (area.getGame().equals(game.getName())) {
				area.getEntities().forEach(Entity::remove);
			}
		}
	}

	/**
	 * Gets an area by given location.
	 * @param loc {@link Location}
	 * @return {@link GameArea}
	 */
	public static GameArea getAreaByLocation(Location loc) {
		for (GameArea area : GAMEAREAS.values()) {
			if (area.inArea(loc)) {
				return area;
			}
		}

		return null;
	}

	/**
	 * Checks if the given location is in the set of area.
	 * @param loc {@link Location}
	 * @return true if yes
	 */
	public static boolean inArea(Location loc) {
		return getAreaByLocation(loc) != null;
	}

	/**
	 * Gets all area by given location.
	 * @param loc {@link Location}
	 * @return the list of {@link GameArea}
	 */
	public static List<GameArea> getAreasByLocation(Location loc) {
		return GAMEAREAS.values().stream().filter(area -> area.inArea(loc)).collect(Collectors.toList());
	}
}
