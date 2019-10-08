package hu.montlikadani.ragemode.API.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.gameLogic.Game;

public class PlayerKickedFromGame extends BaseEvent {

	private Game game;
	private CommandSender by;
	private Player target;

	public PlayerKickedFromGame(Game game, CommandSender by, Player target) {
		this.game = game;
		this.by = by;
		this.target = target;
	}

	public Game getGame() {
		return game;
	}

	public CommandSender getBySender() {
		return by;
	}

	public Player getTarget() {
		return target;
	}
}
