package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameCreateEvent;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
	name = "addgame",
	params = "<gameName> [maxPlayers] [minPlayers] [gameType]",
	desc = "Adds a new game",
	permission = "ragemode.admin.addgame",
	playerOnly = true)
public final class addgame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%",
					"/rm addgame <gameName> [maxPlayers] [minPlayers] [gameType]"));
			return false;
		}

		String name = args[1];

		if (GameUtils.isGameExist(name)) {
			sendMessage(sender, RageMode.getLang().get("setup.addgame.already-exists", "%game%", name));
			return false;
		}

		Player player = (Player) sender;

		if (!GameUtils.checkName(player, name)) {
			return false;
		}

		int x = 4;
		if (args.length == 3) {
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sendMessage(player, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
				return false;
			}

			if (x < 2) {
				sendMessage(player, RageMode.getLang().get("setup.at-least-two"));
				return false;
			}
		}

		int m = 2;
		if (args.length == 4) {
			try {
				m = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				sendMessage(player, RageMode.getLang().get("not-a-number", "%wrong-number%", args[3]));
				return false;
			}

			if (m < 2) {
				sendMessage(player, RageMode.getLang().get("setup.at-least-two"));
				return false;
			}
		}

		GameType type = GameType.NORMAL;

		if (args.length == 5 && (type = GameType.getByName(args[4])) == null) {
			type = GameType.NORMAL;
		}

		Game game = new Game(name, type);
		plugin.getGames().add(game);

		Utils.callEvent(new RMGameCreateEvent(game, x, m));

		FileConfiguration c = plugin.getConfiguration().getArenasCfg();
		c.set("arenas." + name + ".maxplayers", x);
		c.set("arenas." + name + ".minplayers", m);
		c.set("arenas." + name + ".world", player.getWorld().getName());
		c.set("arenas." + name + ".gametype", type.toString().toLowerCase());
		Configuration.saveFile(c, plugin.getConfiguration().getArenasFile());

		sendMessage(player, RageMode.getLang().get("setup.addgame.success-added", "%game%", name));

		if (plugin.getSetupGui().openGui(player, game).isOpened()) {
			sendMessage(player, "&2Opening setup gui... /rm setup");
		}

		return true;
	}
}
