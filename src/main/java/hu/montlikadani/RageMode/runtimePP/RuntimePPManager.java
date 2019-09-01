package hu.montlikadani.ragemode.runtimePP;

import java.util.List;
import java.util.logging.Level;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.MergeSort;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.statistics.MySQLStats;
import hu.montlikadani.ragemode.statistics.SQLStats;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class RuntimePPManager {

	private static List<PlayerPoints> RuntimePPList;

	public static void getPPListFromMySQL() {
		RuntimePPList = MySQLStats.getAllPlayerStatistics();
		MergeSort ms = new MergeSort();
		ms.sort(RuntimePPList);
	}

	public static void getPPListFromSQL() {
		RuntimePPList = SQLStats.getAllPlayerStatistics();
		MergeSort ms = new MergeSort();
		ms.sort(RuntimePPList);
	}

	public static void getPPListFromYAML() {
		RuntimePPList = YAMLStats.getAllPlayerStatistics();
		MergeSort ms = new MergeSort();
		ms.sort(RuntimePPList);
	}

	/**
	 * Gets the specified player statistics from database.
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPPFromDatabase(String uuid) {
		PlayerPoints pp = null;
		switch (RageMode.getInstance().getConfiguration().getCV().getStatistics()) {
		case "yaml":
			pp = YAMLStats.getPlayerStatsFromData(uuid);
			break;
		case "sql":
		case "sqlite":
			pp = SQLStats.getPlayerStatsFromData(uuid);
			break;
		case "mysql":
			pp = MySQLStats.getPlayerStatsFromData(uuid);
			break;
		default:
			break;
		}

		return pp;
	}

	public static PlayerPoints getPPForPlayer(String sUUID) {
		PlayerPoints rpp = null;
		if (RuntimePPList != null) {
			int i = 0;
			int imax = RuntimePPList.size();
			while (i < imax) {
				if (RuntimePPList.get(i).getPlayerUUID().equals(sUUID)) {
					rpp = RuntimePPList.get(i);
					rpp.setRank(i + 1);
					break;
				}
				i++;
			}
		}
		return rpp;
	}

	public synchronized static void updatePlayerEntry(PlayerPoints pp) {
		if (RuntimePPList == null || RuntimePPList.isEmpty()) {
			Debug.logConsole(Level.WARNING, "The database does not loaded correctly when this plugin started.");
			Debug.logConsole("Restart your server, or if this not helps, contact the developer.");
			return;
		}

		PlayerPoints oldRPP = getPPForPlayer(pp.getPlayerUUID());
		if (oldRPP == null) {
			int i = RuntimePPList.size();
			while (RuntimePPList.get(i).getPoints() < pp.getPoints())
				i--;

			PlayerPoints newRPP = new PlayerPoints(pp.getPlayerUUID());
			newRPP.setAxeDeaths(pp.getAxeDeaths());
			newRPP.setAxeKills(pp.getAxeKills());
			newRPP.setCurrentStreak(pp.getCurrentStreak());
			newRPP.setDeaths(pp.getDeaths());
			newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newRPP.setDirectArrowKills(pp.getDirectArrowKills());
			newRPP.setExplosionDeaths(pp.getExplosionDeaths());
			newRPP.setExplosionKills(pp.getExplosionKills());
			newRPP.setKills(pp.getKills());
			newRPP.setKnifeDeaths(pp.getKnifeDeaths());
			newRPP.setKnifeKills(pp.getKnifeKills());
			newRPP.setLongestStreak(pp.getLongestStreak());
			newRPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newRPP.setWins(1);
			else
				newRPP.setWins(0);

			if (pp.getDeaths() != 0)
				newRPP.setKD(((double) (pp.getKills())) / ((double) (pp.getDeaths())));
			else
				newRPP.setKD(1.0d);

			newRPP.setGames(1);

			RuntimePPList.add(i + 1, newRPP);
		} else if (pp.getPoints() == oldRPP.getPoints())
			return;// TODO kills&deaths...
		else if (pp.getPoints() > oldRPP.getPoints()) {
			boolean norankchange = false;
			int i = oldRPP.getRank() - 2;

			if (RuntimePPList.get(i).getPoints() < pp.getPoints()) {
				RuntimePPList.remove(i + 1);
				i--;
			} else
				norankchange = true;

			while (RuntimePPList.get(i).getPoints() < pp.getPoints()) {
				i--;
				if (i < 0)
					i = 0;
				break;
			}

			PlayerPoints newRPP = new PlayerPoints(pp.getPlayerUUID());
			newRPP.setAxeDeaths(pp.getAxeDeaths());
			newRPP.setAxeKills(pp.getAxeKills());
			newRPP.setCurrentStreak(pp.getCurrentStreak());
			newRPP.setDeaths(pp.getDeaths());
			newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newRPP.setDirectArrowKills(pp.getDirectArrowKills());
			newRPP.setExplosionDeaths(pp.getExplosionDeaths());
			newRPP.setExplosionKills(pp.getExplosionKills());
			newRPP.setKills(pp.getKills());
			newRPP.setKnifeDeaths(pp.getKnifeDeaths());
			newRPP.setKnifeKills(pp.getKnifeKills());
			newRPP.setLongestStreak(pp.getLongestStreak());
			newRPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newRPP.setWins(oldRPP.getWins() + 1);
			else
				newRPP.setWins(oldRPP.getWins());

			if (oldRPP.getDeaths() + pp.getDeaths() != 0)
				newRPP.setKD(((double) (pp.getKills() + oldRPP.getKills()))
						/ ((double) (pp.getDeaths() + oldRPP.getDeaths())));
			else
				newRPP.setKD(1.0d);

			newRPP.setGames(oldRPP.getGames() + 1);

			if (norankchange)
				RuntimePPList.set(oldRPP.getRank() - 1, newRPP);
			else
				RuntimePPList.add(i + 1, newRPP);
		} else {
			boolean norankchange = false;
			boolean last = false;
			int i = oldRPP.getRank();
			int imax = RuntimePPList.size();

			if (RuntimePPList.get(i).getPoints() > pp.getPoints())
				RuntimePPList.remove(i - 1);
			else
				norankchange = true;

			while (RuntimePPList.get(i).getPoints() > pp.getPoints()) {
				i++;
				if (i >= imax) {
					i = imax;
					last = true;
					break;
				}
			}

			PlayerPoints newRPP = new PlayerPoints(pp.getPlayerUUID());
			newRPP.setAxeDeaths(pp.getAxeDeaths());
			newRPP.setAxeKills(pp.getAxeKills());
			newRPP.setCurrentStreak(pp.getCurrentStreak());
			newRPP.setDeaths(pp.getDeaths());
			newRPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newRPP.setDirectArrowKills(pp.getDirectArrowKills());
			newRPP.setExplosionDeaths(pp.getExplosionDeaths());
			newRPP.setExplosionKills(pp.getExplosionKills());
			newRPP.setKills(pp.getKills());
			newRPP.setKnifeDeaths(pp.getKnifeDeaths());
			newRPP.setKnifeKills(pp.getKnifeKills());
			newRPP.setLongestStreak(pp.getLongestStreak());
			newRPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newRPP.setWins(oldRPP.getWins() + 1);
			else
				newRPP.setWins(oldRPP.getWins());

			if (oldRPP.getDeaths() + pp.getDeaths() != 0)
				newRPP.setKD(((double) (pp.getKills() + oldRPP.getKills())) / ((double) (pp.getDeaths() + oldRPP.getDeaths())));
			else
				newRPP.setKD(1.0d);

			newRPP.setGames(oldRPP.getGames() + 1);
			if (norankchange)
				RuntimePPList.set(oldRPP.getRank() - 1, newRPP);
			else if (last)
				RuntimePPList.add(newRPP);
			else
				RuntimePPList.add(i, newRPP);
		}
	}
}
