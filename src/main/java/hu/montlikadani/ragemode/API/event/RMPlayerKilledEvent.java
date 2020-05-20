package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.scores.KilledWith;

public class RMPlayerKilledEvent extends GameEvent {

	private Player killer;
	private Player player;
	private KilledWith tool;

	public RMPlayerKilledEvent(Game game, Player player, Player killer, KilledWith tool) {
		super(game);
		this.player = player;
		this.killer = killer;
		this.tool = tool;
	}

	public Player getKiller() {
		return killer;
	}

	public Player getPlayer() {
		return player;
	}

	public KilledWith getTool() {
		return tool;
	}
}
