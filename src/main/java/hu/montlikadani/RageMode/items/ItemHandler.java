package hu.montlikadani.ragemode.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHandler implements Cloneable {

	private Material item;
	private int amount = 1;
	private String displayName;
	private String customName;
	private List<String> lore;
	private Enchantment enchant;
	private int enchantLevel = 1;
	private int slot = -1;

	private ItemStack result;

	public Material getItem() {
		return item;
	}

	public int getAmount() {
		return amount;
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

	public Enchantment getEnchant() {
		return enchant;
	}

	public int getEnchantLevel() {
		return enchantLevel;
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

	public ItemHandler setAmount(int amount) {
		this.amount = amount < 1 ? 1 : amount;
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

	public ItemHandler setEnchant(Enchantment enchant) {
		this.enchant = enchant;
		return this;
	}

	public ItemHandler setEnchantLevel(int enchantLevel) {
		this.enchantLevel = enchantLevel < 1 ? 1 : enchantLevel;
		return this;
	}

	public ItemStack build() {
		if (item == null) {
			return null;
		}

		ItemStack item = new ItemStack(this.item, amount);
		assert item != null;

		ItemMeta meta = item.getItemMeta();
		assert meta != null;

		if (displayName != null && !displayName.isEmpty()) {
			meta.setDisplayName(displayName);
		}

		if (lore != null && !lore.isEmpty()) {
			meta.setLore(lore);
		}

		if (enchant != null) {
			meta.addEnchant(enchant, enchantLevel, false);
		}

		item.setItemMeta(meta);

		result = item;
		return item;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return null;
	}
}
