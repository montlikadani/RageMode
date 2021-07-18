package hu.montlikadani.ragemode.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.utils.ServerVersion;

public final class Reflections {

	private static Method handleMethod, sendPacketMethod;
	private static Field playerConnectionField;

	public static Object getAsIChatBaseComponent(String name) throws Exception {
		if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R2)) {
			Class<?> chatSerializer = ClazzContainer.classByName(null, "ChatSerializer");
			return ClazzContainer.getIChatBaseComponent().cast(
					chatSerializer.getMethod("a", String.class).invoke(chatSerializer, "{\"text\":\"" + name + "\"}"));
		}

		return ClazzContainer.getJsonComponentMethod().invoke(ClazzContainer.getIChatBaseComponent(),
				"{\"text\":\"" + name + "\"}");
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			if (handleMethod == null) {
				handleMethod = player.getClass().getMethod("getHandle");
			}

			Object handle = handleMethod.invoke(player);

			if (playerConnectionField == null) {
				playerConnectionField = handle.getClass().getField(
						(ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_17_R1) ? "b" : "playerConnection"));
			}

			Object playerConnection = playerConnectionField.get(handle);

			if (sendPacketMethod == null) {
				sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", ClazzContainer.getPacket());
			}

			sendPacketMethod.invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
