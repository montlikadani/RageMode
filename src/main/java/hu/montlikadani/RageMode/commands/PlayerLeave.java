package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class PlayerLeave extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		PlayerList.removePlayer(p);
		PlayerList.removeSpectatorPlayer(p);
		return false;
	}
}
