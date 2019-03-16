package hu.montlikadani.ragemode.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class ForceStart extends RmCommand {

	// Stops the LobbyTimer task when wants to force starts game
	public static List<String> toStopTask = new ArrayList<>();

	public ForceStart(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.forcestart")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 2) {
			p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
			return;
		}
		if (!GetGames.isGameExistent(args[1])) {
			p.sendMessage(RageMode.getLang().get("commands.forcestart.game-not-exist"));
			return;
		}
		String game = args[1];
		if (PlayerList.getPlayersGame(p) != null) {
			if (!PlayerList.isGameRunning(game)) {
				if (toStopTask.contains(game))
					toStopTask.remove(game);

				toStopTask.add(game);

				new GameLoader(game);
				p.sendMessage(RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
			} else
				p.sendMessage(RageMode.getLang().get("game.running"));
		} else
			p.sendMessage(RageMode.getLang().get("commands.forcestart.not-enough-players"));
		return;
	}
}
