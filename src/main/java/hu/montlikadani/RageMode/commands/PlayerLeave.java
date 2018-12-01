package hu.montlikadani.RageMode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.RageMode.RageMode;
import hu.montlikadani.RageMode.gameLogic.PlayerList;
import hu.montlikadani.RageMode.signs.SignCreator;

public class PlayerLeave extends RmCommand {

	public PlayerLeave(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.leave")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		PlayerList.removePlayerSynced(p);
		PlayerList.removePlayer(p);
		SignCreator.updateAllSigns(PlayerList.getPlayersGame(p));
		return;
	}
}
