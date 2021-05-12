package hu.montlikadani.ragemode.utils.reflection;

import java.lang.reflect.Method;

import hu.montlikadani.ragemode.utils.ServerVersion;

public final class NMSContainer {

	private static Class<?> packetClass, iChatBaseComponent;
	private static Class<?>[] chatBaseDeclaredClasses;
	private static Method jsonComponentMethod;

	static {
		try {
			packetClass = getNMSClass("Packet");
			iChatBaseComponent = getNMSClass("IChatBaseComponent");

			if ((chatBaseDeclaredClasses = iChatBaseComponent.getDeclaredClasses()).length > 0) {
				jsonComponentMethod = chatBaseDeclaredClasses[0].getMethod("a", String.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + ServerVersion.getArrayVersion()[3] + "." + name);
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
