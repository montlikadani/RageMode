package hu.montlikadani.ragemode.runtimePP;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.storage.MySQLDB;
import hu.montlikadani.ragemode.storage.SQLDB;
import hu.montlikadani.ragemode.storage.YAMLDB;

public class RuntimePPManager {

	private static final List<PlayerPoints> RUNTIMEPPLIST = new ArrayList<>();

	public static List<PlayerPoints> getRuntimePPList() {
		return RUNTIMEPPLIST;
	}

	public static void loadPPListFromDatabase() {
		switch (RageMode.getInstance().getDatabaseHandler().getDBType()) {
		case SQLITE:
			RUNTIMEPPLIST.addAll(SQLDB.getAllPlayerStatistics());
			break;
		case MYSQL:
			RUNTIMEPPLIST.addAll(MySQLDB.getAllPlayerStatistics());
			break;
		case YAML:
			RUNTIMEPPLIST.addAll(YAMLDB.getAllPlayerStatistics());
			break;
		default:
			break;
		}
	}

	/**
	 * Get the points database for the given player uuid.
	 * 
	 * @deprecated converting string to uuid is too long time
	 * @param sUUID Player uuid
	 * @see #getPPForPlayer(UUID)
	 * @return {@link PlayerPoints}
	 */
	@Deprecated
	public static PlayerPoints getPPForPlayer(String sUUID) {
		return getPPForPlayer(UUID.fromString(sUUID));
	}

	/**
	 * Get the points database for the given player uuid.
	 * 
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints} the player current stats
	 */
	public static PlayerPoints getPPForPlayer(UUID uuid) {
		int i = 0;
		int imax = RUNTIMEPPLIST.size();
		while (i < imax) {
			PlayerPoints pp = RUNTIMEPPLIST.get(i);
			if (pp != null && pp.getUUID().equals(uuid)) {
				return pp;
			}

			i++;
		}

		return null;
	}

	/**
	 * Checks if the given player have enough points.
	 * 
	 * @param uuid   Player uuid
	 * @param points points
	 * @return <code>false</code> if the player is in the database and have enough, otherwise <code>false</code>
	 */
	public static boolean hasPoints(UUID uuid, int points) {
		PlayerPoints pp = getPPForPlayer(uuid);
		return pp != null && pp.hasPoints(points);
	}

	public synchronized static void updatePlayerEntry(PlayerPoints pp) {
		PlayerPoints oldPP = getPPForPlayer(pp.getUUID());
		if (oldPP == null) {
			PlayerPoints newPP = new PlayerPoints(pp.getUUID());
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
			newPP.setZombieKills(pp.getZombieKills());
			newPP.setPoints(pp.getPoints());

			newPP.setWins(pp.isWinner() ? 1 : 0);

			if (pp.getDeaths() != 0)
				newPP.setKD(((double) (pp.getKills())) / ((double) (pp.getDeaths())));
			else
				newPP.setKD(1.0d);

			newPP.setGames(1);

			RUNTIMEPPLIST.add(newPP);
			return;
		}

		int i = 0;
		while (i < RUNTIMEPPLIST.size()) {
			PlayerPoints plp = RUNTIMEPPLIST.get(i);
			if (plp != null && plp.getUUID().equals(pp.getUUID())) {
				PlayerPoints newPP = new PlayerPoints(pp.getUUID());
				newPP.setAxeDeaths(oldPP.getAxeDeaths() + pp.getAxeDeaths());
				newPP.setAxeKills(oldPP.getAxeKills() + pp.getAxeKills());
				newPP.setCurrentStreak(oldPP.getCurrentStreak() + pp.getCurrentStreak());
				newPP.setDeaths(oldPP.getDeaths() + pp.getDeaths());
				newPP.setDirectArrowDeaths(oldPP.getDirectArrowDeaths() + pp.getDirectArrowDeaths());
				newPP.setDirectArrowKills(oldPP.getDirectArrowKills() + pp.getDirectArrowKills());
				newPP.setExplosionDeaths(oldPP.getExplosionDeaths() + pp.getExplosionDeaths());
				newPP.setExplosionKills(oldPP.getExplosionKills() + pp.getExplosionKills());
				newPP.setKills(oldPP.getKills() + pp.getKills());
				newPP.setKnifeDeaths(oldPP.getKnifeDeaths() + pp.getKnifeDeaths());
				newPP.setKnifeKills(oldPP.getKnifeKills() + pp.getKnifeKills());
				newPP.setLongestStreak(oldPP.getLongestStreak() + pp.getLongestStreak());
				newPP.setZombieKills(oldPP.getZombieKills() + pp.getZombieKills());
				newPP.setPoints(oldPP.getPoints() + pp.getPoints());

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

				RUNTIMEPPLIST.set(i, newPP);
				break;
			}

			i++;
		}
	}
}
