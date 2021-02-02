package hu.montlikadani.ragemode.gameUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.scores.KilledWith;
import hu.montlikadani.ragemode.utils.Utils;

public final class CacheableHitTarget {

	private Entity target;
	private KilledWith tool;

	private final Set<UUID> nearTargets = new HashSet<>();

	public CacheableHitTarget(Entity target, KilledWith tool) {
		this.target = target;
		this.tool = tool;
	}

	public Entity getTarget() {
		return target;
	}

	public KilledWith getTool() {
		return tool;
	}

	public Set<UUID> getNearTargets() {
		return nearTargets;
	}

	public void add(int radius) {
		if (tool.getMetaName().isEmpty()) {
			return;
		}

		if (tool == KilledWith.PRESSUREMINE && target instanceof Player) {
			nearTargets.add(target.getUniqueId());
			return;
		}

		if (target instanceof Arrow && ((Arrow) target).getShooter() instanceof Player) {
			nearTargets.add(((Player) ((Arrow) target).getShooter()).getUniqueId());
		}

		for (Entity near : Utils.getNearbyEntities(target, radius)) {
			nearTargets.remove(near.getUniqueId());

			if (near instanceof Player) {
				nearTargets.add(near.getUniqueId());
			}
		}
	}
}
