package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.holder.HoloHolder;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class holostats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.holo")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (!plugin.isHologramEnabled()) {
			sendMessage(p, RageMode.getLang().get("missing-dependencies", "%depend%", "HolographicDisplays"));
			return false;
		}

		if (args.length >= 2) {
			switch (args[1].toLowerCase()) {
			case "add":
				HoloHolder.addHolo(p.getLocation());
				break;
			case "remove":
				if (!HoloHolder.deleteHologram(HoloHolder.getClosest(p, true))) {
					sendMessage(p, RageMode.getLang().get("commands.holostats.no-holo-found"));
					return false;
				}

				break;
			case "tp":
				if (HoloHolder.getHologramLocation(HoloHolder.getClosest(p, false)) == null) {
					sendMessage(p, RageMode.getLang().get("commands.holostats.no-holo-found"));
					return false;
				}

				p.teleport(HoloHolder.getHologramLocation(HoloHolder.getClosest(p, false)));
				break;
			default:
				break;
			}
		} else {
			sendMessage(p,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <add/remove/tp>"));
			return false;
		}

		return true;
	}
}
