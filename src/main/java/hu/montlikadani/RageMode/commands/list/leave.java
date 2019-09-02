package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class leave extends ICommand {

	@Override
	public boolean run(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		GameUtils.runCommands(p, Game.getPlayersGame(p), "leave");
		GameUtils.sendActionBarMessages(p, Game.getPlayersGame(p), "leave");
		p.removeMetadata("killedWith", RageMode.getInstance());
		Game.removePlayer(p);
		Game.removeSpectatorPlayer(p);
		return false;
	}
}
