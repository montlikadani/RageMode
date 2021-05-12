package hu.montlikadani.ragemode.holder.holograms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.utils.Utils;

public final class ArmorStandHologram extends IHoloHolder {

	private final Set<ArmorStands> armorStands = new HashSet<>();

	private final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	@Override
	public void addHolo(final Location loc) {
		if (loc == null) {
			return;
		}

		loc.add(0d, 2d, 0d);
		loc.setPitch(0f);
		loc.setYaw(0f);

		ArmorStands.TextBuilder textBuilder = ArmorStands.holoTextBuilder();

		for (String one : RageMode.getLang().getList("hologram-list")) {
			textBuilder = textBuilder.addLine(one);
		}

		ArmorStands armorStand = textBuilder.build();
		armorStand.setLocation(loc);
		armorStand.append();

		armorStands.add(armorStand);

		for (Player player : Bukkit.getOnlinePlayers()) {
			updateHologramsName(player);
		}

		FileConfiguration c = plugin.getConfiguration().getHolosConfig();
		c.set("data.holos", asLocations());

		Configuration.saveFile(c, plugin.getConfiguration().getHolosFile());
	}

	@Override
	public void loadHolos() {
		deleteAllHologram(false);

		@SuppressWarnings("unchecked")
		List<Location> holos = (List<Location>) plugin.getConfiguration().getHolosConfig().get("data.holos");

		if (holos == null || holos.isEmpty()) {
			return;
		}

		for (Location loc : holos) {
			ArmorStands.TextBuilder textBuilder = ArmorStands.holoTextBuilder();

			for (String one : RageMode.getLang().getList("hologram-list")) {
				textBuilder = textBuilder.addLine(one);
			}

			ArmorStands armorStand = textBuilder.build();
			armorStand.setLocation(loc);
			armorStand.append();

			armorStands.add(armorStand);
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			updateHologramsName(player);
		}
	}

	@Override
	public void updateHologramsName(final Player player) {
		if (player == null) {
			return;
		}

		for (ArmorStands as : armorStands) {
			for (int i = 0; i <= as.getTextLines().length - 1; i++) {
				as.getArmorStands().get(i).setCustomName(Utils.setPlaceholders(as.getTextLines()[i], player));
			}
		}
	}

	@Override
	public boolean deleteHologram(Location loc) {
		if (loc == null)
			return false;

		for (ArmorStands holo : armorStands) {
			if (loc.equals(holo.getLocation())) {
				holo.delete();
			}
		}

		FileConfiguration c = plugin.getConfiguration().getHolosConfig();
		c.set("data.holos", asLocations());

		Configuration.saveFile(c, plugin.getConfiguration().getHolosFile());
		return true;
	}

	@Override
	public void deleteAllHologram(boolean removeFromFile) {
		for (ArmorStands holo : armorStands) {
			holo.delete();
		}

		if (removeFromFile) {
			FileConfiguration c = plugin.getConfiguration().getHolosConfig();
			c.set("data.holos", null);

			Configuration.saveFile(c, plugin.getConfiguration().getHolosFile());
		}

		armorStands.clear();
	}

	@Override
	public Optional<ArmorStands> getClosest(Player player, boolean eyeLoc) {
		if (player == null)
			return Optional.empty();

		double lowestDist = Double.MAX_VALUE;
		Location currentLoc = eyeLoc ? player.getEyeLocation() : player.getLocation();

		for (ArmorStands holo : armorStands) {
			if (holo.getLocation() == null) {
				continue;
			}

			double dist = holo.getLocation().distance(currentLoc);

			if (dist < lowestDist) {
				lowestDist = dist;
				return Optional.of(holo);
			}
		}

		return Optional.empty();
	}

	private List<Location> asLocations() {
		List<Location> locs = new ArrayList<>();

		for (ArmorStands stand : armorStands) {
			locs.add(stand.getLocation());
		}

		return locs;
	}
}
