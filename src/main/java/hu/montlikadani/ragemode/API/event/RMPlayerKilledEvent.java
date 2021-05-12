package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.items.GameItems;

/**
 * Called when a player killed another living entity in game.
 */
public class RMPlayerKilledEvent extends GameEvent {

	private Player killer;
	private LivingEntity entity;

	private GameItems tool;

	public RMPlayerKilledEvent(Game game, LivingEntity entity, Player killer) {
		this(game, entity, killer, null);
	}

	public RMPlayerKilledEvent(Game game, LivingEntity entity, Player killer, GameItems tool) {
		super(game);

		this.entity = entity;
		this.killer = killer;
		this.tool = tool == null ? GameItems.UNKNOWN : tool;
	}

	/**
	 * The player who is the killer.
	 * 
	 * @return {@link Player}
	 */
	@Nullable
	public Player getKiller() {
		return killer;
	}

	/**
	 * Returns the killed entity
	 * 
	 * @return {@link LivingEntity}
	 */
	@NotNull
	public LivingEntity getEntity() {
		return entity;
	}

	/**
	 * The tool which the killer used and hold in their hand.
	 * 
	 * @return {@link GameItems}
	 */
	@NotNull
	public GameItems getTool() {
		return tool;
	}
}
