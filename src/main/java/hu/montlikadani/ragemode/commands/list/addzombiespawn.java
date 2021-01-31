package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

@CommandProcessor(name = "addzombiespawn", permission = "ragemode.admin.addzombiespawn", playerOnly = true)
public class addzombiespawn implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addzombiespawn <gameName>"));
			return false;
		}

		if (!GameUtils.isGameExist(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game.getGameType() != GameType.APOCALYPSE) {
			return false;
		}

		if (game.isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.running"));
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

		path += i + ".";

		Location loc = p.getLocation();
		aFile.set(path + "world", p.getWorld().getName());
		aFile.set(path + "x", loc.getX());
		aFile.set(path + "y", loc.getY());
		aFile.set(path + "z", loc.getZ());
		aFile.set(path + "yaw", loc.getYaw());
		aFile.set(path + "pitch", loc.getPitch());
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		IGameSpawn spawn = game.getSpawn(GameZombieSpawn.class);
		if (spawn != null) {
			spawn.addSpawn(loc);
		}

		sendMessage(p, RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", game.getName()));
		return true;
	}
}
