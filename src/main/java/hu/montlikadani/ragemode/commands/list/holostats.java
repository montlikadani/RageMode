package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.holder.ArmorStands;
import hu.montlikadani.ragemode.holder.IHoloHolder;

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

		if (args.length < 2) {
			sendMessage(p,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <add/remove/tp>"));
			return false;
		}

		IHoloHolder holder = RageMode.getInstance().getHoloHolder();
		org.bukkit.Location closest = null;

		switch (args[1].toLowerCase()) {
		case "add":
			holder.addHolo(p.getLocation());
			break;
		case "remove":
			if (holder.getClosest(p, true).isPresent()) {
				if (RageMode.getInstance().isPluginEnabled("HolographicDisplays")) {
					closest = ((com.gmail.filoghost.holographicdisplays.api.Hologram) holder.getClosest(p, true).get())
							.getLocation();
				} else {
					closest = ((ArmorStands) holder.getClosest(p, true).get()).getLocation();
				}
			}

			if (!holder.deleteHologram(closest)) {
				sendMessage(p, RageMode.getLang().get("commands.holostats.no-holo-found"));
				return false;
			}

			break;
		case "tp":
			if (holder.getClosest(p, true).isPresent()) {
				if (RageMode.getInstance().isPluginEnabled("HolographicDisplays")) {
					closest = ((com.gmail.filoghost.holographicdisplays.api.Hologram) holder.getClosest(p, false).get())
							.getLocation();
				} else {
					closest = ((ArmorStands) holder.getClosest(p, false).get()).getLocation();
				}
			}

			if (closest == null) {
				sendMessage(p, RageMode.getLang().get("commands.holostats.no-holo-found"));
				return false;
			}

			p.teleport(closest);
			break;
		default:
			break;
		}

		return true;
	}
}
