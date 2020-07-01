package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class setgametype implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.setgametype")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm setgametype <gameName> <gameType>"));
			return false;
		}

		String game = args[1];
		if (!GameUtils.isGameWithNameExists(game)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
			return false;
		}

		GameType type = GameType.getByName(args[2]);
		if (type == null) {
			type = GameType.NORMAL;
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".gametype", type.getName());
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		GameUtils.getGame(game).setGameType(type);
		sendMessage(sender, RageMode.getLang().get("commands.setgametype.set", "%game%", game));
		return true;
	}
}
