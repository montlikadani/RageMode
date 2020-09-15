package hu.montlikadani.ragemode.managers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class BossbarManager {

	private final Map<Player, BossBar> bossbarTask = new HashMap<>();

	public Map<Player, BossBar> getBossbarMap() {
		return bossbarTask;
	}

	public void createBossbar(final Player p, String message, BarColor color, BarStyle style) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			return;
		}

		Validate.notNull(p, "Player can't be null!");

		if (color == null) {
			color = BarColor.BLUE;
		}

		if (style == null) {
			style = BarStyle.SOLID;
		}

		if (bossbarTask.containsKey(p)) {
			return;
		}

		final BossBar boss = Bukkit.createBossBar(message, color, style);
		if (boss == null) {
			return;
		}

		if (boss.getPlayers().contains(p)) {
			boss.removePlayer(p);
		}

		boss.addPlayer(p);
		boss.setVisible(true);

		bossbarTask.put(p, boss);
	}

	public void showBossbar(Player p) {
		showBossbar(p, 4);
	}

	public void showBossbar(final Player p, int secAfterRemove) {
		Validate.notNull(p, "Player can't be null!");

		final BossBar boss = bossbarTask.getOrDefault(p, null);
		if (boss == null) {
			return;
		}

		if (secAfterRemove < 0) {
			secAfterRemove = 4;
		}

		for (int i = 1; i <= secAfterRemove; ++i) {
			if (!GameUtils.isPlayerPlaying(p)) {
				removeBossbar(p);
				break;
			}

			Bukkit.getScheduler().runTaskLater(RageMode.getInstance(), () -> {
				if (!GameUtils.isPlayerPlaying(p) || boss.getProgress() <= 0.2D) {
					removeBossbar(p);
					return;
				}

				if (!boss.isVisible()) {
					boss.setVisible(true);
				}

				boss.setProgress(boss.getProgress() - 0.2D);
			}, i * 20L);
		}
	}

	public void removeBossbar(Player p) {
		Validate.notNull(p, "Player can't be null!");

		BossBar boss = bossbarTask.getOrDefault(p, null);
		if (boss == null) {
			return;
		}

		if (boss.getPlayers().contains(p)) {
			boss.removePlayer(p);
		}

		boss.setVisible(false);
		bossbarTask.remove(p);
	}
}
