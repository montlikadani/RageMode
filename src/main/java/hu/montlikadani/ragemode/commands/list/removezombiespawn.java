package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "removezombiespawn", permission = "ragemode.admin.removezombiespawn")
public class removezombiespawn implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm removezombiespawn <gameName> <id/all>"));
			return false;
		}

		String name = args[1];
		if (!GameUtils.isGameExist(name)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", name));
			return false;
		}

		IGameSpawn spawn = GameUtils.getGame(name).getSpawn(GameZombieSpawn.class);
		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();

		if (args[2].equalsIgnoreCase("all")) {
			if (!aFile.contains("arenas." + name + ".zombie-spawns")) {
				sendMessage(sender, RageMode.getLang().get("commands.removezombiespawn.no-more-spawn"));
				return false;
			}

			aFile.set("arenas." + name + ".zombie-spawns", null);
			Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

			if (spawn != null) {
				spawn.removeAllSpawn();
				sendMessage(sender, RageMode.getLang().get("commands.removezombiespawn.remove-success", "%number%", "all",
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

		if (!aFile.isSet(path + ".zombie-spawns." + i)) {
			sendMessage(sender, RageMode.getLang().get("commands.removezombiespawn.not-valid-spawn-id", "%id%", i));
			return false;
		}

		String sPath = path + ".zombie-spawns." + i + ".";
		String world = aFile.getString(sPath + "world");

		double spawnX = aFile.getDouble(sPath + "x"),
				spawnY = aFile.getDouble(sPath + "y"),
				spawnZ = aFile.getDouble(sPath + "z");

		float spawnYaw = (float) aFile.getDouble(sPath + "yaw");
		float spawnPitch = (float) aFile.getDouble(sPath + "pitch");

		Location loc = new Location(Bukkit.getWorld(world), spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

		if (spawn != null) {
			spawn.removeSpawn(loc);
		}

		aFile.set(path + ".spawns." + i, null);
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		sendMessage(sender,
				RageMode.getLang().get("commands.removezombiespawn.remove-success", "%number%", i, "%game%", name));
		return true;
	}
}
