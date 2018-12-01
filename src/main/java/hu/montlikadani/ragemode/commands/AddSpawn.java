package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class AddSpawn extends RmCommand {

  public AddSpawn(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(RageMode.getLang().get("in-game-only"));
      return;
    }
    Player p = (Player) sender;
    if (!p.hasPermission("ragemode.admin.addspawn")) {
      p.sendMessage(RageMode.getLang().get("no-permission"));
      return;
    }
    if (args.length < 2) {
      p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm addspawn <gameName>"));
      return;
    }
    FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
    int i = 1;
    String path = "arenas." + args[1];
    if (!aFile.isSet(path)) {
      p.sendMessage(RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
      return;
    }

    String num = Integer.toString(i);
    while (aFile.isSet(path + ".spawns." + num)) {
      i++;
    }
    Location loc = p.getLocation();
    aFile.set(path + ".spawns", num);
    aFile.set(path + ".spawns." + num + ".world", p.getWorld().getName());
    aFile.set(path + ".spawns." + num + ".x", loc.getX());
    aFile.set(path + ".spawns." + num + ".y", loc.getY());
    aFile.set(path + ".spawns." + num + ".z", loc.getZ());
    aFile.set(path + ".spawns." + num + ".yaw", loc.getYaw());
    aFile.set(path + ".spawns." + num + ".pitch", loc.getPitch());
    try {
      aFile.save(RageMode.getInstance().getConfiguration().getArenasFile());
    } catch (IOException e) {
      e.printStackTrace();
      RageMode.getInstance().throwMsg();
    }
    p.sendMessage(RageMode.getLang().get("setup.spawn-set-success", "%number%", num, "%game%", args[1]));
    return;
  }
}
