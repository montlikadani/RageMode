package hu.montlikadani.ragemode.commands.list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

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
			if (!GameUtils.isPlayerPlaying(p) || !GameUtils.isSpectatorPlaying(p)) {
				sendMessage(p, RageMode.getLang().get("commands.listplayers.player-currently-not-playing"));
				return false;
			}

			StringBuilder sb = null;
			if (!GameUtils.getGameByPlayer(p).getPlayersFromList().isEmpty()) {
				sb = new StringBuilder();

				for (Iterator<PlayerManager> e = GameUtils.getGameByPlayer(p).getPlayersFromList().iterator(); e.hasNext();) {
					sb.append("&7-&6 " + e.next().getPlayer().getName() + "&a - " + e.next().getGameName());
				}

				sendMessage(p, "&7Players:\n" + sb, true);
			}

			if (!GameUtils.getGameBySpectator(p).getPlayersFromList().isEmpty()) {
				sb = new StringBuilder();

				for (Iterator<PlayerManager> e = GameUtils.getGameBySpectator(p).getPlayersFromList().iterator(); e.hasNext();) {
					sb.append("&7-&6 " + e.next().getPlayer().getName() + "&a - " + e.next().getGameName());
				}

				sendMessage(sender, "&7Spectator players:\n" + sb, true);
			}

			return true;
		}

		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameWithNameExists(game)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			Optional<GameStatus> status = GameUtils.getStatus(game);
			if (status.isPresent() && !(status.get() == GameStatus.RUNNING || status.get() == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			StringBuilder sb = null;
			if (!GameUtils.getGame(game).getPlayers().isEmpty()) {
				sb = new StringBuilder();

				for (PlayerManager pm : GameUtils.getGame(game).getPlayersFromList()) {
					Player player = pm.getPlayer();

					for (Iterator<String> e = Arrays.asList(GameUtils.getGameByPlayer(player).getName())
							.iterator(); e.hasNext();) {
						sb.append("&7-&6 " + player.getName() + "&a - " + e.next());
					}
				}

				sendMessage(sender, "&7Players:\n" + sb, true);
			}

			if (!GameUtils.getGame(game).getSpectatorPlayers().isEmpty()) {
				sb = new StringBuilder();

				for (java.util.Map.Entry<Player, PlayerManager> spec : GameUtils.getGame(game).getSpectatorPlayers()
						.entrySet()) {
					sb.append("\n&7-&6 " + spec.getKey().getName() + "&a - " + spec.getValue());
				}

				sendMessage(sender, "&7Spectator players:\n" + sb, true);
			}
		}

		return true;
	}
}
