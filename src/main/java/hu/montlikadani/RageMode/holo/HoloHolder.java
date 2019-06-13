package hu.montlikadani.ragemode.holo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;
import hu.montlikadani.ragemode.statistics.MySQLStats;
import hu.montlikadani.ragemode.statistics.YAMLStats;

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

		try {
			holosConf.save(holosFile);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			displayHoloToPlayer(player, loc);
		}
	}

	public static void loadHolos() {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		for (Hologram holo : holos) {
			holo.delete();
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			showAllHolosToPlayer(player);
		}
	}

	public static void displayHoloToPlayer(Player player, Location loc) {
		if (!RageMode.getInstance().isHologramEnabled())
			return;

		Hologram hologram = HologramsAPI.createHologram(RageMode.getInstance(), loc);

		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.showTo(player);
		visibilityManager.setVisibleByDefault(false);

		if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("mysql")) {
			final Player mySQLPlayer = player;
			final Hologram mySQLHologram = hologram;
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				final RetPlayerPoints rpp;
				if (RuntimeRPPManager.getRPPForPlayer(mySQLPlayer.getUniqueId().toString()) == null)
					rpp = MySQLStats.getPlayerStatistics(mySQLPlayer.getUniqueId().toString(), RageMode.getMySQL());
				else
					rpp = RuntimeRPPManager.getRPPForPlayer(mySQLPlayer.getUniqueId().toString());
				Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (rpp != null)
						setHologramLines(mySQLHologram, rpp);
					return "Done";
				});
			});
		} else if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("yaml")) {
			final Player yamlPlayer = player;
			final Hologram yamlHologram = hologram;
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				final RetPlayerPoints rpp;
				if (RuntimeRPPManager.getRPPForPlayer(yamlPlayer.getUniqueId().toString()) == null)
					rpp = YAMLStats.getPlayerStatistics(yamlPlayer.getUniqueId().toString());
				else
					rpp = RuntimeRPPManager.getRPPForPlayer(yamlPlayer.getUniqueId().toString());
				Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), () -> {
					if (rpp != null)
						setHologramLines(yamlHologram, rpp);
					return "Done";
				});
			});
		}
	}

	private static void setHologramLines(Hologram hologram, RetPlayerPoints rpp) {
		for (String hList : RageMode.getLang().getList("hologram-list")) {
			if (rpp != null) {
				hList = hList.replace("%rank%", Integer.toString(rpp.getRank()));
				hList = hList.replace("%points%", rpp.getPoints() + "");
				hList = hList.replace("%wins%", rpp.getWins() + "");
				hList = hList.replace("%games%", rpp.getGames() + "");
				hList = hList.replace("%kd%", rpp.getKD() + "");
				hList = hList.replace("%kills%", rpp.getKills() + "");
				hList = hList.replace("%deaths%", rpp.getDeaths() + "");
			} else {
				hList = hList.replace("%rank%", "");
				hList = hList.replace("%points%", "0");
				hList = hList.replace("%wins%", "0");
				hList = hList.replace("%games%", "0");
				hList = hList.replace("%kd%", "0");
				hList = hList.replace("%kills%", "0");
				hList = hList.replace("%deaths%", "0");
			}
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

		try {
			holosConf.save(holosFile);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}

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
				RageMode.getInstance().throwMsg();
			}

			config = new YamlConfiguration();
			config.createSection("data");

			try {
				config.save(file);
			} catch (IOException e2) {
				e2.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
		} else
			config = YamlConfiguration.loadConfiguration(file);

		holosConf = config;
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
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
