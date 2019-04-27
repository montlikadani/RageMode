package hu.montlikadani.ragemode.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class YAMLStats {

	private static boolean inited = false;
	private static File yamlStatsFile;
	private static YamlConfiguration statsConf;

	protected static boolean working = false;

	public static void initS() {
		if (inited) {
			statsConf = YamlConfiguration.loadConfiguration(yamlStatsFile);
			return;
		} else
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
				RageMode.getInstance().throwMsg();
			}

			config = new YamlConfiguration();
			config.createSection("data");

			try {
				config.save(file);
			} catch (IOException e2) {
				e2.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
		} else
			config = YamlConfiguration.loadConfiguration(file);

		statsConf = config;
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	public static void addPlayerStatistics(List<PlayerPoints> pP) {
		if (!inited) return;

		int i = 0;
		int imax = pP.size();
		String uuid = pP.get(i).getPlayerUUID();
		while (i < imax) {
			String path = "data." + uuid + ".";
			if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid)) {
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
				int score = statsConf.getInt(path + "score");
				int games = statsConf.getInt(path + "games");

				statsConf.set(path + "kills", (kills + pP.get(i).getKills()));
				statsConf.set(path + "axe_kills", (axeKills + pP.get(i).getAxeKills()));
				statsConf.set(path + "direct_arrow_kills", (directArrowKills + pP.get(i).getDirectArrowKills()));
				statsConf.set(path + "explosion_kills", (explosionKills + pP.get(i).getExplosionKills()));
				statsConf.set(path + "knife_kills", (knifeKills + pP.get(i).getKnifeKills()));

				statsConf.set(path + "deaths", (deaths + pP.get(i).getDeaths()));
				statsConf.set(path + "axe_deaths", (axeDeaths + pP.get(i).getAxeDeaths()));
				statsConf.set(path + "direct_arrow_deaths", (directArrowDeaths + pP.get(i).getDirectArrowDeaths()));
				statsConf.set(path + "explosion_deaths", (explosionDeaths + pP.get(i).getExplosionDeaths()));
				statsConf.set(path + "knife_deaths", (knifeDeaths + pP.get(i).getKnifeDeaths()));

				if (pP.get(i).isWinner())
					statsConf.set(path + "wins", (wins + 1));
				else
					statsConf.set(path + "wins", wins);

				statsConf.set(path + "score", (score + pP.get(i).getPoints()));
				statsConf.set(path + "games", (games + 1));
				if ((deaths + pP.get(i).getDeaths()) != 0)
					statsConf.set(path + "KD", ((double) ((kills + pP.get(i).getKills())) / ((double)
							(deaths + pP.get(i).getDeaths()))));
				else
					statsConf.set(path + "KD", 1.0d);

			} else {
				statsConf.set(path + "name", Bukkit.getPlayer(UUID.fromString(uuid)).getName());

				statsConf.set(path + "kills", pP.get(i).getKills());
				statsConf.set(path + "axe_kills", pP.get(i).getAxeKills());
				statsConf.set(path + "direct_arrow_kills", pP.get(i).getDirectArrowKills());
				statsConf.set(path + "explosion_kills", pP.get(i).getExplosionKills());
				statsConf.set(path + "knife_kills", pP.get(i).getKnifeKills());

				statsConf.set(path + "deaths", pP.get(i).getDeaths());
				statsConf.set(path + "axe_deaths", pP.get(i).getAxeDeaths());
				statsConf.set(path + "direct_arrow_deaths", pP.get(i).getDirectArrowDeaths());
				statsConf.set(path + "explosion_deaths", pP.get(i).getExplosionDeaths());
				statsConf.set(path + "knife_deaths", pP.get(i).getKnifeDeaths());

				if (pP.get(i).isWinner())
					statsConf.set(path + "wins", 1);
				else
					statsConf.set(path + "wins", 0);

				statsConf.set(path + "score", pP.get(i).getPoints());
				statsConf.set(path + "games", 1);
				if (pP.get(i).getDeaths() != 0)
					statsConf.set(path + "KD",
							((double) pP.get(i).getKills()) / ((double) pP.get(i).getDeaths()));
				else
					statsConf.set(path + "KD", 1.0d);
			}
			i++;
		}

		try {
			statsConf.save(yamlStatsFile);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	/**
	 * Gets the specified uuid of player statistic
	 * 
	 * @param string Player uuid
	 * @return playerStats Player statistic from file
	 */
	public static RetPlayerPoints getPlayerStatistics(String sUUID) {
		if (sUUID == null) {
			throw new IllegalArgumentException("player uuid is null");
		}
		// returns a RetPlayerPoints object containing the GLOBAL statistics of a player
		if (!inited) return null;

		RetPlayerPoints plPo = new RetPlayerPoints(sUUID);

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(sUUID)) {
			String path = "data." + sUUID + ".";
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
		} else
			return null;
		return plPo;
	}

	/**
	 * Gets all player statistic
	 * 
	 * @return playerStats Player statistic from file
	 */
	public static List<RetPlayerPoints> getAllPlayerStatistics() {
		// returns a List of all RetPlayerPoints that are stored
		if (!inited)
			return Collections.emptyList();

		List<RetPlayerPoints> allRPPs = new ArrayList<>();

		for (String UUID : statsConf.getConfigurationSection("data").getKeys(false)) {
			allRPPs.add(getPlayerStatistics(UUID));
		}
		return allRPPs;
	}

	public static void resetPlayerStatistic(String uuid) {
		if (!inited) return;

		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(uuid);

		if (statsConf.getConfigurationSection("data").getKeys(false).contains(uuid)) {
			rpp.setDeaths(0);
			rpp.setAxeDeaths(0);
			rpp.setDirectArrowDeaths(0);
			rpp.setExplosionDeaths(0);
			rpp.setKnifeDeaths(0);

			rpp.setKills(0);
			rpp.setAxeKills(0);
			rpp.setDirectArrowKills(0);
			rpp.setExplosionKills(0);
			rpp.setKnifeKills(0);

			rpp.setCurrentStreak(0);
			rpp.setGames(0);
			rpp.setKD(0);
			rpp.setLongestStreak(0);
			rpp.setPoints(0);
			rpp.setRank(0);
			rpp.setWins(0);
			statsConf.set("data." + uuid, null);

			try {
				statsConf.save(yamlStatsFile);
			} catch (IOException e) {
				e.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
		}
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
					RageMode.getInstance().throwMsg();
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
