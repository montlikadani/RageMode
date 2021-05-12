package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.Game;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "listgames",
		desc = "Listing all available games",
		permission = { "ragemode.listgames", "ragemode.help.playercommands" })
public final class listgames implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (plugin.getGames().isEmpty()) {
			sendMessage(sender, RageMode.getLang().get("commands.listgames.no-games-available"));
			return false;
		}

		int size = plugin.getGames().size();

		sendMessage(sender, RageMode.getLang().get("commands.listgames.listing-games", "%games%", size));

		for (int i = 0; i < size; i++) {
			Game game = plugin.getGames().get(i);
			assert game != null;

			sendMessage(sender,
					RageMode.getLang().get("commands.listgames.game-" + (game.isRunning() ? "running" : "stopped"),
							"%number%", i + 1, "%game%", game.getName()));
		}

		return true;
	}
}
