package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "leave",
		permission = { "ragemode.leave", "ragemode.help.playercommands" },
		desc = "Leave from the current game",
		playerOnly = true)
public final class leave implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerManager pm = GameUtils.getPlayerManager(player);

		if (pm != null && pm.isSpectator()) {
			pm.getPlayerGame().removePlayer(player);
			return true;
		}

		if (pm == null) {
			sendMessage(player, RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		BaseGame game = pm.getPlayerGame();

		RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, pm);
		Utils.callEvent(gameLeaveEvent);

		if (game != null && !gameLeaveEvent.isCancelled()) {
			GameUtils.runCommands(player, game, RewardManager.InGameCommand.CommandType.LEAVE);
			GameUtils.sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.LEAVE,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.ACTIONBAR);

			game.removePlayer(player);
			SignCreator.updateAllSigns(game.getName());
		}

		return true;
	}
}
