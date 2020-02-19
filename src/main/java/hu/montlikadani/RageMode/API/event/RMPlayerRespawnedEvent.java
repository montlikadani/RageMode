package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;

public class RMPlayerRespawnedEvent extends GameEvent {

	private Player player;
	private GameSpawn spawn;

	public RMPlayerRespawnedEvent(Game game, Player player, GameSpawn spawn) {
		super(game);
		this.player = player;
		this.spawn = spawn;
	}

	public Player getPlayer() {
		return player;
	}

	public GameSpawn getSpawn() {
		return spawn;
	}
}
