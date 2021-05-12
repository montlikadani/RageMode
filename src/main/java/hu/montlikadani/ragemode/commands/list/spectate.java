package hu.montlikadani.ragemode.commands.list;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "spectate",
		permission = { "ragemode.spectate", "ragemode.help.playercommands" },
		desc = "Joins to the specified game in spectator mode",
		params = "<gameName>",
		playerOnly = true)
public final class spectate implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return false;
		}

		if (!game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.not-running"));
			return false;
		}

		Player player = (Player) sender;

		if (GameUtils.isPlayerPlaying(player)) {
			sendMessage(player, RageMode.getLang().get("game.player-not-switch-spectate"));
			return false;
		}

		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);

		if (gameSpawn != null && gameSpawn.haveAnySpawn() && game.addPlayer(player, true)) {
			player.teleport(gameSpawn.getRandomSpawn());

			player.setAllowFlight(true);
			player.setFlying(true);
			player.setGameMode(GameMode.SPECTATOR);

			ItemHandler leaveItem = Items.getLobbyItem(1);
			if (leaveItem != null) {
				player.getInventory().setItem(leaveItem.getSlot(), leaveItem.get());
			}

			return true;
		}

		return false;
	}
}
