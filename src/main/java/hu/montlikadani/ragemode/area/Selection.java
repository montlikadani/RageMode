package hu.montlikadani.ragemode.area;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Selection {

	protected final Map<String, Location> loc1 = new HashMap<>();
	protected final Map<String, Location> loc2 = new HashMap<>();

	public void updateLocations(Player player, Location loc1, Location loc2) {
		if (loc1 != null && loc2 != null) {
			this.loc1.put(player.getUniqueId().toString(), loc1);
			this.loc2.put(player.getUniqueId().toString(), loc2);
		}
	}

	public void placeLoc1(Player player, Location loc) {
		if (loc != null) {
			loc1.put(player.getUniqueId().toString(), loc);
		}
	}

	public void placeLoc2(Player player, Location loc) {
		if (loc != null) {
			loc2.put(player.getUniqueId().toString(), loc);
		}
	}

	public Location getLoc1(Player player) {
		return loc1.get(player.getUniqueId().toString());
	}

	public Location getLoc2(Player player) {
		return loc2.get(player.getUniqueId().toString());
	}

	public Area getArea(Player player) {
		return new Area(getLoc1(player), getLoc2(player));
	}

	public boolean hasSetBoth(Player player) {
		return loc1.containsKey(player.getUniqueId().toString()) && loc2.containsKey(player.getUniqueId().toString());
	}

	public void clearSelection(Player player) {
		loc1.remove(player.getUniqueId().toString());
		loc2.remove(player.getUniqueId().toString());
	}
}
