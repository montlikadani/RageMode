package hu.montlikadani.ragemode.utils.stuff;

import java.util.List;

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

public interface Complement {

	String getTitle(InventoryView view);

	String getDisplayName(ItemMeta meta);

	String getDisplayName(Player player);

	String getLine(SignChangeEvent event, int line);

	String getLine(Sign sign, int line);

	void setLine(SignChangeEvent event, int line, String text);

	void setLine(Sign sign, int line, String text);

	Inventory createInventory(InventoryHolder owner, int size, String title);

	Inventory createInventory(InventoryHolder owner, InventoryType type, String title);

	void setLore(ItemMeta meta, List<String> lore);

	List<String> getLore(ItemMeta meta);

	void setDisplayName(ItemMeta meta, String name);

	void setDeathMessage(PlayerDeathEvent event, String message);

	void setJoinMessage(PlayerJoinEvent event, String message);

	void setQuitMessage(PlayerQuitEvent event, String message);

	void setMotd(ServerListPingEvent event, String motd);

	void kickPlayer(Player player, String message);

	void setPlayerListName(Player player, String text);

	void setDisplayName(Player player, String text);

	String getPlayerListName(Player player);

	void setPrefix(Team team, String prefix);

	void setSuffix(Team team, String suffix);

	void setDisplayName(Objective objective, String dName);

	void setPlayerListHeaderFooter(Player player, String header, String footer);

}
