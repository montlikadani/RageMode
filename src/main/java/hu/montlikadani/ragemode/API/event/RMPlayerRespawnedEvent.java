package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;

/**
 * Called when a player respawned.
 */
public class RMPlayerRespawnedEvent extends GameEvent {

	private Player player;
	private IGameSpawn gameSpawn;

	public RMPlayerRespawnedEvent(BaseGame game, Player player, IGameSpawn gameSpawn) {
		super(game);

		this.player = player;
		this.gameSpawn = gameSpawn;
	}

	/**
	 * The player who's respawned.
	 * 
	 * @return {@link Player}
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the spawn where the player respawned.
	 * 
	 * @return {@link IGameSpawn}
	 */
	@NotNull
	public IGameSpawn getSpawn() {
		return gameSpawn;
	}
}
