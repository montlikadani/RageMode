package hu.montlikadani.ragemode.items.shop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

	private ItemStack item;

	private ShopCategory category;

	private String inventoryName;

	private ShopItemCommands itemCommands;

	public ShopItem(ItemStack item, ShopCategory category, String inventoryName) {
		this(item, category, inventoryName, null);
	}

	public ShopItem(ItemStack item, ShopCategory category, String inventoryName, ShopItemCommands itemCommands) {
		this.item = item;
		this.category = category;
		this.inventoryName = inventoryName;
		this.itemCommands = itemCommands;
	}

	public ItemStack getItem() {
		return item;
	}

	public ShopCategory getCategory() {
		return category;
	}

	public String getInventoryName() {
		return inventoryName;
	}

	public ShopItemCommands getItemCommands() {
		return itemCommands;
	}
}
