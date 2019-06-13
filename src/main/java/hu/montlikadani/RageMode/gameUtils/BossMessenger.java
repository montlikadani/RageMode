package hu.montlikadani.ragemode.gameUtils;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.MinecraftVersion.Version;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class BossMessenger {

	private String game;

	public BossMessenger(String game) {
		this.game = game;
	}

	public String getGame() {
		return game;
	}

	public void sendBossBar(String message, Player p, BarStyle style, BarColor color) {
		if (Version.isCurrentEqualOrLower(Version.v1_8_R3)) {
			RageMode.logConsole(Level.WARNING, "[RageMode] Your server version does not support for Bossbar. Only 1.9+");
			return;
		}

		final BossBar boss = Bukkit.createBossBar(message == null ? "" : message, color != null ? color : BarColor.BLUE,
				style != null ? style : BarStyle.SOLID);
		boss.removePlayer(p);
		boss.addPlayer(p);

		for (int i = 1; i <= 6; ++i) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString()))
						boss.removePlayer(p);

					if (boss.getProgress() >= 0.2D)
						boss.setProgress(boss.getProgress() - 0.2D);
					else
						boss.removeAll();
				}
			}, (long) (20 * i));
		}
	}
}
