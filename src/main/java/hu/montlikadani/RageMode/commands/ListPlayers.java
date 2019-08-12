package hu.montlikadani.ragemode.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class ListPlayers extends ICommand {

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.listplayers")) {
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

			if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				sendMessage(p, RageMode.getLang().get("commands.listplayers.player-currently-not-playing"));
				return false;
			}

			for (Iterator<String> e = Arrays.asList(PlayerList.getPlayersGame(p)).iterator(); e.hasNext();) {
				sb.append("&7-&6 " + p.getName() + "&a - " + e.next());
			}

			sendMessage(p, RageMode.getLang().colors("&7Players:\n" + sb));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameWithNameExists(game)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			if (!(GameUtils.getStatus() == GameStatus.RUNNING || GameUtils.getStatus() == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			StringBuilder sb = new StringBuilder();
			for (java.util.Map.Entry<String, String> players : PlayerList.getPlayers().entrySet()) {
				Player player = Bukkit.getPlayer(UUID.fromString(players.getValue()));

				for (Iterator<String> e = Arrays.asList(PlayerList.getPlayersGame(player)).iterator(); e.hasNext();) {
					sb.append("&7-&6 " + player.getName() + "&a - " + e.next());
				}
			}

			sendMessage(sender, RageMode.getLang().colors("&7Players:\n" + sb));
		}
		return false;
	}
}
