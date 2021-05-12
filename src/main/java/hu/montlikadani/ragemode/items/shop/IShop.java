package hu.montlikadani.ragemode.items.shop;

import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.RageMode;

public interface IShop extends InventoryHolder {

	static final RageMode RM = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	List<ShopItem> getItems();

	Optional<ShopItem> getShopItem(ItemStack item);

	void create(Player player);

	void create(Player player, ShopCategory type);
}
