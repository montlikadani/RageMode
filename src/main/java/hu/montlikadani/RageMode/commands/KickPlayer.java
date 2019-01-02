package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.signs.SignCreator;

public class KickPlayer extends RmCommand {

	public KickPlayer(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.kick")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 3) {
			p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName> <playerName>"));
			return;
		}
		String game = args[1];
		if (game == null) {
			p.sendMessage(RageMode.getLang().get("commands.kick.game-not-null"));
			return;
		}
		if (!PlayerList.isGameRunning(game))
			p.sendMessage(RageMode.getLang().get("game.not-running"));
		else {
			Player target = Bukkit.getPlayer(args[2]);
			if (target == null) {
				p.sendMessage(RageMode.getLang().get("commands.kick.player-not-found"));
				return;
			}
			if (PlayerList.isPlayerPlaying(target.getName())) {
				PlayerList.removePlayerSynced(target);
				PlayerList.removePlayer(target);
				if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable"))
					SignCreator.updateAllSigns(game);
				p.sendMessage(RageMode.getLang().get("commands.kick.player-kicked", "%player%", target.getName(), "%game%", game));
			} else
				p.sendMessage(RageMode.getLang().get("commands.kick.player-not-play-currently"));
		}
		return;
	}
}
