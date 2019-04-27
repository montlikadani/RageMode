package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.holo.HoloHolder;

public class HoloStats extends RmCommand {

	public HoloStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.holo")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (RageMode.getInstance().isHologramEnabled()) {
			if (args.length >= 2) {
				if (args[1].equalsIgnoreCase("add")) {
					HoloHolder.addHolo(p.getLocation());
				}
				if (args[1].equalsIgnoreCase("remove")) {
					HoloHolder.deleteHologram(p, HoloHolder.getClosest(p));
				}
				// TODO Teleport to hologram
				/**if (args[1].equalsIgnoreCase("tp")) {
					HoloHolder.teleporttoHologram(p, HoloHolder.getClosest(p));
				}*/
			} else
				sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <add/remove>"));
		} else
			sendMessage(p, RageMode.getLang().get("missing-dependencies", "%depend%", "HolographicDisplays"));
		return;
	}
}
