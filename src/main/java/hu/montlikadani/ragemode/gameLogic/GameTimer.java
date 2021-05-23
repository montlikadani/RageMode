package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Utils;

public final class GameTimer extends TimerTask {

	private final Game game;
	private final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private int timer, nextNotifyTime = 0, nextZombieSpawnTime = 0;

	private int zombieSpawnAmount = 10;
	private boolean firstZombieSpawned = false;

	public GameTimer(Game game) {
		this.game = game;
		timer = game.gameTime;
	}

	public Game getGame() {
		return game;
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
			if (!game.isRunning() || !rm.isEnabled()) {
				cancel();
				return;
			}

			if (game.getGameType() != GameType.APOCALYPSE && !ConfigValues.isDeveloperMode()
					&& game.getPlayers().size() < 2) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("not-enough-players"));
				GameUtils.stopGame(game);
				cancel();
				return;
			}

			for (PlayerManager pm : game.getPlayers()) {
				pm.tickMovement();
			}

			// Broadcast time message should be in this place, before counting
			for (int value : ConfigValues.getGameEndBroadcast()) {
				if (timer == value) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(timer)));
					break;
				}
			}

			if (ConfigValues.isNotifySpectatorsToLeave() && ConfigValues.getSpecTimeBetweenMessageSending() > 0
					&& (nextNotifyTime == 0 || nextNotifyTime >= timer)) {
				for (PlayerManager spec : game.getSpectatorPlayers()) {
					spec.getPlayer().sendMessage(RageMode.getLang().get("game.spec-player-leave-notify"));
				}

				nextNotifyTime = timer - ConfigValues.getSpecTimeBetweenMessageSending();
			}

			if (game.getGameType() == GameType.APOCALYPSE) {
				if (nextZombieSpawnTime == 0 || timer <= nextZombieSpawnTime) { // wait for the next time
					if (!firstZombieSpawned) {
						if (ConfigValues.getDelayBeforeFirstZombiesSpawn() > 0) {
							if (timer <= nextZombieSpawnTime) {
								GameUtils.spawnZombies(game, zombieSpawnAmount);
								firstZombieSpawned = true;
							}

							nextZombieSpawnTime = timer - ConfigValues.getDelayBeforeFirstZombiesSpawn();
						} else {
							GameUtils.spawnZombies(game, zombieSpawnAmount);
						}
					} else {
						IGameSpawn spawn = game.getSpawn(GameSpawn.class);

						if (spawn != null && spawn.haveAnySpawn()) {
							GameAreaManager.getAreaByLocation(spawn.getSpawnLocations().get(0)).ifPresent(area -> {
								java.util.List<Entity> entities = area.getEntities(e -> !e.isDead(),
										org.bukkit.entity.EntityType.ZOMBIE);

								if (entities.size() <= 150
										&& ((ConfigValues.isWaitForNextSpawnAfterZombiesAreDead() && entities.isEmpty())
												|| !ConfigValues.isWaitForNextSpawnAfterZombiesAreDead())) {
									if (ConfigValues.getDelayAfterNextZombiesSpawning() > 0) {
										if (timer <= nextZombieSpawnTime) {
											GameUtils.spawnZombies(game, zombieSpawnAmount);
											nextZombieSpawnTime = timer
													- ConfigValues.getDelayAfterNextZombiesSpawning();
										}
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
				}

				// Trash-made solution for entity move ticking
				// Only fire this if the software is not paper or forks
				// See PaperListener#entityMoveEvent for better performance
				if (!rm.isPaper()) {
					IGameSpawn spawn = game.getSpawn(GameSpawn.class);

					if (spawn != null && spawn.haveAnySpawn()) {
						GameAreaManager.getAreaByLocation(spawn.getSpawnLocations().get(0)).ifPresent(area -> {
							e: for (final Entity f : area.getEntities(e -> !e.isDead(),
									org.bukkit.entity.EntityType.ZOMBIE)) {
								for (PressureMine mines : GameListener.PRESSUREMINES) {
									for (Location mineLoc : mines.getMines()) {
										if (mineLoc.getBlock().getType() == f.getLocation().getBlock().getType()) {
											// Good news! Async catchop throwable error, lmao
											rm.getServer().getScheduler().runTaskLater(rm,
													() -> GameListener.explodeMine(
															rm.getServer().getPlayer(mines.getOwner()),
															f.getLocation()),
													1L);
											break e;
										}
									}
								}
							}
						});
					}
				}
			}

			for (ActionMessengers ac : game.getActionMessengers()) {
				ac.setScoreboard(timer);
				ac.setTabList(timer);
				ac.setPlayerName();
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
