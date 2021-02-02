package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameDeleteEvent;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "removegame", permission = "ragemode.admin.removegame", playerOnly = true)
public class removegame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return true;
		}

		String gameName = args[1];
		if (!GameUtils.isGameExist(gameName)) {
			sendMessage(p, RageMode.getLang().get("setup.removed-non-existent-game"));
			return false;
		}

		Game game = GameUtils.getGame(gameName);
		if (game.isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return false;
		}

		Utils.callEvent(new RMGameDeleteEvent(game));

		game.removeSpawn();
		plugin.removeGame(game);

		plugin.getConfiguration().getArenasCfg().set("arenas." + game.getName(), null);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		sendMessage(p, RageMode.getLang().get("setup.success-removed", "%game%", game.getName()));
		return true;
	}
}
