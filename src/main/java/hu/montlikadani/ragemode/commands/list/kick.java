package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.PlayerKickedFromGame;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "kick",
		params = "<playerName>",
		desc = "Kicks a player from a game where currently playing",
		permission = "ragemode.admin.kick")
public final class kick implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <playerName>"));
			return false;
		}

		Player target = plugin.getServer().getPlayer(args[2]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-not-found"));
			return false;
		}

		BaseGame playerGame = GameUtils.getGameByPlayer(target);
		if (playerGame == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-currently-not-playing"));
			return false;
		}

		Utils.callEvent(new PlayerKickedFromGame(playerGame, sender, target));
		playerGame.removePlayer(target);
		SignCreator.updateAllSigns(playerGame.getName());

		sendMessage(sender, RageMode.getLang().get("commands.kick.player-kicked", "%player%", target.getName(),
				"%game%", playerGame.getName()));
		return true;
	}
}
