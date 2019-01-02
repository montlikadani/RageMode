package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameBroadcast;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holo.HoloHolder;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;

public class Reload extends RmCommand {

	public Reload(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.reload")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		String[] games = GetGames.getGameNames();
		for (String game : games) {
			if (PlayerList.isGameRunning(game))
				GameBroadcast.broadcastToGame(game, RageMode.getLang().get("game.game-stopped-for-reload"));
		}
		StopGame.stopAllGames();

		RageMode.getInstance().getConfiguration().loadConfig();
		RageMode.getLang().loadLanguage();

		if (RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable")) {
			SignConfiguration.initSignConfiguration();

			for (String game : games) {
				SignCreator.updateAllSigns(game);
			}
		}
		if (RageMode.getInstance().getHologramAvailable())
			HoloHolder.initHoloHolder();

		sender.sendMessage(RageMode.getLang().get("commands.reload.success"));
		return;
	}
}
