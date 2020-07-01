package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class addzombiespawn implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.addzombiespawn")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addzombiespawn <gameName>"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		if (!GameUtils.getGame(args[1]).getGameType().equals(GameType.APOCALYPSE)) {
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();
		String path = "arenas." + args[1];
		if (!aFile.isSet(path)) {
			sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName>"));
			return false;
		}

		path += ".zombie-spawns.";

		int i = 1;
		while (aFile.isSet(path + i))
			i++;

		Location loc = p.getLocation();
		aFile.set(path + i + ".world", p.getWorld().getName());
		aFile.set(path + i + ".x", loc.getX());
		aFile.set(path + i + ".y", loc.getY());
		aFile.set(path + i + ".z", loc.getZ());
		aFile.set(path + i + ".yaw", loc.getYaw());
		aFile.set(path + i + ".pitch", loc.getPitch());
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		for (IGameSpawn spawn : plugin.getSpawns()) {
			if (spawn.getGame().getName().equalsIgnoreCase(args[1]) && spawn instanceof GameZombieSpawn) {
				spawn.addSpawn(loc);
				break;
			}
		}

		sendMessage(p, RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", args[1]));
		return true;
	}
}
