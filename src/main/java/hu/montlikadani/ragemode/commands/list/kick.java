package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.PlayerKickedFromGame;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "kick", permission = "ragemode.admin.kick")
public class kick implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%",
					"/rm " + args[0] + " <gameName> <playerName>"));
			return false;
		}

		String name = args[1];
		if (name == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.game-not-null"));
			return false;
		}

		if (!GameUtils.isGameExist(name)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return false;
		}

		Player target = Bukkit.getPlayer(args[2]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-not-found"));
			return false;
		}

		if (!GameUtils.isPlayerPlaying(target)) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-currently-not-playing"));
			return false;
		}

		Game game = GameUtils.getGameByPlayer(target);

		Utils.callEvent(new PlayerKickedFromGame(game, sender, target));
		game.removePlayer(target);
		SignCreator.updateAllSigns(game.getName());

		sendMessage(sender,
				RageMode.getLang().get("commands.kick.player-kicked", "%player%", target.getName(), "%game%", name));
		return true;
	}
}
