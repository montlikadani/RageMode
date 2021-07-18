package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "spawn",
		params = "<gameName> player/zombie",
		desc = "General commands for game spawns",
		permission = "ragemode.admin.spawn",
		playerOnly = true)
public final class spawn implements ICommand {

	private String usage() {
		return Utils.colors("&6/rm spawn <gameName> player/zombie"
				+ "\n      add &7- Adds a new spawn location"
				+ "\n      &6remove <id/all> &7- Removes a specific or every existing spawn"
				+ "\n      &6list &7- Lists all added spawn points");
	}

	private enum Action {
		ADD, REMOVE, LIST
	}

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 4) {
			sendMessage(sender, usage());
			return false;
		}

		BaseGame game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		Action action = Action.ADD;
		try {
			action = Action.valueOf(args[3].toUpperCase());
		} catch (IllegalArgumentException ex) {
		}

		if (action != Action.LIST && game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.running"));
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();
		String path = "arenas." + game.getName();

		if (action != Action.LIST && !aFile.isSet(path)) {
			sendMessage(sender, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName>"));
			return false;
		}

		boolean isPlayerType;
		if (!(isPlayerType = args[2].equalsIgnoreCase("player")) && !args[2].equalsIgnoreCase("zombie")) {
			isPlayerType = true;
		}

		IGameSpawn spawn = game.getSpawn(isPlayerType ? GameSpawn.class : GameZombieSpawn.class);

		switch (action) {
		case ADD:
			path += (isPlayerType ? ".spawns" : ".zombie-spawns") + ".";

			int i = 1;
			while (aFile.isSet(path + i))
				i++;

			path += i + ".";

			Player player = (Player) sender;
			Location playerLoc = player.getLocation();

			aFile.set(path + "world", player.getWorld().getName());
			aFile.set(path + "x", playerLoc.getX());
			aFile.set(path + "y", playerLoc.getY());
			aFile.set(path + "z", playerLoc.getZ());
			aFile.set(path + "yaw", playerLoc.getYaw());
			aFile.set(path + "pitch", playerLoc.getPitch());
			Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

			if (spawn != null) {
				spawn.addSpawn(playerLoc);
			}

			sendMessage(player,
					RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", game.getName()));
			break;
		case REMOVE:
			if (args.length < 5) {
				sendMessage(sender, usage());
				return false;
			}

			String end = isPlayerType ? ".spawns" : ".zombie-spawns";

			if (args[4].equalsIgnoreCase("all")) {
				if (!aFile.contains("arenas." + game.getName() + end)) {
					sendMessage(sender, RageMode.getLang().get("commands.removespawn.no-more-spawn"));
					return false;
				}

				aFile.set("arenas." + game.getName() + end, null);
				Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

				if (spawn != null) {
					spawn.removeAllSpawn();
					sendMessage(sender,
							RageMode.getLang().get("commands.removespawn.all-spawn-removed", "%game%", game.getName()));
				}

				break;
			}

			java.util.Optional<Integer> opt = Utils.tryParseInt(args[4]);
			if (!opt.isPresent()) {
				sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[4]));
				return false;
			}

			int index = opt.get();

			if (!aFile.isSet(path + end + "." + index)) {
				sendMessage(sender, RageMode.getLang().get("commands.removespawn.not-valid-spawn-id", "%id%", index));
				return false;
			}

			String sPath = path + end + "." + index + ".";

			double spawnX = aFile.getDouble(sPath + "x"), spawnY = aFile.getDouble(sPath + "y"),
					spawnZ = aFile.getDouble(sPath + "z");

			float spawnYaw = (float) aFile.getDouble(sPath + "yaw");
			float spawnPitch = (float) aFile.getDouble(sPath + "pitch");

			org.bukkit.World world = plugin.getServer().getWorld(aFile.getString(sPath + "world", ""));
			if (world == null) {
				world = ((Player) sender).getWorld(); // Breh
			}

			Location loc = new Location(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

			if (spawn != null) {
				spawn.removeSpawn(loc);
			}

			aFile.set(path + end + "." + index, null);
			Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

			sendMessage(sender, RageMode.getLang().get("commands.removespawn.remove-success", "%number%", index,
					"%game%", game.getName()));
			break;
		case LIST:
			if (!spawn.haveAnySpawn()) {
				break;
			}

			java.util.List<Location> locations = spawn.getSpawnLocations();
			StringBuilder builder = new StringBuilder();

			for (int a = 0; a < locations.size(); a++) {
				builder.append((a + 1) + ".&6 " + locations.get(a)).append("\n\n&r");
			}

			sendMessage(sender, Utils.colors(builder.toString()));
			break;
		default:
			return true;
		}

		return true;
	}
}
