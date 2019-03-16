package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class SetLobby extends RmCommand {

	public SetLobby(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.setlobby")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length >= 2) {
			String gameName = args[1];
			FileConfiguration aCfg = RageMode.getInstance().getConfiguration().getArenasCfg();
			if (!aCfg.isSet("arenas." + gameName))
				p.sendMessage(RageMode.getLang().get("setup.not-set-yet"));
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
					aCfg.save(RageMode.getInstance().getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					RageMode.getInstance().throwMsg();
				}
				p.sendMessage(RageMode.getLang().get("setup.lobby.set-success", "%game%", gameName));
			}
		} else
			p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
		return;
	}
}
