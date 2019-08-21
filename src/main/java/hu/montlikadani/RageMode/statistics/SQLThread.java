package hu.montlikadani.ragemode.statistics;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class SQLThread implements Runnable {

	private PlayerPoints pP = null;

	public SQLThread(PlayerPoints pP) {
		this.pP = pP;
	}

	@Override
	public void run() {
		SQLStats.addPlayerStatistics(pP, RageMode.getSQL());
	}
}
