package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

@CommandProcessor(name = "gametime", permission = "ragemode.admin.setgametime", playerOnly = true)
public class gametime implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length >= 3) {
			if (!GameUtils.isGameExist(args[1])) {
				sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			if (!plugin.getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
				sendMessage(p,
						RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
				return false;
			}

			if (!Utils.isInt(args[2])) {
				sendMessage(p, RageMode.getLang().get("not-a-number", "%number%", args[2]));
				return false;
			}

			plugin.getConfiguration().getArenasCfg().set("arenas." + args[1] + ".gametime", Integer.parseInt(args[2]));
			Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());
			sendMessage(p, RageMode.getLang().get("setup.set-game-time-success", "%game%", args[1], "%time%", args[2]));
		} else {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm gametime <gameName> <minutes>"));
			return false;
		}

		return true;
	}
}
