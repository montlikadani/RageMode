package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class RageScores {

	private static HashMap<String, PlayerPoints> playerpoints = new HashMap<>();
	private static int totalPoints = 0;

	public static void addPointsToPlayer(Player killer, Player victim, String killCause) {
		if (!killer.getUniqueId().toString().equals(victim.getUniqueId().toString())) {
			String killerUUID = killer.getUniqueId().toString();
			PlayerPoints killerPoints;
			String victimUUID = victim.getUniqueId().toString();
			PlayerPoints victimPoints;

			switch (killCause.toLowerCase().trim()) {
			case "ragebow":
				int bowPoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.bowkill");
				totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), bowPoints, true);
				addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

				killerPoints = playerpoints.get(killerUUID);
				int oldDirectArrowKills = killerPoints.getDirectArrowKills();
				int newDirectArrowKills = oldDirectArrowKills + 1;
				killerPoints.setDirectArrowKills(newDirectArrowKills);

				victimPoints = playerpoints.get(victimUUID);
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
				totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), axePoints, true);
				addPoints(victim, PlayerList.getPlayersGame(victim), axeMinusPoints, false);

				killerPoints = playerpoints.get(killerUUID);
				int oldAxeKills = killerPoints.getAxeKills();
				int newAxeKills = oldAxeKills + 1;
				killerPoints.setAxeKills(newAxeKills);

				victimPoints = playerpoints.get(victimUUID);
				int oldAxeDeaths = victimPoints.getAxeDeaths();
				int newAxeDeaths = oldAxeDeaths + 1;
				victimPoints.setAxeDeaths(newAxeDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(axePoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%", Integer.toString(axeMinusPoints)));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			case "rageknife":
				int knifePoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.knifekill");
				totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), knifePoints, true);
				addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

				killerPoints = playerpoints.get(killerUUID);
				int oldKnifeKills = killerPoints.getKnifeKills();
				int newKnifeKills = oldKnifeKills + 1;
				killerPoints.setKnifeKills(newKnifeKills);

				victimPoints = playerpoints.get(victimUUID);
				int oldKnifeDeaths = victimPoints.getKnifeDeaths();
				int newKnifeDeaths = oldKnifeDeaths + 1;
				victimPoints.setKnifeDeaths(newKnifeDeaths);

				killer.sendMessage(RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%", "+" + Integer.toString(knifePoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%", ""));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			case "explosion":
				int explosionPoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.explosionkill");
				totalPoints = addPoints(killer, PlayerList.getPlayersGame(killer), explosionPoints, true);
				addPoints(victim, PlayerList.getPlayersGame(victim), 0, false);

				killerPoints = playerpoints.get(killerUUID);
				int oldExplosionKills = killerPoints.getExplosionKills();
				int newExplosionKills = oldExplosionKills + 1;
				killerPoints.setExplosionKills(newExplosionKills);

				victimPoints = playerpoints.get(victimUUID);
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
			PlayerPoints currentPoints = playerpoints.get(killerUUID);
			int currentStreak = currentPoints.getCurrentStreak();
			if (currentStreak == 3 || currentStreak % 5 == 0) {
				currentPoints.setPoints(currentPoints.getPoints() + (currentStreak * 10));

				killer.sendMessage(RageMode.getLang().get("game.message.streak", "%number%", Integer.toString(currentStreak), "%points%",
						"+" + Integer.toString(currentStreak * 10)));
			}
		} else {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			PlayerPoints killerPoints = playerpoints.get(killer.getUniqueId().toString());
			if (killerPoints == null) return;

			int pointLoss = RageMode.getInstance().getConfiguration().getCfg().getInt("game.global.point-loss-when-suicide");
			if (pointLoss > 0) {
				int oldPoints = killerPoints.getPoints();
				int newPoints = oldPoints - pointLoss;
				killerPoints.setPoints(newPoints);
			}

			int oldDeaths = killerPoints.getDeaths();
			int newDeaths = oldDeaths + 1;
			killerPoints.setDeaths(newDeaths);
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
	 * @param string Player uuid
	 * @return playerPoints Player current points
	 */
	public static PlayerPoints getPlayerPoints(String playerUUID) {
		if (playerUUID == null) {
			throw new IllegalArgumentException("player uuid is null");
		}
		return playerpoints.containsKey(playerUUID) ? playerpoints.get(playerUUID) : null;
	}

	private static int addPoints(Player player, String gameName, int points, boolean killer) {
		// returns total points
		String playerUUID = player.getUniqueId().toString();
		if (playerpoints.containsKey(playerUUID)) {
			PlayerPoints pointsHolder = playerpoints.get(playerUUID);
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
				if (playerpoints.get(players[i]).getPoints() > highestPoints) {
					highest = players[i];
					highestPoints = playerpoints.get(players[i]).getPoints();
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
			playerpoints.get(highest).setWinner(true);

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