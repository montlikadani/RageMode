package hu.montlikadani.ragemode.gameUtils.modules;

import org.bukkit.entity.Player;

public class ScoreTeam {

	private Player player;

	public ScoreTeam(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
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
	public void remove() {
		player.setPlayerListName(player.getName());
	}
}
