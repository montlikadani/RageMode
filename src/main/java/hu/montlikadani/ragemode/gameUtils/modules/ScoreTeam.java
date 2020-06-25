package hu.montlikadani.ragemode.gameUtils.modules;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class ScoreTeam {

	private Player player;

	public ScoreTeam(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets the current player prefix/suffix if the player is in game.
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(String prefix, String suffix) {
		if (!GameUtils.isPlayerPlaying(player)) {
			remove();
			return;
		}

		final Scoreboard scoreboard = player.getScoreboard();
		final String teamName = player.getName();
		Team team = scoreboard.getTeam(teamName);
		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
		}

		NMS.addEntry(player, team);

		// Retrieves the last char from prefix
		ChatColor color = ChatColor.getByChar(prefix.substring(prefix.length() - 1));
		if (color != null)
			team.setColor(color);

		prefix = NMS.splitStringByVersion(prefix);
		suffix = NMS.splitStringByVersion(suffix);

		team.setPrefix(prefix);
		team.setSuffix(suffix);

		GameUtils.getGameByPlayer(player).getAllPlayers().stream()
				.filter(pl -> pl.getPlayer().getScoreboard() == scoreboard)
				.forEach(pl -> pl.getPlayer().setScoreboard(scoreboard));
	}

	/**
	 * Removes the team from player if the team exists.
	 */
	public void remove() {
		final Scoreboard scoreboard = player.getScoreboard();
		Team team = scoreboard.getTeam(player.getName());
		if (team == null) {
			return;
		}

		NMS.removeEntry(player, scoreboard);
		team.unregister();

		if (GameUtils.isPlayerPlaying(player)) {
			GameUtils.getGameByPlayer(player).getAllPlayers().forEach(pl -> pl.getPlayer().setScoreboard(scoreboard));
		}
	}
}
