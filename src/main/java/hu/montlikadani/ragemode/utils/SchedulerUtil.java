package hu.montlikadani.ragemode.utils;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import hu.montlikadani.ragemode.RageMode;

public abstract class SchedulerUtil {

	private static final org.bukkit.plugin.Plugin RM = org.bukkit.plugin.java.JavaPlugin
			.getProvidingPlugin(RageMode.class);

	public static <V> V submitSync(Supplier<V> sup) {
		if (!RM.getServer().isPrimaryThread()) { // Check if current thread is async
			try {
				return RM.getServer().getScheduler().callSyncMethod(RM, () -> sup.get()).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		return sup.get();
	}
}
