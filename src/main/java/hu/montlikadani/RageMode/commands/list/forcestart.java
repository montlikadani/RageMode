package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameLoader;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class forcestart {

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

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.player-not-in-game"));
			return false;
		}

		if (GameUtils.getGameByName(game).isGameRunning(game)) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return false;
		}

		if (GameUtils.getGameByName(game).getPlayers().size() < 2) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.not-enough-players"));
			return false;
		}

		if (GameUtils.getGameByName(game).getLobbyTimer() != null) {
			GameUtils.getGameByName(game).getLobbyTimer().cancel();
			p.setLevel(0); // Set level counter back to 0
		}

		sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
		RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
				() -> new GameLoader(GameUtils.getGameByName(game)));
		return false;
	}
}
