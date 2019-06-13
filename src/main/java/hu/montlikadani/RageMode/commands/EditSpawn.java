package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class EditSpawn extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.editspawn")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (args.length < 3) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm editspawn <gameName> <id>"));
			return false;
		}
		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		if (!Utils.isInt(args[2])) {
			sendMessage(p, RageMode.getLang().get("not-a-number", "%number%", args[2]));
			return false;
		}

		int i = Integer.parseInt(args[2]);
		YamlConfiguration aFile = plugin.getConfiguration().getArenasCfg();
		String path = "arenas." + args[1];
		Location loc = p.getLocation();

		if (!aFile.isSet(path + ".spawns." + i)) {
			sendMessage(p, RageMode.getLang().get("commands.editspawn.not-valid-spawn-id", "%id%", i));
			return false;
		}

		aFile.set(path + ".spawns." + i + ".world", p.getWorld().getName());
		aFile.set(path + ".spawns." + i + ".x", loc.getX());
		aFile.set(path + ".spawns." + i + ".y", loc.getY());
		aFile.set(path + ".spawns." + i + ".z", loc.getZ());
		aFile.set(path + ".spawns." + i + ".yaw", loc.getYaw());
		aFile.set(path + ".spawns." + i + ".pitch", loc.getPitch());
		try {
			aFile.save(plugin.getConfiguration().getArenasFile());
		} catch (IOException e) {
			e.printStackTrace();
			plugin.throwMsg();
		}
		sendMessage(p, RageMode.getLang().get("commands.editspawn.edit-success", "%number%", i, "%game%", args[1]));
		return false;
	}
}
