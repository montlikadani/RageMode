package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.PlayerWinEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class RageScores {

	private static final Map<UUID, PlayerPoints> PLAYERPOINTS = new HashMap<>();

	public static void addPointsToPlayer(Player killer, LivingEntity entity) {
		getPlayerPoints(killer.getUniqueId()).ifPresent(killerPoints -> {
			int currentZombieKills = killerPoints.getZombieKills();
			int newZombieKills = currentZombieKills + 1;
			killerPoints.setZombieKills(newZombieKills);

			int totalPoints = addPoints(killer, 1, true);

			if (!PlayerManager.DEATHMESSAGESTOGGLE.getOrDefault(killer.getUniqueId(), false)
					&& java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 100) < 25) {
				String msg = RageMode.getLang().get("game.broadcast.zombie-kill", "%entity%", entity.getName(),
						"%killer%", killer.getName(), "%remainEntities%",
						GameAreaManager.inArea(killer.getLocation())
								? GameAreaManager.getAreaByLocation(killer.getLocation()).get().getEntities().size()
								: 0);
				GameUtils.broadcastToGame(GameUtils.getGameByPlayer(killer), msg);

				sendMessage(killer, RageMode.getLang().get("game.message.zombie-kill", "%entity%", entity.getName(),
						"%points%", "+1"));
				sendMessage(killer, RageMode.getLang().get("game.message.current-points", "%points%", totalPoints));
			}
		});
	}

	public static void addPointsToPlayer(Player killer, Player victim, KilledWith killCause) {
		UUID killerUUID = killer.getUniqueId(),
				victimUUID = victim.getUniqueId();

		// Check if player not killed itself
		if (killerUUID.equals(victimUUID)) {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			PlayerPoints pointsHolder = getPlayerPoints(killerUUID).orElseGet(() -> {
				PlayerPoints ph = new PlayerPoints(killerUUID);
				PLAYERPOINTS.put(killerUUID, ph);
				return ph;
			});

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

		PlayerPoints killerPoints = null,
				victimPoints = null;

		String killerMessage = "",
				victimMsg = "",
				killerMsg2 = "";

		int totalPoints = 0;

		switch (killCause) {
		case RAGEBOW:
			int bowPoints = ConfigValues.getBowKill();
			totalPoints = addPoints(killer, bowPoints, true);
			addPoints(victim, 0, false);

			killerPoints = getPlayerPoints(killerUUID).get();
			int oldDirectArrowKills = killerPoints.getDirectArrowKills();
			int newDirectArrowKills = oldDirectArrowKills + 1;
			killerPoints.setDirectArrowKills(newDirectArrowKills);

			victimPoints = getPlayerPoints(victimUUID).get();
			int oldDirectArrowDeaths = victimPoints.getDirectArrowDeaths();
			int newDirectArrowDeaths = oldDirectArrowDeaths + 1;
			victimPoints.setDirectArrowDeaths(newDirectArrowDeaths);

			killerMessage = RageMode.getLang().get("game.message.arrow-kill", "%victim%", victim.getName(), "%points%",
					"+" + bowPoints);

			victimMsg = RageMode.getLang().get("game.message.arrow-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case COMBATAXE:
			int axePoints = ConfigValues.getAxeKill();
			int axeMinusPoints = ConfigValues.getAxeDeath();
			totalPoints = addPoints(killer, axePoints, true);
			addPoints(victim, axeMinusPoints, false);

			killerPoints = getPlayerPoints(killerUUID).get();
			int oldAxeKills = killerPoints.getAxeKills();
			int newAxeKills = oldAxeKills + 1;
			killerPoints.setAxeKills(newAxeKills);

			victimPoints = getPlayerPoints(victimUUID).get();
			int oldAxeDeaths = victimPoints.getAxeDeaths();
			int newAxeDeaths = oldAxeDeaths + 1;
			victimPoints.setAxeDeaths(newAxeDeaths);

			killerMessage = RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%",
					"+" + axePoints);

			victimMsg = RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%",
					axeMinusPoints);

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case RAGEKNIFE:
			int knifePoints = ConfigValues.getKnifeKill();
			totalPoints = addPoints(killer, knifePoints, true);
			addPoints(victim, 0, false);

			killerPoints = getPlayerPoints(killerUUID).get();
			int oldKnifeKills = killerPoints.getKnifeKills();
			int newKnifeKills = oldKnifeKills + 1;
			killerPoints.setKnifeKills(newKnifeKills);

			victimPoints = getPlayerPoints(victimUUID).get();
			int oldKnifeDeaths = victimPoints.getKnifeDeaths();
			int newKnifeDeaths = oldKnifeDeaths + 1;
			victimPoints.setKnifeDeaths(newKnifeDeaths);

			killerMessage = RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%",
					"+" + knifePoints);

			victimMsg = RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case EXPLOSION:
			int explosionPoints = ConfigValues.getExplosionKill();
			totalPoints = addPoints(killer, explosionPoints, true);
			addPoints(victim, 0, false);

			killerPoints = getPlayerPoints(killerUUID).get();
			int oldExplosionKills = killerPoints.getExplosionKills();
			int newExplosionKills = oldExplosionKills + 1;
			killerPoints.setExplosionKills(newExplosionKills);

			victimPoints = getPlayerPoints(victimUUID).get();
			int oldExplosionDeaths = victimPoints.getExplosionDeaths();
			int newExplosionDeaths = oldExplosionDeaths + 1;
			victimPoints.setExplosionDeaths(newExplosionDeaths);

			killerMessage = RageMode.getLang().get("game.message.explosion-kill", "%victim%", victim.getName(),
					"%points%", "+" + explosionPoints);

			victimMsg = RageMode.getLang().get("game.message.explosion-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		case GRENADE:
			int grenadePoints = ConfigValues.getGrenadeKill();
			totalPoints = addPoints(killer, grenadePoints, true);
			addPoints(victim, 0, false);

			killerPoints = getPlayerPoints(killerUUID).orElse(null);

			killerMessage = RageMode.getLang().get("game.message.grenade-kill", "%victim%", victim.getName(),
					"%points%", "+" + grenadePoints);

			victimMsg = RageMode.getLang().get("game.message.grenade-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", totalPoints);
			break;
		default:
			break;
		}

		sendMessage(killer, killerMessage);
		sendMessage(victim, victimMsg);
		sendMessage(killer, killerMsg2);

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

	/**
	 * Removes the given player cached points statistic.
	 * 
	 * @param playerUUID {@link UUID}
	 */
	public static void removePointsForPlayer(UUID playerUUID) {
		Validate.notNull(playerUUID, "Player UUID can't be null!");

		if (PLAYERPOINTS.containsKey(playerUUID)) {
			PLAYERPOINTS.remove(playerUUID);
		}
	}

	/**
	 * Gets the given player points
	 * 
	 * @param playerUUID UUID of player
	 * @return {@link PlayerPoints} if present
	 */
	public static Optional<PlayerPoints> getPlayerPoints(UUID playerUUID) {
		Validate.notNull(playerUUID, "Player UUID can't be null!");

		return Optional.ofNullable(PLAYERPOINTS.get(playerUUID));
	}

	/**
	 * Gets the map of cached players containing {@link PlayerPoints}
	 * 
	 * @return the map of cached players
	 */
	public static Map<UUID, PlayerPoints> getPlayerPointsMap() {
		return PLAYERPOINTS;
	}

	private static int addPoints(Player player, int points, boolean killer) {
		UUID playerUUID = player.getUniqueId();
		int totalKills = 0,
				totalDeaths = 0,
				currentStreak = 0,
				longestStreak = 0;

		if (PLAYERPOINTS.containsKey(playerUUID)) {
			PlayerPoints pointsHolder = getPlayerPoints(playerUUID).get();
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
			longestStreak = (currentStreak > pointsHolder.getLongestStreak()) ? currentStreak
					: pointsHolder.getLongestStreak();

			pointsHolder.setKills(totalKills);
			pointsHolder.setDeaths(totalDeaths);
			pointsHolder.setCurrentStreak(currentStreak);
			pointsHolder.setLongestStreak(longestStreak);
			pointsHolder.addPoints(
					killer ? (points + hu.montlikadani.ragemode.gameLogic.Bonus.getPointBonus()) : points);

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
		PLAYERPOINTS.put(playerUUID, pointsHolder);
		return pointsHolder.getPoints();
	}

	public static UUID calculateWinner(Game game, Set<PlayerManager> players) {
		game.setStatus(GameStatus.WINNER_CALCULATING);

		UUID highest = UUID.randomUUID(),
				resultPlayer = null,
				goy = highest;
		int highestPoints = 0;
		for (PlayerManager pm : players) {
			UUID uuid = pm.getPlayer().getUniqueId();
			if (getPlayerPoints(uuid).isPresent() && getPlayerPoints(uuid).get().getPoints() > highestPoints) {
				highest = uuid;
				highestPoints = getPlayerPoints(uuid).get().getPoints();
				resultPlayer = uuid;
			}
		}

		if (resultPlayer == null) {
			Debug.logConsole(Level.WARNING,
					"There was an error while calculating the winner player. Seems no winner player.");
			return null;
		}

		if (goy.equals(highest)) {
			sendMessage(Bukkit.getPlayer(resultPlayer), RageMode.getLang().get("game.message.player-won", "%player%",
					"Herobrine", "%game%", game.getName()));
			return null;
		}

		getPlayerPoints(highest).ifPresent(p -> p.setWinner(true));

		Player winner = Bukkit.getPlayer(highest);
		if (resultPlayer.equals(highest)) {
			sendMessage(winner, RageMode.getLang().get("game.message.you-won", "%game%", game.getName()));
		} else {
			sendMessage(Bukkit.getPlayer(resultPlayer), RageMode.getLang().get("game.message.player-won", "%player%",
					winner.getName(), "%game%", game.getName()));
		}

		Utils.callEvent(new PlayerWinEvent(game, winner));
		return highest;
	}
}