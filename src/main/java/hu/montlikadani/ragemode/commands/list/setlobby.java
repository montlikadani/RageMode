package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "setlobby", permission = "ragemode.admin.setlobby", playerOnly = true)
public class setlobby implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length >= 2) {
			String gameName = args[1];
			if (!GameUtils.isGameWithNameExists(gameName)) {
				sendMessage(p, RageMode.getLang().get("invalid-game"));
				return false;
			}

			FileConfiguration aCfg = plugin.getConfiguration().getArenasCfg();
			if (!aCfg.isSet("arenas." + gameName)) {
				sendMessage(p,
						RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
				return false;
			}

			String path = "arenas." + gameName + ".lobby";
			Location loc = p.getLocation();
			aCfg.set(path + ".world", p.getWorld().getName());
			aCfg.set(path + ".x", Double.valueOf(loc.getX()));
			aCfg.set(path + ".y", Double.valueOf(loc.getY()));
			aCfg.set(path + ".z", Double.valueOf(loc.getZ()));
			aCfg.set(path + ".yaw", Double.valueOf(loc.getYaw()));
			aCfg.set(path + ".pitch", Double.valueOf(loc.getPitch()));

			Configuration.saveFile(aCfg, plugin.getConfiguration().getArenasFile());
			sendMessage(p, RageMode.getLang().get("setup.lobby.set-success", "%game%", gameName));
		} else {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
			return false;
		}

		return true;
	}
}
