package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "listgames", permission = "ragemode.listgames")
public class listgames implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (GetGames.getConfigGamesCount() < 0) {
			sendMessage(sender, RageMode.getLang().get("commands.listgames.no-games-available"));
			return false;
		}

		String[] games = GetGames.getGameNames();

		sendMessage(sender, RageMode.getLang().get("commands.listgames.listing-games", "%games%", games.length));

		for (int i = 0; i < games.length; i++) {
			String game = games[i];
			assert game != null;

			if (game.isEmpty()) {
				continue;
			}

			String running = GameUtils.getGame(game).isGameRunning() ? "running" : "stopped";
			sendMessage(sender,
					RageMode.getLang().get("commands.listgames.game-" + running, "%number%", i + 1, "%game%", game));
		}

		return true;
	}
}
