package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
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

	private final HashMap<Player, ScoreBoardHolder> scoreboards = new HashMap<>();

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

	@SuppressWarnings("deprecation")
	public void loadScoreboard(Player player) {
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
	 * Sets the title for the created ScoreBoard.
	 * @param player The player who needs to set the title
	 * @param title The String, where the title should be set to.
	 */
	public void setTitle(Player player, String title) {
		if (Version.isCurrentLower(Version.v1_13_R1) && title.length() > 32) {
			title = title.substring(0, 32);
		} else if (Version.isCurrentEqualOrHigher(Version.v1_13_R1) && title.length() > 128) {
			title = title.substring(0, 128);
		}

		final String newTitle = title;

		getScoreboard(player).ifPresent(board -> board.getObjective().setDisplayName(newTitle));
	}

	/**
	 * Sets one score-line with the given String as the name and the Integer as
	 * a score which should be displayed next to the name.
	 * 
	 * @param player The player who needs to set the lines
	 * @param line The name of the new score.
	 * @param dummyScore The integer, the score should be set to.
	 */
	public void setLine(Player player, String line, int dummyScore) {
		getScoreboard(player).ifPresent(board -> {
			Team team = board.getScoreboard().getTeam("SLOT_" + dummyScore);
			if (team == null) {
				return;
			}

			String entry = ChatColor.values()[dummyScore].toString();
			if (!board.getScoreboard().getEntries().contains(entry)) {
				board.getObjective().getScore(entry).setScore(dummyScore);
			}

			// TODO: Do we need to limit the text length?
			team.setPrefix(line);
			team.setSuffix(ChatColor.getLastColors(line));
		});
	}

	/**
	 * Resets the scores with the given score to prevent duplication on scoreboard.
	 * @param player {@link Player}
	 * @param score where the line should reset
	 */
	public void resetScores(Player player, int score) {
		getScoreboard(player).ifPresent(board -> {
			String entry = ChatColor.values()[score].toString();
			if (board.getScoreboard().getEntries().contains(entry)) {
				board.getScoreboard().resetScores(entry);
			}
		});
	}

	/**
	 * Sets the ScoreBoard for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be set.
	 */
	public void setScoreBoard(Player player) {
		getScoreboard(player).ifPresent(board -> player.setScoreboard(board.getScoreboard()));
	}

	/**
	 * Removes the ScoreBoard and the player who exist in the list for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be removed.
	 */
	public void remove(Player pl) {
		getScoreboard(pl).ifPresent(board -> {
			board.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);

			Objective obj = board.getScoreboard().getObjective("ragescores");
			if (obj != null) {
				obj.unregister();
			}
		});

		scoreboards.remove(pl);
	}

	/**
	 * Returns the HashMap with all the ScoreBoardHolder elements for each Player within this ScoreBoard.
	 * @return {@link #scoreboards}
	 */
	public HashMap<Player, ScoreBoardHolder> getScoreboards() {
		return scoreboards;
	}

	/**
	 * Returns an {@link Optional} player scoreboard which is saved.
	 * @param player Player
	 * @return {@link Optional} scoreboard
	 */
	public Optional<ScoreBoardHolder> getScoreboard(Player player) {
		return Optional.ofNullable(scoreboards.get(player));
	}
}
