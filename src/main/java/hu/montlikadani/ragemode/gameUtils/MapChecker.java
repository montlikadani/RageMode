package hu.montlikadani.ragemode.gameUtils;

import java.util.logging.Level;

import org.apache.commons.lang.Validate;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.utils.Debug;

public class MapChecker {

	private BaseGame game;
	private boolean isValid = false;
	private String message = "";

	public MapChecker(BaseGame game) {
		Validate.notNull(game, "Game can't be null");

		this.game = game;

		checkMapName();
		if (isValid)
			checkBasics();
		if (isValid)
			checkLobby();
		if (isValid)
			checkSpawns();
		if (isValid)
			checkArea();
	}

	public boolean isValid() {
		return isValid;
	}

	public String getMessage() {
		return message;
	}

	private void checkMapName() {
		if (!GameUtils.isGameExist(game.getName())) {
			message = RageMode.getLang().get("invalid-game", "%game%", game.getName());
			isValid = false;
			return;
		}

		if (!GameUtils.checkName(null, game.getName())) {
			message = RageMode.getLang().get("bad-ragemode-name");
			isValid = false;
			return;
		}

		isValid = true;
	}

	private void checkBasics() {
		isValid = false;

		if (!game.getWorld().isPresent()) {
			message = RageMode.getLang().get("game.worldname-not-set");
			return;
		}

		if (game.minPlayers < 1) {
			game.minPlayers = 2;
		}

		if (game.maxPlayers < game.minPlayers) {
			Debug.logConsole(Level.WARNING,
					"Minimum players can't greater than maximum players. Attempting to modify...");
			game.maxPlayers = game.minPlayers + 1;
		}

		isValid = true;
	}

	private void checkLobby() {
		if (game.getGameLobby().location == null) {
			message = RageMode.getLang().get("setup.lobby.not-set", "%game%", game.getName());
			isValid = false;
			return;
		}

		if (game.getGameLobby().location.getWorld() == null) {
			message = RageMode.getLang().get("game.worldname-not-set");
			isValid = false;
			return;
		}

		isValid = true;
	}

	private void checkSpawns() {
		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);
		if (gameSpawn == null) {
			message = RageMode.getLang().get("game.no-spawns-configured", "%game%", game.getName());
			isValid = false;
			return;
		}

		if (!gameSpawn.isReady()) {
			message = RageMode.getLang().get("game.too-few-spawns");
			isValid = false;
			return;
		}

		if (!game.randomSpawnForZombies && game.getGameType() == GameType.APOCALYPSE
				&& ((gameSpawn = game.getSpawn(GameZombieSpawn.class)) == null || !gameSpawn.isReady())) {
			message = RageMode.getLang().get("game.no-spawns-configured", "%game%", game.getName());
			isValid = false;
			return;
		}

		isValid = true;
	}

	private void checkArea() {
		isValid = false;

		for (GameArea map : GameAreaManager.getGameAreas().values()) {
			if (map.getGameName().equalsIgnoreCase(game.getName())) {
				isValid = true;
				break;
			}
		}

		if (!isValid) {
			message = RageMode.getLang().get("game.areas-not-set");
		}
	}
}
