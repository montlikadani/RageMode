package hu.montlikadani.ragemode.commands.list;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
	name = "givesaveditems",
	desc = "Returns the saved inventory to the player",
	params = "<player/all>",
	permission = "ragemode.admin.givesaveditems")
public final class givesaveditems implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hu.montlikadani.ragemode.config.configconstants.ConfigValues.isSavePlayerData()) {
			sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.not-enabled"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm givesaveditems <player/all>"));
			return false;
		}

		FileConfiguration datas = plugin.getConfiguration().getDatasCfg();
		ConfigurationSection section = datas.getConfigurationSection("datas");
		if (section == null) {
			sendMessage(sender, RageMode.getLang().get("commands.givesaveditems.not-saved"));
			return true;
		}

		if (args[1].equalsIgnoreCase("all")) {
			for (String one : section.getKeys(false)) {
				for (Player target : plugin.getServer().getOnlinePlayers()) {
					if (!GameUtils.isPlayerPlaying(target) && one.equalsIgnoreCase(target.getName())) {
						giveBack(target, one, section);
						Configuration.saveFile(datas, plugin.getConfiguration().getDatasFile());
						break;
					}
				}
			}

			return true;
		}

		Player target = plugin.getServer().getPlayer(args[1]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("player-non-existent"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(target)) {
			sendMessage(sender,
					RageMode.getLang().get("commands.givesaveditems.player-is-in-game", "%player%", args[1]));
			return false;
		}

		for (String one : section.getKeys(false)) {
			if (one.equalsIgnoreCase(args[1])) {
				giveBack(target, one, section);
				Configuration.saveFile(datas, plugin.getConfiguration().getDatasFile());
				break;
			}
		}

		return true;
	}

	private void giveBack(Player target, String one, ConfigurationSection section) {
		List<?> contentList = section.getList(one + ".contents");
		if (contentList != null) {
			target.getInventory().setContents(contentList.toArray(new ItemStack[0]));
		}

		List<?> armorList = section.getList(one + ".armor-contents");
		if (armorList != null) {
			target.getInventory().setArmorContents(armorList.toArray(new ItemStack[0]));
		}

		target.setExp(section.getInt(one + ".exp"));
		target.setLevel(section.getInt(one + ".level"));

		try {
			target.setGameMode(GameMode.valueOf(section.getString(one + ".game-mode", "")));
		} catch (IllegalArgumentException e) {
		}

		section.set(one, null);
	}
}
