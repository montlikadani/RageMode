package hu.montlikadani.ragemode.gameUtils;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import hu.montlikadani.ragemode.RageMode;

public class BungeeUtils {

	private RageMode plugin;

	public BungeeUtils(RageMode plugin) {
		this.plugin = plugin;
	}

	public void connectToHub(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(hu.montlikadani.ragemode.config.ConfigValues.getHubName());
		player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
}
