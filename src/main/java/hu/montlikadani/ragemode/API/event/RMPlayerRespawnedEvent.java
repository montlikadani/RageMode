package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;

/**
 * Called when a player respawned.
 */
public class RMPlayerRespawnedEvent extends GameEvent {

	private Player player;
	private IGameSpawn gameSpawn;

	public RMPlayerRespawnedEvent(Game game, Player player, IGameSpawn gameSpawn) {
		super(game);
		this.player = player;
		this.gameSpawn = gameSpawn;
	}

	/**
	 * The player who's respawned.
	 * 
	 * @return {@link Player}
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the spawn where the player respawned.
	 * 
	 * @return {@link IGameSpawn}
	 */
	public IGameSpawn getSpawn() {
		return gameSpawn;
	}
}
