package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.toolbox.GameBroadcast;
import hu.montlikadani.ragemode.toolbox.GetGames;

public class Reload extends RmCommand {

  public Reload(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("ragemode.admin.reload")) {
      sender.sendMessage(RageMode.getLang().get("no-permission"));
      return;
    }
    String[] games = GetGames.getGameNames();
    for (String game : games) {
      if (PlayerList.isGameRunning(game)) {
        GameBroadcast.broadcastToGame(game, "&cThe game has stopped because we reloading the plugin and need to stop the game. Sorry!");
      }
    }
    StopGame.stopAllGames();

    RageMode.getInstance().getConfiguration().loadConfig();
    RageMode.getLang().loadLanguage();

    SignConfiguration.initSignConfiguration();

    for (String game : games) {
      SignCreator.updateAllSigns(game);
    }
    if (RageMode.getInstance().getHologramAvailabe()) {
      HoloHolder.initHoloHolder();
    }

    sender.sendMessage(RageMode.getLang().get("commands.reload.success"));
    return;
  }
}
