package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;

public class RMPlayerRespawnedEvent extends BaseEvent {

	private Game game;
	private Player player;
	private GameSpawn spawn;

	public RMPlayerRespawnedEvent(Game game, Player player, GameSpawn spawn) {
		this.game = game;
		this.player = player;
		this.spawn = spawn;
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}

	public GameSpawn getSpawn() {
		return spawn;
	}
}
