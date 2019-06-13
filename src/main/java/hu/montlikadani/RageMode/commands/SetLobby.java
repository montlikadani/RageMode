package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class SetLobby extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.setlobby")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (args.length >= 2) {
			String gameName = args[1];
			if (!GameUtils.isGameWithNameExists(gameName)) {
				sendMessage(p, RageMode.getLang().get("invalid-game"));
				return false;
			}

			YamlConfiguration aCfg = plugin.getConfiguration().getArenasCfg();
			if (!aCfg.isSet("arenas." + gameName))
				sendMessage(p, RageMode.getLang().get("setup.not-set-yet"));
			else {
				String path = "arenas." + gameName + ".lobby";
				Location loc = p.getLocation();
				aCfg.set(path + ".world", p.getWorld().getName());
				aCfg.set(path + ".x", loc.getX());
				aCfg.set(path + ".y", loc.getY());
				aCfg.set(path + ".z", loc.getZ());
				aCfg.set(path + ".yaw", loc.getYaw());
				aCfg.set(path + ".pitch", loc.getPitch());
				try {
					aCfg.save(plugin.getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					plugin.throwMsg();
				}
				sendMessage(p, RageMode.getLang().get("setup.lobby.set-success", "%game%", gameName));
			}
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
		return false;
	}
}
