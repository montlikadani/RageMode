package hu.montlikadani.ragemode.gameUtils.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils.Reflections;

public abstract class ActionBar {

	public static void sendActionBar(Player player, String message) {
		if (player == null) {
			return;
		}

		if (message == null)
			message = "";

		String nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		try {
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");

			Object craftPlayer = craftPlayerClass.cast(player);
			Object packet;

			Class<?> packetPlayOutChatClass = Reflections.getNMSClass("PacketPlayOutChat"),
					packetClass = Reflections.getNMSClass("Packet"),
					chatComponentTextClass = Reflections.getNMSClass("ChatComponentText"),
					iChatBaseComponentClass = Reflections.getNMSClass("IChatBaseComponent");

			Object chatCompontentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
			try {
				Class<?> chatMessageTypeClass = Reflections.getNMSClass("ChatMessageType");
				Object chatMessageType = null;
				for (Object obj : chatMessageTypeClass.getEnumConstants()) {
					if (obj.toString().equals("GAME_INFO") || obj.toString().equalsIgnoreCase("ACTION_BAR")) {
						chatMessageType = obj;
						break;
					}
				}

				if (chatMessageType == null) {
					return;
				}

				try {
					packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass)
							.newInstance(chatCompontentText, chatMessageType);
				} catch (NoSuchMethodException e) {
					packet = packetPlayOutChatClass
							.getConstructor(iChatBaseComponentClass, chatMessageTypeClass, UUID.class)
							.newInstance(chatCompontentText, chatMessageType, player.getUniqueId());
				}
			} catch (ClassNotFoundException e1) {
				packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class)
						.newInstance(chatCompontentText, (byte) 2);
			}

			Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
			Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
			Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
			Object playerConnection = playerConnectionField.get(craftPlayerHandle);
			Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
			sendPacketMethod.invoke(playerConnection, packet);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
