package hu.montlikadani.ragemode.area;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

public class Selection {

	protected final Map<UUID, Location> loc1 = new HashMap<>(), loc2 = new HashMap<>();

	public void updateLocations(UUID uuid, Location loc1, Location loc2) {
		if (loc1 != null && loc2 != null) {
			this.loc1.put(uuid, loc1);
			this.loc2.put(uuid, loc2);
		}
	}

	public void placeLoc1(UUID uuid, Location loc) {
		if (loc != null) {
			loc1.put(uuid, loc);
		}
	}

	public void placeLoc2(UUID uuid, Location loc) {
		if (loc != null) {
			loc2.put(uuid, loc);
		}
	}

	public Location getLoc1(UUID uuid) {
		return loc1.get(uuid);
	}

	public Location getLoc2(UUID uuid) {
		return loc2.get(uuid);
	}

	public Area getArea(UUID uuid) {
		return new Area(getLoc1(uuid), getLoc2(uuid));
	}

	public boolean hasSetBoth(UUID uuid) {
		return loc1.containsKey(uuid) && loc2.containsKey(uuid);
	}

	public void clearSelection(UUID uuid) {
		loc1.remove(uuid);
		loc2.remove(uuid);
	}
}
