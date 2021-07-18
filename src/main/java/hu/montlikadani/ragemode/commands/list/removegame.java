package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameDeleteEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "removegame",
		permission = "ragemode.admin.removegame",
		params = "<gameName>",
		desc = "Removes the specified game",
		playerOnly = true)
public final class removegame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return true;
		}

		BaseGame game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("setup.removed-non-existent-game"));
			return false;
		}

		if (game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.running"));
			return false;
		}

		GameAreaManager.removeAreaByGame(game);
		plugin.removeGame(game.getName());
		Utils.callEvent(new RMGameDeleteEvent(game));

		plugin.getConfiguration().getArenasCfg().set("arenas." + game.getName(), null);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		sendMessage(sender, RageMode.getLang().get("setup.success-removed", "%game%", game.getName()));
		return true;
	}
}
