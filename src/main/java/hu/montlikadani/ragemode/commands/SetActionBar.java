package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetActionBar extends RmCommand {

    public SetActionBar(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(RageMode.getLang().get("in-game-only"));
            return;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ragemode.admin.setactionbar")) {
            p.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        if(args.length >= 3) {
            if(!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
                p.sendMessage(RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
                return;
            }
            RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".actionbar", Boolean.parseBoolean(args[2]));
            try {
                RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
            } catch(IOException e) {
                e.printStackTrace();
            }
            p.sendMessage(RageMode.getLang().get("setup.success"));
        } else
            p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm actionbar <gameName> <true|false>"));
        return;
    }
}
