package hu.montlikadani.ragemode.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ServerVersion;

public class BossbarManager {

	private final Map<UUID, BossBar> bossbarTask = new HashMap<>();

	public Map<UUID, BossBar> getBossbarMap() {
		return bossbarTask;
	}

	public void createBossbar(final Player player, String message, BarColor color, BarStyle style) {
		if (ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
			return;
		}

		Validate.notNull(player, "Player can't be null!");

		if (bossbarTask.containsKey(player.getUniqueId())) {
			return;
		}

		if (color == null) {
			color = BarColor.BLUE;
		}

		if (style == null) {
			style = BarStyle.SOLID;
		}

		final BossBar boss = Bukkit.createBossBar(message, color, style);

		boss.removePlayer(player);
		boss.addPlayer(player);
		boss.setVisible(true);

		bossbarTask.put(player.getUniqueId(), boss);
	}

	public void showBossbar(Player player) {
		showBossbar(player, 4);
	}

	public void showBossbar(final Player player, int secAfterRemove) {
		Validate.notNull(player, "Player can't be null!");

		final BossBar boss = bossbarTask.get(player.getUniqueId());
		if (boss == null) {
			return;
		}

		if (secAfterRemove < 0) {
			secAfterRemove = 4;
		}

		for (int i = 1; i <= secAfterRemove; ++i) {
			if (!GameUtils.isPlayerPlaying(player)) {
				removeBossbar(player);
				break;
			}

			Bukkit.getScheduler().runTaskLater(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(RageMode.class), () -> {
				if (!GameUtils.isPlayerPlaying(player) || boss.getProgress() <= 0.2D) {
					removeBossbar(player);
					return;
				}

				if (!boss.isVisible()) {
					boss.setVisible(true);
				}

				boss.setProgress(boss.getProgress() - 0.2D);
			}, i * 20L);
		}
	}

	public void removeBossbar(Player player) {
		Validate.notNull(player, "Player can't be null!");

		BossBar boss = bossbarTask.remove(player.getUniqueId());
		if (boss == null) {
			return;
		}

		boss.removePlayer(player);
		boss.setVisible(false);
	}
}
