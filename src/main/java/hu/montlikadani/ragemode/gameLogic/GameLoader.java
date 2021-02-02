package hu.montlikadani.ragemode.gameLogic;

import java.util.Iterator;
import java.util.Timer;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.gameUtils.GameUtils.ActionMessageType;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

public class GameLoader {

	private Game game;

	public GameLoader(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public boolean startGame() {
		if (!canTeleport()) {
			return false; // stop starting the game if the game not set up correctly
		}

		Utils.callEvent(new RMGameStartEvent(game));

		game.setGameRunning(true);
		game.setStatus(GameStatus.RUNNING);

		new Timer().scheduleAtFixedRate(new GameTimer(game), 0, 60 * 20L);

		SignCreator.updateAllSigns(game.getName());

		for (PlayerManager pm : game.getPlayers()) {
			Player p = pm.getPlayer();

			GameUtils.addGameItems(p, true);
			if (ConfigValues.isHidePlayerNameTag()) {
				p.setCustomNameVisible(false);
			}
			GameUtils.runCommands(p, game, "start");
			GameUtils.sendActionMessage(p, game, ActionMessageType.START, ActionMessageType.asBossbar());
			GameUtils.sendActionMessage(p, game, ActionMessageType.START, ActionMessageType.asActionbar());
			LobbyShop.buyElements(p);

			java.util.UUID uuid = p.getUniqueId();
			if (!RageScores.getPlayerPointsMap().containsKey(uuid)) {
				RageScores.getPlayerPointsMap().put(uuid, new PlayerPoints(uuid));
			}
		}

		return true;
	}

	private boolean canTeleport() {
		if (game == null) {
			return false;
		}

		if (game.getGameType() == hu.montlikadani.ragemode.gameUtils.GameType.APOCALYPSE
				&& game.getSpawn(GameZombieSpawn.class) != null && !game.getSpawn(GameZombieSpawn.class).isReady()) {
			GameUtils.broadcastToGame(game, RageMode.getLang().get("game.not-set-up"));
			return false;
		}

		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);
		if (gameSpawn != null && gameSpawn.isReady()) {
			GameUtils.teleportPlayersToGameSpawns(gameSpawn);
			return true;
		}

		GameUtils.broadcastToGame(game, RageMode.getLang().get("game.not-set-up"));

		for (Iterator<PlayerManager> it = game.getPlayers().iterator(); it.hasNext();) {
			game.removePlayer(it.next().getPlayer());
		}

		return false;
	}
}
