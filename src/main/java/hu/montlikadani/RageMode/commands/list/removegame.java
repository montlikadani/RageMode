package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameDeleteEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

import java.util.Iterator;

public class removegame {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
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

			if (GameUtils.getGameByName(game).isGameRunning(game)) {
				sendMessage(p, RageMode.getLang().get("game.running"));
				return false;
			}

			RMGameDeleteEvent event = new RMGameDeleteEvent(GameUtils.getGameByName(game));
			Bukkit.getPluginManager().callEvent(event);

			for (Iterator<GameSpawnGetter> it = plugin.getSpawns().iterator(); it.hasNext();) {
				if (it.next().getGame().getName().equalsIgnoreCase(game)) {
					it.remove();
				}
			}

			for (Iterator<Game> gt = plugin.getGames().iterator(); gt.hasNext();) {
				if (gt.next().getName().equalsIgnoreCase(game)) {
					gt.remove();
					break;
				}
			}

			plugin.getConfiguration().getArenasCfg().set("arenas." + game, null);
			Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

			sendMessage(p, RageMode.getLang().get("setup.success-removed", "%game%", game));
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return false;
	}
}
