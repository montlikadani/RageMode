package hu.montlikadani.ragemode.storage;

import hu.montlikadani.ragemode.scores.PlayerPoints;

public class DBThreads implements Runnable {

	private PlayerPoints pP = null;

	public DBThreads(PlayerPoints pP) {
		this.pP = pP;
	}

	@Override
	public void run() {
		switch (hu.montlikadani.ragemode.config.ConfigValues.getDatabaseType()) {
		case "mysql":
			MySQLDB.addPlayerStatistics(pP);
			break;
		case "sqlite":
		case "sql":
			SQLDB.addPlayerStatistics(pP);
			break;
		default:
			YAMLDB.createPlayersStats(pP);
			break;
		}
	}
}
