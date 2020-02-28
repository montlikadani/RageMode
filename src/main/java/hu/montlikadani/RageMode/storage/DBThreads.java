package hu.montlikadani.ragemode.storage;

import hu.montlikadani.ragemode.scores.PlayerPoints;

public class DBThreads implements Runnable {

	private PlayerPoints pP = null;

	public DBThreads(PlayerPoints pP) {
		this.pP = pP;
	}

	@Override
	public void run() {
		switch (hu.montlikadani.ragemode.RageMode.getInstance().getDatabaseHandler().getDBType()) {
		case MYSQL:
			MySQLDB.addPlayerStatistics(pP);
			break;
		case SQLITE:
			SQLDB.addPlayerStatistics(pP);
			break;
		case YAML:
			YAMLDB.createPlayersStats(pP);
			break;
		default:
			break;
		}
	}
}
