package hu.montlikadani.ragemode.commands.list;

import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class listplayers {

	public boolean run(CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.listplayers")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length <= 1) {
			if (!(sender instanceof Player)) {
				sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm listplayers <game>"));
				return false;
			}

			Player p = (Player) sender;
			StringBuilder sb = new StringBuilder();

			if (!GameUtils.isPlayerPlaying(p)) {
				sendMessage(p, RageMode.getLang().get("commands.listplayers.player-currently-not-playing"));
				return false;
			}

			for (Iterator<String> e = Arrays.asList(GameUtils.getGameByPlayer(p).getPlayersGame(p)).iterator(); e.hasNext();) {
				sb.append("&7-&6 " + p.getName() + "&a - " + e.next());
			}

			sendMessage(p, Utils.colors("&7Players:\n" + sb));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameWithNameExists(game)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			if (!(GameUtils.getStatus(game) == GameStatus.RUNNING || GameUtils.getStatus(game) == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			StringBuilder sb = new StringBuilder();
			for (PlayerManager pm : GameUtils.getGame(game).getPlayersFromList()) {
				Player player = pm.getPlayer();

				for (Iterator<String> e = Arrays.asList(GameUtils.getGameByPlayer(player).getPlayersGame(player))
						.iterator(); e.hasNext();) {
					sb.append("&7-&6 " + player.getName() + "&a - " + e.next());
				}
			}

			sendMessage(sender, Utils.colors("&7Players:\n" + sb));
		}
		return false;
	}
}
