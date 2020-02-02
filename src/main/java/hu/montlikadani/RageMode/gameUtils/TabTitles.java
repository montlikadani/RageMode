package hu.montlikadani.ragemode.gameUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.Utils;

public class TabTitles implements IObjectives {

	public static HashMap<String, TabTitles> allTabLists = new HashMap<>();

	/**
	 * Just creates a new instance of ScoreBoard.
	 */
	public TabTitles() {
	}

	/**
	 * Adds this instance to the global TabList list allTabLists.
	 * @param gameName the unique game-name for which the TabList element should be saved for.
	 * @return Whether the TabList was stored successfully or not.
	 */
	public boolean addToList(String gameName) {
		return addToList(gameName, true);
	}

	/**
	 * Adds this instance to the global TabList list allTabLists.
	 * @param gameName the unique game-name for which the TabList element should be saved for.
	 * @param forceReplace force the game put to the list
	 * @return Whether the TabList was stored successfully or not.
	 */
	@Override
	public boolean addToList(String gameName, boolean forceReplace) {
		if (!allTabLists.containsKey(gameName)) {
			allTabLists.put(gameName, this);
		} else if (forceReplace) {
			allTabLists.remove(gameName);
			allTabLists.put(gameName, this);
		} else {
			return false;
		}

		return true;
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
			Object tabHeader = Utils.getAsIChatBaseComponent(header);
			Object tabFooter = Utils.getAsIChatBaseComponent(footer);
			Constructor<?> titleConstructor = Utils.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
			Object packet = titleConstructor.newInstance();
			Field aField = null;
			Field bField = null;
			if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
				aField = packet.getClass().getDeclaredField("header");
				bField = packet.getClass().getDeclaredField("footer");
			} else {
				aField = packet.getClass().getDeclaredField("a");
				bField = packet.getClass().getDeclaredField("b");
			}
			aField.setAccessible(true);
			aField.set(packet, tabHeader);

			bField.setAccessible(true);
			bField.set(packet, tabFooter);
			Utils.sendPacket(player, packet);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the tablist from the given player that are currently playing in a game.
	 * @param player Player
	 */
	@Override
	public void remove(Player player) {
		sendTabTitle(player, null, null);
	}
}