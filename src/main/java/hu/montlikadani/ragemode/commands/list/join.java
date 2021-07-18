package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
	name = "join",
	permission = { "ragemode.join", "ragemode.help.playercommands" },
	params = "<gameName>",
	desc = "Joins to the specified game",
	playerOnly = true)
public final class join implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm join <gameName>"));
			return false;
		}

		BaseGame game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		Player player = (Player) sender;

		if (GameUtils.isPlayerPlaying(player)) {
			sendMessage(player, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		if (ConfigValues.isPerJoinPermissions() && !player.hasPermission("ragemode.join." + args[1])) {
			sendMessage(player, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (ReJoinDelay.checkRejoinDelay(player, "")) {
			GameUtils.joinPlayer(player, game);
		}

		return true;
	}
}
