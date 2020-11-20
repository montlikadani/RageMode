package hu.montlikadani.ragemode.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import hu.montlikadani.ragemode.database.DB;
import hu.montlikadani.ragemode.database.DBConnector;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@DB(type = DBType.YAML)
public class YAMLDB implements Database {

	private File yamlStatsFile;
	private YamlConfiguration statsConf;

	@Override
	public DBConnector getDatabase() {
		return null;
	}

	public File getFile() {
		return yamlStatsFile;
	}

	public YamlConfiguration getConf() {
		return statsConf;
	}

	@Override
	public void loadPlayerStatistics() {
		if (!statsConf.isConfigurationSection("data")) {
			return;
		}

		int totalPlayers = 0;

		for (String one : statsConf.getConfigurationSection("data").getKeys(false)) {
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
			plPo.setZombieKills(statsConf.getInt(path + "zombie-kills"));

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

	@Override
	public void saveData() {
		RuntimePPManager.getRuntimePPList().forEach(this::addPlayerStatistics);
	}

	@Override
	public void addPlayerStatistics(PlayerPoints points) {
		UUID uuid = points.getUUID();
		String path = "data." + uuid.toString() + ".";

		statsConf.set(path + "name", Bukkit.getOfflinePlayer(uuid).getName());

		if (statsConf.isConfigurationSection("data")
				&& statsConf.getConfigurationSection("data").getKeys(false).contains(uuid.toString())) {

			int kills = statsConf.getInt(path + "kills"), axeKills = statsConf.getInt(path + "axe-kills"),
					directArrowKills = statsConf.getInt(path + "direct-arrow-kills"),
					explosionKills = statsConf.getInt(path + "explosion-kills"),
					knifeKills = statsConf.getInt(path + "knife-kills"),
					zombieKills = statsConf.getInt(path + "zombie-kills"),

					deaths = statsConf.getInt(path + "deaths"), axeDeaths = statsConf.getInt(path + "axe-deaths"),
					directArrowDeaths = statsConf.getInt(path + "direct-arrow-deaths"),
					explosionDeaths = statsConf.getInt(path + "explosion-deaths"),
					knifeDeaths = statsConf.getInt(path + "knife-deaths"),

					wins = statsConf.getInt(path + "wins"), games = statsConf.getInt(path + "games"),
					score = statsConf.getInt(path + "score");

			statsConf.set(path + "kills", (kills + points.getKills()));
			statsConf.set(path + "axe_kills", (axeKills + points.getAxeKills()));
			statsConf.set(path + "direct_arrow_kills", (directArrowKills + points.getDirectArrowKills()));
			statsConf.set(path + "explosion_kills", (explosionKills + points.getExplosionKills()));
			statsConf.set(path + "knife_kills", (knifeKills + points.getKnifeKills()));
			statsConf.set(path + "zombie-kills", (zombieKills + points.getZombieKills()));

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
			statsConf.set(path + "kills", points.getKills());
			statsConf.set(path + "axe_kills", points.getAxeKills());
			statsConf.set(path + "direct_arrow_kills", points.getDirectArrowKills());
			statsConf.set(path + "explosion_kills", points.getExplosionKills());
			statsConf.set(path + "knife_kills", points.getKnifeKills());
			statsConf.set(path + "zombie-kills", points.getZombieKills());

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

	@Override
	public void addPoints(int points, UUID uuid) {
		if (!statsConf.isConfigurationSection("data")) {
			return;
		}

		int currentPoints = getPlayerStatsFromData(uuid).getPoints();
		String path = "data." + uuid.toString() + ".";

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid.toString())) {
			statsConf.set(path + "score", currentPoints + points);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	@Override
	public List<PlayerPoints> getAllPlayerStatistics() {
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

	@Override
	public PlayerPoints getPlayerStatsFromData(UUID uuid) {
		if (!statsConf.contains("data." + uuid.toString())) {
			return null;
		}

		String path = "data." + uuid + ".";

		int kills = statsConf.getInt(path + "kills"), axeKills = statsConf.getInt(path + "axe-kills"),
				directArrowKills = statsConf.getInt(path + "direct-arrow-kills"),
				explosionKills = statsConf.getInt(path + "explosion-kills"),
				knifeKills = statsConf.getInt(path + "knife-kills"),
				zombieKills = statsConf.getInt(path + "zombie-kills"),

				deaths = statsConf.getInt(path + "deaths"), axeDeaths = statsConf.getInt(path + "axe-deaths"),
				directArrowDeaths = statsConf.getInt(path + "direct-arrow-deaths"),
				explosionDeaths = statsConf.getInt(path + "explosion-deaths"),
				knifeDeaths = statsConf.getInt(path + "knife-deaths"),

				wins = statsConf.getInt(path + "wins"), games = statsConf.getInt(path + "games"),
				score = statsConf.getInt(path + "score"), KD = statsConf.getInt(path + "KD");

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
		pp.setZombieKills(zombieKills);

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

	@Override
	public boolean resetPlayerStatistic(UUID uuid) {
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
			statsConf.set(path + "zombie-kills", 0);

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

	@Override
	public void loadJoinDelay() {
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
			ReJoinDelay.setTime(Bukkit.getOfflinePlayer(UUID.fromString(o)), section.getLong(o));
		}

		config.set("players", null);
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveJoinDelay() {
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

	@Override
	public void connectDatabase() {
		if (yamlStatsFile != null) {
			statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
			return;
		}

		File file = new File(RageMode.getInstance().getFolder(), "stats.yml");
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

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (!config.contains("data")) {
			config.createSection("data");
		}

		statsConf = config;
		Configuration.saveFile(config, file);
	}

	@Override
	public void loadDatabase(boolean startup) {
		connectDatabase();
		RuntimePPManager.loadPPListFromDatabase();
		loadPlayerStatistics();

		if (startup) {
			loadJoinDelay();
		}
	}

	@Override
	public void saveDatabase() {
		saveData();
		saveJoinDelay();
	}

	@Override
	public boolean convertDatabase(final String type) {
		if (type == null || type.trim().isEmpty()
				|| RageMode.getInstance().getDatabaseType().toString().equalsIgnoreCase(type.trim())) {
			return false;
		}

		ConfigValues.databaseType = type;

		final Configuration conf = RageMode.getInstance().getConfiguration();
		conf.getCfg().set("database.type", type);
		Configuration.saveFile(conf.getCfg(), conf.getCfgFile());

		RageMode.getInstance().setDatabase(type);

		RuntimePPManager.getRuntimePPList().forEach(RageMode.getInstance().getDatabase()::addPlayerStatistics);
		RuntimePPManager.loadPPListFromDatabase();
		return true;
	}
}
