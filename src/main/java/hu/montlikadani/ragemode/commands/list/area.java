package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.io.IOException;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.area.Area;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class area implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.area")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm area add/remove/info/list"));
			return false;
		}

		if (args.length <= 4 && args[1].equalsIgnoreCase("add")) {
			if (args.length < 4) {
				sendMessage(p,
						RageMode.getLang().get("missing-arguments", "%usage%", "/rm area add <gameName> <areaName>"));
				return false;
			}

			if (!GameUtils.isGameWithNameExists(args[2])) {
				sendMessage(p, RageMode.getLang().get("invalid-game"));
				return false;
			}

			if (GameAreaManager.isAreaExist(args[3])) {
				sendMessage(p, RageMode.getLang().get("commands.area.already-exists"));
				return false;
			}

			if (!plugin.getSelection().hasSetBoth(p)) {
				sendMessage(p,
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
			aFile.set("areas." + areaName + ".world", p.getWorld().getName());
			aFile.set("areas." + areaName + ".game", args[2]);

			Area area = plugin.getSelection().getArea(p);
			String path = "areas." + areaName + ".loc1.";
			aFile.set(path + "x", area.getLowLoc().getX());
			aFile.set(path + "y", area.getLowLoc().getY());
			aFile.set(path + "z", area.getLowLoc().getZ());

			path = "areas." + areaName + ".loc2.";
			aFile.set(path + "x", area.getHighLoc().getX());
			aFile.set(path + "y", area.getHighLoc().getY());
			aFile.set(path + "z", area.getHighLoc().getZ());
			Configuration.saveFile(aFile, plugin.getConfiguration().getAreasFile());

			GameAreaManager.getGameAreas().put(areaName, new GameArea(args[2], area));
			sendMessage(p, RageMode.getLang().get("commands.area.set", "%name%", areaName));
		} else if (args.length <= 3 && args[1].equalsIgnoreCase("remove")) {
			if (args.length < 3) {
				sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm area remove <areaName>"));
				return false;
			}

			if (!GameAreaManager.isAreaExist(args[2])) {
				sendMessage(p, RageMode.getLang().get("commands.area.not-exists"));
				return false;
			}

			GameAreaManager.getGameAreas().remove(args[2]);
			plugin.getConfiguration().getAreasCfg().set("areas." + args[2], null);
			Configuration.saveFile(plugin.getConfiguration().getAreasCfg(), plugin.getConfiguration().getAreasFile());
			sendMessage(p, RageMode.getLang().get("commands.area.removed", "%name%", args[3]));
		} else if (args.length >= 1) {
			if (args[1].equalsIgnoreCase("info")) {
				String t = "";

				for (Map.Entry<String, GameArea> map : GameAreaManager.getGameAreas().entrySet()) {
					if (map.getValue().inArea(p.getLocation())) {
						if (!t.isEmpty()) {
							t += ", ";
						}

						t += map.getKey();
					}
				}

				sendMessage(p, !t.isEmpty() ? t : RageMode.getLang().get("commands.area.not-exists"));
			} else if (args[1].equalsIgnoreCase("list")) {
				Map<String, GameArea> map = GameAreaManager.getGameAreas();
				if (map.isEmpty()) {
					sendMessage(p, RageMode.getLang().get("commands.area.empty"));
					return false;
				}

				int i = 0;
				for (Map.Entry<String, GameArea> area : map.entrySet()) {
					Area a = area.getValue().getArea();
					sendMessage(p,
							Utils.colors("&eArea list:\n&a" + ++i + ". " + area.getKey() + "\n  &eLow:\n  - &cx:"
									+ a.getLowLoc().getBlockX() + ", y:" + a.getLowLoc().getBlockY() + ", z:"
									+ a.getLowLoc().getBlockZ() + "\n  &eHigh:\n  - &cx:" + a.getHighLoc().getBlockX()
									+ ", y:" + a.getHighLoc().getBlockY() + ", z:" + a.getHighLoc().getBlockZ()));
				}
			}
		}

		return true;
	}
}
