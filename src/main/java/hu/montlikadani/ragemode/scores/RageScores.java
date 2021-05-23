package hu.montlikadani.ragemode.scores;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.PlayerWinEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.GameItems;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class RageScores {

	private static final Map<UUID, PlayerPoints> PLAYERPOINTS = new HashMap<>();

	private static final RageMode PLUGIN = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public static void addPointsToPlayer(Player killer, LivingEntity entity) {
		PlayerPoints killerPoints = addPoints(killer, 1, true);

		killerPoints.setZombieKills(killerPoints.getZombieKills() + 1);

		if (java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 100) > 25
				|| PlayerManager.DEATH_MESSAGES_TOGGLE.getOrDefault(killer.getUniqueId(), false)) {
			return;
		}

		GameAreaManager.getAreaByLocation(killer.getLocation())
				.ifPresent(area -> GameUtils.broadcastToGame(area.getGame(),
						RageMode.getLang().get("game.broadcast.zombie-kill", "%entity%", entity.getName(), "%killer%",
								killer.getName(), "%remainEntities%", area.getEntities().size())));

		sendMessage(killer,
				RageMode.getLang().get("game.message.zombie-kill", "%entity%", entity.getName(), "%points%", "+1"));
		sendMessage(killer,
				RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints()));
	}

	public static void addPointsToPlayer(Player killer, Player victim, GameItems killCause) {
		UUID killerUUID = killer.getUniqueId(), victimUUID = victim.getUniqueId();

		// Check if player not killed itself
		if (killerUUID.equals(victimUUID)) {
			killer.sendMessage(RageMode.getLang().get("game.message.suicide"));

			PlayerPoints pointsHolder = getPlayerPoints(killerUUID).orElseGet(() -> {
				PlayerPoints ph = new PlayerPoints(killerUUID);
				PLAYERPOINTS.put(killerUUID, ph);
				return ph;
			});

			if (ConfigValues.getSuicide() != 0) {
				pointsHolder.addPoints(ConfigValues.getSuicide());
			}

			pointsHolder.setDeaths(pointsHolder.getDeaths() + 1);
			return;
		}

		PlayerPoints killerPoints = null, victimPoints = null;

		String killerMessage = "", victimMsg = "", killerMsg2 = "";

		switch (killCause) {
		case RAGEBOW:
			int bowPoints = ConfigValues.getBowKill();

			killerPoints = addPoints(killer, bowPoints, true);
			victimPoints = addPoints(victim, 0, false);

			killerPoints.setDirectArrowKills(killerPoints.getDirectArrowKills() + 1);
			victimPoints.setDirectArrowDeaths(victimPoints.getDirectArrowDeaths() + 1);

			killerMessage = RageMode.getLang().get("game.message.arrow-kill", "%victim%", victim.getName(), "%points%",
					"+" + bowPoints);

			victimMsg = RageMode.getLang().get("game.message.arrow-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints());
			break;
		case COMBATAXE:
			int axePoints = ConfigValues.getAxeKill();
			int axeNegativePoints = ConfigValues.getAxeDeath();

			killerPoints = addPoints(killer, axePoints, true);
			victimPoints = addPoints(victim, axeNegativePoints, false);

			killerPoints.setAxeKills(killerPoints.getAxeKills() + 1);
			victimPoints.setAxeDeaths(victimPoints.getAxeDeaths() + 1);

			killerMessage = RageMode.getLang().get("game.message.axe-kill", "%victim%", victim.getName(), "%points%",
					"+" + axePoints);

			victimMsg = RageMode.getLang().get("game.message.axe-death", "%killer%", killer.getName(), "%points%",
					axeNegativePoints);

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints());
			break;
		case RAGEKNIFE:
			int knifePoints = ConfigValues.getKnifeKill();

			killerPoints = addPoints(killer, knifePoints, true);
			victimPoints = addPoints(victim, 0, false);

			killerPoints.setKnifeKills(killerPoints.getKnifeKills() + 1);
			victimPoints.setKnifeDeaths(victimPoints.getKnifeDeaths() + 1);

			killerMessage = RageMode.getLang().get("game.message.knife-kill", "%victim%", victim.getName(), "%points%",
					"+" + knifePoints);

			victimMsg = RageMode.getLang().get("game.message.knife-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints());
			break;
		case EXPLOSION:
			int explosionPoints = ConfigValues.getExplosionKill();

			killerPoints = addPoints(killer, explosionPoints, true);
			victimPoints = addPoints(victim, 0, false);

			killerPoints.setExplosionKills(killerPoints.getExplosionKills() + 1);
			victimPoints.setExplosionDeaths(victimPoints.getExplosionDeaths() + 1);

			killerMessage = RageMode.getLang().get("game.message.explosion-kill", "%victim%", victim.getName(),
					"%points%", "+" + explosionPoints);

			victimMsg = RageMode.getLang().get("game.message.explosion-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints());
			break;
		case GRENADE:
			int grenadePoints = ConfigValues.getGrenadeKill();

			killerPoints = addPoints(killer, grenadePoints, true);

			addPoints(victim, 0, false);

			killerMessage = RageMode.getLang().get("game.message.grenade-kill", "%victim%", victim.getName(),
					"%points%", "+" + grenadePoints);

			victimMsg = RageMode.getLang().get("game.message.grenade-death", "%killer%", killer.getName(), "%points%",
					"");

			killerMsg2 = RageMode.getLang().get("game.message.current-points", "%points%", killerPoints.getPoints());
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
	 * Returns the given player points if present.
	 * 
	 * @param playerUUID {@link UUID} of player
	 * @return {@link PlayerPoints} if present, otherwise {@link Optional#empty()}
	 */
	public static Optional<PlayerPoints> getPlayerPoints(UUID playerUUID) {
		return Optional.ofNullable(PLAYERPOINTS.get(playerUUID));
	}

	/**
	 * Returns the cached players in map containing {@link PlayerPoints}
	 * 
	 * @return the map of cached players
	 */
	public static Map<UUID, PlayerPoints> getPlayerPointsMap() {
		return PLAYERPOINTS;
	}

	private static PlayerPoints addPoints(Player player, int points, boolean killer) {
		UUID playerUUID = player.getUniqueId();
		int totalKills = 0, totalDeaths = 0, currentStreak = 0, longestStreak = 0;

		PlayerPoints pointsHolder = PLAYERPOINTS.get(playerUUID);

		if (pointsHolder != null) {
			totalKills = pointsHolder.getKills();
			totalDeaths = pointsHolder.getDeaths();

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
			pointsHolder.addPoints(killer ? points + PLUGIN.getRewardManager().getPointBonus() : points);

			return pointsHolder;
		}

		if (killer) {
			totalKills = currentStreak = longestStreak = 1;
		} else {
			totalDeaths = 1;
			currentStreak = 0;
		}

		pointsHolder = new PlayerPoints(playerUUID);
		pointsHolder.setPoints(points);
		pointsHolder.setKills(totalKills);
		pointsHolder.setDeaths(totalDeaths);
		pointsHolder.setCurrentStreak(currentStreak);
		pointsHolder.setLongestStreak(longestStreak);

		PLAYERPOINTS.put(playerUUID, pointsHolder);
		return pointsHolder;
	}

	public static UUID calculateWinner(Game game, Set<PlayerManager> players) {
		game.setStatus(GameStatus.WINNER_CALCULATING);

		UUID highest = UUID.randomUUID(), resultPlayer = null, goy = highest;
		int highestPoints = 0;

		for (PlayerManager pm : players) {
			PlayerPoints pp = PLAYERPOINTS.get(pm.getUniqueId());

			if (pp != null && pp.getPoints() > highestPoints) {
				resultPlayer = highest = pm.getUniqueId();
				highestPoints = pp.getPoints();
			}
		}

		if (resultPlayer == null) {
			Debug.logConsole(Level.WARNING,
					"There was an error while calculating the winner player. Seems no winner player.");
			return null;
		}

		if (goy.equals(highest)) {
			sendMessage(PLUGIN.getServer().getPlayer(resultPlayer), RageMode.getLang().get("game.message.player-won",
					"%player%", "Herobrine", "%game%", game.getName()));
			return null;
		}

		getPlayerPoints(highest).ifPresent(p -> p.setWinner(true));

		Player winner = PLUGIN.getServer().getPlayer(highest);

		if (resultPlayer.equals(highest)) {
			sendMessage(winner, RageMode.getLang().get("game.message.you-won", "%game%", game.getName()));
		} else {
			sendMessage(PLUGIN.getServer().getPlayer(resultPlayer), RageMode.getLang().get("game.message.player-won",
					"%player%", winner.getName(), "%game%", game.getName()));
		}

		Utils.callEvent(new PlayerWinEvent(game, winner));
		return highest;
	}
}