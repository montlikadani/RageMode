package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Misc;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class leave {

	public boolean run(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!Misc.hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		Game game = GameUtils.getGameByPlayer(p);
		if (game == null) {
			p.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		GameUtils.runCommands(p, game.getName(), "leave");
		GameUtils.sendActionBarMessages(p, game.getName(), "leave");
		GameUtils.leavePlayer(p, game);
		return true;
	}
}
