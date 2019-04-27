package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.LobbyTimer;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class ForceStart extends RmCommand {

	public ForceStart(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.forcestart")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
			return;
		}
		String game = args[1];
		if (!GetGames.isGameExistent(game)) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.game-not-exist"));
			return;
		}
		if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.player-not-in-game"));
			return;
		}
		if (PlayerList.getPlayersInGame(game).length < 2) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.not-enough-players"));
			return;
		}
		if (PlayerList.isGameRunning(game)) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return;
		}

		if (LobbyTimer.map.containsKey(game)) {
			LobbyTimer.map.get(game).cancel();
			LobbyTimer.map.remove(game);

			// Set level back to 0
			p.setLevel(0);
		}

		new GameLoader(game);
		sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
		return;
	}
}
