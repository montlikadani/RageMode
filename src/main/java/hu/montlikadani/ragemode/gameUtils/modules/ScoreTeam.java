package hu.montlikadani.ragemode.gameUtils.modules;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

@SuppressWarnings("deprecation")
public class ScoreTeam {

	private UUID playerUUID;

	public ScoreTeam(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(playerUUID);
	}

	/**
	 * Sets the current player prefix/suffix if the player is in game.
	 * 
	 * @param prefix String
	 * @param suffix String
	 */
	public void setTeam(String prefix, String suffix) {
		Player player = getPlayer();

		if (!GameUtils.isPlayerPlaying(player)) {
			remove();
			return;
		}

		final Scoreboard scoreboard = player.getScoreboard();
		final String teamName = player.getName();
		final Team team = Optional.ofNullable(scoreboard.getTeam(teamName)).orElse(scoreboard.registerNewTeam(teamName));

		addEntry(player, team);

		if (Version.isCurrentEqualOrHigher(Version.v1_13_R1) && !prefix.isEmpty()) {
			// Retrieves the last char from prefix
			ChatColor color = ChatColor.getByChar(prefix.substring(prefix.length() - 1));
			if (color != null)
				team.setColor(color);
		}

		prefix = splitStringByVersion(prefix);
		suffix = splitStringByVersion(suffix);

		team.setPrefix(prefix);
		team.setSuffix(suffix);

		GameUtils.getGameByPlayer(player).getAllPlayers().forEach(pl -> pl.getPlayer().setScoreboard(scoreboard));
	}

	/**
	 * Removes the team from player if the team exists.
	 */
	public void remove() {
		final Player player = getPlayer();
		if (player == null) {
			return;
		}

		final Scoreboard scoreboard = player.getScoreboard();
		final Team team = scoreboard.getTeam(player.getName());
		if (team == null) {
			return;
		}

		removeEntry(player, scoreboard);
		team.unregister();

		if (GameUtils.isPlayerPlaying(player)) {
			GameUtils.getGameByPlayer(player).getAllPlayers().forEach(pl -> pl.getPlayer().setScoreboard(scoreboard));
		}
	}

	private String splitStringByVersion(String s) {
		if (Version.isCurrentLower(Version.v1_13_R1) && s.length() > 16) {
			s = s.substring(0, 16);
		} else if (Version.isCurrentEqualOrHigher(Version.v1_13_R1) && s.length() > 64) {
			s = s.substring(0, 64);
		}

		return s;
	}

	private void addEntry(Player player, Team team) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			if (!team.hasPlayer(player)) {
				team.addPlayer(player);
			}
		} else if (!team.hasEntry(player.getName())) {
			team.addEntry(player.getName());
		}
	}

	private void removeEntry(Player player, Scoreboard board) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			Optional.ofNullable(board.getPlayerTeam(player)).ifPresent(team -> team.removePlayer(player));
		} else {
			Optional.ofNullable(board.getEntryTeam(player.getName())).ifPresent(team -> team.removeEntry(team.getName()));
		}
	}
}
