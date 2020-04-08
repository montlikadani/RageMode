package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class listgames implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
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
			if (GameUtils.getGame(games[i]).isGameRunning())
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
