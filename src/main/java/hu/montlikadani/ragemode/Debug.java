package hu.montlikadani.ragemode;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.config.ConfigValues;

public class Debug {

	public static void logConsole(String msg) {
		logConsole(Level.INFO, msg);
	}

	public static void logConsole(String msg, Object... p) {
		logConsole(Level.INFO, msg, p);
	}

	public static void logConsole(Level lvl, String msg, Object... p) {
		if (ConfigValues.isLogConsole() && msg != null && !msg.isEmpty())
			Bukkit.getLogger().log(lvl == null ? Level.INFO : lvl, "[RageMode] " + msg, p);
	}

	public static void sendMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(Utils.colors(msg));
	}
}
