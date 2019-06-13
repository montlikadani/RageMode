package hu.montlikadani.ragemode.signs;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.events.SignsUpdateEvent;

public class SignScheduler implements Runnable, Listener {

	private final RageMode plugin;

	public SignScheduler(RageMode plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		final List<SignData> signs = SignCreator.getSignData();
		SignsUpdateEvent event = new SignsUpdateEvent(signs);
		Bukkit.getPluginManager().callEvent(event);
		Bukkit.getScheduler().runTaskLater(plugin, this, plugin.getConfiguration().getCfg().getInt("signs.update-time") * 20L);
	}

	@EventHandler
	public void onEvent(SignsUpdateEvent event) {
		if (!event.isCancelled()) {
			Iterator<SignData> list = event.getSigns().iterator();
			while (list.hasNext()) {
				SignData sign = list.next();
				if (sign != null)
					sign.updateSign();
			}
		}
	}
}
