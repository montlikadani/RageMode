package hu.montlikadani.ragemode.gameUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import hu.montlikadani.ragemode.holder.ScoreBoardHolder;

public class ScoreBoard {

	public static HashMap<String, ScoreBoard> allScoreBoards = new HashMap<>();
	private List<Player> player = new CopyOnWriteArrayList<>();
	private ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
	private HashMap<Player, ScoreBoardHolder> scoreboards = new HashMap<>();

	/**
	 * Creates a new instance of ScoreBoard, which manages the ScoreBoards for
	 * the given List of Players.
	 * 
	 * @param player The List of player, the ScoreBoard should be set to later.
	 */
	@SuppressWarnings("deprecation")
	public ScoreBoard(List<Player> player) {
		this.player = player;
		for (Player loopPlayer : player) {
			Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
			scoreboard.clearSlot(DisplaySlot.SIDEBAR);
			removeScoreBoard(loopPlayer, false);

			Objective objective = scoreboard.registerNewObjective("ragescores", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			scoreboards.put(loopPlayer, new ScoreBoardHolder(loopPlayer, scoreboard, objective));
		}
	}

	/**
	 * Creates a new instance of ScoreBoard, which manages the ScoreBoards for
	 * the given List of Player UUID Strings.
	 * 
	 * @param player The List of player, the ScoreBoard should be set to later.
	 * @param isList Set it to whatever you want, it won't be used. Just to be able to create a new
	 * constructor with the List parameter.
	 */
	@SuppressWarnings("deprecation")
	public ScoreBoard(List<String> playerString, boolean isList) {
		for (String player : playerString) {
			this.player.add(Bukkit.getPlayer(UUID.fromString(player)));
		}

		// Iterator is not work in this, so throws an error
		// Probably some issues with Java
		for (Player loopPlayer : this.player) {
			Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
			scoreboard.clearSlot(DisplaySlot.SIDEBAR);
			removeScoreBoard(loopPlayer, false);

			Objective objective = scoreboard.registerNewObjective("ragescores", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			scoreboards.put(loopPlayer, new ScoreBoardHolder(loopPlayer, scoreboard, objective));
		}
	}

	/**
	 * Sets the title for the created ScoreBoard.
	 * 
	 * @param title The String, where the title should be set to.
	 */
	public void setTitle(String title) {
		for (Player player : this.player) {
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
		for (Player player : this.player) {
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
		for (Player player : this.player) {
			setScoreBoard(player);
		}
	}

	/**
	 * Sets the ScoreBoard for the given Player.
	 * 
	 * @param player The Player instance for which the ScoreBoard should be set.
	 */
	public void setScoreBoard(Player player) {
		player.setScoreboard(scoreboards.get(player).getScoreboard());
	}

	/**
	 * Removes the ScoreBoard for all the Players given in the constructor.
	 */
	public void removeScoreBoard() {
		for (Player player : this.player) {
			removeScoreBoard(player, false);
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
			for (int i = 0; i < this.player.size(); i++) {
				if (player.equals(this.player.get(i)))
					this.player.remove(i);
			}
		}
	}

	/**
	 * Removes the player from the stored list.
	 * @param pl Player
	 */
	public void removePlayer(Player pl) {
		for (int i = 0; i < this.player.size(); i++) {
			if (pl.equals(this.player.get(i)))
				this.player.remove(i);
		}
	}

	/**
	 * Adds this instance to the global ScoreBoards list allScoreBoards. This
	 * can be accessed with the getScoreBoard(String gameName) method.
	 * 
	 * @param gameName the unique game-name for which the ScoreBoards element should be saved for.
	 * 
	 * @return Whether the ScoreBoard was stored successfully or not.
	 */
	public boolean addToScoreBoards(String gameName, boolean forceReplace) {
		if (!allScoreBoards.containsKey(gameName)) {
			allScoreBoards.put(gameName, this);
			return true;
		} else if (forceReplace) {
			allScoreBoards.remove(gameName);
			allScoreBoards.put(gameName, this);
			return true;
		} else
			return false;
	}

	/**
	 * Returns the HashMap with all the ScoreBoardHolder elements for each Player within this ScoreBoard.
	 * @return The ScoreBoardHolder HashMap.
	 */
	public HashMap<Player, ScoreBoardHolder> getScoreboards() {
		return scoreboards;
	}

	/**
	 * Returns the players who added to the list.
	 * 
	 * @return List player
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(player);
	}
}
