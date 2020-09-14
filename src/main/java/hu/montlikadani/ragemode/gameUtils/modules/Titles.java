package hu.montlikadani.ragemode.gameUtils.modules;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils.Reflections;

import java.lang.reflect.Constructor;

public class Titles {

	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title,
			String subtitle) {
		try {
			Object e, chatTitle, chatSubtitle, titlePacket, subtitlePacket;
			Constructor<?> subtitleConstructor;

			if (title == null)
				title = "";

			e = Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES")
					.get((Object) null);
			chatTitle = Reflections.getAsIChatBaseComponent(title);
			subtitleConstructor = Reflections.getNMSClass("PacketPlayOutTitle").getConstructor(
					Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
					Reflections.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
			titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
			Reflections.sendPacket(player, titlePacket);

			e = Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE")
					.get((Object) null);
			chatTitle = Reflections.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
					.getMethod("a", String.class).invoke((Object) null, "{\"text\":\"" + title + "\"}");
			subtitleConstructor = Reflections.getNMSClass("PacketPlayOutTitle").getConstructor(
					Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
					Reflections.getNMSClass("IChatBaseComponent"));
			titlePacket = subtitleConstructor.newInstance(e, chatTitle);
			Reflections.sendPacket(player, titlePacket);

			e = Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES")
					.get((Object) null);
			chatSubtitle = Reflections.getAsIChatBaseComponent(title);
			subtitleConstructor = Reflections.getNMSClass("PacketPlayOutTitle").getConstructor(
					Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
					Reflections.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
			subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
			Reflections.sendPacket(player, subtitlePacket);

			if (subtitle == null)
				subtitle = "";

			e = Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE")
					.get((Object) null);
			chatSubtitle = Reflections.getAsIChatBaseComponent(subtitle);
			subtitleConstructor = Reflections.getNMSClass("PacketPlayOutTitle").getConstructor(
					Reflections.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
					Reflections.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
			subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
			Reflections.sendPacket(player, subtitlePacket);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears the current set title.
	 * 
	 * @param player {@link Player}
	 */
	public static void clearTitle(Player player) {
		sendTitle(player, 0, 0, 0, "", "");
	}
}
