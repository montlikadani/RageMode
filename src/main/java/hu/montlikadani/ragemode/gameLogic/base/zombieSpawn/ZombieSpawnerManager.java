package hu.montlikadani.ragemode.gameLogic.base.zombieSpawn;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.base.ZombieGame;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.SchedulerUtil;
import hu.montlikadani.ragemode.utils.ServerVersion;

public final class ZombieSpawnerManager implements IZombieSpawner {

	private final RageMode rm;
	private final ZombieGame game;

	private int nextZombieSpawnTime = 0;
	private int zombieSpawnAmount = 10;

	private boolean firstZombieSpawned = false;

	public ZombieSpawnerManager(RageMode rm, ZombieGame game) {
		this.rm = rm;
		this.game = game;
	}

	@Override
	public boolean canSpawn(int gameTimer) {
		return nextZombieSpawnTime == 0 || gameTimer <= nextZombieSpawnTime;
	}

	@Override
	public boolean canSpawnFirstZombie(int gameTimer) {
		if (ConfigValues.getDelayBeforeFirstZombiesSpawn() <= 0) {
			return true;
		}

		if (gameTimer <= nextZombieSpawnTime) {
			firstZombieSpawned = true;
		}

		nextZombieSpawnTime = gameTimer - ConfigValues.getDelayBeforeFirstZombiesSpawn();
		return firstZombieSpawned;
	}

	@Override
	public boolean canSpawnNextZombie(int gameTimer) {
		if (zombieSpawnAmount < 1000) {
			if (ConfigValues.isWaitForNextSpawnAfterZombiesAreDead()) {
				zombieSpawnAmount += 20;
			} else { // Preventing too much zombie spawning
				zombieSpawnAmount += 1;
			}
		} else {
			zombieSpawnAmount = 1000;
		}

		if (ConfigValues.getDelayAfterNextZombiesSpawning() <= 0) {
			return true;
		}

		if (gameTimer <= nextZombieSpawnTime) {
			nextZombieSpawnTime = gameTimer - ConfigValues.getDelayAfterNextZombiesSpawning();
			return true;
		}

		return false;
	}

	@Override
	public boolean isFirstZombieSpawned() {
		return firstZombieSpawned;
	}

	@Override
	public void setFirstZombieSpawned() {
		if (!firstZombieSpawned) {
			firstZombieSpawned = true;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void spawnZombies(int gameTimer) {
		if (zombieSpawnAmount < 1 || game.getStatus() != GameStatus.RUNNING) {
			return;
		}

		if (zombieSpawnAmount > 1000) {
			zombieSpawnAmount = 1000;
		}

		IGameSpawn spawn = game.randomSpawnForZombies ? game.getSpawn(GameSpawn.class)
				: game.getSpawn(GameZombieSpawn.class);
		if (spawn == null || !spawn.haveAnySpawn()) {
			return;
		}

		if (firstZombieSpawned) {
			java.util.Optional<GameArea> opt = GameAreaManager.getAreaByLocation(spawn.getSpawnLocations().get(0));

			if (!opt.isPresent()) {
				return;
			}

			java.util.List<org.bukkit.entity.Entity> entities = opt.get().getEntities(e -> !e.isDead(),
					org.bukkit.entity.EntityType.ZOMBIE);

			if ((ConfigValues.isWaitForNextSpawnAfterZombiesAreDead() && !entities.isEmpty())
					|| !canSpawnNextZombie(gameTimer)) {
				return;
			}
		}

		game.getWorld().ifPresent(world -> {
			final GameArea gameArea = game.randomSpawnForZombies ? GameAreaManager.getAreaByGame(game) : null;

			SchedulerUtil.submitSync(() -> {
				for (PlayerManager pm : game.getPlayers()) {
					Player player = pm.getPlayer();

					if (player != null) {
						player.setPlayerTime(6000L, true); // 6000 - midnight
					}
				}

				ThreadLocalRandom random = ThreadLocalRandom.current();

				for (int i = 0; i <= zombieSpawnAmount; i++) {
					Location location = null;

					if (game.randomSpawnForZombies) {
						// Spawn zombies around the center of game area
						if (gameArea != null) {
							Location centerArea = gameArea.getCenter();

							double angle = Math.toRadians(random.nextDouble() * 360);
							double x = centerArea.getX() + (random.nextDouble() * 3 * Math.cos(angle));
							double z = centerArea.getZ() + (random.nextDouble() * 3 * Math.sin(angle));

							location = new Location(world, x, centerArea.getY(), z);
						}
					} else {
						location = spawn.getRandomSpawn().clone();
					}

					// Suppose the area is not exist
					if (location == null) {
						location = spawn.getRandomSpawn().clone().add(15, 0, 15);
					}

					// TODO we need something to do not spawn zombies in-blocks such as in bushes
					// (leaves)

					Zombie zombie = (Zombie) world.spawnEntity(location, org.bukkit.entity.EntityType.ZOMBIE);

					if (rm.isPaper() && ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R3)) {
						zombie.setCanBreakDoors(false);
					}

					// Do not spawn too much baby zombie
					if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R2)) {
						if (!zombie.isAdult() && random.nextInt(0, 10) < 2) {
							zombie.setAdult();
						}
					} else if (zombie.isBaby() && random.nextInt(0, 10) < 2) {
						zombie.setBaby(false);
					}
				}

				return 1;
			});
		});
	}
}
