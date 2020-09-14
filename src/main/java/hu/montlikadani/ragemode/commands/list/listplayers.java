package hu.montlikadani.ragemode.commands.list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class listplayers implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
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

			if (!GameUtils.getGameByPlayer(p).getPlayersFromList().isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (Iterator<PlayerManager> e = GameUtils.getGameByPlayer(p).getPlayersFromList().iterator(); e
						.hasNext();) {
					sb.append("&7-&6 " + e.next().getPlayer().getName() + "&a - " + e.next().getGameName());
				}

				sendMessage(p, "&7Players:\n" + sb, true);
			}

			if (!GameUtils.getGameBySpectator(p).getPlayersFromList().isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (Iterator<PlayerManager> e = GameUtils.getGameBySpectator(p).getPlayersFromList().iterator(); e
						.hasNext();) {
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

			GameStatus status = GameUtils.getGame(game).getStatus();
			if (!(status == GameStatus.RUNNING || status == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			if (!GameUtils.getGame(game).getPlayers().isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (PlayerManager pm : GameUtils.getGame(game).getPlayersFromList()) {
					Player player = pm.getPlayer();

					for (Iterator<String> e = Arrays.asList(GameUtils.getGameByPlayer(player).getName()).iterator(); e
							.hasNext();) {
						sb.append("&7-&6 " + player.getName() + "&a - " + e.next());
					}
				}

				sendMessage(sender, "&7Players:\n" + sb, true);
			}

			if (!GameUtils.getGame(game).getSpectatorPlayers().isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (Entry<UUID, PlayerManager> spec : GameUtils.getGame(game).getSpectatorPlayers()
						.entrySet()) {
					Player pl = Bukkit.getPlayer(spec.getKey());
					if (pl != null) {
						sb.append("\n&7-&6 " + pl.getName() + "&a - " + spec.getValue());
					}
				}

				sendMessage(sender, "&7Spectator players:\n" + sb, true);
			}
		}

		return true;
	}
}
