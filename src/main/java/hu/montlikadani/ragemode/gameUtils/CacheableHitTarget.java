package hu.montlikadani.ragemode.gameUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.items.GameItems;
import hu.montlikadani.ragemode.utils.Utils;

public final class CacheableHitTarget {

	private final GameItems tool;
	private final Set<UUID> nearTargets = new HashSet<>();

	public CacheableHitTarget(GameItems tool, Entity target, int radius) {
		this.tool = tool;

		if (tool.getMetaName().isEmpty()) {
			return;
		}

		if (tool == GameItems.PRESSUREMINE) {
			nearTargets.add(target.getUniqueId());
			return;
		}

		if (target instanceof Arrow) {
			Arrow arrowTarget = (Arrow) target;

			if (arrowTarget.getShooter() instanceof Player) {
				nearTargets.add(((Player) arrowTarget.getShooter()).getUniqueId());
			}
		}

		if (radius > 0) {
			for (Entity near : Utils.getNearbyEntities(target, radius)) {
				nearTargets.add(near.getUniqueId());
			}
		}
	}

	public GameItems getTool() {
		return tool;
	}

	public Set<UUID> getNearTargets() {
		return nearTargets;
	}
}
