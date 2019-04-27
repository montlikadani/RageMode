package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.signs.SignCreator;

public class PlayerJoin extends RmCommand {

	public PlayerJoin(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.join")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm join <gameName>"));
			return;
		}

		String map = args[1];
		if (!GetGames.isGameExistent(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return;
		}

		Configuration conf = RageMode.getInstance().getConfiguration();
		org.bukkit.inventory.PlayerInventory inv = p.getInventory();

		if (PlayerList.getStatus() == GameStatus.RUNNING) {
			if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				//GameUtils.savePlayerData(p, true);

				if (PlayerList.addSpectatorPlayer(p)) {
					GameSpawnGetter gameSpawnGetter = new GameSpawnGetter(PlayerList.getPlayersGame(p));
					gameSpawnGetter.randomSpawn(p);
					p.setAllowFlight(true);
					p.setFlying(true);
					p.setGameMode(GameMode.SPECTATOR);
					if (conf.getCfg().contains("items.leavegameitem"))
						inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());
				}
			} else {
				sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
				return;
			}
		} else {
			MapChecker mapChecker = new MapChecker(map);
			if (mapChecker.isValid()) {
				String path = "arenas." + map + ".lobby.";
				String world = conf.getArenasCfg().getString(path + "world");
				double lobbyX = conf.getArenasCfg().getDouble(path + "x");
				double lobbyY = conf.getArenasCfg().getDouble(path + "y");
				double lobbyZ = conf.getArenasCfg().getDouble(path + "z");
				double lobbyYaw = conf.getArenasCfg().getDouble(path + "yaw");
				double lobbyPitch = conf.getArenasCfg().getDouble(path + "pitch");

				Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
				lobbyLocation.setYaw((float) lobbyYaw);
				lobbyLocation.setPitch((float) lobbyPitch);
				if (PlayerList.addPlayer(p, map)) {
					GameUtils.savePlayerData(p);

					p.teleport(lobbyLocation);

					if (conf.getCfg().contains("items.leavegameitem"))
						inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

					if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
						inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

					GameUtils.broadcastToGame(map, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

					String title = conf.getCfg().getString("titles.join-game.title");
					String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
					if (title != null && subtitle != null) {
						title = title.replace("%game%", map);
						subtitle = subtitle.replace("%game%", map);
						Titles.sendTitle(p, conf.getCfg().getInt("titles.join-game.fade-in"),
								conf.getCfg().getInt("titles.join-game.stay"),
								conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
					}
					SignCreator.updateAllSigns(map);
				} else
					RageMode.logConsole(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", map));
			} else
				sendMessage(p, mapChecker.getMessage());
		}
		return;
	}
}
