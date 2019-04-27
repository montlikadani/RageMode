package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class KickPlayer extends RmCommand {

	public KickPlayer(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.kick")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 3) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName> <playerName>"));
			return;
		}
		String game = args[1];
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.game-not-null"));
			return;
		}

		if (!GetGames.isGameExistent(game)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return;
		}

		if (!PlayerList.isGameRunning(game))
			sendMessage(sender, RageMode.getLang().get("game.not-running"));
		else {
			Player target = Bukkit.getPlayer(args[2]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.kick.player-not-found"));
				return;
			}
			if (PlayerList.isPlayerPlaying(target.getUniqueId().toString())) {
				PlayerList.removePlayer(target);

				sendMessage(sender, RageMode.getLang().get("commands.kick.player-kicked", "%player%", target.getName(), "%game%", game));
			} else
				sendMessage(sender, RageMode.getLang().get("commands.kick.player-currently-not-playing"));
		}
		return;
	}
}
