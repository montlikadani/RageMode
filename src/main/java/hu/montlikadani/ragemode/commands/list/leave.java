package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GameUtils.ActionMessageType;
import hu.montlikadani.ragemode.utils.Misc;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class leave implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!Misc.hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		Game game = GameUtils.getGameBySpectator(p);
		if (game != null) {
			game.removeSpectatorPlayer(p);
			return true;
		}

		game = GameUtils.getGameByPlayer(p);
		if (game == null) {
			sendMessage(p, RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		GameUtils.runCommands(p, game.getName(), "leave");
		GameUtils.sendActionMessage(p, game.getName(), ActionMessageType.LEAVE, ActionMessageType.asActionbar());
		GameUtils.leavePlayer(p, game);
		return true;
	}
}
