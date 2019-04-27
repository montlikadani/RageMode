package hu.montlikadani.ragemode.gameUtils;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class BossMessenger {

	private String game;

	public BossMessenger(String game) {
		this.game = game;
	}

	public String getGame() {
		return game;
	}

	public void sendBossBar(String message, String inGamePlayer, BarStyle style) {
		if (Utils.getVersion().contains("1.8")) {
			RageMode.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			return;
		}

		final BossBar boss = Bukkit.createBossBar(message == null ? "" : message, BarColor.RED, style != null ? style : BarStyle.SOLID);
		boss.removePlayer(Bukkit.getPlayer(UUID.fromString(inGamePlayer)));
		boss.addPlayer(Bukkit.getPlayer(UUID.fromString(inGamePlayer)));

		for (int i = 1; i <= 6; ++i) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (boss.getProgress() >= 0.2D)
						boss.setProgress(boss.getProgress() - 0.2D);
					else
						boss.removeAll();
				}
			}, (long) (20 * i));
		}
	}
}
