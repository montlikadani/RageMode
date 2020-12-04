package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.scores.KilledWith;

public final class CacheableHitTarget {

	public static final Map<UUID, UUID> VICTIMS = new HashMap<>();

	public static void addMetadata(Entity target, int radius, KilledWith tool) {
		if (tool.getMetaName().isEmpty()) {
			return;
		}

		if (tool == KilledWith.PRESSUREMINE && target instanceof Player) {
			target.removeMetadata("killedWith", RageMode.getInstance());
			target.setMetadata("killedWith", new FixedMetadataValue(RageMode.getInstance(), tool.getMetaName()));
			return;
		}

		for (Entity near : Utils.getNearbyEntities(target, radius)) {
			VICTIMS.remove(near.getUniqueId());

			if (target instanceof Arrow && ((Arrow) target).getShooter() != null) {
				VICTIMS.put(near.getUniqueId(), ((Player) ((Arrow) target).getShooter()).getUniqueId());
			}

			if (near instanceof Player) {
				near.removeMetadata("killedWith", RageMode.getInstance());
				near.setMetadata("killedWith", new FixedMetadataValue(RageMode.getInstance(), tool.getMetaName()));
			}
		}
	}
}
