package hu.montlikadani.ragemode.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreBoardHolder {

	private Player player;
	private Scoreboard scoreboard;
	private Objective objective;
	private String oldPointsLine;
	private String oldKdLine;

	public ScoreBoardHolder(Player player, Scoreboard scoreboard, Objective objective) {
		this.player = player;
		this.scoreboard = scoreboard;
		this.objective = objective;
	}

	public Player getPlayer() {
		return player;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public Objective getObjective() {
		return objective;
	}

	public String getOldPointsLine() {
		return oldPointsLine;
	}

	public void setOldPointsLine(String oldPointsLine) {
		this.oldPointsLine = oldPointsLine;
	}

	public String getOldKdLine() {
		return oldKdLine;
	}

	public void setOldKdLine(String oldKdLine) {
		this.oldKdLine = oldKdLine;
	}
}
