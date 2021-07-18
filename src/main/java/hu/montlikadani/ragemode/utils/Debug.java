package hu.montlikadani.ragemode.utils;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.config.configconstants.ConfigValues;

public final class Debug {

	public static void logConsole(String msg) {
		logConsole(Level.INFO, msg);
	}

	public static void logConsole(String msg, Object... p) {
		logConsole(Level.INFO, msg, p);
	}

	public static void logConsole(Level lvl, String msg, Object... p) {
		if (ConfigValues.isLogConsole())
			Bukkit.getServer().getLogger().log(lvl, "[RageMode] " + msg, p);
	}

	public static void sendMessage(String msg) {
		Bukkit.getServer().getConsoleSender().sendMessage(Utils.colors(msg));
	}
}
