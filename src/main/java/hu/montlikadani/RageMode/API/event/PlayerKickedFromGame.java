package hu.montlikadani.ragemode.API.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerKickedFromGame extends BaseEvent {

	private String game;
	private CommandSender by;
	private Player target;

	public PlayerKickedFromGame(String game, CommandSender by, Player target) {
		this.game = game;
		this.by = by;
		this.target = target;
	}

	public String getGame() {
		return game;
	}

	public CommandSender getBySender() {
		return by;
	}

	public Player getTarget() {
		return target;
	}
}
