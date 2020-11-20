package hu.montlikadani.ragemode.API;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.RageMode;

public class RageModeAPI {

	/**
	 * Returns RageMode as a plugin
	 * 
	 * @return {@link Plugin}
	 */
	public static Plugin getPlugin() {
		return JavaPlugin.getPlugin(RageMode.class);
	}

	/**
	 * Gets the RageMode instance
	 * @deprecated We don't need to call another method
	 * @return Instance
	 */
	@Deprecated(forRemoval = true)
	public static RageMode getRageMode() {
		return RageMode.getInstance();
	}
}
