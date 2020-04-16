package hu.montlikadani.ragemode.items.shop;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import hu.montlikadani.ragemode.items.shop.inventory.CustomInventoryType;

public interface IShop extends InventoryHolder {

	CustomInventoryType getType();

	void setType(CustomInventoryType type);

	Set<Player> getPlayers();

	boolean isOpened(Player player);

	void removeAll();

	void remove(Player player);

	void create(Player player);
}
