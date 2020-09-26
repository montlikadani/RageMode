package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class addspawn implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.addspawn")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addspawn <gameName>"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();
		String path = "arenas." + args[1];
		if (!aFile.isSet(path)) {
			sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName>"));
			return false;
		}

		path += ".spawns.";

		int i = 1;
		while (aFile.isSet(path + i))
			i++;

		path += i + ".";

		Location loc = p.getLocation();
		aFile.set(path + "world", p.getWorld().getName());
		aFile.set(path + "x", loc.getX());
		aFile.set(path + "y", loc.getY());
		aFile.set(path + "z", loc.getZ());
		aFile.set(path + "yaw", loc.getYaw());
		aFile.set(path + "pitch", loc.getPitch());
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		IGameSpawn spawn = GameUtils.getGameSpawn(args[1]);
		if (spawn != null) {
			spawn.addSpawn(loc);
		}

		sendMessage(p, RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", args[1]));
		return true;
	}
}
