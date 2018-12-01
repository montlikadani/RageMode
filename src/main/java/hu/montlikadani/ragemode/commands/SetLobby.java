package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetLobby extends RmCommand {

    public SetLobby(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(RageMode.getLang().get("in-game-only"));
            return;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ragemode.admin.setlobby")) {
            p.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        if(args.length >= 2) {
            String gameName = args[1];
            if(!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + gameName))
                p.sendMessage(RageMode.getLang().get("setup.not-set-yet"));
            else {
                String path = "arenas." + gameName + ".lobby";
                Location loc = p.getLocation();
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".world", p.getWorld().getName());
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".x", loc.getX());
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".y", loc.getY());
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".z", loc.getZ());
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".yaw", loc.getYaw());
                RageMode.getInstance().getConfiguration().getArenasCfg().set(path + ".pitch", loc.getPitch());
                try {
                    RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
                } catch(IOException e) {
                    e.printStackTrace();
                    RageMode.getInstance().throwMsg();
                }
                p.sendMessage(RageMode.getLang().get("setup.lobby-set-success", "%game%", gameName));
            }
        } else
            p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
        return;
    }
}
