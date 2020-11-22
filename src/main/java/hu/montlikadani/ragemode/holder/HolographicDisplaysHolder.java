package hu.montlikadani.ragemode.holder;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;

public class HolographicDisplaysHolder extends IHoloHolder {

	@Override
	public void addHolo(Location loc) {
		HologramsAPI.getHolograms(RageMode.getInstance()).forEach(Hologram::delete);

		if (loc == null)
			return;

		loc.add(0d, 2d, 0d);
		loc.setPitch(0f);
		loc.setYaw(0f);

		synchronized (HOLOS) {
			HOLOS.add(loc);
		}

		FileConfiguration c = RageMode.getInstance().getConfiguration().getHolosConfig();
		c.set("data.holos", HOLOS);

		Configuration.saveFile(c, RageMode.getInstance().getConfiguration().getHolosFile());

		Bukkit.getOnlinePlayers().forEach(player -> displayHoloToPlayer(player, loc));
	}

	@Override
	public void loadHolos() {
		HologramsAPI.getHolograms(RageMode.getInstance()).forEach(Hologram::delete);
		HOLOS.clear();

		@SuppressWarnings("unchecked")
		List<Location> loc = (List<Location>) RageMode.getInstance().getConfiguration().getHolosConfig()
				.get("data.holos");
		if (loc == null || loc.isEmpty()) {
			return;
		}

		synchronized (HOLOS) {
			loc.forEach(HOLOS::add);
		}

		Bukkit.getOnlinePlayers().forEach(this::showAllHolosToPlayer);
	}

	@Override
	public void displayHoloToPlayer(final Player player, final Location loc) {
		if (player == null || loc == null)
			return;

		final Hologram hologram = HologramsAPI.createHologram(RageMode.getInstance(), loc);
		final VisibilityManager visibilityManager = hologram.getVisibilityManager();

		visibilityManager.showTo(player);
		visibilityManager.setVisibleByDefault(false);

		for (String hList : RageMode.getLang().getList("hologram-list")) {
			hologram.appendTextLine(Utils.setPlaceholders(hList, player));
		}
	}

	@Override
	public boolean deleteHologram(Location loc) {
		if (loc == null || !HOLOS.contains(loc))
			return false;

		synchronized (HOLOS) {
			HOLOS.remove(loc);
		}

		FileConfiguration c = RageMode.getInstance().getConfiguration().getHolosConfig();
		c.set("data.holos", HOLOS);

		Configuration.saveFile(c, RageMode.getInstance().getConfiguration().getHolosFile());

		HologramsAPI.getHolograms(RageMode.getInstance()).stream().filter(h -> loc.equals(h.getLocation()))
				.forEach(Hologram::delete);

		loadHolos();
		return true;
	}

	@Override
	public void deleteAllHologram() {
		FileConfiguration c = RageMode.getInstance().getConfiguration().getHolosConfig();
		c.set("data.holos", null);

		Configuration.saveFile(c, RageMode.getInstance().getConfiguration().getHolosFile());

		HOLOS.forEach(loc -> HologramsAPI.getHolograms(RageMode.getInstance()).stream()
				.filter(h -> loc.equals(h.getLocation())).forEach(Hologram::delete));

		HOLOS.clear();
	}

	@Override
	public void deleteHoloObjectsOfPlayer(Player player) {
		if (player == null) {
			return;
		}

		HologramsAPI.getHolograms(RageMode.getInstance()).stream()
				.filter(h -> h.getVisibilityManager().isVisibleTo(player)).forEach(Hologram::delete);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<Hologram> getClosest(Player player, boolean eyeLoc) {
		if (player == null)
			return Optional.empty();

		double lowestDist = Double.MAX_VALUE;

		for (Hologram holo : HologramsAPI.getHolograms(RageMode.getInstance())) {
			double dist = holo.getLocation().distance(eyeLoc ? player.getEyeLocation() : player.getLocation());
			if (dist < lowestDist) {
				lowestDist = dist;
				return Optional.ofNullable(holo);
			}
		}

		return Optional.empty();
	}

	@Override
	public void showAllHolosToPlayer(Player player) {
		if (player == null)
			return;

		synchronized (HOLOS) {
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
