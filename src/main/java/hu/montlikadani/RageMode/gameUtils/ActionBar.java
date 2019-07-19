package hu.montlikadani.ragemode.gameUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.Utils;

public class ActionBar {

	//public static String nmsver;
	//public static boolean useOldMethods = false;

	public static void sendActionBar(Player player, String message) {
		if (!player.isOnline())
			return;

		if (message == null)
			message = "";

		try {
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Utils.getClassVersion() + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object packet;
			Class<?> packetPlayOutChatClass = Utils.getNMSClass("PacketPlayOutChat");
			Class<?> packetClass = Utils.getNMSClass("Packet");
			//if (useOldMethods) {
			Class<?> chatSerializerClass = Utils.getNMSClass("ChatSerializer");
			Class<?> iChatBaseComponentClass = Utils.getNMSClass("IChatBaseComponent");
			Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
			Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));

			packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, byte.class }).newInstance(cbc, (byte) 2);
			/*} else {
				Class<?> chatComponentTextClass = Utils.getNMSClass("ChatComponentText");
				Class<?> iChatBaseComponentClass = Utils.getNMSClass("IChatBaseComponent");
				try {
					Class<?> chatMessageTypeClass = Utils.getNMSClass("ChatMessageType");
					Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
					Object chatMessageType = null;
					for (Object obj : chatMessageTypes) {
						if (obj.toString().equals("GAME_INFO")) {
							chatMessageType = obj;
						}
					}
					Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[] { String.class }).newInstance(message);
					packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, chatMessageTypeClass })
							.newInstance(chatCompontentText, chatMessageType);
				} catch (ClassNotFoundException cnfe) {
					Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[] { String.class }).newInstance(message);
					packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, byte.class })
							.newInstance(chatCompontentText, (byte) 2);
				}
			}*/
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
