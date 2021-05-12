package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.holder.holograms.ArmorStands;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.Optional;

@CommandProcessor(
		name = "holostats",
		permission = "ragemode.admin.holo",
		desc = "General commands for handling holograms",
		params = "<add/remove/teleport>",
		playerOnly = true)
public final class holostats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <add/remove/tp>"));
			return false;
		}

		Player player = (Player) sender;

		switch (args[1].toLowerCase()) {
		case "add":
			plugin.getHoloHolder().addHolo(player.getLocation());
			break;
		case "remove":
			if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
				plugin.getHoloHolder().deleteAllHologram(true);
				break;
			}

			if (!plugin.getHoloHolder().deleteHologram(getClosest(player, plugin, true))) {
				sendMessage(player, RageMode.getLang().get("commands.holostats.no-holo-found"));
				return false;
			}

			break;
		case "tp":
		case "teleport":
			Location closest = getClosest(player, plugin, false);

			if (closest == null) {
				sendMessage(player, RageMode.getLang().get("commands.holostats.no-holo-found"));
				return false;
			}

			player.teleport(closest);
			break;
		default:
			break;
		}

		return true;
	}

	private Location getClosest(Player player, RageMode plugin, boolean eye) {
		Optional<ArmorStands> closest = plugin.getHoloHolder().getClosest(player, eye);
		return closest.isPresent() ? closest.get().getLocation() : null;
	}
}
