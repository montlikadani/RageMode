package hu.montlikadani.ragemode.utils.reflection;

import java.lang.reflect.Method;

import hu.montlikadani.ragemode.utils.ServerVersion;

public final class ClazzContainer {

	private static Class<?> packetClass, iChatBaseComponent;
	private static Class<?>[] chatBaseDeclaredClasses;
	private static Method jsonComponentMethod;

	static {
		try {
			packetClass = classByName("net.minecraft.network.protocol", "Packet");
			iChatBaseComponent = classByName("net.minecraft.network.chat", "IChatBaseComponent");

			if ((chatBaseDeclaredClasses = iChatBaseComponent.getDeclaredClasses()).length > 0) {
				jsonComponentMethod = chatBaseDeclaredClasses[0].getMethod("a", String.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Class<?> classByName(String newPackageName, String name) throws ClassNotFoundException {
		if (ServerVersion.isCurrentLower(ServerVersion.v1_17_R1) || newPackageName == null) {
			newPackageName = "net.minecraft.server." + ServerVersion.getArrayVersion()[3];
		}

		return Class.forName(newPackageName + "." + name);
	}

	public static Class<?> getPacket() {
		return packetClass;
	}

	public static Class<?> getIChatBaseComponent() {
		return iChatBaseComponent;
	}

	public static Class<?>[] getChatBaseDeclaredClasses() {
		return chatBaseDeclaredClasses;
	}

	public static Method getJsonComponentMethod() {
		return jsonComponentMethod;
	}
}
