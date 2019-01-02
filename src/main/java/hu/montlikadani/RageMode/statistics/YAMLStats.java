package hu.montlikadani.ragemode.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class YAMLStats {

	private static boolean inited = false;
	private static File yamlStatsFile;
	private static FileConfiguration statsConf;

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
		String uuid = pP.size() > 0 ? pP.get(i).getPlayerUUID() : null; // Fixes IndexOutOfBoundsException
		while (i < imax) {
			Set<String> contents = statsConf.getConfigurationSection("data").getKeys(false);
			if (contents.contains(uuid)) {
				statsConf.set("data." + uuid + ".name", Bukkit.getPlayer(UUID.fromString(uuid)).getName());

				int kills = statsConf.getInt("data." + uuid + ".kills");
				int axeKills = statsConf.getInt("data." + uuid + ".axe-kills");
				int directArrowKills = statsConf.getInt("data." + uuid + ".direct-arrow-kills");
				int explosionKills = statsConf.getInt("data." + uuid + ".explosion-kills");
				int knifeKills = statsConf.getInt("data." + uuid + ".knife-kills");

				int deaths = statsConf.getInt("data." + uuid + ".deaths");
				int axeDeaths = statsConf.getInt("data." + uuid + ".axe-deaths");
				int directArrowDeaths = statsConf.getInt("data." + uuid + ".direct-arrow-deaths");
				int explosionDeaths = statsConf.getInt("data." + uuid + ".explosion-deaths");
				int knifeDeaths = statsConf.getInt("data." + uuid + ".knife-deaths");

				int wins = statsConf.getInt("data." + uuid + ".wins");
				int score = statsConf.getInt("data." + uuid + ".score");
				int games = statsConf.getInt("data." + uuid + ".games");

				statsConf.set("data." + uuid + ".kills", (kills + pP.get(i).getKills()));
				statsConf.set("data." + uuid + ".axe_kills", (axeKills + pP.get(i).getAxeKills()));
				statsConf.set("data." + uuid + ".direct_arrow_kills", (directArrowKills + pP.get(i).getDirectArrowKills()));
				statsConf.set("data." + uuid + ".explosion_kills", (explosionKills + pP.get(i).getExplosionKills()));
				statsConf.set("data." + uuid + ".knife_kills", (knifeKills + pP.get(i).getKnifeKills()));

				statsConf.set("data." + uuid + ".deaths", (deaths + pP.get(i).getDeaths()));
				statsConf.set("data." + uuid + ".axe_deaths", (axeDeaths + pP.get(i).getAxeDeaths()));
				statsConf.set("data." + uuid + ".direct_arrow_deaths", (directArrowDeaths + pP.get(i).getDirectArrowDeaths()));
				statsConf.set("data." + uuid + ".explosion_deaths", (explosionDeaths + pP.get(i).getExplosionDeaths()));
				statsConf.set("data." + uuid + ".knife_deaths", (knifeDeaths + pP.get(i).getKnifeDeaths()));

				if (pP.get(i).isWinner())
					statsConf.set("data." + uuid + ".wins", (wins + 1));
				else
					statsConf.set("data." + uuid + ".wins", wins);

				statsConf.set("data." + uuid + ".score", (score + pP.get(i).getPoints()));
				statsConf.set("data." + uuid + ".games", (games + 1));
				if ((deaths + pP.get(i).getDeaths()) != 0)
					statsConf.set("data." + uuid + ".KD", ((double) ((kills + pP.get(i).getKills())) / ((double)
							(deaths + pP.get(i).getDeaths()))));
				else
					statsConf.set("data." + uuid + ".KD", 1.0d);

			} else {
				statsConf.set("data." + uuid + ".name", Bukkit.getPlayer(UUID.fromString(uuid)).getName());

				statsConf.set("data." + uuid + ".kills", pP.get(i).getKills());
				statsConf.set("data." + uuid + ".axe_kills", pP.get(i).getAxeKills());
				statsConf.set("data." + uuid + ".direct_arrow_kills", pP.get(i).getDirectArrowKills());
				statsConf.set("data." + uuid + ".explosion_kills", pP.get(i).getExplosionKills());
				statsConf.set("data." + uuid + ".knife_kills", pP.get(i).getKnifeKills());

				statsConf.set("data." + uuid + ".deaths", pP.get(i).getDeaths());
				statsConf.set("data." + uuid + ".axe_deaths", pP.get(i).getAxeDeaths());
				statsConf.set("data." + uuid + ".direct_arrow_deaths", pP.get(i).getDirectArrowDeaths());
				statsConf.set("data." + uuid + ".explosion_deaths", pP.get(i).getExplosionDeaths());
				statsConf.set("data." + uuid + ".knife_deaths", pP.get(i).getKnifeDeaths());

				if (pP.get(i).isWinner())
					statsConf.set("data." + uuid + ".wins", 1);
				else
					statsConf.set("data." + uuid + ".wins", 0);

				statsConf.set("data." + uuid + ".score", pP.get(i).getPoints());
				statsConf.set("data." + uuid + ".games", 1);
				if (pP.get(i).getDeaths() != 0)
					statsConf.set("data." + uuid + ".KD",
							((double) pP.get(i).getKills()) / ((double) pP.get(i).getDeaths()));
				else
					statsConf.set("data." + uuid + ".KD", 1.0d);
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
			plPo.setKills(statsConf.getInt("data." + sUUID + ".kills"));
			plPo.setAxeKills(statsConf.getInt("data." + sUUID + ".axe_kills"));
			plPo.setDirectArrowKills(statsConf.getInt("data." + sUUID + ".direct_arrow_kills"));
			plPo.setExplosionKills(statsConf.getInt("data." + sUUID + ".explosion_kills"));
			plPo.setKnifeKills(statsConf.getInt("data." + sUUID + ".knife_kills"));

			plPo.setDeaths(statsConf.getInt("data." + sUUID + ".deaths"));
			plPo.setAxeDeaths(statsConf.getInt("data." + sUUID + ".axe_deaths"));
			plPo.setDirectArrowDeaths(statsConf.getInt("data." + sUUID + ".direct_arrow_deaths"));
			plPo.setExplosionDeaths(statsConf.getInt("data." + sUUID + ".explosion_deaths"));
			plPo.setKnifeDeaths(statsConf.getInt("data." + sUUID + ".knife_deaths"));

			plPo.setWins(statsConf.getInt("data." + sUUID + ".wins"));
			plPo.setPoints(statsConf.getInt("data." + sUUID + ".score"));
			plPo.setGames(statsConf.getInt("data." + sUUID + ".games"));

			plPo.setKD(statsConf.getDouble("data." + sUUID + ".KD"));
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
		if (!inited) return null;

		List<RetPlayerPoints> allRPPs = new ArrayList<>();

		Set<String> allUUIDs = statsConf.getConfigurationSection("data").getKeys(false);

		for (String UUID : allUUIDs) {
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
		} else
			return;
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
