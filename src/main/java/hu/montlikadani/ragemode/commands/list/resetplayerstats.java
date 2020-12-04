package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.UUID;

@CommandProcessor(name = "resetplayerstats", permission = "ragemode.admin.stats.reset")
public class resetplayerstats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
				return false;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}

			if (GameUtils.isPlayerPlaying(target)) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}

			if (reset(target.getUniqueId())) {
				sendMessage(sender,
						RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
				return true;
			}

			sendMessage(sender, RageMode.getLang().get("commands.stats.could-not-reset-player-stats"));
			return false;
		}

		Player p = (Player) sender;
		if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}

			if (GameUtils.isPlayerPlaying(target)) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}

			if (reset(target.getUniqueId())) {
				sendMessage(p,
						RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
				return true;
			}

			sendMessage(p, RageMode.getLang().get("commands.stats.could-not-reset-player-stats"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return false;
		}

		if (reset(p.getUniqueId())) {
			sendMessage(p, RageMode.getLang().get("commands.stats.reseted"));
			return true;
		}

		sendMessage(p, RageMode.getLang().get("commands.stats.could-not-reset-player-stats"));
		return false;
	}

	private boolean reset(UUID uuid) {
		return RageMode.getInstance().getDatabase().resetPlayerStatistic(uuid);
	}
}
