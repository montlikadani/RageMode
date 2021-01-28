package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class GameTimer extends TimerTask {

	private Game game;
	private final int time;
	private int timer;

	private BukkitTask timeElapsed;

	private int zombieSpawnAmount = 10;
	private boolean firstZombieSpawned = false;
	private boolean canSendSpectatorNotify = true;

	public GameTimer(Game game, int time) {
		this.game = game;
		timer = (this.time = time);
	}

	public Game getGame() {
		return game;
	}

	/**
	 * @return returns the fixed (final) game time
	 */
	public int getGameTime() {
		return time;
	}

	/**
	 * @return returns the current game time
	 */
	public int getCurrentGameTime() {
		return timer;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong
			if (!game.isGameRunning() || !RageMode.getInstance().isEnabled()) {
				cancel();
				return;
			}

			if (!ConfigValues.isDeveloperMode() && game.getPlayers().size() < 2) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("not-enough-players"));
				GameUtils.stopGame(game);
				cancel();
				return;
			}

			// Broadcast time message should be in this place, before counting
			for (int value : ConfigValues.getGameEndBroadcast()) {
				if (timer == value) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(timer)));
					break;
				}
			}

			if (ConfigValues.isNotifySpectatorsToLeave() && canSendSpectatorNotify) {
				canSendSpectatorNotify = false;

				Bukkit.getScheduler().runTaskLater(RageMode.getInstance(), () -> {
					for (PlayerManager spec : game.getSpectatorPlayers()) {
						spec.getPlayer().sendMessage(RageMode.getLang().get("game.spec-player-leave-notify"));
						canSendSpectatorNotify = true;
					}
				}, ConfigValues.getSpecTimeBetweenMessageSending() * 20);
			}

			if (game.getGameType() == GameType.APOCALYPSE && game.getSpawn(GameSpawn.class) != null) {
				Location loc = game.getSpawn(GameSpawn.class).getSpawnLocations().get(0);

				if (timeElapsed == null) { // wait for the scheduler task
					if (!firstZombieSpawned) {
						if (ConfigValues.getDelayBeforeFirstZombiesSpawn() > 0) {
							timeElapsed = Bukkit.getScheduler().runTaskLater(RageMode.getInstance(), () -> {
								GameUtils.spawnZombies(game, zombieSpawnAmount);
								firstZombieSpawned = true;
								timeElapsed = null;
							}, ConfigValues.getDelayBeforeFirstZombiesSpawn() * 20);
						} else {
							GameUtils.spawnZombies(game, zombieSpawnAmount);
						}
					} else {
						GameAreaManager.getAreaByLocation(loc).ifPresent(area -> {
							java.util.List<Entity> entities = area.getEntities(org.bukkit.entity.EntityType.ZOMBIE);
							if (entities.size() <= 150
									&& ((ConfigValues.isWaitForNextSpawnAfterZombiesAreDead() && entities.isEmpty())
											|| !ConfigValues.isWaitForNextSpawnAfterZombiesAreDead())) {
								if (ConfigValues.getDelayAfterNextZombiesSpawning() > 0) {
									timeElapsed = Bukkit.getScheduler().runTaskLater(RageMode.getInstance(), () -> {
										GameUtils.spawnZombies(game, zombieSpawnAmount);
										timeElapsed = null;
									}, ConfigValues.getDelayAfterNextZombiesSpawning() * 20);
								} else {
									GameUtils.spawnZombies(game, zombieSpawnAmount);
								}

								if (ConfigValues.isWaitForNextSpawnAfterZombiesAreDead()) {
									zombieSpawnAmount += 20;
								} else { // Preventing too much zombie spawning
									zombieSpawnAmount += 1;
								}
							}
						});
					}
				}

				// Fire EntityInteractEvent to call pressure mines to be exploded
				GameAreaManager.getAreaByLocation(loc).ifPresent(area -> {
					e: for (Entity f : area.getEntities()) {
						if (f.isDead()) {
							continue;
						}

						for (PressureMine mines : GameListener.PRESSUREMINES) {
							for (Location mineLoc : mines.getMines()) {
								if (mineLoc.getBlock() == f.getLocation().getBlock()) {
									EntityInteractEvent interact = new EntityInteractEvent(f,
											f.getLocation().getBlock());
									if (!interact.isCancelled()) {
										Utils.callEvent(interact);
										break e;
									}
								}
							}
						}
					}
				});
			}

			for (ActionMessengers ac : game.getActionMessengers()) {
				if (game.isSpectatorInList(ac.getPlayer())) {
					continue;
				}

				ac.setScoreboard(timer);
				ac.setTabList(timer);
				ac.setTeam();
			}

			if (timer == 0) {
				cancel();
				GameUtils.stopGame(game);
				return;
			}

			timer--;
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.forceStopGame(game);
			return;
		}
	}
}
