package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

@CommandProcessor(
	name = "gametime",
	permission = "ragemode.admin.setgametime",
	desc = "Adding game time (in minutes) to a specific game",
	params = "<gameName> <minutes>",
	playerOnly = true)
public final class gametime implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm gametime <gameName> <minutes>"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		if (!plugin.getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
			sendMessage(sender,
					RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return false;
		}

		java.util.Optional<Integer> opt = Utils.tryParseInt(args[2]);
		if (!opt.isPresent()) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[2]));
			return false;
		}

		if (game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.running"));
			return false;
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + args[1] + ".gametime", opt.get());
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());
		sendMessage(sender,
				RageMode.getLang().get("setup.set-game-time-success", "%game%", args[1], "%time%", args[2]));

		return true;
	}
}
