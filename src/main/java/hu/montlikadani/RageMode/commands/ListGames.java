package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class ListGames extends RmCommand {

	public ListGames(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.listgames")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (GetGames.getConfigGamesCount() > 0) {
			if (GetGames.getGameNames() != null) {
				String[] games = GetGames.getGameNames();
				int i = 0;
				int imax = games.length;

				sender.sendMessage(RageMode.getLang().get("commands.listgames.listing-games", "%games%", imax));

				while (i < imax) {
					if (PlayerList.isGameRunning(games[i]))
						sender.sendMessage(RageMode.getLang().get("commands.listgames.game-running", "%number%", i + 1, "%game%", games[i]));
					else
						sender.sendMessage(RageMode.getLang().get("commands.listgames.game-stopped", "%number%", i + 1, "%game%", games[i]));
					i++;
				}
			}
		} else
			sender.sendMessage(RageMode.getLang().get("commands.listgames.no-games-available"));
		return;
	}
}
