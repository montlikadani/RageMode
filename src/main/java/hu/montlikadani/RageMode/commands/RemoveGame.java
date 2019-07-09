package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameDeleteEvent;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class RemoveGame extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.removegame")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameWithNameExists(game)) {
				sendMessage(p, RageMode.getLang().get("setup.removed-non-existent-game"));
				return false;
			}

			if (PlayerList.isGameRunning(game))
				sendMessage(p, RageMode.getLang().get("game.running"));
			else {
				GameDeleteEvent event = new GameDeleteEvent(game);
				Bukkit.getPluginManager().callEvent(event);

				for (int x = 0; x < plugin.getSpawns().size(); x++) {
					if (plugin.getSpawns().get(x).getGameName().equalsIgnoreCase(game))
						plugin.getSpawns().remove(x);
				}
				PlayerList.deleteGameFromList(game);

				plugin.getConfiguration().getArenasCfg().set("arenas." + game, null);
				try {
					plugin.getConfiguration().getArenasCfg().save(plugin.getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					plugin.throwMsg();
				}

				sendMessage(p, RageMode.getLang().get("setup.success-removed", "%game%", game));
			}
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return false;
	}
}
