package hu.montlikadani.ragemode.statistics;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class MySQLThread implements Runnable {

	private PlayerPoints pP = null;

	public MySQLThread(PlayerPoints playerPoints) {
		this.pP = playerPoints;
	}

	@Override
	public void run() {
		MySQLStats.addPlayerStatistics(pP, RageMode.getMySQL());
	}
}