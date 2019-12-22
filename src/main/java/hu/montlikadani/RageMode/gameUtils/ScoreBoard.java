package hu.montlikadani.ragemode.gameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import hu.montlikadani.ragemode.holder.ScoreBoardHolder;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class ScoreBoard implements IObjectives {

	public static HashMap<String, ScoreBoard> allScoreBoards = new HashMap<>();
	private List<Player> players = new ArrayList<>();
	private ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
	private HashMap<Player, ScoreBoardHolder> scoreboards = new HashMap<>();

	/**
	 * Creates a new instance of ScoreBoard, which manages the ScoreBoards for
	 * the given List of Player UUID Strings.
	 * 
	 * @param players The list of {@link PlayerManager}
	 */
	@SuppressWarnings("deprecation")
	public ScoreBoard(List<PlayerManager> players) {
		players.forEach(pm -> this.players.add(pm.getPlayer()));

		synchronized (scoreboardManager) {
			Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
			Objective objective = scoreboard.getObjective("ragescores");
			if (objective != null) {
				objective.unregister();
			}

			objective = scoreboard.registerNewObjective("ragescores", "dummy");

			for (Player loopPlayer : this.players) {
				removeScoreBoard(loopPlayer, false);

				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				scoreboards.put(loopPlayer, new ScoreBoardHolder(loopPlayer, scoreboard, objective));
			}
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
	 * @param title The String, where the title should be set to.
	 */
	public void setTitle(String title) {
		for (Player player : this.players) {
			scoreboards.get(player).getObjective().setDisplayName(title);
		}
	}

	/**
	 * Sets one score-line with the given String as the name and the Integer as
	 * a score which should be displayed next to the name.
	 * 
	 * @param line The name of the new score.
	 * @param dummyScore The integer, the score should be set to.
	 */
	public void setLine(String line, int dummyScore) {
		for (Player player : this.players) {
			Score score = scoreboards.get(player).getObjective().getScore(line);
			score.setScore(dummyScore);
		}
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
	 * Sets the ScoreBoard for all the Players given in the constructor.
	 */
	public void setScoreBoard() {
		this.players.forEach(this::setScoreBoard);
	}

	/**
	 * Sets the ScoreBoard for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be set.
	 */
	public void setScoreBoard(Player player) {
		player.setScoreboard(scoreboards.get(player).getScoreboard());
	}

	/**
	 * Removes the ScoreBoard for all the players given in the constructor.
	 */
	@Override
	public void remove() {
		for (Player player : this.players) {
			removeScoreBoard(player, false);
		}
	}

	/**
	 * Removes the player from the stored list.
	 * @param pl Player
	 */
	@Override
	public void remove(Player pl) {
		for (Iterator<Player> it = players.iterator(); it.hasNext();) {
			if (it.next().equals(pl)) {
				it.remove();
				break;
			}
		}
	}

	/**
	 * Removes the ScoreBoard and the player who exist in the list for the given Player.
	 * @param player The Player instance for which the ScoreBoard should be removed.
	 * @param rem if true removes the exists player from the list.
	 */
	public void removeScoreBoard(Player player, boolean rem) {
		player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		scoreboards.remove(player);

		if (rem) {
			remove(player);
		}
	}

	/**
	 * Returns the HashMap with all the ScoreBoardHolder elements for each Player within this ScoreBoard.
	 * @return {@link #scoreboards}
	 */
	public HashMap<Player, ScoreBoardHolder> getScoreboards() {
		return scoreboards;
	}

	/**
	 * Returns the players who added to the list.
	 * @return List player
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}
}
