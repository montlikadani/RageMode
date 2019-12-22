package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

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

		if (GameUtils.getGame(game).isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return false;
		}

		if (GameUtils.getGame(game).getPlayers().size() < 2) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.not-enough-players"));
			return false;
		}

		GameUtils.forceStart(GameUtils.getGame(game));
		sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game));
		return true;
	}
}
