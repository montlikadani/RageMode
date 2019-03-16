package hu.montlikadani.ragemode.gameUtils;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import hu.montlikadani.ragemode.RageMode;

public class BossUtils {

	private String message;

	public BossUtils(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void sendBossBar(String game, String inGamePlayer, BarStyle style) {
		if (RageMode.getVersion().contains("1.8")) {
			RageMode.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			return;
		}

		BossBar boss = Bukkit.createBossBar(message, BarColor.RED, style != null ? style : BarStyle.SOLID);
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
