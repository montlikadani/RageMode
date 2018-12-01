package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.toolbox.GetGames;

public class RemoveGame extends RmCommand {

  public RemoveGame(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(RageMode.getLang().get("in-game-only"));
      return;
    }
    Player p = (Player) sender;
    if (!p.hasPermission("ragemode.admin.removegame")) {
      p.sendMessage(RageMode.getLang().get("no-permission"));
      return;
    }
    if (args.length >= 2) {
      String game = args[1];

      if (!GetGames.isGameExistent(game)) {
        p.sendMessage(RageMode.getLang().get("setup.removed-non-existent-game"));
        return;
      } else {
        if (PlayerList.isGameRunning(game)) {
          p.sendMessage(RageMode.getLang().get("game.running"));
          return;
        } else {
          PlayerList.deleteGameFromList(game);

          RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + game, null);
          try {
            RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
          } catch (IOException e) {
            e.printStackTrace();
          }

          p.sendMessage(RageMode.getLang().get("setup.success-removed", "%game%", game));
        }
      }
    } else {
      p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm removegame <gameName>"));
    }
    return;
  }
}
