package hu.montlikadani.ragemode.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class YAMLStats {

	private static List<RetPlayerPoints> points = new ArrayList<>();
	private static boolean inited = false;
	private static File yamlStatsFile;
	private static YamlConfiguration statsConf;

	protected static boolean working = false;

	public static void initS() {
		if (inited) {
			statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
			return;
		}

		inited = true;

		File file = new File(RageMode.getInstance().getFolder(), "stats.yml");
		YamlConfiguration config = null;
		yamlStatsFile = file;

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				Debug.throwMsg();
			}

			config = new YamlConfiguration();
			config.createSection("data");

			Configuration.saveFile(config, file);
		} else
			config = YamlConfiguration.loadConfiguration(file);

		statsConf = config;
		Configuration.saveFile(config, file);
	}

	public static void loadPlayerStatistics() {
		if (!inited)
			return;

		int totalPlayers = 0;
		ConfigurationSection section = statsConf.getConfigurationSection("data");

		if (section == null)
			return;

		points.clear();

		for (String one : section.getKeys(false)) {
			String path = "data." + one + ".";
			RetPlayerPoints plPo = RuntimeRPPManager.getRPPForPlayer(one);
			if (plPo == null) {
				plPo = new RetPlayerPoints(one);
				RageScores.getPlayerPointsMap().put(UUID.fromString(one).toString(), plPo);
			}

			plPo.setKills(statsConf.getInt(path + "kills"));
			plPo.setAxeKills(statsConf.getInt(path + "axe_kills"));
			plPo.setDirectArrowKills(statsConf.getInt(path + "direct_arrow_kills"));
			plPo.setExplosionKills(statsConf.getInt(path + "explosion_kills"));
			plPo.setKnifeKills(statsConf.getInt(path + "knife_kills"));

			plPo.setDeaths(statsConf.getInt(path + "deaths"));
			plPo.setAxeDeaths(statsConf.getInt(path + "axe_deaths"));
			plPo.setDirectArrowDeaths(statsConf.getInt(path + "direct_arrow_deaths"));
			plPo.setExplosionDeaths(statsConf.getInt(path + "explosion_deaths"));
			plPo.setKnifeDeaths(statsConf.getInt(path + "knife_deaths"));

			plPo.setWins(statsConf.getInt(path + "wins"));
			plPo.setPoints(statsConf.getInt(path + "score"));
			plPo.setGames(statsConf.getInt(path + "games"));

			plPo.setKD(statsConf.getDouble(path + "KD"));

			points.add(plPo);
		}

		totalPlayers += section.getKeys(false).size();

		if (totalPlayers > 0)
			Debug.logConsole("Loaded " + totalPlayers + " player" + (totalPlayers > 1 ? "s" : "") + " database.");
	}

	public static void addPlayerStatistics(List<PlayerPoints> pP) {
		if (!inited) return;

		int i = 0;
		int imax = pP.size();
		String uuid = pP.get(i).getPlayerUUID();
		while (i < imax) {
			PlayerPoints points = pP.get(i);
			String path = "data." + uuid + ".";

			if (statsConf.isConfigurationSection("data") &&
					statsConf.getConfigurationSection("data").getKeys(false).contains(uuid)) {
				statsConf.set(path + "name", Bukkit.getPlayer(UUID.fromString(uuid)).getName());

				int kills = statsConf.getInt(path + "kills");
				int axeKills = statsConf.getInt(path + "axe-kills");
				int directArrowKills = statsConf.getInt(path + "direct-arrow-kills");
				int explosionKills = statsConf.getInt(path + "explosion-kills");
				int knifeKills = statsConf.getInt(path + "knife-kills");

				int deaths = statsConf.getInt(path + "deaths");
				int axeDeaths = statsConf.getInt(path + "axe-deaths");
				int directArrowDeaths = statsConf.getInt(path + "direct-arrow-deaths");
				int explosionDeaths = statsConf.getInt(path + "explosion-deaths");
				int knifeDeaths = statsConf.getInt(path + "knife-deaths");

				int wins = statsConf.getInt(path + "wins");
				int games = statsConf.getInt(path + "games");

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

				if (points.isWinner())
					statsConf.set(path + "wins", (wins + 1));
				else
					statsConf.set(path + "wins", wins);

				// Do not add previous points to the current one, as it will be a dupe
				statsConf.set(path + "score", points.getPoints());
				statsConf.set(path + "games", (games + 1));
				if ((deaths + points.getDeaths()) != 0)
					statsConf.set(path + "KD", ((double) ((kills + points.getKills())) / ((double)
							(deaths + points.getDeaths()))));
				else
					statsConf.set(path + "KD", 1.0d);

			} else {
				statsConf.set(path + "name", Bukkit.getPlayer(UUID.fromString(uuid)).getName());

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

				if (points.isWinner())
					statsConf.set(path + "wins", 1);
				else
					statsConf.set(path + "wins", 0);

				statsConf.set(path + "score", points.getPoints());
				statsConf.set(path + "games", 1);
				if (points.getDeaths() != 0)
					statsConf.set(path + "KD",
							((double) points.getKills()) / ((double) points.getDeaths()));
				else
					statsConf.set(path + "KD", 1.0d);
			}
			i++;
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
	}

	/**
	 * Gets the specified uuid of player statistic
	 * @param sUUID Player uuid
	 * @return returns a RetPlayerPoints object containing the GLOBAL statistics of a player
	 */
	public static RetPlayerPoints getPlayerStatistics(String sUUID) {
		if (sUUID == null) {
			throw new IllegalArgumentException("player uuid is null");
		}

		if (!inited || points.isEmpty())
			return null;

		for (RetPlayerPoints rpp : points) {
			if (rpp.getPlayerUUID().equals(sUUID)) {
				return rpp;
			}
		}

		return null;
	}

	/**
	 * Gets all player statistic
	 * @return returns a List of all RetPlayerPoints that are stored
	 */
	public static List<RetPlayerPoints> getAllPlayerStatistics() {
		if (!inited)
			return Collections.emptyList();

		List<RetPlayerPoints> allRPPs = new ArrayList<>();

		if (statsConf.contains("data") && statsConf.isConfigurationSection("data")) {
			for (String UUID : statsConf.getConfigurationSection("data").getKeys(false)) {
				allRPPs.add(getPlayerStatistics(UUID));
			}
		}
		return allRPPs;
	}

	/**
	 * Restores all data of the specified player to 0
	 * @param uuid UUID of player
	 * @return true if the class inited and player found in database
	 */
	public static boolean resetPlayerStatistic(String uuid) {
		if (!inited)
			return false;

		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(uuid);
		if (rpp == null)
			return false;

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid)) {
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

			rpp.setCurrentStreak(0);
			rpp.setLongestStreak(0);
			rpp.setRank(0);
		}

		Configuration.saveFile(statsConf, yamlStatsFile);
		return true;
	}

	public static File getFile() {
		return yamlStatsFile;
	}

	public static YamlConfiguration getConf() {
		return statsConf;
	}

	private static class AddToPlayersStats implements Runnable {
		private List<PlayerPoints> lsUUIDs = null;

		public AddToPlayersStats(List<PlayerPoints> ls) {
			super();
			this.lsUUIDs = ls;
		}

		@Override
		public void run() {
			while (working) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Debug.throwMsg();
				}
			}
			working = true;
			addPlayerStatistics(lsUUIDs);
			working = false;
		}
	}

	public static AddToPlayersStats createPlayersStats(List<PlayerPoints> ls) {
		return new AddToPlayersStats(ls);
	}
}
