package hu.montlikadani.ragemode.commands;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.signs.SignCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerLeave extends RmCommand {

    public PlayerLeave(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(RageMode.getLang().get("in-game-only"));
            return;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ragemode.leave")) {
            p.sendMessage(RageMode.getLang().get("no-permission"));
            return;
        }
        PlayerList.removePlayerSynced(p);
        PlayerList.removePlayer(p);
        SignCreator.updateAllSigns(PlayerList.getPlayersGame(p));
        return;
    }
}
