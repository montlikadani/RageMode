package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

@CommandProcessor(
	name = "setgametype",
	desc = "Sets the new type of the given game",
	params = "<gameName> <gameType>",
	permission = "ragemode.admin.setgametype")
public final class setgametype implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm setgametype <gameName> <gameType>"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		GameType type = GameType.getByName(args[2]);
		if (type == null) {
			type = GameType.NORMAL;
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game.getName() + ".gametype",
				type.toString().toLowerCase());
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		game.setGameType(type);
		sendMessage(sender, RageMode.getLang().get("commands.setgametype.set", "%game%", game.getName()));
		return true;
	}
}
