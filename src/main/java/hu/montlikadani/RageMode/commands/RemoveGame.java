package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameDeleteEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class RemoveGame extends ICommand {

	@Override
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

			if (Game.isGameRunning(game)) {
				sendMessage(p, RageMode.getLang().get("game.running"));
				return false;
			}

			GameDeleteEvent event = new GameDeleteEvent(game);
			Bukkit.getPluginManager().callEvent(event);

			for (GameSpawnGetter spawn : plugin.getSpawns()) {
				if (spawn.getGameName().equalsIgnoreCase(game)) {
					plugin.getSpawns().remove(spawn);
				}
			}

			for (Game name : plugin.getGames()) {
				if (name.getName().equalsIgnoreCase(game)) {
					plugin.getGames().remove(name);
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
