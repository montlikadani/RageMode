package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "listgames", permission = "ragemode.listgames")
public class listgames implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (plugin.getGames().isEmpty()) {
			sendMessage(sender, RageMode.getLang().get("commands.listgames.no-games-available"));
			return false;
		}

		sendMessage(sender,
				RageMode.getLang().get("commands.listgames.listing-games", "%games%", plugin.getGames().size()));

		for (int i = 0; i < plugin.getGames().size(); i++) {
			Game game = plugin.getGames().get(i);
			assert game != null;

			String running = game.isGameRunning() ? "running" : "stopped";
			sendMessage(sender,
					RageMode.getLang().get("commands.listgames.game-" + running, "%number%", i + 1, "%game%", game.getName()));
		}

		return true;
	}
}
