package hu.montlikadani.ragemode.gameUtils.modules;

import static hu.montlikadani.ragemode.utils.reflection.NMSContainer.getNMSClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.reflection.NMSContainer;
import hu.montlikadani.ragemode.utils.reflection.Reflections;

public abstract class TitleSender {

	private static Class<?> packetPlayOutTitle, enumTitleAction, packetPlayOutChatClass, chatMessageTypeClass,
			playerListHeaderFooter;
	private static Object TIMES, TITLE, SUBTITLE, chatMessageType;
	private static Constructor<?> playerListHeaderFooterConstructor, chatComponentTextConstructor,
			packetPlayOutChatConstructor, playOutTitleTimesConstructor, titleConstructor;

	static {
		try {
			packetPlayOutTitle = getNMSClass("PacketPlayOutTitle");
			packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
			playerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
			enumTitleAction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");

			TIMES = getField(enumTitleAction, "TIMES").get(null);
			TITLE = getField(enumTitleAction, "TITLE").get(null);
			SUBTITLE = getField(enumTitleAction, "SUBTITLE").get(null);

			chatComponentTextConstructor = getNMSClass("ChatComponentText").getConstructor(String.class);

			try {
				playerListHeaderFooterConstructor = playerListHeaderFooter.getConstructor();
			} catch (NoSuchMethodException e) {
				try {
					playerListHeaderFooterConstructor = playerListHeaderFooter
							.getConstructor(NMSContainer.getIChatBaseComponent());
				} catch (NoSuchMethodException ex) {
				}
			}

			playOutTitleTimesConstructor = packetPlayOutTitle.getConstructor(enumTitleAction,
					NMSContainer.getIChatBaseComponent(), Integer.TYPE, Integer.TYPE, Integer.TYPE);
			titleConstructor = packetPlayOutTitle.getConstructor(enumTitleAction, NMSContainer.getIChatBaseComponent());

			try {
				chatMessageTypeClass = getNMSClass("ChatMessageType");

				for (Object obj : chatMessageTypeClass.getEnumConstants()) {
					if (obj.toString().equalsIgnoreCase("GAME_INFO") || obj.toString().equalsIgnoreCase("ACTION_BAR")) {
						chatMessageType = obj;
						break;
					}
				}
			} catch (ClassNotFoundException e) {
			}

			if (chatMessageTypeClass == null) {
				packetPlayOutChatConstructor = packetPlayOutChatClass
						.getConstructor(NMSContainer.getIChatBaseComponent(), byte.class);
			} else if (chatMessageType != null) {
				try {
					packetPlayOutChatConstructor = packetPlayOutChatClass
							.getConstructor(NMSContainer.getIChatBaseComponent(), chatMessageTypeClass);
				} catch (NoSuchMethodException e) {
					packetPlayOutChatConstructor = packetPlayOutChatClass
							.getConstructor(NMSContainer.getIChatBaseComponent(), chatMessageTypeClass, UUID.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendActionBar(Player player, String message) {
		if (player == null)
			return;

		if (message == null)
			message = "";

		try {
			if (chatMessageTypeClass == null) {
				Reflections.sendPacket(player, packetPlayOutChatConstructor
						.newInstance(chatComponentTextConstructor.newInstance(message), (byte) 2));
			} else if (chatMessageType != null) {
				if (packetPlayOutChatConstructor.getParameterCount() == 2) {
					Reflections.sendPacket(player, packetPlayOutChatConstructor
							.newInstance(chatComponentTextConstructor.newInstance(message), chatMessageType));
				} else {
					Reflections.sendPacket(player, packetPlayOutChatConstructor.newInstance(
							chatComponentTextConstructor.newInstance(message), chatMessageType, player.getUniqueId()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subTitle) {
		if (player == null)
			return;

		try {
			if (title == null)
				title = "";

			if (subTitle == null)
				subTitle = "";

			Object chatTitle = Reflections.getAsIChatBaseComponent(title);
			Object titlePacket = playOutTitleTimesConstructor.newInstance(TIMES, chatTitle, fadeIn, stay, fadeOut);
			Reflections.sendPacket(player, titlePacket);

			titlePacket = titleConstructor.newInstance(TITLE, chatTitle);
			Reflections.sendPacket(player, titlePacket);

			Object chatSubtitle = Reflections.getAsIChatBaseComponent(subTitle);
			Object subTitlePacket = playOutTitleTimesConstructor.newInstance(TIMES, chatSubtitle, fadeIn, stay,
					fadeOut);
			Reflections.sendPacket(player, subTitlePacket);

			chatSubtitle = Reflections.getAsIChatBaseComponent(subTitle);
			subTitlePacket = playOutTitleTimesConstructor.newInstance(SUBTITLE, chatSubtitle, fadeIn, stay, fadeOut);
			Reflections.sendPacket(player, subTitlePacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final RageMode PLUGIN = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public static void sendTabTitle(Player player, String header, String footer) {
		if (player == null)
			return;

		if (header == null)
			header = "";

		if (footer == null)
			footer = "";

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R1)) {
			PLUGIN.getComplement().setPlayerListHeaderFooter(player, header, footer);
			return;
		}

		try {
			try {
				Object packet = playerListHeaderFooterConstructor.newInstance(),
						tabHeader = Reflections.getAsIChatBaseComponent(header),
						tabFooter = Reflections.getAsIChatBaseComponent(footer);

				Class<?> packetClass = packet.getClass();
				if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2)) {
					getField(packetClass, "header").set(packet, tabHeader);
					getField(packetClass, "footer").set(packet, tabFooter);
				} else {
					getField(packetClass, "a").set(packet, tabHeader);
					getField(packetClass, "b").set(packet, tabFooter);
				}

				Reflections.sendPacket(player, packet);
			} catch (Exception f) {
				Object packet = null;

				try {
					if (ServerVersion.isCurrentLower(ServerVersion.v1_12_R1)) {
						packet = playerListHeaderFooterConstructor
								.newInstance(Reflections.getAsIChatBaseComponent(header));
					}
				} catch (IllegalArgumentException e) {
					try {
						packet = playerListHeaderFooterConstructor.newInstance();
					} catch (IllegalArgumentException ex) {
					}
				}

				if (packet != null) {
					getField(packet.getClass(), "b").set(packet, Reflections.getAsIChatBaseComponent(footer));
					Reflections.sendPacket(player, packet);
				}
			}
		} catch (Exception t) {
			t.printStackTrace();
		}
	}

	private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}