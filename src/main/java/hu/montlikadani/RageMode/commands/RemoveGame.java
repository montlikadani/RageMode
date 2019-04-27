package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class RemoveGame extends RmCommand {

	public RemoveGame(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.removegame")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length >= 2) {
			String game = args[1];
			if (!GetGames.isGameExistent(game)) {
				sendMessage(p, RageMode.getLang().get("setup.removed-non-existent-game"));
				return;
			}

			if (PlayerList.isGameRunning(game))
				sendMessage(p, RageMode.getLang().get("game.running"));
			else {
				PlayerList.deleteGameFromList(game);

				RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + game, null);
				try {
					RageMode.getInstance().getConfiguration().getArenasCfg()
							.save(RageMode.getInstance().getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					RageMode.getInstance().throwMsg();
				}

				sendMessage(p, RageMode.getLang().get("setup.success-removed", "%game%", game));
			}
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return;
	}
}
