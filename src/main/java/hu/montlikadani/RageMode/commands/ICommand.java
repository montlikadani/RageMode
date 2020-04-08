package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;

public interface ICommand {

	boolean run(RageMode plugin, CommandSender sender, String[] args);
}
