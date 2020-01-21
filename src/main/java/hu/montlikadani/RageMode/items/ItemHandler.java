package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHandler {

	private Material item;
	private String displayName;
	private String customName;
	private List<String> lore;
	private int slot = -1;

	private ItemStack result;

	public Material getItem() {
		return item;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getCustomName() {
		return customName;
	}

	public List<String> getLore() {
		return lore;
	}

	public int getSlot() {
		return slot;
	}

	public ItemHandler setSlot(int slot) {
		if (slot < 0) {
			org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "Slot number can't be less than 0!");
			return null;
		}

		this.slot = slot;
		return this;
	}

	public ItemHandler setItem(Material item) {
		this.item = item;
		return this;
	}

	public ItemHandler setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public ItemHandler setCustomName(String customName) {
		this.customName = customName;
		return this;
	}

	public ItemHandler setLore(List<String> lore) {
		this.lore = lore;
		return this;
	}

	public ItemStack build() {
		return build(null, 1);
	}

	public ItemStack build(Enchantment enchant, int level) {
		if (item == null) {
			return null;
		}

		ItemStack item = new ItemStack(this.item);
		assert item != null;

		ItemMeta meta = item.getItemMeta();
		assert meta != null;

		if (displayName != null && !displayName.isEmpty()) {
			meta.setDisplayName(displayName);
		}

		if (lore != null && !lore.isEmpty()) {
			meta.setLore(lore);
		}

		if (enchant != null && level != 0) {
			meta.addEnchant(enchant, level, false);
		}

		item.setItemMeta(meta);

		result = item;
		return item;
	}

	public ItemStack getResult() {
		return result;
	}
}
