package hu.montlikadani.ragemode.API.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a player kicked from a game.
 */
public class PlayerKickedFromGame extends GameEvent {

	private CommandSender requestedBy;
	private Player target;

	public PlayerKickedFromGame(Game game, CommandSender requestedBy, Player target) {
		super(game);

		this.requestedBy = requestedBy;
		this.target = target;
	}

	/**
	 * The player who kicked the target.
	 * 
	 * @return {@link CommandSender} the sender
	 */
	@NotNull
	public CommandSender getBySender() {
		return requestedBy;
	}

	/**
	 * The target player who's been kicked.
	 * 
	 * @return {@link Player}
	 */
	@NotNull
	public Player getTarget() {
		return target;
	}
}
