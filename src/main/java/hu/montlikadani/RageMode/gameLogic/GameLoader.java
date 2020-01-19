package hu.montlikadani.ragemode.gameLogic;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameLoader {

	private Game game;

	public GameLoader(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public boolean startGame() {
		if (!checkTeleport()) {
			return false; // stop starting the game if the game not set up correctly
		}

		List<PlayerManager> l = game.getPlayersFromList();
		Utils.callEvent(new RMGameStartEvent(game, l));

		String name = game.getName();

		game.setGameRunning();
		GameUtils.setStatus(name, GameStatus.RUNNING);

		int time = !RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + name + ".gametime")
				? ConfigValues.getDefaultGameTime() < 0 ? 5 : ConfigValues.getDefaultGameTime()
				: GetGames.getGameTime(name);

		GameTimer gameTimer = new GameTimer(game, time * 60);
		Timer t = new Timer();
		t.scheduleAtFixedRate(gameTimer, 0, 60 * 20L);

		SignCreator.updateAllSigns(name);

		for (PlayerManager pm : l) {
			Player p = pm.getPlayer();

			GameUtils.addGameItems(p, true);
			if (ConfigValues.isHidePlayerNameTag()) {
				p.setCustomNameVisible(false);
			}
			GameUtils.runCommands(p, name, "start");
			GameUtils.sendBossBarMessages(p, name, "start");
			GameUtils.sendActionBarMessages(p, name, "start");
		}

		return true;
	}

	private boolean checkTeleport() {
		if (game == null) {
			return false;
		}

		GameSpawn gameSpawn = GameUtils.getGameSpawn(game);
		if (gameSpawn.isGameReady()) {
			GameUtils.teleportPlayersToGameSpawns(gameSpawn);
			return true;
		}

		GameUtils.broadcastToGame(game, RageMode.getLang().get("game.not-set-up"));

		for (Iterator<PlayerManager> it = game.getPlayersFromList().iterator(); it.hasNext();) {
			game.removePlayer(it.next().getPlayer());
		}

		return false;
	}
}
