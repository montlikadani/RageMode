package hu.montlikadani.ragemode.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class RemoveSpawn extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.removespawn")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 3) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm removespawn <gameName> <id/all>"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();

		if (args[2].equalsIgnoreCase("all")) {
			if (!aFile.contains("arenas." + args[1] + ".spawns")) {
				sendMessage(sender, RageMode.getLang().get("commands.removespawn.no-more-spawn"));
				return false;
			}

			aFile.set("arenas." + args[1] + ".spawns", null);
			Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());
			sendMessage(sender, RageMode.getLang().get("commands.removespawn.remove-success", "%number%", "all", "%game%", args[1]));
			return false;
		}

		if (!Utils.isInt(args[2])) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[2]));
			return false;
		}

		int i = Integer.parseInt(args[2]);
		String path = "arenas." + args[1];

		if (!aFile.isSet(path + ".spawns." + i)) {
			sendMessage(sender, RageMode.getLang().get("commands.removespawn.not-valid-spawn-id", "%id%", i));
			return false;
		}

		Location loc = null;
		for (String spawnName : aFile.getConfigurationSection(path + ".spawns").getKeys(false)) {
			String sPath = path + ".spawns." + spawnName;

			String world = aFile.getString(sPath + ".world");
			double spawnX = aFile.getDouble(sPath + ".x");
			double spawnY = aFile.getDouble(sPath + ".y");
			double spawnZ = aFile.getDouble(sPath + ".z");
			double spawnYaw = aFile.getDouble(sPath + ".yaw");
			double spawnPitch = aFile.getDouble(sPath + ".pitch");

			loc = new Location(Bukkit.getWorld(world), spawnX, spawnY, spawnZ);
			loc.setYaw((float) spawnYaw);
			loc.setPitch((float) spawnPitch);
		}

		if (loc == null) {
			Debug.logConsole(Level.WARNING, "Something went wrong with locations. Try again.");
			return false;
		}

		for (GameSpawnGetter spawn : plugin.getSpawns()) {
			if (spawn.getGameName().equalsIgnoreCase(args[1])) {
				for (int y = 0; y < spawn.getSpawnLocations().size(); y++) {
					if (spawn.getSpawnLocations().get(y).equals(loc)) {
						spawn.getSpawnLocations().remove(y);
					}
				}
			}
		}

		aFile.set(path + ".spawns." + i, null);
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		sendMessage(sender, RageMode.getLang().get("commands.removespawn.remove-success", "%number%", i, "%game%", args[1]));

		return false;
	}
}
