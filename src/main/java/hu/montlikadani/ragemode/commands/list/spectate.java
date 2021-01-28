package hu.montlikadani.ragemode.commands.list;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.Items;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "spectate", permission = "ragemode.spectate", playerOnly = true)
public class spectate implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		String map = args[1];
		if (!GameUtils.isGameExist(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		Game game = GameUtils.getGame(map);

		if (!game.isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.not-running"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
			return false;
		}

		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);
		if (gameSpawn != null && gameSpawn.haveAnySpawn() && game.addSpectatorPlayer(p)) {
			p.teleport(gameSpawn.getRandomSpawn());

			p.setAllowFlight(true);
			p.setFlying(true);
			p.setGameMode(GameMode.SPECTATOR);
			if (Items.getLobbyItem(1) != null) {
				p.getInventory().setItem(Items.getLobbyItem(1).getSlot(), Items.getLobbyItem(1).get());
			}

			return true;
		}

		return false;
	}
}
