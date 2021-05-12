package hu.montlikadani.ragemode.items.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class PressureMine {

	private UUID playerUUID;

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

	public boolean addMine(Location loc) {
		return addMine(loc.getBlock(), loc);
	}

	public boolean addMine(Block block, Location loc) {
		return block != null && block.getType() == Material.TRIPWIRE && mines.add(loc);
	}

	public void removeMines() {
		for (Location loc : mines) {
			loc.getBlock().setType(Material.AIR);
		}

		mines.clear();
	}

	public void removeMine(Location loc) {
		if (mines.remove(loc)) {
			loc.getBlock().setType(Material.AIR);
		}
	}
}
