package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.scores.KilledWith;

public class RMPlayerKilledEvent extends GameEvent {

	private Player killer;
	private LivingEntity entity;

	private KilledWith tool;

	public RMPlayerKilledEvent(Game game, LivingEntity entity, Player killer) {
		this(game, entity, killer, null);
	}

	public RMPlayerKilledEvent(Game game, LivingEntity entity, Player killer, KilledWith tool) {
		super(game);
		this.entity = entity;
		this.killer = killer;
		this.tool = tool == null ? KilledWith.UNKNOWN : tool;
	}

	public Player getKiller() {
		return killer;
	}

	/**
	 * The killed entity
	 * 
	 * @return {@link LivingEntity}
	 */
	public LivingEntity getEntity() {
		return entity;
	}

	public KilledWith getTool() {
		return tool;
	}
}
