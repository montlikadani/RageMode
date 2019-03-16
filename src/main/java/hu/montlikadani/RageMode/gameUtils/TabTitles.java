package hu.montlikadani.ragemode.gameUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.GameLoader;

public class TabTitles {

	public static HashMap<String, TabTitles> allTabLists = new HashMap<>();
	private List<Player> player = new ArrayList<>();

	/**
	 * Creates a new instance of TabList, which manages the Tablist for
	 * the List of Players.
	 * 
	 * @param playerString List players that can be add to the list
	 */
	public TabTitles(List<String> playerString) {
		for (String player : playerString) {
			this.player.add(Bukkit.getPlayer(UUID.fromString(player)));
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
	 * @param header TabList header
	 * @param footer TabList footer
	 */
	public void sendTabTitle(String header, String footer) {
		for (Player player : this.player) {
			sendTabTitle(player, header, footer);
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

		if (footer == null) footer = "";

		try {
			Object tabHeader = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + header + "\"}" });
			Object tabFooter = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + footer + "\"}" });
			Constructor<?> titleConstructor = Utils.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(new Class[0]);
			Object packet = titleConstructor.newInstance(new Object[0]);
			Field aField = null;
			Field bField = null;
			if (RageMode.getPackageVersion().equals("v1_13_R2")) {
				aField = packet.getClass().getDeclaredField("header");
				aField.setAccessible(true);
				aField.set(packet, tabHeader);
				bField = packet.getClass().getDeclaredField("footer");
			} else if (!RageMode.getPackageVersion().equals("v1_13_R2")) {
				aField = packet.getClass().getDeclaredField("a");
				aField.setAccessible(true);
				aField.set(packet, tabHeader);
				bField = packet.getClass().getDeclaredField("b");
			}
			bField.setAccessible(true);
			bField.set(packet, tabFooter);
			Utils.sendPacket(player, packet);
		} catch (Throwable e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	/**
	 * Removing the tablist from all online player that are currently playing in the game.
	 */
	public void removeTabList() {
		if (GameLoader.task2 != null)
			GameLoader.task2.cancel();

		for (Player player : this.player) {
			sendTabTitle(player, null, null);
		}
	}

	/**
	 * 
	 * @param player Player name to remove tablist from the specified player that are currently
	 * playing the game.
	 */
	public void removeTabList(Player player) {
		if (GameLoader.task2 != null)
			GameLoader.task2.cancel();

		sendTabTitle(player, null, null);
	}
}