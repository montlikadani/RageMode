package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.holo.HoloHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HoloStats extends RmCommand {

    public HoloStats(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(RageMode.getLang().get("in-game-only"));
            return;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ragemode.admin.holo")) {
            p.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        if(RageMode.getInstance().getHologramAvailabe()) {
            if(args.length >= 2) {
                if(args[1].equalsIgnoreCase("add")) {
                    addHolo(p);
                }
                if(args[1].equalsIgnoreCase("remove")) {
                    removeHolo(p);
                }
                if(args[1].equalsIgnoreCase("tp")) {
                    tptoHolo(p);
                }
            } else
                p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm holo <add/remove/tp>"));
        } else
            p.sendMessage(RageMode.getLang().get("missing-dependencies", "%depend%", "HolographicDisplays"));
        return;
    }

    private void addHolo(Player p) {
        if(RageMode.getInstance().getHologramAvailabe())
            HoloHolder.addHolo(p.getLocation());
    }

    private void removeHolo(Player p) {
        if(RageMode.getInstance().getHologramAvailabe())
            HoloHolder.deleteHologram(p, HoloHolder.getClosest(p));
    }

    private void tptoHolo(Player p) {
        if(RageMode.getInstance().getHologramAvailabe())
            HoloHolder.teleporttoHologram(p, HoloHolder.getClosest(p));
    }
}
