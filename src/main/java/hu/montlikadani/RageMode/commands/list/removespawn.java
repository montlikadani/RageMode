package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class removespawn {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.removespawn")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 3) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm removespawn <gameName> <id/all>"));
			return false;
		}

		String name = args[1];
		if (!GameUtils.isGameWithNameExists(name)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", name));
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();

		if (args[2].equalsIgnoreCase("all")) {
			if (!aFile.contains("arenas." + name + ".spawns")) {
				sendMessage(sender, RageMode.getLang().get("commands.removespawn.no-more-spawn"));
				return false;
			}

			aFile.set("arenas." + name + ".spawns", null);
			Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

			if (GameUtils.getGameSpawn(name) != null) {
				GameUtils.getGameSpawn(name).removeAllSpawn();
				sendMessage(sender, RageMode.getLang().get("commands.removespawn.remove-success", "%number%", "all",
						"%game%", name));
				return true;
			}

			return false;
		}

		if (!Utils.isInt(args[2])) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[2]));
			return false;
		}

		int i = Integer.parseInt(args[2]);
		String path = "arenas." + name;

		if (!aFile.isSet(path + ".spawns." + i)) {
			sendMessage(sender, RageMode.getLang().get("commands.removespawn.not-valid-spawn-id", "%id%", i));
			return false;
		}

		String sPath = path + ".spawns." + i + ".";
		String world = aFile.getString(sPath + "world");
		double spawnX = aFile.getDouble(sPath + "x");
		double spawnY = aFile.getDouble(sPath + "y");
		double spawnZ = aFile.getDouble(sPath + "z");
		double spawnYaw = aFile.getDouble(sPath + "yaw");
		double spawnPitch = aFile.getDouble(sPath + "pitch");

		Location loc = new Location(Bukkit.getWorld(world), spawnX, spawnY, spawnZ);
		loc.setYaw((float) spawnYaw);
		loc.setPitch((float) spawnPitch);

		if (GameUtils.getGameSpawn(name) != null) {
			GameUtils.getGameSpawn(name).removeSpawn(loc);
		}

		aFile.set(path + ".spawns." + i, null);
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		sendMessage(sender, RageMode.getLang().get("commands.removespawn.remove-success", "%number%", i, "%game%", name));
		return true;
	}
}
