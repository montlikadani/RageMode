package hu.montlikadani.ragemode.utils.reflection;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.utils.ServerVersion;

public final class Reflections {

	private static int jVersion = -1;

	public static Object getAsIChatBaseComponent(String name) throws Exception {
		if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R2)) {
			Class<?> chatSerializer = NMSContainer.getNMSClass("ChatSerializer");
			return NMSContainer.getIChatBaseComponent().cast(
					chatSerializer.getMethod("a", String.class).invoke(chatSerializer, "{\"text\":\"" + name + "\"}"));
		}

		return NMSContainer.getJsonComponentMethod().invoke(NMSContainer.getIChatBaseComponent(),
				"{\"text\":\"" + name + "\"}");
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", NMSContainer.getPacket()).invoke(playerConnection, packet);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static int getCurrentJavaVersion() {
		if (jVersion != 0) {
			return jVersion;
		}

		String currentVersion = System.getProperty("java.version");
		String[] split = currentVersion.split("_", 2);

		if (split.length > 0) {
			currentVersion = split[0];
		}

		currentVersion = currentVersion.replaceAll("[^\\d]|_", "");

		for (int i = 8; i <= 18; i++) {
			if (currentVersion.contains(Integer.toString(i))) {
				return jVersion = i;
			}
		}

		return 0;
	}
}
