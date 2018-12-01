package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyDelay extends RmCommand {

    public SetLobbyDelay(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(RageMode.getLang().get("in-game-only"));
            return;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ragemode.admin.setlobbydelay")) {
            p.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        if(args.length >= 3) {
            if(!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
                p.sendMessage(RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
                return;
            }
            if(isInt(args[2])) {
                RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".lobbydelay", Integer.parseInt(args[2]));
                p.sendMessage(RageMode.getLang().get("setup.success"));
            } else
                p.sendMessage(RageMode.getLang().get("not-a-number", "%number%", args[2]));
        } else
            p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm lobbydelay <gameName> <seconds>"));
        return;
    }

    private boolean isInt(String string) {
        try {
            Integer.parseInt(string);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
}
