package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameLogic.base.ZombieGame;
import hu.montlikadani.ragemode.gameLogic.base.zombieSpawn.IZombieSpawner;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.handler.PressureMine;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.Utils;

public final class GameTimer extends TimerTask {

	private final BaseGame game;
	private final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private int timer, nextNotifyTime = 0;

	public GameTimer(BaseGame game) {
		this.game = game;
		timer = game.gameTime;
	}

	public BaseGame getGame() {
		return game;
	}

	/**
	 * @return returns the current game time
	 */
	public int getCurrentGameTime() {
		return timer;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong
			if (!game.isRunning() || !rm.isEnabled()) {
				cancel();
				return;
			}

			if (game.getGameType() != GameType.APOCALYPSE && !ConfigValues.isDeveloperMode()
					&& game.getPlayers().size() < 2) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("not-enough-players"));
				GameUtils.stopGame(game);
				cancel();
				return;
			}

			for (PlayerManager pm : game.getPlayers()) {
				pm.tickMovement();
			}

			// Broadcast time message should be in this place, before counting
			for (int value : ConfigValues.getGameEndBroadcast()) {
				if (timer == value) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(timer)));
					break;
				}
			}

			if (ConfigValues.isNotifySpectatorsToLeave() && ConfigValues.getSpecTimeBetweenMessageSending() > 0
					&& (nextNotifyTime == 0 || nextNotifyTime >= timer)) {
				for (PlayerManager spec : game.getSpectatorPlayers()) {
					Misc.sendMessage(spec.getPlayer(), RageMode.getLang().get("game.spec-player-leave-notify"));
				}

				nextNotifyTime = timer - ConfigValues.getSpecTimeBetweenMessageSending();
			}

			if (game.getGameType() == GameType.APOCALYPSE) {
				IZombieSpawner zombieSpawner = ((ZombieGame) game).getZombieSpawnerManager();

				if (zombieSpawner.canSpawn(timer)
						&& (zombieSpawner.isFirstZombieSpawned() || zombieSpawner.canSpawnFirstZombie(timer))) {
					zombieSpawner.spawnZombies(timer);
				}

				// Trash-made solution for entity move ticking
				// Only fire this if the software is not paper or forks
				// See PaperListener#entityMoveEvent for better performance
				if (!rm.isPaper()) {
					IGameSpawn spawn = game.getSpawn(GameSpawn.class);

					if (spawn != null && spawn.haveAnySpawn()) {
						GameAreaManager.getAreaByLocation(spawn.getSpawnLocations().get(0)).ifPresent(area -> {
							e: for (final Entity f : area.getEntities(e -> !e.isDead(),
									org.bukkit.entity.EntityType.ZOMBIE)) {
								for (PressureMine mines : GameListener.PRESSUREMINES) {
									for (Location mineLoc : mines.getMines()) {
										if (mineLoc.getBlock().getType() == f.getLocation().getBlock().getType()) {
											hu.montlikadani.ragemode.utils.SchedulerUtil.submitSync(() -> {
												GameListener.explodeMine(f.getLocation());
												return 1;
											});

											break e;
										}
									}
								}
							}
						});
					}
				}
			}

			for (ActionMessengers ac : game.getActionMessengers()) {
				ac.setScoreboard(timer);
				ac.setTabList(timer);
				ac.setPlayerName();
			}

			if (timer == 0) {
				cancel();
				GameUtils.stopGame(game);
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
