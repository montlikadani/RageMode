package hu.montlikadani.ragemode.gameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class ScoreTeam {

	public static HashMap<String, ScoreTeam> allTeams = new HashMap<>();
	private List<Player> player = new ArrayList<>();

	/**
	 * Creates a new instance of Team, which manages the Team for
	 * the team prefixes/suffixes.
	 * 
	 * @param playerString List players that can be add to the list
	 */
	public ScoreTeam(List<String> playerString) {
		for (String player : playerString) {
			this.player.add(Bukkit.getPlayer(UUID.fromString(player)));
		}
	}

	/**
	 * Adds this instance to the global ScoreTeam. This
	 * can be accessed with the getScore(String gameName) method.
	 * 
	 * @param gameName the unique game-name for which the ScoreTeam element should be saved for.
	 * @return Whether the ScoreTeam was stored successfully or not.
	 */
	public boolean addToTeam(String gameName, boolean forceReplace) {
		if (!allTeams.containsKey(gameName)) {
			allTeams.put(gameName, this);
			return true;
		} else if (forceReplace) {
			allTeams.remove(gameName);
			allTeams.put(gameName, this);
			return true;
		} else
			return false;
	}

	/**
	 * Returns the ScoreTeam element which was saved for the unique gameName
	 * String with the addToScore method.
	 * 
	 * @param gameName The unique String key for which the TabList was saved.
	 * @return The ScoreTeam element which was saved for the given String.
	 */
	public ScoreTeam getScore(String gameName) {
		return allTeams.containsKey(gameName) ? allTeams.get(gameName) : null;
	}

	/**
	 * Sends ScoreTeam to all online players that are currently playing in the game.
	 * 
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(String prefix, String suffix) {
		for (Player player : this.player) {
			setTeam(player, prefix, suffix);
		}
	}

	/**
	 * Sets the current player prefix/suffix.
	 * 
	 * @param player Player
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(Player player, String prefix, String suffix) {
		Scoreboard board = player.getScoreboard();
		Team team = getScoreboardTeam(board, player.getName());

		if (!team.hasEntry(player.getName()))
			team.addEntry(player.getName());

		if (prefix == null) prefix = "";
		if (suffix == null) suffix = "";

		prefix = RageMode.getLang().colors(prefix);
		suffix = RageMode.getLang().colors(suffix);

		// Prefix & suffix char limit, to prevent error
		if (Version.isCurrentLower(Version.v1_13_R1)) {
			if (prefix.length() > 15) prefix = prefix.substring(0, 16);
			if (suffix.length() > 15) suffix = suffix.substring(0, 16);
		} else {
			if (prefix.length() > 63) prefix = prefix.substring(0, 64);
			if (suffix.length() > 63) suffix = suffix.substring(0, 64);
		}

		team.setPrefix(prefix);
		team.setSuffix(suffix);
		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
			team.setColor(Utils.fromPrefix(prefix));

		player.setScoreboard(board);
	}

	/**
	 * Removes the team from player
	 * 
	 * @param player Player
	 */
	public void removeTeam(Player player) {
		Scoreboard board = player.getScoreboard();
		getScoreboardTeam(board, player.getName()).unregister();
		player.setScoreboard(board);
	}

	/**
	 * Removing the team from all online player that are currently playing in the game.
	 */
	public void removeTeam() {
		for (Player player : this.player) {
			removeTeam(player);
		}
	}

	/**
	 * Gets the scoreboard team. If not exists, then creates a new
	 * 
	 * @param board Scoreboard
	 * @param name String
	 * @return Team if exist
	 */
	private Team getScoreboardTeam(Scoreboard board, String name) {
		return board.getTeam(name) == null ? board.registerNewTeam(name) : board.getTeam(name);
	}
}
