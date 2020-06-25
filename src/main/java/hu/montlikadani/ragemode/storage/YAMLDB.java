package hu.montlikadani.ragemode.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

public class YAMLDB {

	private static boolean inited = false;
	private static File yamlStatsFile;
	private static YamlConfiguration statsConf;

	public static void initFile() {
		if (inited) {
			statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
			return;
		}

		inited = true;

		File file = new File(RageMode.getInstance().getFolder(), "stats.yml");
		YamlConfiguration config = null;
		yamlStatsFile = file;

		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = YamlConfiguration.loadConfiguration(file);
		if (!config.contains("data")) {
			config.createSection("data");
		}

		statsConf = config;
		Configuration.saveFile(config, file);
	}

	public static void loadPlayerStatistics() {
		if (!inited) {
			return;
		}

		ConfigurationSection section = statsConf.getConfigurationSection("data");
		if (section == null) {
			return;
		}

		int totalPlayers = 0;

		for (String one : section.getKeys(false)) {
			UUID uuid = UUID.fromString(one);
			PlayerPoints plPo = RuntimePPManager.getPPForPlayer(uuid);
			if (plPo == null) {
				plPo = new PlayerPoints(uuid);
			}

			String path = "data." + one + ".";

			plPo.setAxeDeaths(statsConf.getInt(path + "axe-deaths"));
			plPo.setAxeKills(statsConf.getInt(path + "axe-kills"));
			plPo.setDeaths(statsConf.getInt(path + "deaths"));
			plPo.setDirectArrowDeaths(statsConf.getInt(path + "direct-arrow-deaths"));
			plPo.setDirectArrowKills(statsConf.getInt(path + "direct-arrow-kills"));
			plPo.setExplosionDeaths(statsConf.getInt(path + "explosion-deaths"));
			plPo.setExplosionKills(statsConf.getInt(path + "explosion-kills"));
			plPo.setKills(statsConf.getInt(path + "kills"));
			plPo.setKnifeDeaths(statsConf.getInt(path + "knife-deaths"));
			plPo.setKnifeKills(statsConf.getInt(path + "knife-kills"));

			plPo.setKD(statsConf.getDouble(path + "KD"));

			plPo.setWins(statsConf.getInt(path + "wins"));
			plPo.setPoints(statsConf.getInt(path + "score"));
			plPo.setGames(statsConf.getInt(path + "games"));

			totalPlayers++;
		}

		if (totalPlayers > 0) {
			Debug.logConsole("Loaded {0} player{1} database.", totalPlayers, (totalPlayers > 1 ? "s" : ""));
		}
	}

	/**
	 * Saves all players data to the database.
	 */
	public static void saveData() {
		RuntimePPManager.getRuntimePPList().forEach(YAMLDB::addPlayerStatistics);
	}

	public static void addPlayerStatistics(PlayerPoints points) {
		if (!inited) {
			return;
		}

		UUID uuid = points.getUUID();
		String path = "data." + uuid.toString() + ".";

		if (statsConf.isConfigurationSection("data")
				&& statsConf.getConfigurationSection("data").getKeys(false).contains(uuid.toString())) {
			statsConf.set(path + "name", Bukkit.getPlayer(uuid).getName());

			int kills = statsConf.getInt(path + "kills"),
					axeKills = statsConf.getInt(path + "axe-kills"),
					directArrowKills = statsConf.getInt(path + "direct-arrow-kills"),
					explosionKills = statsConf.getInt(path + "explosion-kills"),
					knifeKills = statsConf.getInt(path + "knife-kills"),

					deaths = statsConf.getInt(path + "deaths"),
					axeDeaths = statsConf.getInt(path + "axe-deaths"),
					directArrowDeaths = statsConf.getInt(path + "direct-arrow-deaths"),
					explosionDeaths = statsConf.getInt(path + "explosion-deaths"),
					knifeDeaths = statsConf.getInt(path + "knife-deaths"),

					wins = statsConf.getInt(path + "wins"),
					games = statsConf.getInt(path + "games"),
					score = statsConf.getInt(path + "score");

			statsConf.set(path + "kills", (kills + points.getKills()));
			statsConf.set(path + "axe_kills", (axeKills + points.getAxeKills()));
			statsConf.set(path + "direct_arrow_kills", (directArrowKills + points.getDirectArrowKills()));
			statsConf.set(path + "explosion_kills", (explosionKills + points.getExplosionKills()));
			statsConf.set(path + "knife_kills", (knifeKills + points.getKnifeKills()));

			statsConf.set(path + "deaths", (deaths + points.getDeaths()));
			statsConf.set(path + "axe_deaths", (axeDeaths + points.getAxeDeaths()));
			statsConf.set(path + "direct_arrow_deaths", (directArrowDeaths + points.getDirectArrowDeaths()));
			statsConf.set(path + "explosion_deaths", (explosionDeaths + points.getExplosionDeaths()));
			statsConf.set(path + "knife_deaths", (knifeDeaths + points.getKnifeDeaths()));

			statsConf.set(path + "wins", points.isWinner() ? (wins + 1) : wins);

			statsConf.set(path + "score", (points.getPoints() + score));
			statsConf.set(path + "games", (games + 1));
			statsConf.set(path + "KD",
					(deaths + points.getDeaths()) != 0
							? ((double) ((kills + points.getKills())) / ((double) (deaths + points.getDeaths())))
							: 1.0d);
		} else {
			statsConf.set(path + "name", Bukkit.getPlayer(uuid).getName());

			statsConf.set(path + "kills", points.getKills());
			statsConf.set(path + "axe_kills", points.getAxeKills());
			statsConf.set(path + "direct_arrow_kills", points.getDirectArrowKills());
			statsConf.set(path + "explosion_kills", points.getExplosionKills());
			statsConf.set(path + "knife_kills", points.getKnifeKills());

			statsConf.set(path + "deaths", points.getDeaths());
			statsConf.set(path + "axe_deaths", points.getAxeDeaths());
			statsConf.set(path + "direct_arrow_deaths", points.getDirectArrowDeaths());
			statsConf.set(path + "explosion_deaths", points.getExplosionDeaths());
			statsConf.set(path + "knife_deaths", points.getKnifeDeaths());

			statsConf.set(path + "wins", points.isWinner() ? 1 : 0);

			statsConf.set(path + "score", points.getPoints());
			statsConf.set(path + "games", 1);
			statsConf.set(path + "KD",
					points.getDeaths() != 0 ? ((double) points.getKills()) / ((double) points.getDeaths()) : 1.0d);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	/**
	 * Add points to the given player database.
	 * @param points the amount of points
	 * @param uuid player uuid
	 */
	public static void addPoints(int points, UUID uuid) {
		if (!inited || !statsConf.isConfigurationSection("data")) {
			return;
		}

		int currentPoints = getPlayerStatsFromData(uuid).getPoints();
		String path = "data." + uuid.toString() + ".";

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid.toString())) {
			statsConf.set(path + "score", currentPoints + points);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	/**
	 * Gets all player statistic
	 * @return returns a List of all PlayerPoints that are stored
	 */
	public static List<PlayerPoints> getAllPlayerStatistics() {
		if (!inited) {
			return Collections.emptyList();
		}

		List<PlayerPoints> allRPPs = new ArrayList<>();

		if (statsConf.isConfigurationSection("data")) {
			for (String d : statsConf.getConfigurationSection("data").getKeys(false)) {
				UUID uuid = UUID.fromString(d);
				PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
				if (pp == null) {
					pp = new PlayerPoints(uuid);
				}

				allRPPs.add(pp);
			}
		}

		return allRPPs;
	}

	/**
	 * Gets all player stats from database.
	 * <p>If the player never played and not found in the database, will be ignored.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	@Deprecated
	public static PlayerPoints getPlayerStatsFromData(String uuid) {
		return getPlayerStatsFromData(UUID.fromString(uuid));
	}

	/**
	 * Gets all player stats from database.
	 * <p>If the player never played and not found in the database, will be ignored.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPlayerStatsFromData(UUID uuid) {
		if (!statsConf.contains("data." + uuid.toString())) {
			return null;
		}

		String path = "data." + uuid + ".";

		int kills = statsConf.getInt(path + "kills"),
				axeKills = statsConf.getInt(path + "axe-kills"),
				directArrowKills = statsConf.getInt(path + "direct-arrow-kills"),
				explosionKills = statsConf.getInt(path + "explosion-kills"),
				knifeKills = statsConf.getInt(path + "knife-kills"),

				deaths = statsConf.getInt(path + "deaths"),
				axeDeaths = statsConf.getInt(path + "axe-deaths"),
				directArrowDeaths = statsConf.getInt(path + "direct-arrow-deaths"),
				explosionDeaths = statsConf.getInt(path + "explosion-deaths"),
				knifeDeaths = statsConf.getInt(path + "knife-deaths"),

				wins = statsConf.getInt(path + "wins"),
				games = statsConf.getInt(path + "games"),
				score = statsConf.getInt(path + "score"),
				KD = statsConf.getInt(path + "KD");

		PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
		if (pp == null) {
			return null;
		}

		// Cloning to ignore overwrite
		pp = (PlayerPoints) pp.clone();

		pp.setKills(kills);
		pp.setAxeKills(axeKills);
		pp.setDirectArrowKills(directArrowKills);
		pp.setExplosionKills(explosionKills);
		pp.setKnifeKills(knifeKills);

		pp.setDeaths(deaths);
		pp.setAxeDeaths(axeDeaths);
		pp.setDirectArrowDeaths(directArrowDeaths);
		pp.setExplosionDeaths(explosionDeaths);
		pp.setKnifeDeaths(knifeDeaths);

		pp.setWins(wins);
		pp.setPoints(score);
		pp.setGames(games);
		pp.setKD(KD);

		return pp;
	}

	/**
	 * Restores all data of the given player to 0
	 * @param uuid UUID of player
	 * @return true if the class inited and player found in database
	 */
	@Deprecated
	public static boolean resetPlayerStatistic(String uuid) {
		return resetPlayerStatistic(UUID.fromString(uuid));
	}

	/**
	 * Restores all data of the given player to 0
	 * @param uuid UUID of player
	 * @return true if the class inited and player found in database
	 */
	public static boolean resetPlayerStatistic(UUID uuid) {
		if (!inited) {
			return false;
		}

		if (!statsConf.contains("data")) {
			return false;
		}

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid.toString())) {
			String path = "data." + uuid + ".";
			statsConf.set(path + "kills", 0);
			statsConf.set(path + "axe_kills", 0);
			statsConf.set(path + "direct_arrow_kills", 0);
			statsConf.set(path + "explosion_kills", 0);
			statsConf.set(path + "knife_kills", 0);

			statsConf.set(path + "deaths", 0);
			statsConf.set(path + "axe_deaths", 0);
			statsConf.set(path + "direct_arrow_deaths", 0);
			statsConf.set(path + "explosion_deaths", 0);
			statsConf.set(path + "knife_deaths", 0);

			statsConf.set(path + "wins", 0);
			statsConf.set(path + "score", 0);
			statsConf.set(path + "games", 0);

			statsConf.set(path + "KD", 0d);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);
		if (rpp != null) {
			rpp.setCurrentStreak(0);
			rpp.setLongestStreak(0);
		}

		return true;
	}

	public static void loadJoinDelay() {
		if (!ConfigValues.isRejoinDelayEnabled() || !ConfigValues.isRememberRejoinDelay()) {
			return;
		}

		File f = new File(RageMode.getInstance().getFolder(), "joinDelays.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
		if (!config.contains("players")) {
			config.createSection("players");
		}

		ConfigurationSection section = config.getConfigurationSection("players");
		if (section.getKeys(false).isEmpty()) {
			return;
		}

		for (String o : section.getKeys(false)) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(o));
			if (p != null) {
				ReJoinDelay.setTime(p, section.getLong(o));
			}
		}

		config.set("players", null);
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveJoinDelay() {
		File f = new File(RageMode.getInstance().getFolder(), "joinDelays.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
		if (!config.contains("players")) {
			config.createSection("players");
		} else {
			config.set("players", null);
		}

		ConfigurationSection section = config.getConfigurationSection("players");
		for (Map.Entry<OfflinePlayer, Long> m : ReJoinDelay.getPlayerTimes().entrySet()) {
			section.set(m.getKey().getName(), m.getValue());
		}

		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File getFile() {
		return yamlStatsFile;
	}

	public static YamlConfiguration getConf() {
		return statsConf;
	}
}
