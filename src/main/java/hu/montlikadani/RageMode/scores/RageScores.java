package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.PlayerWinEvent;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class RageScores {

	private static HashMap<UUID, PlayerPoints> playerpoints = new HashMap<>();
	private static int totalPoints = 0;

	public static void addPointsToPlayer(Player killer, Player victim, String killCause) {
		UUID killerUUID = killer.getUniqueId();
		PlayerPoints killerPoints = null;

		// Check if player not killed itself
		if (!killerUUID.equals(victim.getUniqueId())) {
			UUID victimUUID = victim.getUniqueId();
			PlayerPoints victimPoints = null;

			switch (killCause.toLowerCase().trim()) {
			case "ragebow":
				int bowPoints = RageMode.getInstance().getConfiguration().getCV().getBowKill();
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
				int axeMinusPoints = RageMode.getInstance().getConfiguration().getCV().getAxeDeath();
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
				int knifePoints = RageMode.getInstance().getConfiguration().getCV().getKnifeKill();
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
				int explosionPoints = RageMode.getInstance().getConfiguration().getCV().getExplosionKill();
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
			case "grenade":
				int grenadePoints = RageMode.getInstance().getConfiguration().getCV().getGrenadeKill();
				totalPoints = addPoints(killer, grenadePoints, true);
				addPoints(victim, 0, false);

				killer.sendMessage(RageMode.getLang().get("game.message.grenade-kill", "%victim%", victim.getName(), "%points%",
						"+" + Integer.toString(grenadePoints)));

				victim.sendMessage(RageMode.getLang().get("game.message.grenade-death", "%killer%", killer.getName(), "%points%", ""));

				killer.sendMessage(RageMode.getLang().get("game.message.current-points", "%points%", Integer.toString(totalPoints)));
				break;
			default:
				break;
			}

			// KillStreak
			PlayerPoints currentPoints = getPlayerPoints(killerUUID);
			int currentStreak = currentPoints.getCurrentStreak();
			if (currentStreak == 3 || currentStreak % 5 == 0) {
				currentPoints.addPoints(currentStreak * 10);

				killer.sendMessage(RageMode.getLang().get("game.message.streak", "%number%", Integer.toString(currentStreak), "%points%",
						"+" + Integer.toString(currentStreak * 10)));
			}
		} else {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			int pointLoss = RageMode.getInstance().getConfiguration().getCV().getSuicide();
			PlayerPoints pointsHolder = getPlayerPoints(killerUUID);
			if (pointsHolder != null) {
				if (pointLoss != 0) {
					pointsHolder.addPoints(pointLoss);

					/*if ((pointsHolder.getPoints() + pointLoss) < 0) {
						killer.sendMessage(RageMode.getLang().get("game.no-enough-points"));
					}*/
				}
			} else {
				pointsHolder = new PlayerPoints(killerUUID);
				playerpoints.put(killerUUID, pointsHolder);
			}

			pointsHolder.setDeaths(pointsHolder.getDeaths() + 1);
		}
	}

	@Deprecated
	public static void removePointForPlayer(String uuid) {
		Validate.notNull(uuid, "Player UUID can't be null!");
		Validate.notEmpty(uuid, "Player UUID can't be empty!");

		removePointsForPlayer(UUID.fromString(uuid));
	}

	public static void removePointsForPlayer(UUID playerUUID) {
		Validate.notNull(playerUUID, "Player UUID can't be null!");

		if (playerpoints.containsKey(playerUUID)) {
			playerpoints.remove(playerUUID);
		}
	}

	/**
	 * Gets the given player points
	 * @param playerUUID UUID of player
	 * @return {@link PlayerPoints}
	 */
	@Deprecated
	public static PlayerPoints getPlayerPoints(String uuid) {
		Validate.notNull(uuid, "Player UUID can't be null!");
		Validate.notEmpty(uuid, "Player UUID can't be empty!");

		return getPlayerPoints(UUID.fromString(uuid));
	}

	/**
	 * Gets the given player points
	 * @param playerUUID UUID of player
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPlayerPoints(UUID playerUUID) {
		Validate.notNull(playerUUID, "Player UUID can't be null!");

		return playerpoints.get(playerUUID);
	}

	/**
	 * Gets the {@link PlayerPoints}
	 * @return {@link #playerpoints}
	 */
	public static HashMap<UUID, PlayerPoints> getPlayerPointsMap() {
		return playerpoints;
	}

	private static int addPoints(Player player, int points, boolean killer) {
		UUID playerUUID = player.getUniqueId();
		int totalKills = 0;
		int totalDeaths = 0;
		int currentStreak = 0;
		int longestStreak = 0;

		if (playerpoints.containsKey(playerUUID)) {
			PlayerPoints pointsHolder = getPlayerPoints(playerUUID);
			int oldKills = pointsHolder.getKills();
			int oldDeaths = pointsHolder.getDeaths();

			totalKills = oldKills;
			totalDeaths = oldDeaths;
			if (killer) {
				totalKills++;
				currentStreak = pointsHolder.getCurrentStreak() + 1;
			} else {
				totalDeaths++;
				currentStreak = 0;
			}
			longestStreak = (currentStreak > pointsHolder.getLongestStreak()) ? currentStreak : pointsHolder.getLongestStreak();

			pointsHolder.setKills(totalKills);
			pointsHolder.setDeaths(totalDeaths);
			pointsHolder.setCurrentStreak(currentStreak);
			pointsHolder.setLongestStreak(longestStreak);
			if (killer) {
				pointsHolder.addPoints(points + GameUtils.getBonus().getPointBonus(player));
			} else {
				pointsHolder.addPoints(points);
			}

			return pointsHolder.getPoints();
		}

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
		return pointsHolder.getPoints();
	}

	public static UUID calculateWinner(String game, List<PlayerManager> players) {
		UUID highest = UUID.randomUUID();
		UUID resultPlayer = null;
		UUID goy = highest;
		int highestPoints = 0;
		for (PlayerManager pm : players) {
			UUID uuid = pm.getPlayer().getUniqueId();
			if (uuid == null) {
				continue;
			}

			if (playerpoints.containsKey(uuid)) {
				if (getPlayerPoints(uuid).getPoints() > highestPoints) {
					highest = uuid;
					highestPoints = getPlayerPoints(uuid).getPoints();
					resultPlayer = uuid;
				}
			}
		}

		if (resultPlayer == null) {
			Debug.logConsole(Level.WARNING, "There was an error while calculating the winner player. Seems no winner player.");
			return null;
		}

		if (goy == highest) {
			Bukkit.getPlayer(resultPlayer).sendMessage(
					RageMode.getLang().get("game.message.player-won", "%player%", "Herobrine", "%game%", game));
			return null;
		}

		getPlayerPoints(highest).setWinner(true);

		Player winner = Bukkit.getPlayer(highest);
		if (resultPlayer.equals(highest))
			winner.sendMessage(RageMode.getLang().get("game.message.you-won", "%game%", game));
		else
			Bukkit.getPlayer(resultPlayer).sendMessage(RageMode.getLang().get("game.message.player-won",
					"%player%", winner.getName(), "%game%", game));

		PlayerWinEvent event = new PlayerWinEvent(GameUtils.getGame(game), winner);
		Utils.callEvent(event);

		return highest;
	}
}