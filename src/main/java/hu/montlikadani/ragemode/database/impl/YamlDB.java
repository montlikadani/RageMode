package hu.montlikadani.ragemode.database.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableList;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.database.DB;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.database.connector.DBConnector;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@DB(type = DBType.YAML)
public class YamlDB implements Database {

	private final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

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
		ConfigurationSection section = statsConf.getConfigurationSection("data");
		if (section == null) {
			return;
		}

		int totalPlayers = 0;

		for (String one : section.getKeys(false)) {
			UUID uuid;

			try {
				uuid = UUID.fromString(one);
			} catch (IllegalArgumentException e) {
				continue;
			}

			PlayerPoints plPo = RuntimePPManager.getPPForPlayer(uuid);
			if (plPo == null) {
				plPo = new PlayerPoints(uuid);
			}

			plPo.setAxeDeaths(section.getInt(one + ".axe-deaths"));
			plPo.setAxeKills(section.getInt(one + ".axe-kills"));
			plPo.setDeaths(section.getInt(one + ".deaths"));
			plPo.setDirectArrowDeaths(section.getInt(one + ".direct-arrow-deaths"));
			plPo.setDirectArrowKills(section.getInt(one + ".direct-arrow-kills"));
			plPo.setExplosionDeaths(section.getInt(one + ".explosion-deaths"));
			plPo.setExplosionKills(section.getInt(one + ".explosion-kills"));
			plPo.setKills(section.getInt(one + ".kills"));
			plPo.setKnifeDeaths(section.getInt(one + ".knife-deaths"));
			plPo.setKnifeKills(section.getInt(one + ".knife-kills"));
			plPo.setZombieKills(section.getInt(one + ".zombie-kills"));

			plPo.setKD(section.getDouble(one + ".KD"));

			plPo.setWins(section.getInt(one + ".wins"));
			plPo.setPoints(section.getInt(one + ".score"));
			plPo.setGames(section.getInt(one + ".games"));

			totalPlayers++;
		}

		if (totalPlayers > 0) {
			Debug.logConsole("Loaded {0} player{1} database.", totalPlayers, (totalPlayers > 1 ? "s" : ""));
		}
	}

	@Override
	public void saveAllPlayerData() {
		RuntimePPManager.getRuntimePPList().forEach(this::addPlayerStatistics);
	}

	@Override
	public void addPlayerStatistics(PlayerPoints points) {
		String path = "data." + points.toStringUUID() + ".";

		statsConf.set(path + "name", Bukkit.getOfflinePlayer(points.getUUID()).getName());

		ConfigurationSection section = statsConf.getConfigurationSection("data");
		if (section == null) {
			section = statsConf.createSection("data");
		}

		path = points.toStringUUID() + ".";

		section.set(path + "kills", points.getKills());
		section.set(path + "axe_kills", points.getAxeKills());
		section.set(path + "direct_arrow_kills", points.getDirectArrowKills());
		section.set(path + "explosion_kills", points.getExplosionKills());
		section.set(path + "knife_kills", points.getKnifeKills());
		section.set(path + "zombie-kills", points.getZombieKills());

		section.set(path + "deaths", points.getDeaths());
		section.set(path + "axe_deaths", points.getAxeDeaths());
		section.set(path + "direct_arrow_deaths", points.getDirectArrowDeaths());
		section.set(path + "explosion_deaths", points.getExplosionDeaths());
		section.set(path + "knife_deaths", points.getKnifeDeaths());

		section.set(path + "wins", points.getWins());

		section.set(path + "score", points.getPoints());
		section.set(path + "games", points.getGames());
		section.set(path + "KD",
				points.getDeaths() > 0 ? ((double) points.getKills() / ((double) points.getDeaths())) : 1.0d);

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	@Override
	public void addPoints(int points, UUID uuid) {
		ConfigurationSection section = statsConf.getConfigurationSection("data");
		if (section == null) {
			return;
		}

		String stringId = uuid.toString();
		if (section.contains(stringId)) {
			section.set(stringId + ".score", getPlayerStatsFromData(uuid).getPoints() + points);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	@Override
	public ImmutableList<PlayerPoints> getAllPlayerStatistics() {
		List<PlayerPoints> allRPPs = new ArrayList<>();

		ConfigurationSection section = statsConf.getConfigurationSection("data");

		if (section != null) {
			for (String d : section.getKeys(false)) {
				UUID uuid;

				try {
					uuid = UUID.fromString(d);
				} catch (IllegalArgumentException e) {
					continue;
				}

				PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
				if (pp == null) {
					pp = new PlayerPoints(uuid);
				}

				allRPPs.add(pp);
			}
		}

		return ImmutableList.copyOf(allRPPs);
	}

	@Override
	public PlayerPoints getPlayerStatsFromData(UUID uuid) {
		PlayerPoints pp = RuntimePPManager.getPPForPlayer(uuid);
		if (pp == null) {
			return null;
		}

		String stringId = uuid.toString();

		if (!statsConf.contains("data." + stringId)) {
			return null;
		}

		String path = "data." + stringId + ".";

		// Cloning to ignore overwrite
		pp = (PlayerPoints) pp.clone();

		pp.setKills(statsConf.getInt(path + "kills"));
		pp.setAxeKills(statsConf.getInt(path + "axe-kills"));
		pp.setDirectArrowKills(statsConf.getInt(path + "direct-arrow-kills"));
		pp.setExplosionKills(statsConf.getInt(path + "explosion-kills"));
		pp.setKnifeKills(statsConf.getInt(path + "knife-kills"));
		pp.setZombieKills(statsConf.getInt(path + "zombie-kills"));

		pp.setDeaths(statsConf.getInt(path + "deaths"));
		pp.setAxeDeaths(statsConf.getInt(path + "axe-deaths"));
		pp.setDirectArrowDeaths(statsConf.getInt(path + "direct-arrow-deaths"));
		pp.setExplosionDeaths(statsConf.getInt(path + "explosion-deaths"));
		pp.setKnifeDeaths(statsConf.getInt(path + "knife-deaths"));

		pp.setWins(statsConf.getInt(path + "wins"));
		pp.setPoints(statsConf.getInt(path + "score"));
		pp.setGames(statsConf.getInt(path + "games"));
		pp.setKD(statsConf.getInt(path + "KD"));

		return pp;
	}

	@Override
	public boolean resetPlayerStatistic(UUID uuid) {
		ConfigurationSection section = statsConf.getConfigurationSection("data");
		if (section == null) {
			return false;
		}

		String stringId = uuid.toString();

		if (section.contains(stringId)) {
			section.set(stringId + ".kills", 0);
			section.set(stringId + ".axe_kills", 0);
			section.set(stringId + ".direct_arrow_kills", 0);
			section.set(stringId + ".explosion_kills", 0);
			section.set(stringId + ".knife_kills", 0);
			section.set(stringId + ".zombie-kills", 0);

			section.set(stringId + ".deaths", 0);
			section.set(stringId + ".axe_deaths", 0);
			section.set(stringId + ".direct_arrow_deaths", 0);
			section.set(stringId + ".explosion_deaths", 0);
			section.set(stringId + ".knife_deaths", 0);

			section.set(stringId + ".wins", 0);
			section.set(stringId + ".score", 0);
			section.set(stringId + ".games", 0);

			section.set(stringId + ".KD", 0d);
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
	public void connectDatabase() {
		if (yamlStatsFile != null) {
			statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
			return;
		}

		yamlStatsFile = new File(rm.getFolder(), "stats.yml");

		if (!yamlStatsFile.exists()) {
			yamlStatsFile.getParentFile().mkdirs();

			try {
				yamlStatsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
		if (!statsConf.contains("data")) {
			statsConf.createSection("data");
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	@Override
	public void loadDatabase(boolean startup) {
		connectDatabase();
		RuntimePPManager.loadPPListFromDatabase(rm.getDatabase());
		loadPlayerStatistics();

		if (startup) {
			loadMiscPlayersData();
		}
	}

	@Override
	public void saveDatabase() {
		saveAllPlayerData();
		saveMiscPlayersData();
	}

	@Override
	public CompletableFuture<Boolean> convertDatabase(final String type) {
		return CompletableFuture.supplyAsync(() -> {
			if (getDatabaseType().toString().equalsIgnoreCase(type)) {
				return false;
			}

			ConfigValues.databaseType = type;

			rm.getConfig().set("database.type", type);
			Configuration.saveFile(rm.getConfig(), rm.getConfiguration().getCfgFile());

			rm.connectDatabase(true);

			RuntimePPManager.getRuntimePPList().forEach(rm.getDatabase()::addPlayerStatistics);
			RuntimePPManager.loadPPListFromDatabase(rm.getDatabase());
			return true;
		});
	}

	@Override
	public void loadMiscPlayersData() {
		File f = new File(rm.getFolder(), "players.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
		ConfigurationSection section = config.getConfigurationSection("players");
		if (section == null) {
			return;
		}

		java.util.Set<String> keys = section.getKeys(false);
		if (keys.isEmpty()) {
			return;
		}

		for (String id : keys) {
			UUID uuid;

			try {
				uuid = UUID.fromString(id);
			} catch (IllegalArgumentException e) {
				continue;
			}

			if (ConfigValues.isRejoinDelayEnabled() && ConfigValues.isRememberRejoinDelay()) {
				ReJoinDelay.setTime(Bukkit.getOfflinePlayer(uuid), section.getLong(uuid + ".join-delay"));
			}

			if (section.getBoolean(uuid + ".deathMessagesEnabled")) {
				PlayerManager.DEATH_MESSAGES_TOGGLE.put(uuid, true);
			}
		}

		config.set("players", null);
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveMiscPlayersData() {
		File f = new File(rm.getFolder(), "players.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
		ConfigurationSection section = config.getConfigurationSection("players");
		if (section == null) {
			section = config.createSection("players");
		} else {
			config.set("players", null);
		}

		for (Map.Entry<UUID, Boolean> map : PlayerManager.DEATH_MESSAGES_TOGGLE.entrySet()) {
			if (map.getValue()) {
				section.set(map.getKey().toString() + ".deathMessagesEnabled", map.getValue());
			}
		}

		if (ConfigValues.isRejoinDelayEnabled() && ConfigValues.isRememberRejoinDelay()) {
			for (Map.Entry<OfflinePlayer, Long> m : ReJoinDelay.getPlayerTimes().entrySet()) {
				if (m.getValue().longValue() > System.currentTimeMillis()) {
					section.set(m.getKey().getUniqueId().toString() + ".join-delay", m.getValue());
				}
			}
		}

		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
