package hu.montlikadani.ragemode.gameUtils.modules;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;

import java.lang.reflect.Constructor;

public class Titles {

	/**
	 * Sends for player the title and the subtitle and how long they should appear or stay or disappear.
	 * 
	 * @param player Player
	 * @param fadeIn FadeIn
	 * @param stay Stay
	 * @param fadeOut FadeOut
	 * @param title Title
	 * @param subtitle SubTitle
	 */
	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		try {
			Object e, chatTitle, chatSubtitle, titlePacket, subtitlePacket;
			Constructor<?> subtitleConstructor;

			if (title == null) title = "";

			title = Utils.colors(title);
			e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
			chatTitle = Utils.getAsIChatBaseComponent(title);
			subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle")
					.getConstructor(new Class[] { Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
							Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
			titlePacket = subtitleConstructor.newInstance(new Object[] { e, chatTitle, fadeIn, stay, fadeOut });
			Utils.sendPacket(player, titlePacket);

			e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get((Object) null);
			chatTitle = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
					.getMethod("a", new Class[] { String.class })
					.invoke((Object) null, new Object[] { "{\"text\":\"" + title + "\"}" });
			subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle").getConstructor(new Class[] {
					Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], Utils.getNMSClass("IChatBaseComponent") });
			titlePacket = subtitleConstructor.newInstance(new Object[] { e, chatTitle });
			Utils.sendPacket(player, titlePacket);

			if (subtitle == null) subtitle = "";

			subtitle = Utils.colors(subtitle);
			e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
			chatSubtitle = Utils.getAsIChatBaseComponent(title);
			subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle")
					.getConstructor(new Class[] { Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
							Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
			subtitlePacket = subtitleConstructor
					.newInstance(new Object[] { e, chatSubtitle, fadeIn, stay, fadeOut });
			Utils.sendPacket(player, subtitlePacket);

			e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get((Object) null);
			chatSubtitle = Utils.getAsIChatBaseComponent(subtitle);
			subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle")
					.getConstructor(new Class[] { Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
							Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
			subtitlePacket = subtitleConstructor
					.newInstance(new Object[] { e, chatSubtitle, fadeIn, stay, fadeOut });
			Utils.sendPacket(player, subtitlePacket);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears the current set title.
	 * @param player Player
	 */
	public static void clearTitle(Player player) {
		sendTitle(player, 0, 0, 0, "", "");
	}
}
