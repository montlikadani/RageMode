package hu.montlikadani.ragemode.gameUtils.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.holder.ScoreBoardHolder;
import hu.montlikadani.ragemode.utils.ServerVersion;

public class ScoreBoard {

	private final Map<UUID, ScoreBoardHolder> scoreboards = new HashMap<>();

	private final Scoreboard scoreboard = org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard();

	private final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

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

		final ChatColor[] values = ChatColor.values();

		for (int i = 1; i <= 15; i++) {
			Team team = scoreboard.getTeam("SLOT_" + i);

			if (team == null) {
				team = scoreboard.registerNewTeam("SLOT_" + i);
			}

			team.addEntry(values[i].toString());
		}

		scoreboards.put(player.getUniqueId(), new ScoreBoardHolder(scoreboard, objective));
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

			if (ServerVersion.isCurrentLower(ServerVersion.v1_13_R1) && newTitle.length() > 32) {
				newTitle = newTitle.substring(0, 32);
			} else if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R1) && newTitle.length() > 128) {
				newTitle = newTitle.substring(0, 128);
			}

			plugin.getComplement().setDisplayName(board.getObjective(), newTitle);
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
		final ChatColor[] values = ChatColor.values();

		if (dummyScore >= values.length) {
			return;
		}

		getScoreboard(player).ifPresent(board -> {
			Team team = board.getScoreboard().getTeam("SLOT_" + dummyScore);
			if (team == null) {
				return;
			}

			board.getObjective().getScore(values[dummyScore].toString()).setScore(dummyScore);

			String prefix = line;

			if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1) && prefix.length() > 16) {
				prefix = prefix.substring(0, 16);
			}

			plugin.getComplement().setPrefix(team, prefix);
			plugin.getComplement().setSuffix(team, ChatColor.getLastColors(prefix));
		});
	}

	/**
	 * Resets the scores with the given score to prevent duplication on scoreboard.
	 * 
	 * @param player {@link Player}
	 * @param score  where the line should reset
	 */
	public void resetScores(Player player, int score) {
		final ChatColor[] values = ChatColor.values();

		if (score < values.length) {
			getScoreboard(player).ifPresent(board -> board.getScoreboard().resetScores(values[score].toString()));
		}
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
	 * Removes the ScoreBoard from the given player if present.
	 * 
	 * @param pl The {@link Player} instance for which the ScoreBoard should be
	 *           removed.
	 */
	public void remove(Player player) {
		ScoreBoardHolder board = scoreboards.remove(player.getUniqueId());

		if (board == null) {
			return;
		}

		Objective obj = board.getScoreboard().getObjective("ragescores");
		if (obj != null) {
			obj.unregister();
		}

		for (int i = 1; i <= 15; i++) {
			Team team = board.getScoreboard().getTeam("SLOT_" + i);

			if (team != null) {
				team.unregister();
			}
		}

		board.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(player.getScoreboard());
	}

	/**
	 * Returns the Map with all the ScoreBoardHolder elements for each Player within
	 * this ScoreBoard.
	 * 
	 * @return {@link Map}
	 */
	@NotNull
	public Map<UUID, ScoreBoardHolder> getScoreboards() {
		return scoreboards;
	}

	/**
	 * Returns the given player scoreboard if present.
	 * 
	 * @param player {@link Player}
	 * @return {@link ScoreBoardHolder} if present, otherwise
	 *         {@link Optional#empty()}
	 */
	@NotNull
	public Optional<ScoreBoardHolder> getScoreboard(Player player) {
		return Optional.ofNullable(scoreboards.get(player.getUniqueId()));
	}
}
