package hu.montlikadani.ragemode.gameUtils.modules;

import static hu.montlikadani.ragemode.utils.reflection.ClazzContainer.classByName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.reflection.ClazzContainer;
import hu.montlikadani.ragemode.utils.reflection.Reflections;

public abstract class TitleSender {

	private static Class<?> chatMessageTypeClass;
	private static Object TIMES, TITLE, SUBTITLE, chatMessageType;
	private static Constructor<?> playerListHeaderFooterConstructor, chatComponentTextConstructor,
			packetPlayOutChatConstructor, playOutTitleTimesConstructor, titleConstructor,
			clientboundSetTitleTextConstructor, clientboundSetSubTitleTextConstructor,
			clientboundSetTitlesAnimationConstructor;

	static {
		try {
			if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_17_R1)) {
				clientboundSetTitleTextConstructor = classByName("net.minecraft.network.protocol.game",
						"ClientboundSetTitleTextPacket").getConstructor(ClazzContainer.getIChatBaseComponent());
				clientboundSetSubTitleTextConstructor = classByName("net.minecraft.network.protocol.game",
						"ClientboundSetSubtitleTextPacket").getConstructor(ClazzContainer.getIChatBaseComponent());
				clientboundSetTitlesAnimationConstructor = classByName("net.minecraft.network.protocol.game",
						"ClientboundSetTitlesAnimationPacket").getConstructor(int.class, int.class, int.class);
			} else {
				Class<?> packetPlayOutTitle = classByName("net.minecraft.network.protocol.game", "PacketPlayOutTitle");
				Class<?>[] declaredClasses = packetPlayOutTitle.getDeclaredClasses();

				if (declaredClasses.length > 0) {
					Class<?> enumTitleAction = declaredClasses[0];

					TIMES = enumTitleAction.getField("TIMES").get(null);
					TITLE = enumTitleAction.getField("TITLE").get(null);
					SUBTITLE = enumTitleAction.getField("SUBTITLE").get(null);

					playOutTitleTimesConstructor = packetPlayOutTitle.getConstructor(enumTitleAction,
							ClazzContainer.getIChatBaseComponent(), int.class, int.class, int.class);
					titleConstructor = packetPlayOutTitle.getConstructor(enumTitleAction,
							ClazzContainer.getIChatBaseComponent());
				}
			}

			chatComponentTextConstructor = classByName("net.minecraft.network.chat", "ChatComponentText")
					.getConstructor(String.class);

			if (ServerVersion.isCurrentLower(ServerVersion.v1_16_R1)) {
				Class<?> playerListHeaderFooter = classByName(null, "PacketPlayOutPlayerListHeaderFooter");

				try {
					playerListHeaderFooterConstructor = playerListHeaderFooter.getConstructor();
				} catch (NoSuchMethodException e) {
					try {
						playerListHeaderFooterConstructor = playerListHeaderFooter
								.getConstructor(ClazzContainer.getIChatBaseComponent());
					} catch (NoSuchMethodException ex) {
					}
				}
			}

			try {
				chatMessageTypeClass = classByName("net.minecraft.network.chat", "ChatMessageType");

				for (Object obj : chatMessageTypeClass.getEnumConstants()) {
					if (obj.toString().equalsIgnoreCase("GAME_INFO") || obj.toString().equalsIgnoreCase("ACTION_BAR")) {
						chatMessageType = obj;
						break;
					}
				}
			} catch (ClassNotFoundException e) {
			}

			Class<?> packetPlayOutChatClass = classByName("net.minecraft.network.protocol.game", "PacketPlayOutChat");

			if (chatMessageTypeClass == null) {
				packetPlayOutChatConstructor = packetPlayOutChatClass
						.getConstructor(ClazzContainer.getIChatBaseComponent(), byte.class);
			} else if (chatMessageType != null) {
				try {
					packetPlayOutChatConstructor = packetPlayOutChatClass
							.getConstructor(ClazzContainer.getIChatBaseComponent(), chatMessageTypeClass);
				} catch (NoSuchMethodException e) {
					packetPlayOutChatConstructor = packetPlayOutChatClass
							.getConstructor(ClazzContainer.getIChatBaseComponent(), chatMessageTypeClass, UUID.class);
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

		if (title == null)
			title = "";

		if (subTitle == null)
			subTitle = "";

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_17_R1)) {
			try {
				Reflections.sendPacket(player,
						clientboundSetTitlesAnimationConstructor.newInstance(fadeIn, stay, fadeOut));
				Reflections.sendPacket(player,
						clientboundSetTitleTextConstructor.newInstance(Reflections.getAsIChatBaseComponent(title)));
				Reflections.sendPacket(player, clientboundSetSubTitleTextConstructor
						.newInstance(Reflections.getAsIChatBaseComponent(subTitle)));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}

		try {
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

	private static Field headerField, footerField;

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

				if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2)) {
					if (headerField == null) {
						headerField = getField(packet.getClass(), "header");
					}

					if (footerField == null) {
						footerField = getField(packet.getClass(), "footer");
					}
				} else {
					if (headerField == null) {
						headerField = getField(packet.getClass(), "a");
					}

					if (footerField == null) {
						footerField = getField(packet.getClass(), "b");
					}
				}

				headerField.set(packet, tabHeader);
				footerField.set(packet, tabFooter);

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
					if (footerField == null) {
						footerField = getField(packet.getClass(), "b");
					}

					footerField.set(packet, Reflections.getAsIChatBaseComponent(footer));
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