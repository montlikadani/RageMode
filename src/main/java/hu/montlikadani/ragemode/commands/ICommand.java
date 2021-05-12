package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;

public interface ICommand {

	boolean run(RageMode plugin, CommandSender sender, String[] args);

	default CommandProcessor getCommandProcessor() {
		return getClass().getAnnotation(CommandProcessor.class);
	}
}
