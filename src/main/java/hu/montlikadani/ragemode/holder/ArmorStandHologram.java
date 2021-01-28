package hu.montlikadani.ragemode.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;

public class ArmorStandHologram extends IHoloHolder {

	private final Map<UUID, List<ArmorStands>> armorStands = new HashMap<>();

	@Override
	public void addHolo(final Location loc) {
		armorStands.values().forEach(a -> a.forEach(ArmorStands::delete));
		armorStands.clear();

		if (loc == null) {
			return;
		}

		loc.add(0d, 2d, 0d);
		loc.setPitch(0f);
		loc.setYaw(0f);

		HOLOS.add(loc);

		FileConfiguration c = RageMode.getInstance().getConfiguration().getHolosConfig();
		c.set("data.holos", HOLOS);

		Configuration.saveFile(c, RageMode.getInstance().getConfiguration().getHolosFile());

		Bukkit.getOnlinePlayers().forEach(player -> displayHoloToPlayer(player, loc));
	}

	@Override
	public void loadHolos() {
		HOLOS.clear();

		@SuppressWarnings("unchecked")
		List<Location> loc = (List<Location>) RageMode.getInstance().getConfiguration().getHolosConfig()
				.get("data.holos");
		if (loc == null || loc.isEmpty()) {
			return;
		}

		loc.forEach(HOLOS::add);
		Bukkit.getOnlinePlayers().forEach(this::showAllHolosToPlayer);
	}

	@Override
	public void displayHoloToPlayer(final Player player, final Location loc) {
		if (player == null || loc == null) {
			return;
		}

		List<ArmorStands> l = new ArrayList<>();
		for (String hList : RageMode.getLang().getList("hologram-list")) {
			ArmorStands armorStand = ArmorStands.holoTextBuilder().addLine(Utils.setPlaceholders(hList, player))
					.build();
			armorStand.setLocation(loc);
			armorStand.append();
			l.add(armorStand);
		}

		armorStands.put(player.getUniqueId(), l);
	}

	@Override
	public boolean deleteHologram(Location loc) {
		if (loc == null || !HOLOS.contains(loc))
			return false;

		HOLOS.remove(loc);

		FileConfiguration c = RageMode.getInstance().getConfiguration().getHolosConfig();
		c.set("data.holos", HOLOS);

		Configuration.saveFile(c, RageMode.getInstance().getConfiguration().getHolosFile());

		for (List<ArmorStands> l : armorStands.values()) {
			for (ArmorStands holo : l) {
				if (loc.equals(holo.getLocation())) {
					holo.delete();
				}
			}
		}

		loadHolos();
		return true;
	}

	@Override
	public void deleteAllHologram() {
		for (Location loc : HOLOS) {
			for (List<ArmorStands> l : armorStands.values()) {
				for (ArmorStands holo : l) {
					if (loc.equals(holo.getLocation())) {
						holo.delete();
					}
				}
			}
		}

		armorStands.clear();
		HOLOS.clear();
	}

	@Override
	public void deleteHoloObjectsOfPlayer(Player player) {
		if (player == null) {
			return;
		}

		for (Entry<UUID, List<ArmorStands>> map : armorStands.entrySet()) {
			if (map.getKey().equals(player.getUniqueId())) {
				map.getValue().forEach(ArmorStands::delete);
			}
		}

		armorStands.remove(player.getUniqueId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<ArmorStands> getClosest(Player player, boolean eyeLoc) {
		if (player == null)
			return Optional.empty();

		double lowestDist = Double.MAX_VALUE;

		for (List<ArmorStands> l : armorStands.values()) {
			for (ArmorStands holo : l) {
				if (holo.getLocation() == null) {
					continue;
				}

				double dist = holo.getLocation().distance(eyeLoc ? player.getEyeLocation() : player.getLocation());
				if (dist < lowestDist) {
					lowestDist = dist;
					return Optional.ofNullable(holo);
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public void showAllHolosToPlayer(Player player) {
		if (player != null) {
			HOLOS.forEach(loc -> displayHoloToPlayer(player, loc));
		}
	}

	@Override
	public void updateHolosForPlayer(Player player) {
		if (player == null)
			return;

		deleteHoloObjectsOfPlayer(player);
		showAllHolosToPlayer(player);
	}
}
