package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GameUtils.ActionMessageType;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "leave", permission = "ragemode.leave", playerOnly = true)
public class leave implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
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

		GameUtils.runCommands(p, game, "leave");
		GameUtils.sendActionMessage(p, game, ActionMessageType.LEAVE, ActionMessageType.asActionbar());
		GameUtils.leavePlayer(p, game);
		return true;
	}
}
