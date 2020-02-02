package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.List;

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

	/**
	 * Creates a new instance of ScoreBoard.
	 */
	public ScoreBoard() {
	}

	/**
	 * Loads the scoreboard for game players.
	 * @param players {@link PlayerManager} the List of players
	 */
	@SuppressWarnings("deprecation")
	public void loadScoreboard(List<PlayerManager> players) {
		// TODO: Fixing the scoreboard appears from other players to other players, so the same
		for (PlayerManager pm : players) {
			Player loopPlayer = pm.getPlayer();
			remove(loopPlayer);

			// getNewScoreboard() is Asynchronously, and can't fix bukkit task
			Scoreboard scoreboard = loopPlayer.getScoreboard();
			Objective objective = scoreboard.getObjective("ragescores");
			if (objective != null) {
				objective.unregister();
			}

			objective = scoreboard.registerNewObjective("ragescores", "dummy");

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
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
		scoreboards.get(player).getObjective().setDisplayName(title);
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
		Score score = scoreboards.get(player).getObjective().getScore(line);
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
		scoreboards.get(player).getScoreboard().resetScores(oldLine);
		Score score = scoreboards.get(player).getObjective().getScore(line);
		score.setScore(dummyScore);
	}

	/**
	 * Sets the ScoreBoard for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be set.
	 */
	public void setScoreBoard(Player player) {
		player.setScoreboard(scoreboards.get(player).getScoreboard());
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
}
