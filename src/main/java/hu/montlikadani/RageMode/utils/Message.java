package hu.montlikadani.ragemode.utils;

import org.bukkit.command.CommandSender;

public class Message {

	public static boolean hasPerm(CommandSender sender, String perm) {
		if (sender.hasPermission(perm))
			return true;

		return false;
	}

	public static void sendMessage(CommandSender sender, String msg) {
		if (sender != null && msg != null && !msg.equals(""))
			sender.sendMessage(msg);
	}
}
