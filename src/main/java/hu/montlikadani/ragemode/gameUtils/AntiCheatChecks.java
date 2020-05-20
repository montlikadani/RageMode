package hu.montlikadani.ragemode.gameUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class AntiCheatChecks {

	private static final Set<UUID> PLAYERS = new HashSet<>();

	public static boolean checkBowShoot(final Player p) {
		final UUID uuid = p.getUniqueId();
		if (PLAYERS.contains(uuid)) {
			return true;
		}

		PLAYERS.add(uuid);

		Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> PLAYERS.remove(uuid), 8L);
		return false;
	}
}
