package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class ForceStart extends RmCommand {

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.forcestart")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
			return false;
		}
		String game = args[1];
		if (!GameUtils.isGameWithNameExists(game)) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.game-not-exist"));
			return false;
		}
		if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.player-not-in-game"));
			return false;
		}
		if (PlayerList.getPlayers().size() < 2) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.not-enough-players"));
			return false;
		}
		if (PlayerList.isGameRunning(game)) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return false;
		}

		PlayerList.getLobbyTimer().cancel();
		// Set level back to 0
		p.setLevel(0);

		new GameLoader(game);
		sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
		return false;
	}
}
