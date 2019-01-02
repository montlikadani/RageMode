package hu.montlikadani.ragemode.commands;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.gameUtils.MapChecker;
import hu.montlikadani.ragemode.gameUtils.Titles;
import hu.montlikadani.ragemode.signs.SignCreator;

public class PlayerJoin extends RmCommand {

	public PlayerJoin(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.join")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 2) {
			p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm join <gameName>"));
			return;
		}
		String map = args[1];
		MapChecker mapChecker = new MapChecker(map);
		if (mapChecker.isValid()) {
			Configuration conf = RageMode.getInstance().getConfiguration();
			String world = conf.getArenasCfg().getString("arenas." + map + ".lobby.world");
			double lobbyX = conf.getArenasCfg().getDouble("arenas." + map + ".lobby.x");
			double lobbyY = conf.getArenasCfg().getDouble("arenas." + map + ".lobby.y");
			double lobbyZ = conf.getArenasCfg().getDouble("arenas." + map + ".lobby.z");
			double lobbyYaw = conf.getArenasCfg().getDouble("arenas." + map + ".lobby.yaw");
			double lobbyPitch = conf.getArenasCfg().getDouble("arenas." + map + ".lobby.pitch");

			Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
			lobbyLocation.setYaw((float) lobbyYaw);
			lobbyLocation.setPitch((float) lobbyPitch);

			if (PlayerList.addPlayer(p, map)) {
				org.bukkit.inventory.PlayerInventory inv = p.getInventory();
				PlayerList.oldLocations.addToBoth(p, p.getLocation());
				PlayerList.oldInventories.addToBoth(p, inv.getContents());
				PlayerList.oldArmor.addToBoth(p, inv.getArmorContents());
				PlayerList.oldHealth.addToBoth(p, p.getHealth());
				PlayerList.oldHunger.addToBoth(p, p.getFoodLevel());
				PlayerList.oldEffects.addToBoth(p, p.getActivePotionEffects());
				PlayerList.oldGameMode.addToBoth(p, p.getGameMode());
				PlayerList.oldDisplayName.addToBoth(p, p.getDisplayName());
				PlayerList.oldListName.addToBoth(p, p.getPlayerListName());

				inv.clear();
				p.teleport(lobbyLocation);
				p.setGameMode(GameMode.ADVENTURE);
				p.setHealth(20);
				p.setFoodLevel(20);
				inv.setHelmet(null);
				inv.setChestplate(null);
				inv.setLeggings(null);
				inv.setBoots(null);
				GameBroadcast.broadcastToGame(map, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));
				String title = conf.getCfg().getString("titles.join-game.title");
				String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
				if (title != null && !title.equals("") && subtitle != null && !subtitle.equals("")) {
					title = title.replace("%game%", map);
					subtitle = subtitle.replace("%game%", map);
					Titles.sendTitle(p, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
							conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
				}
				if (conf.getCfg().getBoolean("signs.enable"))
					SignCreator.updateAllSigns(map);
			} else
				RageMode.logConsole(Level.INFO, RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", map));
		} else
			p.sendMessage(mapChecker.getMessage());
		return;
	}
}
