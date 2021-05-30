package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "lobbydelay",
		permission = "ragemode.admin.setlobbydelay",
		params = "<gameName> <seconds>",
		desc = "Lobby waiting time in seconds",
		playerOnly = true)
public final class lobbydelay implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm lobbydelay <gameName> <seconds>"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		if (!plugin.getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
			sendMessage(sender,
					RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return false;
		}

		if (!plugin.getConfiguration().getArenasCfg().contains("arenas." + args[1] + ".lobby")) {
			sendMessage(sender, RageMode.getLang().get("setup.lobby.not-set", "%game%", args[1]));
			return false;
		}

		java.util.Optional<Integer> opt = Utils.tryParseInt(args[2]);
		if (!opt.isPresent()) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[2]));
			return false;
		}

		if (game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.running"));
			return false;
		}

		game.getGameLobby().lobbyTime = opt.get();

		plugin.getConfiguration().getArenasCfg().set("arenas." + args[1] + ".lobbydelay", opt.get());
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());
		sendMessage(sender, RageMode.getLang().get("setup.success"));

		return true;
	}
}
