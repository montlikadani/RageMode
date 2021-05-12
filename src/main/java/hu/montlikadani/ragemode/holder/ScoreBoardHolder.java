package hu.montlikadani.ragemode.holder;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreBoardHolder {

	private Scoreboard scoreboard;
	private Objective objective;

	public ScoreBoardHolder(Scoreboard scoreboard, Objective objective) {
		this.scoreboard = scoreboard;
		this.objective = objective;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public Objective getObjective() {
		return objective;
	}
}
