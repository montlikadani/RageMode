package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import hu.montlikadani.ragemode.holder.ScoreBoardHolder;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class ScoreBoard implements IObjectives {

	public static HashMap<String, ScoreBoard> allScoreBoards = new HashMap<>();

	private final HashMap<Player, ScoreBoardHolder> scoreboards = new HashMap<>();
	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

	@SuppressWarnings("deprecation")
	public void loadScoreboard(List<PlayerManager> players) {
		// TODO: Same scoreboard appears for players and duplicating lines
		Objective objective = scoreboard.getObjective("ragescores");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("ragescores", "dummy");
		}

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (PlayerManager pm : players) {
			Player loopPlayer = pm.getPlayer();
			scoreboards.put(loopPlayer, new ScoreBoardHolder(loopPlayer, scoreboard, objective));
		}
	}

	/**
	 * Adds this instance to the global ScoreBoards list allScoreBoards.
	 * @param gameName the unique game-name for which the ScoreBoards element should be saved for.
	 * @return Whether the ScoreBoard was stored successfully or not.
	 */
	public boolean addToList(String gameName) {
		return addToList(gameName, true);
	}

	/**
	 * Adds this instance to the global ScoreBoards list allScoreBoards.
	 * @param gameName the unique game-name for which the ScoreBoards element should be saved for.
	 * @param forceReplace force the game put to the list
	 * @return Whether the ScoreBoard was stored successfully or not.
	 */
	@Override
	public boolean addToList(String gameName, boolean forceReplace) {
		if (!allScoreBoards.containsKey(gameName)) {
			allScoreBoards.put(gameName, this);
		} else if (forceReplace) {
			allScoreBoards.remove(gameName);
			allScoreBoards.put(gameName, this);
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Sets the title for the created ScoreBoard.
	 * @param player The player who needs to set the title
	 * @param title The String, where the title should be set to.
	 */
	public void setTitle(Player player, String title) {
		Optional<ScoreBoardHolder> board = getScoreboard(player);
		if (board.isPresent()) {
			board.get().getObjective().setDisplayName(title);
		}
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
		Optional<ScoreBoardHolder> board = getScoreboard(player);
		if (!board.isPresent()) {
			return;
		}

		Score score = board.get().getObjective().getScore(line);
		score.setScore(dummyScore);
	}

	/**
	 * Updates one score-line with the given String as the name and the Integer
	 * as a score which should be displayed next to the name.
	 * 
	 * @param player The Player for which the Line should be set.
	 * @param oldLine The old score-name which should be updated.
	 * @param line The name of the new score.
	 * @param dummyScore The integer, the score should be set to.
	 */
	public void updateLine(Player player, String oldLine, String line, int dummyScore) {
		Optional<ScoreBoardHolder> board = getScoreboard(player);
		if (!board.isPresent()) {
			return;
		}

		board.get().getScoreboard().resetScores(oldLine);
		Score score = board.get().getObjective().getScore(line);
		score.setScore(dummyScore);
	}

	/**
	 * Sets the ScoreBoard for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be set.
	 */
	public void setScoreBoard(Player player) {
		Optional<ScoreBoardHolder> board = getScoreboard(player);
		if (board.isPresent()) {
			player.setScoreboard(board.get().getScoreboard());
		}
	}

	/**
	 * Removes the ScoreBoard and the player who exist in the list for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be removed.
	 */
	@Override
	public void remove(Player pl) {
		pl.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
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
