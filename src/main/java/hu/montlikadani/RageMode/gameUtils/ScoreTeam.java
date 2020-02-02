package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class ScoreTeam implements IObjectives {

	public static HashMap<String, ScoreTeam> allTeams = new HashMap<>();

	/**
	 * Just creates a new instance of Team.
	 */
	public ScoreTeam() {
	}

	/**
	 * Adds this instance to the global ScoreTeam.
	 * @param gameName the unique game-name for which the ScoreTeam element should be saved for.
	 * @return Whether the ScoreTeam was stored successfully or not.
	 */
	public boolean addToList(String gameName) {
		return addToList(gameName, true);
	}

	/**
	 * Adds this instance to the global ScoreTeam.
	 * @param gameName the unique game-name for which the ScoreTeam element should be saved for.
	 * @param forceReplace force the game put to the list
	 * @return Whether the ScoreTeam was stored successfully or not.
	 */
	@Override
	public boolean addToList(String gameName, boolean forceReplace) {
		if (!allTeams.containsKey(gameName)) {
			allTeams.put(gameName, this);
		} else if (forceReplace) {
			allTeams.remove(gameName);
			allTeams.put(gameName, this);
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Sets the current player prefix/suffix.
	 * @param player Player
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(Player player, String prefix, String suffix) {
		// TODO: Work with scoreboard teams to prevent showing formats
		// without playing in a game
		player.setPlayerListName(prefix + player.getName() + suffix);
	}

	/**
	 * Removes the team from player
	 * @param player Player
	 */
	@Override
	public void remove(Player player) {
		player.setPlayerListName(player.getName());
	}
}
