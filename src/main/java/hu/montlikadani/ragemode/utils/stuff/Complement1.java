package hu.montlikadani.ragemode.utils.stuff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("deprecation")
public final class Complement1 implements Complement {

	@Override
	public String getDisplayName(ItemMeta meta) {
		return meta.getDisplayName();
	}

	@Override
	public void setPlayerListName(Player player, String text) {
		player.setPlayerListName(text);
	}

	@Override
	public String getPlayerListName(Player player) {
		return player.getPlayerListName();
	}

	@Override
	public String getTitle(InventoryView view) {
		return view.getTitle();
	}

	@Override
	public String getLine(SignChangeEvent event, int line) {
		String one = event.getLine(line);
		return one == null ? "" : one;
	}

	@Override
	public void setLine(SignChangeEvent event, int line, String text) {
		event.setLine(line, text);
	}

	@Override
	public String getLine(Sign sign, int line) {
		return sign.getLine(line);
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, int size, String title) {
		return Bukkit.createInventory(owner, size, title);
	}

	@Override
	public void setLore(ItemMeta meta, List<String> lore) {
		if (meta != null) {
			meta.setLore(lore);
		}
	}

	@Override
	public void setDisplayName(ItemMeta meta, String name) {
		if (meta != null) {
			meta.setDisplayName(name);
		}
	}

	@Override
	public String getDisplayName(Player player) {
		return player.getDisplayName();
	}

	@Override
	public void setLine(Sign sign, int line, String text) {
		sign.setLine(line, text);
	}

	@Override
	public List<String> getLore(ItemMeta meta) {
		return (meta != null && meta.hasLore()) ? meta.getLore() : new ArrayList<>();
	}

	@Override
	public void setDeathMessage(PlayerDeathEvent event, String message) {
		event.setDeathMessage(message);
	}

	@Override
	public void setJoinMessage(PlayerJoinEvent event, String message) {
		event.setJoinMessage(message);
	}

	@Override
	public void setQuitMessage(PlayerQuitEvent event, String message) {
		event.setQuitMessage(message);
	}

	@Override
	public void setMotd(ServerListPingEvent event, String motd) {
		event.setMotd(motd);
	}

	@Override
	public void kickPlayer(Player player, String message) {
		player.kickPlayer(message);
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
		return Bukkit.createInventory(owner, type, title);
	}

	@Override
	public void setDisplayName(Player player, String text) {
		player.setDisplayName(text);
	}

	@Override
	public void setPrefix(Team team, String prefix) {
		team.setPrefix(prefix);
	}

	@Override
	public void setSuffix(Team team, String suffix) {
		team.setSuffix(suffix);
	}

	@Override
	public void setDisplayName(Objective objective, String dName) {
		objective.setDisplayName(dName);
	}

	@Override
	public void setPlayerListHeaderFooter(Player player, String header, String footer) {
		player.setPlayerListHeaderFooter(header, footer);
	}
}
