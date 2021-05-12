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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Complement2 implements Complement {

	private String serialize(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

	private Component deserialize(String t) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(t);
	}

	@Override
	public void setPlayerListName(Player player, String text) {
		player.playerListName(deserialize(text));
	}

	@Override
	public String getPlayerListName(Player player) {
		Component listName = player.playerListName();
		return listName == null ? "" : serialize(listName);
	}

	@Override
	public String getTitle(InventoryView view) {
		return serialize(view.title());
	}

	@Override
	public String getDisplayName(ItemMeta meta) {
		if (meta == null) {
			return "";
		}

		Component dName = meta.displayName();
		return dName == null ? "" : serialize(dName);
	}

	@Override
	public String getLine(SignChangeEvent event, int line) {
		Component comp = event.line(line);
		return comp == null ? "" : serialize(comp);
	}

	@Override
	public void setLine(SignChangeEvent event, int line, String text) {
		event.line(line, deserialize(text));
	}

	@Override
	public String getLine(Sign sign, int line) {
		Component comp = sign.line(line);
		return comp == null ? "" : serialize(comp);
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, int size, String title) {
		return Bukkit.createInventory(owner, size, deserialize(title));
	}

	@Override
	public void setLore(ItemMeta meta, List<String> lore) {
		if (meta == null) {
			return;
		}

		List<Component> l = new ArrayList<>();

		for (String e : lore) {
			l.add(deserialize(e));
		}

		meta.lore(l);
	}

	@Override
	public void setDisplayName(ItemMeta meta, String name) {
		if (meta != null) {
			meta.displayName(deserialize(name));
		}
	}

	@Override
	public String getDisplayName(Player player) {
		return serialize(player.displayName());
	}

	@Override
	public void setLine(Sign sign, int line, String text) {
		sign.line(line, deserialize(text));
	}

	@Override
	public List<String> getLore(ItemMeta meta) {
		List<String> lore = new ArrayList<>();

		if (meta == null) {
			return lore;
		}

		if (meta.hasLore()) {
			for (Component comp : meta.lore()) {
				lore.add(serialize(comp));
			}
		}

		return lore;
	}

	@Override
	public void setDeathMessage(PlayerDeathEvent event, String message) {
		event.deathMessage(deserialize(message));
	}

	@Override
	public void setJoinMessage(PlayerJoinEvent event, String message) {
		event.joinMessage(deserialize(message));
	}

	@Override
	public void setQuitMessage(PlayerQuitEvent event, String message) {
		event.quitMessage(deserialize(message));
	}

	@Override
	public void setMotd(ServerListPingEvent event, String motd) {
		event.motd(deserialize(motd));
	}

	@Override
	public void kickPlayer(Player player, String message) {
		player.kick(deserialize(message));
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
		return Bukkit.createInventory(owner, type, deserialize(title));
	}

	@Override
	public void setDisplayName(Player player, String text) {
		player.displayName(deserialize(text));
	}

	@Override
	public void setPrefix(Team team, String prefix) {
		team.prefix(deserialize(prefix));
	}

	@Override
	public void setSuffix(Team team, String suffix) {
		team.suffix(deserialize(suffix));
	}

	@Override
	public void setDisplayName(Objective objective, String dName) {
		objective.displayName(deserialize(dName));
	}

	@Override
	public void setPlayerListHeaderFooter(Player player, String header, String footer) {
		player.sendPlayerListHeaderAndFooter(deserialize(header), deserialize(footer));
	}
}
