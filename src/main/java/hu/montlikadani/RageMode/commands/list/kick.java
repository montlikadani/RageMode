package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.PlayerKickedFromGame;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class kick {

	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.kick")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 3) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName> <playerName>"));
			return false;
		}

		String name = args[1];
		if (name == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.game-not-null"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(name)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return false;
		}

		if (!GameUtils.getGameByName(name).isGameRunning(name)) {
			sendMessage(sender, RageMode.getLang().get("game.not-running"));
			return false;
		}

		Player target = Bukkit.getPlayer(args[2]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-not-found"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(target)) {
			Game game = GameUtils.getGameByPlayer(target);

			PlayerKickedFromGame event = new PlayerKickedFromGame(game, sender, target);
			Bukkit.getPluginManager().callEvent(event);

			game.removePlayer(target);

			sendMessage(sender, RageMode.getLang().get("commands.kick.player-kicked", "%player%", target.getName(), "%game%", game));
		} else
			sendMessage(sender, RageMode.getLang().get("commands.kick.player-currently-not-playing"));
		return false;
	}
}
