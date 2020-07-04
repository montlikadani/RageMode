package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class GameTimer extends TimerTask {

	private Game game;
	private final int time;
	private int timer;

	private int timeElapsed = -1;
	private int zombieSpawnAmount = 5;
	private boolean firstZombieSpawned = false;

	public GameTimer(Game game, int time) {
		this.game = game;
		this.time = time;
		timer = time;
	}

	public Game getGame() {
		return game;
	}

	public int getGameTime() {
		return time;
	}

	public int getCurrentGameTime() {
		return timer;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning()) {
				cancel();
				return;
			}

			if (game.getPlayers().size() < 2) {
				GameUtils.stopGame(game.getName());
				cancel();
				return;
			}

			// Broadcast time message should be in this place, before counting
			for (String val : RageMode.getInstance().getConfiguration().getCfg()
					.getStringList("game.values-to-send-game-end-broadcast")) {
				if (timer == Integer.parseInt(val)) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(timer)));
					break;
				}
			}

			if (game.getGameType().equals(GameType.APOCALYPSE)) {
				if (!firstZombieSpawned) {
					if (ConfigValues.getDelayBeforeFirstZombiesSpawn() > 0) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> {
							GameUtils.spawnZombies(game, zombieSpawnAmount);
							firstZombieSpawned = true;
						}, ConfigValues.getDelayBeforeFirstZombiesSpawn() * 20);
					} else {
						GameUtils.spawnZombies(game, zombieSpawnAmount);
					}

				} else if (timeElapsed == -1) { // wait for the scheduler task
					for (PlayerManager pm : game.getPlayersFromList()) {
						org.bukkit.Location loc = pm.getPlayer().getLocation();
						if (GameAreaManager.inArea(loc)) {
							if (ConfigValues.isWaitForNextSpawnAfterZombiesAreDead()
									&& !GameAreaManager.getAreaByLocation(loc)
											.getEntities(GameAreaManager.getAreaByLocation(loc).getEntities())
											.filter(e -> e instanceof org.bukkit.entity.Zombie)
											.collect(Collectors.toList()).isEmpty()) {
								break;
							}

							if (ConfigValues.getDelayAfterNextZombiesSpawning() > 0) {
								timeElapsed = Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
										() -> {
											GameUtils.spawnZombies(game, zombieSpawnAmount);
											timeElapsed = -1;
										}, ConfigValues.getDelayAfterNextZombiesSpawning() * 20);
							} else {
								GameUtils.spawnZombies(game, zombieSpawnAmount);
							}

							zombieSpawnAmount += 5;
						}
					}
				}
			}

			for (ActionMessengers ac : game.getActionMessengers()) {
				if (game.isSpectatorInList(ac.getPlayer())) {
					continue;
				}

				ac.setScoreboard(timer);
				ac.setTabList(timer);
				ac.setTeam();
			}

			if (timer == 0) {
				cancel();
				GameUtils.stopGame(game.getName());
				return;
			}

			timer--;
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.forceStopGame(game);
			return;
		}
	}
}
