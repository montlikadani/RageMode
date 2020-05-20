package hu.montlikadani.ragemode;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.config.ConfigValues;

public class Debug {

	/**
	 * Logging to console to write debug messages
	 * @see #logConsole(String, Object...)
	 * @param msg Error message
	 */
	public static void logConsole(String msg) {
		logConsole(Level.INFO, msg);
	}

	/**
	 * Logging to console to write debug messages
	 * @see #logConsole(Level, String, Object...)
	 * @param msg Error message
	 * @param p params
	 */
	public static void logConsole(String msg, Object... p) {
		logConsole(Level.INFO, msg, p);
	}

	/**
	 * Logging to console to write debug messages
	 * @param lvl Logging level
	 * @param msg Error message
	 * @param p params
	 */
	public static void logConsole(Level lvl, String msg, Object... p) {
		if (msg != null && !msg.isEmpty() && ConfigValues.isLogConsole())
			Bukkit.getLogger().log(lvl == null ? Level.INFO : lvl, "[RageMode] " + msg, p);
	}

	/**
	 * Sends a message to the console with colors.
	 * @param msg String
	 */
	public static void sendMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(Utils.colors(msg));
	}
}
