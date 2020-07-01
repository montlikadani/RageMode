package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;

public class RMPlayerRespawnedEvent extends GameEvent {

	private Player player;
	private IGameSpawn gameSpawn;

	public RMPlayerRespawnedEvent(Game game, Player player, IGameSpawn gameSpawn) {
		super(game);
		this.player = player;
		this.gameSpawn = gameSpawn;
	}

	public Player getPlayer() {
		return player;
	}

	public IGameSpawn getSpawn() {
		return gameSpawn;
	}
}
