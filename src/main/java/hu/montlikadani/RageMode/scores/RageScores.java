package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class RageScores {

	private static HashMap<String, PlayerPoints> playerpoints = new HashMap<>();
	private static int totalPoints = 0;

	public static void load() {
		if (!YAMLStats.getFile().exists())
			return;

		int totalPlayers = 0;

		org.bukkit.configuration.file.YamlConfiguration conf = YAMLStats.getConf();
		ConfigurationSection section = conf.getConfigurationSection("data");

		if (section == null)
			return;

		for (String one : section.getKeys(false)) {
			String uuid = UUID.fromString(one).toString();
			playerpoints.put(uuid, new PlayerPoints(uuid));
			totalPlayers += section.getKeys(false).size();
		}

		if (totalPlayers > 0)
			RageMode.logConsole("[RageMode] Loaded " + totalPlayers + " player" + (totalPlayers < 1 ? "s" : "") + " database.");
	}

	public static void addPointsToPlayer(Player killer, Player victim, String killCause) {
		String killerUUID = killer.getUniqueId().toString();
		PlayerPoints killerPoints = null;

		if (!killer.getUniqueId().toString().equals(victim.getUniqueId().toString())) {
			String victimUUID = victim.getUniqueId().toString();
			PlayerPoints victimPoints = null;

			switch (killCause.toLowerCase().trim()) {
			case "ragebow":
				int bowPoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.bowkill");
				totalPoints = addPoints(killer, bowPoints, true);
				addPoints(victim, 0, false);

				killerPoints = getPlayerPoints(killerUUID);
				int oldDirectArrowKills = killerPoints.getDirectArrowKills();
				int newDirectArrowKills = oldDirectArrowKills + 1;
				killerPoints.setDirectArrowKills(newDirectArrowKills);

				victimPoints = getPlayerPoints(victimUUID);
				int oldDirectArrowDeaths = victimPoints.getDirectArrowDeaths();
				int newDirectArrowDeaths = oldDirectArrowDeaths + 1;
				victimPoints.setDirectArrowDeaths(newDirectArrowDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.arrow-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(bowPoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.arrow-death", "%killer%", killer.getName(), "%points%", ""));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			case "combataxe":
				int axePoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.axekill");
				int axeMinusPoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.axedeath");
				totalPoints = addPoints(killer, axePoints, true);
				addPoints(victim, axeMinusPoints, false);

				killerPoints = getPlayerPoints(killerUUID);
				int oldAxeKills = killerPoints.getAxeKills();
				int newAxeKills = oldAxeKills + 1;
				killerPoints.setAxeKills(newAxeKills);

				victimPoints = getPlayerPoints(victimUUID);
				int oldAxeDeaths = victimPoints.getAxeDeaths();
				int newAxeDeaths = oldAxeDeaths + 1;
				victimPoints.setAxeDeaths(newAxeDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(axePoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%", Integer.toString(axeMinusPoints)));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			case "rageknife":
				int knifePoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.knifekill");
				totalPoints = addPoints(killer, knifePoints, true);
				addPoints(victim, 0, false);

				killerPoints = getPlayerPoints(killerUUID);
				int oldKnifeKills = killerPoints.getKnifeKills();
				int newKnifeKills = oldKnifeKills + 1;
				killerPoints.setKnifeKills(newKnifeKills);

				victimPoints = getPlayerPoints(victimUUID);
				int oldKnifeDeaths = victimPoints.getKnifeDeaths();
				int newKnifeDeaths = oldKnifeDeaths + 1;
				victimPoints.setKnifeDeaths(newKnifeDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(knifePoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%", ""));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			case "explosion":
				int explosionPoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.explosionkill");
				totalPoints = addPoints(killer, explosionPoints, true);
				addPoints(victim, 0, false);

				killerPoints = getPlayerPoints(killerUUID);
				int oldExplosionKills = killerPoints.getExplosionKills();
				int newExplosionKills = oldExplosionKills + 1;
				killerPoints.setExplosionKills(newExplosionKills);

				victimPoints = getPlayerPoints(victimUUID);
				int oldExplosionDeaths = victimPoints.getExplosionDeaths();
				int newExplosionDeaths = oldExplosionDeaths + 1;
				victimPoints.setExplosionDeaths(newExplosionDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.explosion-kill", "%victim%", victim.getName(), "%points%",
						"+" + Integer.toString(explosionPoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.explosion-death", "%killer%", killer.getName(), "%points%", ""));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			default:
				break;
			}

			// KillStreak
			PlayerPoints currentPoints = getPlayerPoints(killerUUID);
			int currentStreak = currentPoints.getCurrentStreak();
			if (currentStreak == 3 || currentStreak % 5 == 0) {
				currentPoints.setPoints(currentPoints.getPoints() + (currentStreak * 10));

				killer.sendMessage(RageMode.getLang().get("game.message.streak", "%number%", Integer.toString(currentStreak), "%points%",
						"+" + Integer.toString(currentStreak * 10)));
			}
		} else {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			int pointLoss = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.point-loss-for-suicide");
			if (pointLoss > 0)
				takePoints(killer, pointLoss);
		}
	}

	public static void removePointsForPlayers(String[] playerUUIDs) {
		for (String playerUUID : playerUUIDs) {
			if (playerpoints.containsKey(playerUUID))
				playerpoints.remove(playerUUID);
		}
	}

	/**
	 * Gets the specified player points
	 * 
	 * @param string UUID of player
	 * @return playerPoints Player points
	 */
	public static PlayerPoints getPlayerPoints(String playerUUID) {
		if (playerUUID == null) {
			throw new IllegalArgumentException("player uuid is null");
		}
		return playerpoints.get(playerUUID);
	}

	private static int addPoints(Player player, int points, boolean killer) {
		// returns total points
		String playerUUID = player.getUniqueId().toString();
		if (playerpoints.containsKey(playerUUID)) {
			PlayerPoints pointsHolder = getPlayerPoints(playerUUID);
			int oldPoints = pointsHolder.getPoints();
			int oldKills = pointsHolder.getKills();
			int oldDeaths = pointsHolder.getDeaths();
			// playerpoints.remove(playerUUID);
			int totalPoints = oldPoints + points;
			int totalKills = oldKills;
			int totalDeaths = oldDeaths;
			int currentStreak = 0;
			int longestStreak = 0;
			if (killer) {
				totalKills++;
				currentStreak = pointsHolder.getCurrentStreak() + 1;
			} else {
				totalDeaths++;
				currentStreak = 0;
			}
			longestStreak = (currentStreak > pointsHolder.getLongestStreak()) ? currentStreak : pointsHolder.getLongestStreak();

			pointsHolder.setPoints(totalPoints);
			pointsHolder.setKills(totalKills);
			pointsHolder.setDeaths(totalDeaths);
			pointsHolder.setCurrentStreak(currentStreak);
			pointsHolder.setLongestStreak(longestStreak);
			// playerpoints.put(playerUUID, pointsHolder);
			return totalPoints;
		} else {
			int totalKills = 0;
			int totalDeaths = 0;
			int currentStreak = 0;
			int longestStreak = 0;
			if (killer) {
				totalKills = 1;
				currentStreak = 1;
				longestStreak = 1;
			} else {
				totalDeaths = 1;
				currentStreak = 0;
			}
			PlayerPoints pointsHolder = new PlayerPoints(playerUUID);
			pointsHolder.setPoints(points);
			pointsHolder.setKills(totalKills);
			pointsHolder.setDeaths(totalDeaths);
			pointsHolder.setCurrentStreak(currentStreak);
			pointsHolder.setLongestStreak(longestStreak);
			playerpoints.put(playerUUID, pointsHolder);
			return points;
		}
	}

	public static int takePoints(Player player, int points) {
		// returns total points
		String playerUUID = player.getUniqueId().toString();
		if (playerpoints.containsKey(playerUUID)) {
			PlayerPoints pointsHolder = getPlayerPoints(playerUUID);
			int oldPoints = pointsHolder.getPoints();
			int oldDeaths = pointsHolder.getDeaths();
			int totalPoints = oldPoints > 0 ? (oldPoints - points) : 0;
			int totalDeaths = oldDeaths;

			totalDeaths++;

			pointsHolder.setPoints(totalPoints);
			pointsHolder.setDeaths(totalDeaths);

			player.sendMessage(RageMode.getLang().get("game.message.points-loss-for-suicide", "%amount%", totalPoints,
					"%deaths%", totalDeaths));
			return totalPoints;
		}
		return points;
	}

	public static String calculateWinner(String game, String[] players) {
		String highest = UUID.randomUUID().toString();
		String goy = highest;
		int highestPoints = -200000000;
		int i = 0;
		int imax = players.length;
		while (i < imax) {
			if (playerpoints.containsKey(players[i])) {
				// Bukkit.broadcastMessage(Bukkit.getPlayer(UUID.fromString(players[i])).getName()
				// + " " + Integer.toString(i) + " " +
				// playerpoints.get(players[i]).getPoints() + " " +
				// Integer.toString(highestPoints));
				if (getPlayerPoints(players[i]).getPoints() > highestPoints) {
					highest = players[i];
					highestPoints = getPlayerPoints(players[i]).getPoints();
				}
				// else
				// Bukkit.broadcastMessage("nothighest"+players[i]);
			}
			// else
			// Bukkit.broadcastMessage(players[i]);
			i++;

		}

		if (goy == highest) {
			i = 0;
			while (i < imax) {
					Bukkit.getPlayer(UUID.fromString(players[i])).sendMessage(RageMode.getLang().get("game.message.player-won", "%player%",
							"Herobrine", "%game%", game));
				i++;
			}
			return null;
		} else {
			getPlayerPoints(highest).setWinner(true);

			i = 0;
			while (i < imax) {
				if (players[i].equals(highest))
					Bukkit.getPlayer(UUID.fromString(highest)).sendMessage(RageMode.getLang().get("game.message.you-won", "%game%", game));
				else
					Bukkit.getPlayer(UUID.fromString(players[i])).sendMessage(RageMode.getLang().get("game.message.player-won", "%player%",
							Bukkit.getPlayer(UUID.fromString(highest)).getName(), "%game%", game));
				i++;
			}
		}
		return highest;
	}
}