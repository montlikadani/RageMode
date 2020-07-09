package hu.montlikadani.ragemode.gameUtils.modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.Utils;

public class TabTitles {

	/**
	 * Sends TabList to the specified player that are currently playing in the game.
	 * 
	 * @param player Player name to send tablist for the specified player
	 * @param header TabList header if null sending empty line
	 * @param footer TabList footer if null sending empty line
	 */
	public void sendTabTitle(Player player, String header, String footer) {
		if (player == null) {
			return;
		}

		if (header == null) header = "";
		if (footer == null) footer = "";

		if (Version.isCurrentEqualOrHigher(Version.v1_16_R1)) {
			player.setPlayerListHeaderFooter(header, footer);
			return;
		}

		try {
			Object tabHeader = Utils.getAsIChatBaseComponent(header);
			Object tabFooter = Utils.getAsIChatBaseComponent(footer);
			Constructor<?> titleConstructor = Utils.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
			Object packet = titleConstructor.newInstance();
			Field aField = null, bField = null;

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
}