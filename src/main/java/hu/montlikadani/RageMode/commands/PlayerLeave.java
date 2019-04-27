package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class PlayerLeave extends RmCommand {

	public PlayerLeave(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		PlayerList.removePlayer(p);
		PlayerList.removeSpectatorPlayer(p);
		return;
	}
}
