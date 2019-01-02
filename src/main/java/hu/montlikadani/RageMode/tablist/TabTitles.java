package hu.montlikadani.ragemode.tablist;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.events.TabTitleSendEvent;

public class TabTitles {

	public static HashMap<String, TabTitles> allTabLists = new HashMap<>();
	private List<Player> player = new ArrayList<>();
	private BukkitTask taskTimer;

	/**
	 * Creates a new instance of TabList, which manages the Tablist for
	 * the given List of Players.
	 * 
	 * @param player The List of player, the TabList should be set to later.
	 */
	@SuppressWarnings("deprecation")
	public TabTitles(int timer, String header, String footer) {
		for (Player loopPlayer : player) {
			taskTimer = Bukkit.getScheduler().runTaskTimer(RageMode.getInstance(), new BukkitRunnable() {
				@Override
				public void run() {
					sendTabTitle(loopPlayer, header, footer);
				}
			}, timer * 20, timer * 20);
		}
	}

	/**
	 * Adds this instance to the global TabList list allTabLists. This
	 * can be accessed with the getTabList(String gameName) method.
	 * 
	 * @param gameName the unique game-name for which the TabList element should be saved for.
	 * 
	 * @return Whether the TabList was stored successfully or not.
	 */
	public boolean addToTabList(String gameName, boolean forceReplace) {
		if (!allTabLists.containsKey(gameName)) {
			allTabLists.put(gameName, this);
			return true;
		} else if (forceReplace) {
			allTabLists.remove(gameName);
			allTabLists.put(gameName, this);
			return true;
		} else
			return false;
	}

	/**
	 * Returns the TabList element which was saved for the unique gameName
	 * String with the addTabList method.
	 * 
	 * @param gameName The unique String key for which the TabList was saved.
	 * @return The TabList element which was saved for the given String.
	 */
	public TabTitles getTabList(String gameName) {
		if (allTabLists.containsKey(gameName))
			return allTabLists.get(gameName);
		else
			return null;
	}

	/**
	 * Sends TabList to all online players that are currently playing in the game.
	 * 
	 * @param header TabList header if null sending empty line
	 * @param footer TabList footer if null sending empty line
	 */
	public void sendTabTitle(String header, String footer) {
		if (header == null) header = "";

		header = RageMode.getLang().colors(header);
		if (footer == null) footer = "";

		footer = RageMode.getLang().colors(footer);
		for (Player player : this.player) {
			TabTitleSendEvent tabTitleSendEvent = new TabTitleSendEvent(player, header, footer);
			Bukkit.getPluginManager().callEvent(tabTitleSendEvent);
			if (tabTitleSendEvent.isCancelled()) return;

			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

			try {
				Object tabHeader = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + header + "\"}" });
				Object tabFooter = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + footer + "\"}" });
				Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(new Class[0]);
				Object packet = titleConstructor.newInstance(new Object[0]);
				Field aField = null;
				Field bField = null;
				if (version.equals("v1_13_R2")) {
					aField = packet.getClass().getDeclaredField("header");
					aField.setAccessible(true);
					aField.set(packet, tabHeader);
					bField = packet.getClass().getDeclaredField("footer");
				} else if (!version.equals("v1_13_R2")) {
					aField = packet.getClass().getDeclaredField("a");
					aField.setAccessible(true);
					aField.set(packet, tabHeader);
					bField = packet.getClass().getDeclaredField("b");
				}
				bField.setAccessible(true);
				bField.set(packet, tabFooter);
				sendPacket(player, packet);
			} catch (Exception e) {
				e.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
		}
	}

	/**
	 * Sends TabList to the specified player that are currently playing in the game.
	 * 
	 * @param player Player name to send tablist for the specified player
	 * @param header TabList header if null sending empty line
	 * @param footer TabList footer if null sending empty line
	 */
	public void sendTabTitle(Player player, String header, String footer) {
		if (header == null) header = "";

		header = RageMode.getLang().colors(header);
		if (footer == null) footer = "";

		footer = RageMode.getLang().colors(footer);
		TabTitleSendEvent tabTitleSendEvent = new TabTitleSendEvent(player, header, footer);
		Bukkit.getPluginManager().callEvent(tabTitleSendEvent);
		if (tabTitleSendEvent.isCancelled()) return;

		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

		try {
			Object tabHeader = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + header + "\"}" });
			Object tabFooter = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + footer + "\"}" });
			Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(new Class[0]);
			Object packet = titleConstructor.newInstance(new Object[0]);
			Field aField = null;
			Field bField = null;
			if (version.equals("v1_13_R2")) {
				aField = packet.getClass().getDeclaredField("header");
				aField.setAccessible(true);
				aField.set(packet, tabHeader);
				bField = packet.getClass().getDeclaredField("footer");
			} else if (!version.equals("v1_13_R2")) {
				aField = packet.getClass().getDeclaredField("a");
				aField.setAccessible(true);
				aField.set(packet, tabHeader);
				bField = packet.getClass().getDeclaredField("b");
			}
			bField.setAccessible(true);
			bField.set(packet, tabFooter);
			sendPacket(player, packet);
		} catch (Exception e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	/**
	 * Removing the tablist from all online player that are currently playing in the game.
	 */
	public void removeTabList() {
		for (Player player : this.player) {
			sendTabTitle(player, null, null);
		}
		cancelTask();
	}

	/**
	 * 
	 * @param player Player name to remove tablist from the specified player that are currently
	 * playing the game.
	 */
	public void removeTabList(Player player) {
		sendTabTitle(player, null, null);
		cancelTask();
	}

	private void cancelTask() {
		if (taskTimer != null)
			taskTimer.cancel();
	}

	private void sendPacket(Player player, Object packet) {
		try {
			Object handle = getNMSPlayer(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") }).invoke(playerConnection, new Object[] { packet });
		} catch (Exception e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	private Object getNMSPlayer(Player p) throws Exception {
		return p.getClass().getMethod("getHandle", new Class[0]).invoke(p, new Object[0]);
	}

	private Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}