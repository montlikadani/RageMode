package hu.montlikadani.ragemode.items.shop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

	private ItemStack item;
	private ShopType type;
	private String inventoryName;

	public ShopItem(ItemStack item, ShopType type, String inventoryName) {
		this.item = item;
		this.type = type;
		this.inventoryName = inventoryName;
	}

	public ItemStack getItem() {
		return item;
	}

	public ShopType getType() {
		return type;
	}

	public String getInventoryName() {
		return inventoryName;
	}
}
