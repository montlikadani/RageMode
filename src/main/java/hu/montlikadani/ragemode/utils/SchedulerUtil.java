package hu.montlikadani.ragemode.utils;

import java.util.concurrent.Future;

import hu.montlikadani.ragemode.RageMode;

public abstract class SchedulerUtil {

	private static final org.bukkit.plugin.Plugin RM = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(RageMode.class);

	public static <V> Future<V> submitSync(Runnable run, V type) {
		return RM.getServer().getScheduler().callSyncMethod(RM, () -> {
			run.run();
			return type;
		});
	}
}
