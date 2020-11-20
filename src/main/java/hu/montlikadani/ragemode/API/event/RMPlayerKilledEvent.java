package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.scores.KilledWith;

/**
 * Called when a player killed another living entity in game.
 */
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

	/**
	 * The player who is the killer.
	 * 
	 * @return {@link Player}
	 */
	public Player getKiller() {
		return killer;
	}

	/**
	 * Returns the killed entity
	 * 
	 * @return {@link LivingEntity}
	 */
	public LivingEntity getEntity() {
		return entity;
	}

	/**
	 * The tool which the killer used and hold in their hand.
	 * 
	 * @return {@link KilledWith}
	 */
	public KilledWith getTool() {
		return tool;
	}
}
