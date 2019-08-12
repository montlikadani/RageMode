package hu.montlikadani.ragemode.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;

public class ICommand {

	public boolean run(CommandSender sender) {
		return false;
	}

	public boolean run(RageMode plugin, CommandSender sender) {
		return false;
	}

	public boolean run(CommandSender sender, String[] args) {
		return false;
	}

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		return false;
	}

	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		return false;
	}
}
