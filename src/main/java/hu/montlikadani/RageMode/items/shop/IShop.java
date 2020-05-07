package hu.montlikadani.ragemode.items.shop;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface IShop extends InventoryHolder {

	List<ShopItem> getItems();

	ShopItem getShopItem(ItemStack item);

	void create(Player player);
}
