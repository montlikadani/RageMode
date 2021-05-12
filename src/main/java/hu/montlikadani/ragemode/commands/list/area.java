package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.io.IOException;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.Area;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

@CommandProcessor(
	name = "area",
	permission = "ragemode.admin.area",
	params = "add/remove/info/list",
	desc = "Setup area for specific game",
	playerOnly = true)
public final class area implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm area add/remove/info/list"));
			return false;
		}

		if (args.length <= 4 && args[1].equalsIgnoreCase("add")) {
			if (args.length < 4) {
				sendMessage(sender,
						RageMode.getLang().get("missing-arguments", "%usage%", "/rm area add <gameName> <areaName>"));
				return false;
			}

			Game game = GameUtils.getGame(args[2]);
			if (game == null) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[2]));
				return false;
			}

			if (GameAreaManager.isAreaExist(args[3])) {
				sendMessage(sender, RageMode.getLang().get("commands.area.already-exists"));
				return false;
			}

			Player player = (Player) sender;

			if (!plugin.getSelection().hasSetBoth(player.getUniqueId())) {
				sendMessage(player,
						RageMode.getLang().get("commands.area.select", "%tool%", ConfigValues.getSelectionItem()));
				return false;
			}

			if (!plugin.getConfiguration().getAreasFile().exists()) {
				try {
					plugin.getConfiguration().getAreasFile().createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			FileConfiguration aFile = plugin.getConfiguration().getAreasCfg();

			String areaName = args[3];
			aFile.set("areas." + areaName + ".world", player.getWorld().getName());
			aFile.set("areas." + areaName + ".game", args[2]);

			Area area = plugin.getSelection().getArea(player.getUniqueId());

			String path = "areas." + areaName + ".loc1.";
			aFile.set(path + "x", area.getLowLoc().getX());
			aFile.set(path + "y", area.getLowLoc().getY());
			aFile.set(path + "z", area.getLowLoc().getZ());

			path = "areas." + areaName + ".loc2.";
			aFile.set(path + "x", area.getHighLoc().getX());
			aFile.set(path + "y", area.getHighLoc().getY());
			aFile.set(path + "z", area.getHighLoc().getZ());
			Configuration.saveFile(aFile, plugin.getConfiguration().getAreasFile());

			GameAreaManager.getGameAreas().put(areaName, new GameArea(game, area, areaName));
			sendMessage(player, RageMode.getLang().get("commands.area.set", "%name%", areaName));
		} else if (args.length <= 3 && args[1].equalsIgnoreCase("remove")) {
			if (args.length < 3) {
				sendMessage(sender,
						RageMode.getLang().get("missing-arguments", "%usage%", "/rm area remove <areaName>"));
				return false;
			}

			String area = args[2];

			if (GameAreaManager.getGameAreas().remove(area) == null) {
				sendMessage(sender, RageMode.getLang().get("commands.area.not-exists"));
				return false;
			}

			plugin.getConfiguration().getAreasCfg().set("areas." + area, null);
			Configuration.saveFile(plugin.getConfiguration().getAreasCfg(), plugin.getConfiguration().getAreasFile());
			sendMessage(sender, RageMode.getLang().get("commands.area.removed", "%name%", area));
		} else if (args.length >= 1) {
			if (args[1].equalsIgnoreCase("info")) {
				String t = "";
				Player player = (Player) sender;

				for (Map.Entry<String, GameArea> map : GameAreaManager.getGameAreas().entrySet()) {
					if (map.getValue().inArea(player.getLocation())) {
						if (!t.isEmpty()) {
							t += ", ";
						}

						t += map.getKey();
					}
				}

				sendMessage(player,
						!t.isEmpty()
								? RageMode.getLang().get("commands.area.info", "%area%", t, "%location%",
										player.getLocation())
								: RageMode.getLang().get("commands.area.not-exists"));
			} else if (args[1].equalsIgnoreCase("list")) {
				if (GameAreaManager.getGameAreas().isEmpty()) {
					sendMessage(sender, RageMode.getLang().get("commands.area.empty"));
					return false;
				}

				int i = 0;

				for (Map.Entry<String, GameArea> area : GameAreaManager.getGameAreas().entrySet()) {
					Area a = area.getValue().getArea();

					sendMessage(sender,
							RageMode.getLang().get("commands.area.list", "%num%", ++i, "%area%", area.getKey(),
									"%lowx%", a.getLowLoc().getBlockX(), "%lowy%", a.getLowLoc().getBlockY(), "%lowz%",
									a.getLowLoc().getBlockZ(), "%highx%", a.getHighLoc().getBlockX(), "%highy%",
									a.getHighLoc().getBlockY(), "%highz%", a.getHighLoc().getBlockZ()));
				}
			}
		}

		return true;
	}
}
