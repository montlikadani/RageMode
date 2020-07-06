package hu.montlikadani.ragemode.holder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class HoloHolder {

	private static File holosFile;
	private static YamlConfiguration holosConf;

	private static List<Location> holos = new ArrayList<>();

	public static void addHolo(Location loc) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		loc.add(0d, 2d, 0d);
		loc.setPitch(0f);
		loc.setYaw(0f);

		holos.add(loc);
		holosConf.set("data.holos", holos);

		Configuration.saveFile(holosConf, holosFile);

		Bukkit.getOnlinePlayers().forEach(player -> displayHoloToPlayer(player, loc));
	}

	public static void loadHolos() {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		for (Hologram holo : holos) {
			holo.delete();
		}

		@SuppressWarnings("unchecked")
		List<Location> loc = (List<Location>) holosConf.get("data.holos");
		if (loc == null || loc.isEmpty()) {
			return;
		}

		for (Location l : loc) {
			HoloHolder.holos.add(l);
		}

		Bukkit.getOnlinePlayers().forEach(HoloHolder::showAllHolosToPlayer);
	}

	public static void displayHoloToPlayer(Player player, Location loc) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		Hologram hologram = HologramsAPI.createHologram(RageMode.getInstance(), loc);

		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.showTo(player);
		visibilityManager.setVisibleByDefault(false);

		final UUID uuid = player.getUniqueId();
		final Hologram dataHologram = hologram;

		switch (RageMode.getInstance().getDatabaseHandler().getDBType()) {
		case MYSQL:
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				final PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);

				Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (rpp != null)
						setHologramLines(dataHologram, rpp);
					return "Done";
				});
			});
			break;
		case YAML:
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				final PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);

				Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (rpp != null)
						setHologramLines(dataHologram, rpp);
					return "Done";
				});
			});
			break;
		case SQLITE:
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				final PlayerPoints rpp = RuntimePPManager.getPPForPlayer(uuid);

				Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (rpp != null)
						setHologramLines(dataHologram, rpp);

					return "Done";
				});
			});
			break;
		default:
			break;
		}
	}

	private static void setHologramLines(Hologram hologram, PlayerPoints pp) {
		for (String hList : RageMode.getLang().getList("hologram-list")) {
			hList = Utils.setPlaceholders(hList, pp);
			hologram.appendTextLine(hList);
		}
	}

	public static void deleteHoloObjectsOfPlayer(Player player) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		for (Hologram holo : holos) {
			if (holo.getVisibilityManager().isVisibleTo(player))
				holo.delete();
		}
	}

	public static boolean deleteHologram(Hologram holo) {
		if (!RageMode.getInstance().isHologramEnabled())
			return false;

		if (holo == null)
			return false;

		if (!holos.contains(holo.getLocation()))
			return false;

		holos.remove(holo.getLocation());
		holosConf.set("data.holos", holos);

		Configuration.saveFile(holosConf, holosFile);

		holo.delete();
		loadHolos();

		return true;
	}

	public static Location getHologramLocation(Hologram holo) {
		if (holo == null)
			return null;

		return holo.getLocation();
	}

	/**
	 * Getting the hologram around the player.
	 * @param player Player
	 * @param eyeLoc player eye location or current location
	 * @return Hologram if found around the player
	 */
	public static Hologram getClosest(Player player, boolean eyeLoc) {
		if (!RageMode.getInstance().isHologramEnabled())
			return null;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		Hologram closest = null;
		double lowestDist = Double.MAX_VALUE;

		for (Hologram holo : holos) {
			double dist = holo.getLocation().distance(eyeLoc ? player.getEyeLocation() : player.getLocation());
			if (dist < lowestDist) {
				lowestDist = dist;
				closest = holo;
			}
		}
		return closest;
	}

	public static void initHoloHolder() {
		File file = new File(RageMode.getInstance().getFolder(), "holos.yml");
		YamlConfiguration config = null;
		holosFile = file;

		if (!file.exists()) {
			file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			config = new YamlConfiguration();
			config.createSection("data");

			Configuration.saveFile(config, file);
		} else
			config = YamlConfiguration.loadConfiguration(file);

		holosConf = config;
		Configuration.saveFile(config, file);
		loadHolos();
	}

	public static void showAllHolosToPlayer(Player player) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		int i = 0;
		int imax = holos.size();
		while (i < imax) {
			displayHoloToPlayer(player, holos.get(i));
			i++;
		}
	}

	public static void updateHolosForPlayer(Player player) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		deleteHoloObjectsOfPlayer(player);
		showAllHolosToPlayer(player);
	}
}
