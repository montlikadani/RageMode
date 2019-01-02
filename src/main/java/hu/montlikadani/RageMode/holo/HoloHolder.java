package hu.montlikadani.ragemode.holo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
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
	private static FileConfiguration holosConf;

	@SuppressWarnings("unchecked")
	public static void addHolo(Location loc) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		loc.add(0d, 2d, 0d);
		loc.setPitch(0f);
		loc.setYaw(0f);
		List<Location> savedHolos;
		if (holosConf.isSet("data.holos"))
			savedHolos = (List<Location>) holosConf.getList("data.holos");
		else
			savedHolos = new ArrayList<>();

		savedHolos.add(loc);
		holosConf.set("data.holos", savedHolos);

		try {
			holosConf.save(holosFile);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}

		Collection<? extends Player> onlines = Bukkit.getOnlinePlayers();
		for (Player player : onlines) {
			displayHoloToPlayer(player, loc);
		}
	}

	public static void loadHolos() {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		for (Hologram holo : holos) {
			holo.delete();
		}
		Collection<? extends Player> onlines = Bukkit.getOnlinePlayers();
		for (Player player : onlines) {
			showAllHolosToPlayer(player);
		}
	}

	public static void displayHoloToPlayer(Player player, Location loc) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		Hologram hologram = HologramsAPI.createHologram(RageMode.getInstance(), loc);

		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.showTo(player);
		visibilityManager.setVisibleByDefault(false);

		// RetPlayerPoints rpp = null;

		if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("mysql")) {
			final Player mySQLPlayer = player;
			final Hologram mySQLHologram = hologram;
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					final RetPlayerPoints rpp;
					if (RuntimeRPPManager.getRPPForPlayer(mySQLPlayer.getUniqueId().toString()) == null)
						rpp = MySQLStats.getPlayerStatistics(mySQLPlayer.getUniqueId().toString(), RageMode.getMySQL());
					else
						rpp = RuntimeRPPManager.getRPPForPlayer(mySQLPlayer.getUniqueId().toString());
					Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), new Callable<String>() {

						@Override
						public String call() throws Exception {
							setHologramLines(mySQLHologram, rpp);
							return "Done";
						}
					});
				}
			});
		} else if (RageMode.getInstance().getConfiguration().getCfg().getString("statistics").equals("yaml")) {
			final Player yamlPlayer = player;
			final Hologram yamlHologram = hologram;
			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), new Runnable() {
				@Override
				public void run() {
					final RetPlayerPoints rpp;
					if (RuntimeRPPManager.getRPPForPlayer(yamlPlayer.getUniqueId().toString()) == null)
						rpp = YAMLStats.getPlayerStatistics(yamlPlayer.getUniqueId().toString());
					else
						rpp = RuntimeRPPManager.getRPPForPlayer(yamlPlayer.getUniqueId().toString());
					Bukkit.getServer().getScheduler().callSyncMethod(RageMode.getInstance(), new Callable<String>() {
						@Override
						public String call() throws Exception {
							setHologramLines(yamlHologram, rpp);
							return "Done";
						}
					});
				}
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
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		for (Hologram holo : holos) {
			if (holo.getVisibilityManager().isVisibleTo(player))
				holo.delete();
		}
	}

	public static void deleteHologram(Player p, Hologram holo) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		if (holo == null)
			return;

		@SuppressWarnings("unchecked")
		List<Location> locList = (List<Location>) holosConf.getList("data.holos");
		if (locList.contains(holo.getLocation()))
			locList.remove(holo.getLocation());
		else {
			p.sendMessage(RageMode.getLang().get("commands.holostats.no-holo-found"));
			return;
		}

		holosConf.set("data.holos", locList);

		try {
			holosConf.save(holosFile);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}

		holo.delete();
		loadHolos();
	}

/**	public static void teleporttoHologram(Player p, Hologram holo) {
		if (holo == null)
			return;

		@SuppressWarnings("unchecked")
		List<Location> locList = (List<Location>) holosConf.getList("data.holos");
		if (locList.contains(holo.getLocation()))
			p.teleport(holo.getLocation());
		else
			p.sendMessage(RageMode.getLang().get("commands.holostats.no-holo-found"));
	}*/

	public static Hologram getClosest(Player player) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return null;

		Collection<Hologram> holos = HologramsAPI.getHolograms(RageMode.getInstance());
		Hologram closest = null;
		double lowestDist = Double.MAX_VALUE;

		for (Hologram holo : holos) {
			double dist = holo.getLocation().distance(player.getLocation());
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

	@SuppressWarnings("unchecked")
	public static void showAllHolosToPlayer(Player player) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		List<Location> holoList;
		if (holosConf.isSet("data.holos"))
			holoList = (List<Location>) holosConf.getList("data.holos");
		else
			return;

		int i = 0;
		int imax = holoList.size();
		while (i < imax) {
			displayHoloToPlayer(player, holoList.get(i));
			i++;
		}
	}

	public static void updateHolosForPlayer(Player player) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
			return;

		deleteHoloObjectsOfPlayer(player);
		showAllHolosToPlayer(player);
	}
}
