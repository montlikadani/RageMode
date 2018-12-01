package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class ForceStart extends RmCommand {

  public ForceStart(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(RageMode.getLang().get("in-game-only"));
      return;
    }
    Player p = (Player) sender;
    if (!p.hasPermission("ragemode.admin.forcestart")) {
      p.sendMessage(RageMode.getLang().get("no-permission"));
      return;
    }
    if (args.length < 2) {
      p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
      return;
    }
    if (PlayerList.getPlayersGame(p) != null) {
      if (!PlayerList.isGameRunning(args[1])) {
        PlayerList.setGameRunning(args[1]);
        p.sendMessage(RageMode.getLang().get("commands.forcestart.game-start", "%game%", args[1]));
      } else {
        p.sendMessage(RageMode.getLang().get("game.running"));
      }
    } else {
      p.sendMessage(RageMode.getLang().get("commands.forcestart.not-enough-players"));
    }
  }
}
