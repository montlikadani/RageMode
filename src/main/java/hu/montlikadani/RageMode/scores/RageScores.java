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
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class RageScores {

	private static HashMap<UUID, PlayerPoints> playerpoints = new HashMap<>();

	public static void addPointsToPlayer(Player killer, Player victim, String killCause) {
		UUID killerUUID = killer.getUniqueId();
		UUID victimUUID = victim.getUniqueId();

		// Check if player not killed itself
		if (killerUUID.equals(victimUUID)) {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			PlayerPoints pointsHolder = getPlayerPoints(killerUUID);
			if (pointsHolder == null) {
				pointsHolder = new PlayerPoints(killerUUID);
				playerpoints.put(killerUUID, pointsHolder);
			}

			int pointLoss = ConfigValues.getSuicide();
			if (pointLoss != 0) {
				pointsHolder.addPoints(pointLoss);
				/*if ((pointsHolder.getPoints() + pointLoss) < 0) {
					killer.sendMessage(RageMode.getLang().get("game.no-enough-points"));
				}*/
			}

			pointsHolder.setDeaths(pointsHolder.getDeaths() + 1);
			return;
		}

		PlayerPoints killerPoints = null;
		PlayerPoints victimPoints = null;

		String killerMessage = "";
		String victimMsg = "";
		String killerMsg2 = "";

		int totalPoints = 0;

		switch (killCause.toLowerCase().trim()) {
		case "ragebow":
			int bowPoints = ConfigValues.getBowKill();
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

			killerMessage = RageMode.getLang().get("game.message.arrow-kill", "%victim%", victim.getName(), "%points%",
					"+" + bowPoints);

			victimMsg = RageMode.getLang().get("game.message.arrow-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case "combataxe":
			int axePoints = RageMode.getInstance().getConfiguration().getCfg().getInt("points.axekill");
			int axeMinusPoints = ConfigValues.getAxeDeath();
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

			killerMessage = RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%",
					"+" + axePoints);

			victimMsg = RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%",
					axeMinusPoints);

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case "rageknife":
			int knifePoints = ConfigValues.getKnifeKill();
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

			killerMessage = RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%",
					"+" + knifePoints);

			victimMsg = RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case "explosion":
			int explosionPoints = ConfigValues.getExplosionKill();
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

			killerMessage = RageMode.getLang().get("game.message.explosion-kill", "%victim%", victim.getName(),
					"%points%", "+" + explosionPoints);

			victimMsg = RageMode.getLang().get("game.message.explosion-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case "grenade":
			int grenadePoints = ConfigValues.getGrenadeKill();
			totalPoints = addPoints(killer, grenadePoints, true);
			addPoints(victim, 0, false);

			killerPoints = getPlayerPoints(killerUUID);

			killerMessage = RageMode.getLang().get("game.message.grenade-kill", "%victim%", victim.getName(),
					"%points%", "+" + grenadePoints);

			victimMsg = RageMode.getLang().get("game.message.grenade-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		default:
			break;
		}

		if (!killerMessage.isEmpty()) {
			sendMessage(killer, killerMessage);
		}

		if (!victimMsg.isEmpty()) {
			sendMessage(victim, victimMsg);
		}

		if (!killerMsg2.isEmpty()) {
			sendMessage(killer, killerMsg2);
		}

		if (killerPoints == null) {
			return;
		}

		// KillStreak
		int currentStreak = killerPoints.getCurrentStreak();
		if (currentStreak == 3 || currentStreak % 5 == 0) {
			killerPoints.addPoints(currentStreak * 10);

			sendMessage(killer, RageMode.getLang().get("game.message.streak", "%number%", currentStreak, "%points%",
					"+" + Integer.toString(currentStreak * 10)));
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
	 * @deprecated converting string to uuid is too long time
	 * @param uuid UUID of player
	 * @see #getPlayerPoints(UUID)
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

			if (playerpoints.containsKey(uuid) && getPlayerPoints(uuid).getPoints() > highestPoints) {
				highest = uuid;
				highestPoints = getPlayerPoints(uuid).getPoints();
				resultPlayer = uuid;
			}
		}

		if (resultPlayer == null) {
			Debug.logConsole(Level.WARNING, "There was an error while calculating the winner player. Seems no winner player.");
			return null;
		}

		if (goy == highest) {
			sendMessage(Bukkit.getPlayer(resultPlayer),
					RageMode.getLang().get("game.message.player-won", "%player%", "Herobrine", "%game%", game));
			return null;
		}

		getPlayerPoints(highest).setWinner(true);

		Player winner = Bukkit.getPlayer(highest);
		if (resultPlayer.equals(highest)) {
			sendMessage(winner, RageMode.getLang().get("game.message.you-won", "%game%", game));
		} else {
			sendMessage(Bukkit.getPlayer(resultPlayer),
					RageMode.getLang().get("game.message.player-won", "%player%", winner.getName(), "%game%", game));
		}

		PlayerWinEvent event = new PlayerWinEvent(GameUtils.getGame(game), winner);
		Utils.callEvent(event);

		return highest;
	}
}