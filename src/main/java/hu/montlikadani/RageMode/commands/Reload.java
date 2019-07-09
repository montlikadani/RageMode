package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;

public class Reload extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd) {
		if (!hasPerm(sender, "ragemode.admin.reload")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}
		String game = GetGames.getGameNames()[GetGames.getConfigGamesCount() - 1];
		if (PlayerList.isGameRunning(game))
			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.game-stopped-for-reload"));

		StopGame.stopAllGames();

		plugin.getConfiguration().loadConfig();
		RageMode.getLang().loadLanguage();

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable"))
			SignConfiguration.initSignConfiguration();

		plugin.loadListeners();

		SignCreator.updateAllSigns(game);

		if (plugin.isHologramEnabled())
			HoloHolder.initHoloHolder();

		sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
		return false;
	}
}
