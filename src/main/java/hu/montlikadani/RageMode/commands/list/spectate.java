package hu.montlikadani.ragemode.commands.list;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.Items;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class spectate implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.spectate")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		String map = args[1];
		if (!GameUtils.isGameWithNameExists(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		if (!GameUtils.getGame(map).isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.not-running"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
			return false;
		}

		if (GameUtils.getGame(map).addSpectatorPlayer(p, map)) {
			p.teleport(GameUtils.getGameSpawn(map).getRandomSpawn());

			p.setAllowFlight(true);
			p.setFlying(true);
			p.setGameMode(GameMode.SPECTATOR);
			if (Items.getLeaveGameItem() != null) {
				p.getInventory().setItem(Items.getLeaveGameItem().getSlot(), Items.getLeaveGameItem().getResult());
			}

			return true;
		}

		return false;
	}
}
