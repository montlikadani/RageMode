package hu.montlikadani.ragemode.gameLogic.base.zombieSpawn;

/**
 * Interface for handling zombie spawn.
 */
public interface IZombieSpawner {

	/**
	 * Checks if zombies can spawn at the given game timer.
	 * 
	 * @param gameTimer the game timer to check the next zombie spawn
	 * @return true if zombies can spawn within the timer, otherwise false
	 */
	boolean canSpawn(int gameTimer);

	/**
	 * Checks if the first zombies can spawn within the given game timer.
	 * 
	 * @param gameTimer the game timer from where to spawn
	 * @return true if first zombies can spawn, otherwise false
	 */
	boolean canSpawnFirstZombie(int gameTimer);

	/**
	 * Checks if the first zombies are spawned before or not.
	 * 
	 * @return true if zombies spawned, otherwise false
	 */
	default boolean isFirstZombieSpawned() {
		return false;
	}

	/**
	 * Checks if the following zombies can spawn from that timer and increases the
	 * number of zombies to spawn as many times this method has been called.
	 * 
	 * @param gameTimer the game timer from where to check
	 * @return true if zombies can spawn, otherwise false
	 */
	boolean canSpawnNextZombie(int gameTimer);

	/**
	 * Sets if the first zombies were spawned before.
	 */
	void setFirstZombieSpawned();

	/**
	 * Attempts to spawn specific amount of zombies in game. If the amount reaches
	 * 1000 will returns immediately.fz
	 * 
	 * @param gameTimer used to check if the zombies can spawn from the given game
	 *                  time
	 */
	void spawnZombies(int gameTimer);
}
