package hu.montlikadani.ragemode.runtimePP;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.statistics.MySQLStats;
import hu.montlikadani.ragemode.statistics.SQLStats;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class RuntimePPManager {

	private static List<PlayerPoints> RuntimePPList = new ArrayList<>();

	public static void loadPPListFromDatabase() {
		switch (RageMode.getInstance().getConfiguration().getCV().getDatabaseType()) {
		case "sql":
		case "sqlite":
			RuntimePPList.addAll(SQLStats.getAllPlayerStatistics());
			break;
		case "mysql":
			RuntimePPList.addAll(MySQLStats.getAllPlayerStatistics());
			break;
		default:
			RuntimePPList.addAll(YAMLStats.getAllPlayerStatistics());
			break;
		}
	}

	/**
	 * Gets the specified player statistics from database.
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPPFromDatabase(String uuid) {
		switch (RageMode.getInstance().getConfiguration().getCV().getDatabaseType()) {
		case "sql":
		case "sqlite":
			return SQLStats.getPlayerStatsFromData(uuid);
		case "mysql":
			return MySQLStats.getPlayerStatsFromData(uuid);
		default:
			return YAMLStats.getPlayerStatsFromData(uuid);
		}
	}

	public static PlayerPoints getPPForPlayer(String sUUID) {
		PlayerPoints pp = null;
		if (RuntimePPList != null) {
			int i = 0;
			int imax = RuntimePPList.size();
			while (i < imax) {
				if (RuntimePPList.get(i).getPlayerUUID().equals(sUUID)) {
					pp = RuntimePPList.get(i);
					pp.setRank(i + 1);
					break;
				}
				i++;
			}
		}
		return pp;
	}

	public static List<PlayerPoints> getRuntimePPList() {
		return RuntimePPList;
	}

	public synchronized static void updatePlayerEntry(PlayerPoints pp) {
		if (RuntimePPList == null || RuntimePPList.isEmpty()) {
			Debug.logConsole(Level.WARNING, "The database does not loaded correctly when this plugin started.");
			Debug.logConsole("Restart your server, or if this not helps, contact the developer.");
			return;
		}

		PlayerPoints oldPP = getPPForPlayer(pp.getPlayerUUID());
		if (oldPP == null) {
			int i = 0;
			while (RuntimePPList.get(i).getPoints() < pp.getPoints())
				i++;

			PlayerPoints newPP = new PlayerPoints(pp.getPlayerUUID());
			newPP.setAxeDeaths(pp.getAxeDeaths());
			newPP.setAxeKills(pp.getAxeKills());
			newPP.setCurrentStreak(pp.getCurrentStreak());
			newPP.setDeaths(pp.getDeaths());
			newPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newPP.setDirectArrowKills(pp.getDirectArrowKills());
			newPP.setExplosionDeaths(pp.getExplosionDeaths());
			newPP.setExplosionKills(pp.getExplosionKills());
			newPP.setKills(pp.getKills());
			newPP.setKnifeDeaths(pp.getKnifeDeaths());
			newPP.setKnifeKills(pp.getKnifeKills());
			newPP.setLongestStreak(pp.getLongestStreak());
			newPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newPP.setWins(1);
			else
				newPP.setWins(0);

			if (pp.getDeaths() != 0)
				newPP.setKD(((double) (pp.getKills())) / ((double) (pp.getDeaths())));
			else
				newPP.setKD(1.0d);

			newPP.setGames(1);

			RuntimePPList.add(i + 1, newPP);
		} else if (pp.getPoints() == oldPP.getPoints())
			return;// TODO kills&deaths...
		else if (pp.getPoints() > oldPP.getPoints()) {
			boolean norankchange = false;
			int i = oldPP.getRank() - 2;

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

			PlayerPoints newPP = new PlayerPoints(pp.getPlayerUUID());
			newPP.setAxeDeaths(pp.getAxeDeaths());
			newPP.setAxeKills(pp.getAxeKills());
			newPP.setCurrentStreak(pp.getCurrentStreak());
			newPP.setDeaths(pp.getDeaths());
			newPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newPP.setDirectArrowKills(pp.getDirectArrowKills());
			newPP.setExplosionDeaths(pp.getExplosionDeaths());
			newPP.setExplosionKills(pp.getExplosionKills());
			newPP.setKills(pp.getKills());
			newPP.setKnifeDeaths(pp.getKnifeDeaths());
			newPP.setKnifeKills(pp.getKnifeKills());
			newPP.setLongestStreak(pp.getLongestStreak());
			newPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newPP.setWins(oldPP.getWins() + 1);
			else
				newPP.setWins(oldPP.getWins());

			if (oldPP.getDeaths() + pp.getDeaths() != 0)
				newPP.setKD(((double) (pp.getKills() + oldPP.getKills()))
						/ ((double) (pp.getDeaths() + oldPP.getDeaths())));
			else
				newPP.setKD(1.0d);

			newPP.setGames(oldPP.getGames() + 1);

			if (norankchange)
				RuntimePPList.set(oldPP.getRank() - 1, newPP);
			else
				RuntimePPList.add(i + 1, newPP);
		} else {
			boolean norankchange = false;
			boolean last = false;
			int i = oldPP.getRank();
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

			PlayerPoints newPP = new PlayerPoints(pp.getPlayerUUID());
			newPP.setAxeDeaths(pp.getAxeDeaths());
			newPP.setAxeKills(pp.getAxeKills());
			newPP.setCurrentStreak(pp.getCurrentStreak());
			newPP.setDeaths(pp.getDeaths());
			newPP.setDirectArrowDeaths(pp.getDirectArrowDeaths());
			newPP.setDirectArrowKills(pp.getDirectArrowKills());
			newPP.setExplosionDeaths(pp.getExplosionDeaths());
			newPP.setExplosionKills(pp.getExplosionKills());
			newPP.setKills(pp.getKills());
			newPP.setKnifeDeaths(pp.getKnifeDeaths());
			newPP.setKnifeKills(pp.getKnifeKills());
			newPP.setLongestStreak(pp.getLongestStreak());
			newPP.setPoints(pp.getPoints());

			if (pp.isWinner())
				newPP.setWins(oldPP.getWins() + 1);
			else
				newPP.setWins(oldPP.getWins());

			if (oldPP.getDeaths() + pp.getDeaths() != 0)
				newPP.setKD(((double) (pp.getKills() + oldPP.getKills())) / ((double) (pp.getDeaths() + oldPP.getDeaths())));
			else
				newPP.setKD(1.0d);

			newPP.setGames(oldPP.getGames() + 1);
			if (norankchange)
				RuntimePPList.set(oldPP.getRank() - 1, newPP);
			else if (last)
				RuntimePPList.add(newPP);
			else
				RuntimePPList.add(i, newPP);
		}
	}
}
