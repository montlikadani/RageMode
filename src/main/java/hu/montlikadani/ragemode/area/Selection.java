package hu.montlikadani.ragemode.area;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

public class Selection {

	protected final Map<String, Location> loc1 = new HashMap<>(), loc2 = new HashMap<>();

	public void updateLocations(UUID uuid, Location loc1, Location loc2) {
		if (loc1 != null && loc2 != null) {
			String stringId = uuid.toString();

			this.loc1.put(stringId, loc1);
			this.loc2.put(stringId, loc2);
		}
	}

	public void placeLoc1(UUID uuid, Location loc) {
		if (loc != null) {
			loc1.put(uuid.toString(), loc);
		}
	}

	public void placeLoc2(UUID uuid, Location loc) {
		if (loc != null) {
			loc2.put(uuid.toString(), loc);
		}
	}

	public Location getLoc1(UUID uuid) {
		return loc1.get(uuid.toString());
	}

	public Location getLoc2(UUID uuid) {
		return loc2.get(uuid.toString());
	}

	public Area getArea(UUID uuid) {
		return new Area(getLoc1(uuid), getLoc2(uuid));
	}

	public boolean hasSetBoth(UUID uuid) {
		String stringId = uuid.toString();

		return loc1.containsKey(stringId) && loc2.containsKey(stringId);
	}

	public void clearSelection(UUID uuid) {
		String stringId = uuid.toString();

		loc1.remove(stringId);
		loc2.remove(stringId);
	}
}
