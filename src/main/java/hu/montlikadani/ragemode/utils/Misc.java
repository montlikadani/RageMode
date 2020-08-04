package hu.montlikadani.ragemode.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;

public class Misc {

	public static boolean hasPerm(CommandSender sender, String perm) {
		return (!(sender instanceof Player) || sender.hasPermission(perm)) ? true : false;
	}

	public static void sendMessage(CommandSender sender, String msg) {
		sendMessage(sender, msg, false);
	}

	public static void sendMessage(CommandSender sender, String msg, boolean colored) {
		if (sender != null && msg != null && !msg.isEmpty()) {
			sender.sendMessage(colored ? Utils.colors(msg) : msg);
		}
	}
}
