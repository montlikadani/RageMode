package hu.montlikadani.ragemode.items.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

public final class PressureMine {

	private final UUID playerUUID;

	private final Set<Location> mines = new HashSet<>();

	public PressureMine(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	public UUID getOwner() {
		return playerUUID;
	}

	public Set<Location> getMines() {
		return mines;
	}

	public boolean addMine(org.bukkit.block.Block block) {
		return block != null && block.getType() == Material.TRIPWIRE && mines.add(block.getLocation());
	}

	public void removeMines() {
		for (Location loc : mines) {
			loc.getBlock().setType(Material.AIR);
		}

		mines.clear();
	}

	public boolean removeMine(Location loc) {
		if (mines.remove(loc)) {
			loc.getBlock().setType(Material.AIR);
			return true;
		}

		return false;
	}
}
