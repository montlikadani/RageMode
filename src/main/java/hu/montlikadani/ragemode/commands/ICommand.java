package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;

public interface ICommand {

	static final CommandProcessor PROC = ICommand.class.getAnnotation(CommandProcessor.class);

	boolean run(RageMode plugin, CommandSender sender, String[] args);

}
