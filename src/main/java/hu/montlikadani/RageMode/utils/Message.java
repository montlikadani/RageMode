package hu.montlikadani.ragemode.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

	public static boolean hasPerm(CommandSender sender, String perm) {
		if (!(sender instanceof Player) || sender.hasPermission(perm))
			return true;

		return false;
	}

	public static void sendMessage(CommandSender sender, String msg) {
		if (sender != null && msg != null && !msg.isEmpty())
			sender.sendMessage(msg);
	}
}
