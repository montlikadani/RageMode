package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
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
			boolean isPlayerPlaying = GameUtils.isPlayerPlaying(p);
			boolean isSpectatorPlaying = GameUtils.isSpectatorPlaying(p);

			if (!isPlayerPlaying && !isSpectatorPlaying) {
				sendMessage(p, RageMode.getLang().get("commands.listplayers.player-currently-not-playing"));
				return false;
			}

			if (isPlayerPlaying) {
				StringBuilder sb = new StringBuilder();
				for (PlayerManager pm : GameUtils.getGameByPlayer(p).getPlayersFromList()) {
					sb.append("&7-&6 " + pm.getPlayer().getName() + "&a - " + pm.getGameName());
				}

				sendMessage(p, "&7Players:\n" + sb.toString(), true);
			}

			if (isSpectatorPlaying) {
				StringBuilder sb = new StringBuilder();
				for (PlayerManager pm : GameUtils.getGameBySpectator(p).getPlayersFromList()) {
					sb.append("&7-&6 " + pm.getPlayer().getName() + "&a - " + pm.getGameName());
				}

				sendMessage(sender, "&7Spectator players:\n" + sb.toString(), true);
			}

			return true;
		}

		if (args.length >= 2) {
			String gameName = args[1];
			if (!GameUtils.isGameWithNameExists(gameName)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			Game game = GameUtils.getGame(gameName);
			if (!(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			if (!game.getPlayers().isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (PlayerManager pm : game.getPlayersFromList()) {
					sb.append("&7-&6 " + pm.getPlayer().getName() + "&a - " + gameName);
				}

				sendMessage(sender, "&7Players:\n" + sb.toString(), true);
			}

			if (!game.getSpectatorPlayers().isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (PlayerManager spec : game.getSpectatorPlayers().values()) {
					sb.append("\n&7-&6 " + spec.getPlayer().getName() + "&a - " + spec.getGameName());
				}

				sendMessage(sender, "&7Spectator players:\n" + sb.toString(), true);
			}
		}

		return true;
	}
}
