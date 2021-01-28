package hu.montlikadani.ragemode.gameUtils.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.holder.ScoreBoardHolder;

public class ScoreBoard {

	private final Map<Player, ScoreBoardHolder> scoreboards = new HashMap<>();

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

	@SuppressWarnings("deprecation")
	public ScoreBoard(Player player) {
		if (player == null) {
			return;
		}

		Objective objective = scoreboard.getObjective("ragescores");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("ragescores", "dummy");
		}

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (int i = 1; i <= 15; i++) {
			Team team = scoreboard.getTeam("SLOT_" + i);
			if (team == null) {
				team = scoreboard.registerNewTeam("SLOT_" + i);
			}

			team.addEntry(ChatColor.values()[i].toString());
		}

		scoreboards.put(player, new ScoreBoardHolder(player, scoreboard, objective));
	}

	/**
	 * Sets the title for the created ScoreBoard if present.
	 * 
	 * @param player The player who needs to set the title
	 * @param title  The String, where the title should be set to.
	 */
	public void setTitle(final Player player, final String title) {
		getScoreboard(player).ifPresent(board -> {
			String newTitle = title;

			if (Version.isCurrentLower(Version.v1_13_R1) && newTitle.length() > 32) {
				newTitle = newTitle.substring(0, 32);
			} else if (Version.isCurrentEqualOrHigher(Version.v1_13_R1) && newTitle.length() > 128) {
				newTitle = newTitle.substring(0, 128);
			}

			board.getObjective().setDisplayName(newTitle);
		});
	}

	/**
	 * Sets one score-line with the given String as the name and the Integer as a
	 * score which should be displayed next to the name if the scoreboard for the
	 * player is present.
	 * 
	 * @param player     The player who needs to set the lines
	 * @param line       The name of the new score.
	 * @param dummyScore The integer, the score should be set to.
	 */
	public void setLine(Player player, String line, int dummyScore) {
		getScoreboard(player).ifPresent(board -> {
			Team team = board.getScoreboard().getTeam("SLOT_" + dummyScore);
			if (team == null || dummyScore >= ChatColor.values().length) {
				return;
			}

			String entry = ChatColor.values()[dummyScore].toString();
			if (!board.getScoreboard().getEntries().contains(entry)) {
				board.getObjective().getScore(entry).setScore(dummyScore);
			}

			String l = line;
			if (Version.isCurrentLower(Version.v1_9_R1) && l.length() > 16) {
				l = l.substring(0, 16);
			}

			team.setPrefix(l);
			team.setSuffix(ChatColor.getLastColors(l));
		});
	}

	/**
	 * Resets the scores with the given score to prevent duplication on scoreboard.
	 * 
	 * @param player {@link Player}
	 * @param score  where the line should reset
	 */
	public void resetScores(Player player, int score) {
		getScoreboard(player).filter(b -> score >= ChatColor.values().length).ifPresent(board -> {
			String entry = ChatColor.values()[score].toString();
			if (board.getScoreboard().getEntries().contains(entry)) {
				board.getScoreboard().resetScores(entry);
			}
		});
	}

	/**
	 * Sets the ScoreBoard (if present) for the given player.
	 * 
	 * @param player {@link Player}
	 */
	public void setScoreBoard(Player player) {
		getScoreboard(player).ifPresent(board -> player.setScoreboard(board.getScoreboard()));
	}

	/**
	 * Removes this ScoreBoard if present.
	 * 
	 * @param pl The Player instance for which the ScoreBoard should be removed.
	 */
	public void remove(Player pl) {
		getScoreboard(pl).ifPresent(board -> {
			board.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);

			Objective obj = board.getScoreboard().getObjective("ragescores");
			if (obj != null) {
				obj.unregister();
			}

			pl.setScoreboard(board.getScoreboard());
		});

		scoreboards.remove(pl);
	}

	/**
	 * Returns the Map with all the ScoreBoardHolder elements for each Player within
	 * this ScoreBoard.
	 * 
	 * @return {@link Map}
	 */
	public Map<Player, ScoreBoardHolder> getScoreboards() {
		return scoreboards;
	}

	/**
	 * Returns the given player scoreboard if present.
	 * 
	 * @param player {@link Player}
	 * @return {@link Optional}
	 */
	public Optional<ScoreBoardHolder> getScoreboard(Player player) {
		return Optional.ofNullable(scoreboards.get(player));
	}
}
