package hu.montlikadani.RageMode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.RageMode.RageMode;
import hu.montlikadani.RageMode.gameLogic.PlayerList;
import hu.montlikadani.RageMode.holo.HoloHolder;
import hu.montlikadani.RageMode.signs.SignConfiguration;
import hu.montlikadani.RageMode.signs.SignCreator;
import hu.montlikadani.RageMode.toolbox.GameBroadcast;
import hu.montlikadani.RageMode.toolbox.GetGames;

public class Reload extends RmCommand {

	public Reload(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.reload")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		String[] games = GetGames.getGameNames();
		for (String game : games) {
			if (PlayerList.isGameRunning(game))
				GameBroadcast.broadcastToGame(game, "&cThe game has stopped because we reloading the plugin and need to stop the game. Sorry!");
		}
		StopGame.stopAllGames();

		RageMode.getInstance().getConfiguration().loadConfig();
		RageMode.getLang().loadLanguage();

		SignConfiguration.initSignConfiguration();

		for (String game : games) {
			SignCreator.updateAllSigns(game);
		}
		if (RageMode.getInstance().getHologramAvailabe())
			HoloHolder.initHoloHolder();

		sender.sendMessage(RageMode.getLang().get("commands.reload.success"));
		return;
	}
}
