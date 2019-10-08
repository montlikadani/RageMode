package hu.montlikadani.ragemode.gameLogic;

import java.util.Iterator;
import java.util.Timer;
import java.util.logging.Level;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.BossMessenger;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameLoader {

	private Game game;

	public GameLoader(Game game) {
		this.game = game;

		game.removeLobbyTimer();

		if (!checkTeleport()) {
			return; // stop starting the game if the game not set up correctly
		}

		RMGameStartEvent RMGameStartEvent = new RMGameStartEvent(game, game.getPlayersFromList());
		Utils.callEvent(RMGameStartEvent);

		String name = game.getName();

		game.setGameRunning(name);
		GameUtils.setStatus(name, GameStatus.RUNNING);

		Configuration conf = RageMode.getInstance().getConfiguration();

		int time = !conf.getArenasCfg().isSet("arenas." + name + ".gametime")
				? conf.getCV().getGameTime() < 0 ? 5 * 60 : conf.getCV().getGameTime() * 60
				: GetGames.getGameTime(name) * 60;

		GameTimer gameTimer = new GameTimer(game, time);
		gameTimer.loadModules();

		Timer t = new Timer();
		t.scheduleAtFixedRate(gameTimer, 0, 60 * 20L);

		GameUtils.runCommandsForAll(name, "start");
		SignCreator.updateAllSigns(name);

		for (PlayerManager pm : game.getPlayersFromList()) {
			Player p = pm.getPlayer();

			GameUtils.addGameItems(p, true);

			if (conf.getArenasCfg().getBoolean("arenas." + name + ".bossbar") || conf.getCV().isBossbarEnabled()) {
				if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
					String bossMessage = conf.getCV().getBossbarMsg();

					if (!bossMessage.equals("")) {
						bossMessage = bossMessage.replace("%game%", name);
						bossMessage = bossMessage.replace("%player%", p.getName());
						bossMessage = Utils.colors(bossMessage);

						new BossMessenger(name).sendBossBar(bossMessage, p,
								BarStyle.valueOf(conf.getCV().getBossbarStyle()),
								BarColor.valueOf(conf.getCV().getBossbarColor()));
					}
				} else
					Debug.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			}

			GameUtils.sendActionBarMessages(p, name, "start");
		}
	}

	private boolean checkTeleport() {
		GameSpawnGetter gameSpawnGetter = GameUtils.getGameSpawnByGame(game);
		if (gameSpawnGetter.isGameReady()) {
			GameUtils.teleportPlayersToGameSpawns(gameSpawnGetter);
			return true;
		}

		GameUtils.broadcastToGame(game.getName(), RageMode.getLang().get("game.not-set-up"));

		for (Iterator<PlayerManager> it = game.getPlayersFromList().iterator(); it.hasNext();) {
			game.removePlayer(it.next().getPlayer());
		}

		return false;
	}

	public Game getGame() {
		return game;
	}
}
