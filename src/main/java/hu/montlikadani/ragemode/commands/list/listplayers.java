package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
	name = "listplayers",
	desc = "Lists all currently playing players",
	params = "<gameName>",
	permission = "ragemode.listplayers")
public final class listplayers implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length <= 1) {
			if (!(sender instanceof Player)) {
				sendMessage(sender,
						RageMode.getLang().get("missing-arguments", "%usage%", "/rm listplayers <gameName>"));
				return false;
			}

			Player player = (Player) sender;
			PlayerManager plManager = GameUtils.getPlayerManager(player);

			if (plManager == null) {
				sendMessage(player, RageMode.getLang().get("commands.listplayers.player-currently-not-playing"));
				return false;
			}

			StringBuilder sb = new StringBuilder();

			if (plManager.isSpectator()) {
				for (PlayerManager pm : plManager.getPlayerGame().getSpectatorPlayers()) {
					Player pl = pm.getPlayer();

					if (pl != null) {
						sb.append("&7-&6 " + pl.getName() + "&a - " + pm.getGameName());
					}
				}

				sendMessage(sender, "&7Spectator players:\n" + sb.toString());
			} else {
				for (PlayerManager pm : plManager.getPlayerGame().getPlayers()) {
					Player pl = pm.getPlayer();

					if (pl != null) {
						sb.append("&7-&6 " + pl.getName() + "&a - " + pm.getGameName());
					}
				}

				sendMessage(player, "&7Players:\n" + sb.toString());
			}

			return true;
		}

		if (args.length >= 2) {
			BaseGame game = GameUtils.getGame(args[1]);

			if (game == null) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			if (!(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.WAITING)) {
				sendMessage(sender, RageMode.getLang().get("commands.listplayers.game-not-running"));
				return false;
			}

			if (!game.getPlayers().isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (PlayerManager pm : game.getPlayers()) {
					Player pl = pm.getPlayer();

					if (pl != null) {
						sb.append("&7-&6 " + pl.getName() + "&a - " + args[1]);
					}
				}

				sendMessage(sender, "&7Players:\n" + sb.toString());
			}

			java.util.Set<PlayerManager> specs = game.getSpectatorPlayers();

			if (!specs.isEmpty()) {
				StringBuilder sb = new StringBuilder();

				for (PlayerManager spec : specs) {
					Player pl = spec.getPlayer();

					if (pl != null) {
						sb.append("&7-&6 " + pl.getName() + "&a - " + args[1]);
					}
				}

				sendMessage(sender, "&7Spectator players:\n" + sb.toString());
			}
		}

		return true;
	}
}
