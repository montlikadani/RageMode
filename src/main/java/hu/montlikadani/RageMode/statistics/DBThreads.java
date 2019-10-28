package hu.montlikadani.ragemode.statistics;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class DBThreads implements Runnable {

	private PlayerPoints pP = null;

	public DBThreads(PlayerPoints pP) {
		this.pP = pP;
	}

	@Override
	public void run() {
		switch (RageMode.getInstance().getConfiguration().getCV().getDatabaseType()) {
		case "mysql":
			MySQLStats.addPlayerStatistics(pP, RageMode.getMySQL());
			break;
		case "sqlite":
		case "sql":
			SQLStats.addPlayerStatistics(pP, RageMode.getSQL());
			break;
		default:
			break;
		}
	}
}
