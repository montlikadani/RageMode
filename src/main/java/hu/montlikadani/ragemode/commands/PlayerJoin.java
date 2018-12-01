package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.toolbox.GameBroadcast;
import hu.montlikadani.ragemode.toolbox.MapChecker;

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
    MapChecker mapChecker = new MapChecker(args[1]);
    if (mapChecker.isValid()) {
      String world = RageMode.getInstance().getConfiguration().getArenasCfg().getString("arenas." + args[1] + ".lobby.world");
      double lobbyX = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble("arenas." + args[1] + ".lobby.x");
      double lobbyY = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble("arenas." + args[1] + ".lobby.y");
      double lobbyZ = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble("arenas." + args[1] + ".lobby.z");
      double lobbyYaw = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble("arenas." + args[1] + ".lobby.yaw");
      double lobbyPitch = RageMode.getInstance().getConfiguration().getArenasCfg().getDouble("arenas." + args[1] + ".lobby.pitch");

      Location lobbyLocation = new Location(Bukkit.getWorld(world), lobbyX, lobbyY, lobbyZ);
      lobbyLocation.setYaw((float) lobbyYaw);
      lobbyLocation.setPitch((float) lobbyPitch);

      if (PlayerList.addPlayer(p, args[1])) {
        PlayerList.oldLocations.addToBoth(p, p.getLocation());
        PlayerList.oldInventories.addToBoth(p, p.getInventory().getContents());
        PlayerList.oldArmor.addToBoth(p, p.getInventory().getArmorContents());
        PlayerList.oldHealth.addToBoth(p, p.getHealth());
        PlayerList.oldHunger.addToBoth(p, p.getFoodLevel());
        PlayerList.oldGameMode.addToBoth(p, p.getGameMode());

        p.getInventory().clear();
        p.teleport(lobbyLocation);
        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        GameBroadcast.broadcastToGame(args[1], RageMode.getLang().get("game.player-joined", "%player%", p.getName()));
        SignCreator.updateAllSigns(args[1]);
      } else {
        Bukkit.getLogger().info(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", args[1]));
      }
    } else {
      p.sendMessage(mapChecker.getMessage());
    }
    return;
  }
}
