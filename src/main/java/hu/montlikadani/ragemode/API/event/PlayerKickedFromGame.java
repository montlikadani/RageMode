package hu.montlikadani.ragemode.API.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

/**
 * Called when a player kicked from a game.
 */
public class PlayerKickedFromGame extends GameEvent {

	private CommandSender by;
	private Player target;

	public PlayerKickedFromGame(Game game, CommandSender by, Player target) {
		super(game);
		this.by = by;
		this.target = target;
	}

	/**
	 * The player who kicked the target.
	 * 
	 * @return {@link CommandSender} the sender
	 */
	public CommandSender getBySender() {
		return by;
	}

	/**
	 * The target player who's been kicked.
	 * 
	 * @return {@link Player}
	 */
	public Player getTarget() {
		return target;
	}
}
