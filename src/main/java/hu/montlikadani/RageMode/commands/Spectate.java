package hu.montlikadani.ragemode.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.items.LeaveGame;

public class Spectate extends RmCommand {

	public Spectate(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}

		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.spectate")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return;
		}

		String map = args[1];
		if (!GetGames.isGameExistent(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return;
		}

		if (!PlayerList.isGameRunning(map)) {
			sendMessage(p, RageMode.getLang().get("game.not-running"));
			return;
		}

		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
			return;
		}

		YamlConfiguration conf = RageMode.getInstance().getConfiguration().getCfg();

		if (PlayerList.addSpectatorPlayer(p)) {
			GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(map);
			gameSpawnGetter.randomSpawn(p);
			p.setAllowFlight(true);
			p.setFlying(true);
			p.setGameMode(GameMode.SPECTATOR);
			if (conf.contains("items.leavegameitem"))
				p.getInventory().setItem(conf.getInt("items.leavegameitem.slot"), LeaveGame.getItem());
		}
		return;
	}
}
