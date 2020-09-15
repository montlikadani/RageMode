package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;
import java.util.stream.Collectors;

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

public class GameTimer extends TimerTask {

	private Game game;
	private final int time;
	private int timer;

	private BukkitTask timeElapsed;

	private int zombieSpawnAmount = 10;
	private boolean firstZombieSpawned = false;

	public GameTimer(Game game, int time) {
		this.game = game;
		this.time = time;
		timer = time;
	}

	public Game getGame() {
		return game;
	}

	public int getGameTime() {
		return time;
	}

	public int getCurrentGameTime() {
		return timer;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning() || !RageMode.getInstance().isEnabled()) {
				cancel();
				return;
			}

			if (game.getPlayers().size() < 2) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("not-enough-players"));
				GameUtils.stopGame(game.getName());
				cancel();
				return;
			}

			// Broadcast time message should be in this place, before counting
			for (String val : RageMode.getInstance().getConfiguration().getCfg()
					.getStringList("game.values-to-send-game-end-broadcast")) {
				if (timer == Integer.parseInt(val)) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(timer)));
					break;
				}
			}

			if (game.getGameType() == GameType.APOCALYPSE) {
				Location loc = GameUtils.getGameSpawn(game).getSpawnLocations().get(0);

				if (!firstZombieSpawned) {
					if (ConfigValues.getDelayBeforeFirstZombiesSpawn() > 0) {
						Bukkit.getScheduler().runTaskLater(RageMode.getInstance(), () -> {
							GameUtils.spawnZombies(game, zombieSpawnAmount);
							firstZombieSpawned = true;
						}, ConfigValues.getDelayBeforeFirstZombiesSpawn() * 20);
					} else {
						GameUtils.spawnZombies(game, zombieSpawnAmount);
					}

				} else if (timeElapsed == null) { // wait for the scheduler task
					GameAreaManager.getAreaByLocation(loc).ifPresent(area -> {
						java.util.List<Entity> entities = area.getEntities().stream()
								.filter(e -> e instanceof org.bukkit.entity.Zombie).collect(Collectors.toList());
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

				// Fire EntityInteractEvent to call pressure mines to be exploded
				GameAreaManager.getAreaByLocation(loc).ifPresent(area -> {
					e: for (Entity f : area.getEntities()) {
						if (f.isDead()) {
							continue;
						}

						for (Location mineLoc : GameListener.PRESSUREMINESOWNER.keySet()) {
							if (mineLoc.getBlock().equals(f.getLocation().getBlock())) {
								EntityInteractEvent interact = new EntityInteractEvent(f, mineLoc.getBlock());
								if (!interact.isCancelled()) {
									Bukkit.getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
										Bukkit.getPluginManager().callEvent(interact);
										return true;
									});

									break e;
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
				GameUtils.stopGame(game.getName());
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
