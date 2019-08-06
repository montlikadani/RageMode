package hu.montlikadani.ragemode.gameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
	 * @param forceReplace force the game put to the list
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
	 * Returns the stored players who added to the list.
	 * @return List player
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(player);
	}

	/**
	 * Sends ScoreTeam to all online players that are currently playing in the game.
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
	 * @param player Player
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(Player player, String prefix, String suffix) {
		player.setPlayerListName(prefix + player.getName() + suffix);
	}

	/**
	 * Removes the team from player
	 * @param player Player
	 */
	public void removeTeam(Player player) {
		player.setPlayerListName(player.getName());

		for (int i = 0; i < this.player.size(); i++) {
			if (player.equals(this.player.get(i))) {
				this.player.remove(i);
			}
		}
	}

	/**
	 * Removing the team from all online player that are currently playing in the game.
	 */
	public void removeTeam() {
		for (Player player : this.player) {
			removeTeam(player);
		}
	}
}
