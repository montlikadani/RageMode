package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class ListGames extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd) {
		if (!hasPerm(sender, "ragemode.listgames")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (GetGames.getConfigGamesCount() > 0) {
			if (GetGames.getGameNames() != null) {
				String[] games = GetGames.getGameNames();
				int i = 0;
				int imax = games.length;

				sendMessage(sender, RageMode.getLang().get("commands.listgames.listing-games", "%games%", imax));

				while (i < imax) {
					if (PlayerList.isGameRunning(games[i]))
						sendMessage(sender, RageMode.getLang().get("commands.listgames.game-running", "%number%", i + 1, "%game%", games[i]));
					else
						sendMessage(sender, RageMode.getLang().get("commands.listgames.game-stopped", "%number%", i + 1, "%game%", games[i]));
					i++;
				}
			}
		} else
			sendMessage(sender, RageMode.getLang().get("commands.listgames.no-games-available"));
		return false;
	}
}
