package hu.montlikadani.ragemode.gameLogic;

import java.util.Iterator;
import java.util.Timer;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

public final class GameLoader {

	private Game game;

	public GameLoader(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	public boolean startGame() {
		if (!canTeleport()) {
			return false; // return from starting the game if not set up correctly
		}

		game.setRunning(true);
		game.setStatus(GameStatus.RUNNING);

		Utils.callEvent(new RMGameStartEvent(game));

		new Timer().scheduleAtFixedRate(new GameTimer(game), 0, 60 * 20L);

		SignCreator.updateAllSigns(game.getName());

		for (PlayerManager pm : game.getPlayers()) {
			Player player = pm.getPlayer();

			GameUtils.addGameItems(player, true);

			if (ConfigValues.isHidePlayerNameTag()) {
				player.setCustomNameVisible(false);
			}

			GameUtils.runCommands(player, game, RewardManager.InGameCommand.CommandType.JOIN);

			GameUtils.sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.START,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.BOSSBAR);
			GameUtils.sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.START,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.ACTIONBAR);

			LobbyShop.buyElements(player);

			if (!RageScores.getPlayerPointsMap().containsKey(pm.getUniqueId())) {
				RageScores.getPlayerPointsMap().put(pm.getUniqueId(), new PlayerPoints(pm.getUniqueId()));
			}
		}

		return true;
	}

	private boolean canTeleport() {
		if (game == null) {
			return false;
		}

		if (game.getGameType() == hu.montlikadani.ragemode.gameUtils.GameType.APOCALYPSE) {
			IGameSpawn zombieSpawn = game.getSpawn(GameZombieSpawn.class);

			if (zombieSpawn != null && !zombieSpawn.isReady()) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.not-set-up"));
				return false;
			}
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
