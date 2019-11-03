package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class listgames {

	public boolean run(CommandSender sender) {
		if (!hasPerm(sender, "ragemode.listgames")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (GetGames.getConfigGamesCount() < 0) {
			sendMessage(sender, RageMode.getLang().get("commands.listgames.no-games-available"));
			return false;
		}

		String[] games = GetGames.getGameNames();
		if (games == null) {
			return false;
		}

		int i = 0;
		int imax = games.length;

		sendMessage(sender, RageMode.getLang().get("commands.listgames.listing-games", "%games%", imax));

		while (i < imax) {
			if (GameUtils.getGame(games[i]).isGameRunning(games[i]))
				sendMessage(sender, RageMode.getLang().get("commands.listgames.game-running", "%number%", i + 1,
						"%game%", games[i]));
			else
				sendMessage(sender, RageMode.getLang().get("commands.listgames.game-stopped", "%number%", i + 1,
						"%game%", games[i]));
			i++;
		}

		return true;
	}
}
