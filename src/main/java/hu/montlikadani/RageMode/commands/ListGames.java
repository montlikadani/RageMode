package hu.montlikadani.RageMode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.RageMode.RageMode;
import hu.montlikadani.RageMode.gameLogic.PlayerList;
import hu.montlikadani.RageMode.toolbox.GetGames;

public class ListGames extends RmCommand {

	public ListGames(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.list")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (GetGames.getGameNames() != null) {
			String[] games = GetGames.getGameNames();
			int i = 0;
			int imax = games.length;

			p.sendMessage(RageMode.getLang().get("commands.listgames.listing-games"));

			while (i < imax) {
				if (PlayerList.isGameRunning(games[i]))
					p.sendMessage(RageMode.getLang().get("commands.listgames.game-running", "%number%", i + 1, "%game%", games[i]));
				else
					p.sendMessage(RageMode.getLang().get("commands.listgames.game-stopped", "%number%", i + 1, "%game%", games[i]));
				i++;
			}
		} else
			p.sendMessage(RageMode.getLang().get("commands.listgames.no-games-available"));
		return;
	}
}
