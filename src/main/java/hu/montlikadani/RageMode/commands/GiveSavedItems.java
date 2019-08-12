package hu.montlikadani.ragemode.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class GiveSavedItems extends RmCommand {

	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!plugin.getConfiguration().getCfg().getBoolean("save-player-datas-to-file")) {
			sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.not-enabled"));
			return false;
		}

		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.givesaveditems")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm givesaveditems <player> [true]"));
			return false;
		}

		Player target = Bukkit.getPlayer(args[1]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("player-non-existent"));
			return false;
		}

		if (PlayerList.isPlayerPlaying(target.getUniqueId().toString())) {
			sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.player-is-in-game", "%player%", args[1]));
			return false;
		}

		FileConfiguration datas = plugin.getConfiguration().getDatasCfg();
		if (datas.getString("datas." + args[1]) == null) {
			sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.player-not-found-in-data-file", "%player%", args[1]));
			return false;
		}

		for (String names : datas.getConfigurationSection("datas").getKeys(false)) {
			if (names.equalsIgnoreCase(args[1])) {
				Utils.clearPlayerInventory(target);

				List<?> contentList = (List<?>) datas.get("datas." + names + ".contents");
				target.getInventory().setContents(contentList.toArray(new ItemStack[contentList.size()]));

				List<?> armorList = (List<?>) datas.get("datas." + names + ".armor-contents");
				target.getInventory().setArmorContents(armorList.toArray(new ItemStack[armorList.size()]));

				target.setExp(datas.getInt("datas." + names + ".exp"));
				target.setLevel(datas.getInt("datas." + names + ".level"));
				target.setGameMode(GameMode.valueOf(datas.getString("datas." + names + ".game-mode")));
			} else if (datas.getConfigurationSection("datas").getKeys(false).isEmpty()) {
				sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.no-player-saved-inventory"));
				return false;
			}
		}

		if (args.length == 3) {
			if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("yes")) {
				datas.set("datas." + args[1], null);
				Configuration.saveFile(datas, plugin.getConfiguration().getDatasFile());
			}
		}
		return false;
	}
}
