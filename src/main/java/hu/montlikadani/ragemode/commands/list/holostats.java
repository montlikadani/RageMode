package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.holder.ArmorStands;
import hu.montlikadani.ragemode.holder.IHoloHolder;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "holostats", permission = "ragemode.admin.holo", playerOnly = true)
public class holostats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;

		if (args.length < 2) {
			sendMessage(p,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <add/remove/tp>"));
			return false;
		}

		IHoloHolder holder = RageMode.getInstance().getHoloHolder();

		switch (args[1].toLowerCase()) {
		case "add":
			holder.addHolo(p.getLocation());
			break;
		case "remove":
			if (!holder.deleteHologram(getClosest(p, holder, true))) {
				sendMessage(p, RageMode.getLang().get("commands.holostats.no-holo-found"));
				return false;
			}

			break;
		case "tp":
			Location closest = getClosest(p, holder, false);
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

	private Location getClosest(Player player, IHoloHolder holder, boolean eye) {
		if (holder.getClosest(player, eye).isPresent()) {
			if (RageMode.getInstance().isPluginEnabled("HolographicDisplays")) {
				return holder.<com.gmail.filoghost.holographicdisplays.api.Hologram>getClosest(player, eye).get()
						.getLocation();
			}

			return holder.<ArmorStands>getClosest(player, eye).get().getLocation();
		}

		return null;
	}
}
