package hu.montlikadani.ragemode.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHandler implements Cloneable, Supplier<ItemStack> {

	private ItemStack builtItem;
	private Material item;

	private String displayName;
	private String customName;

	private List<String> lore;

	private Enchantment enchant;

	private int amount = 1, enchantLevel = 1, slot = -1;

	private double damage = 0;

	private final List<Extra> extras = new ArrayList<>();

	public ItemHandler() {
	}

	public ItemHandler(Consumer<ItemHandler> con) {
		con.accept(this);
	}

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

	public double getDamage() {
		return damage;
	}

	public List<Extra> getExtras() {
		return extras;
	}

	public ItemHandler addExtra(Extra... extra) {
		if (extra != null && extra.length != 0) {
			Arrays.asList(extra).forEach(extras::add);
		}

		return this;
	}

	public ItemHandler setSlot(int slot) {
		this.slot = slot < 0 ? 1 : slot;
		return this;
	}

	public ItemHandler setDamage(double damage) {
		this.damage = damage;
		return this;
	}

	public ItemHandler setItem(String name) {
		return (name == null || name.isEmpty()) ? null : setItem(Material.valueOf(name.toUpperCase()));
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
		this.displayName = displayName == null ? "" : displayName;
		return this;
	}

	public ItemHandler setCustomName(String customName) {
		this.customName = customName == null ? "" : customName;
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

	@Override
	public ItemStack get() {
		if (builtItem != null) {
			return builtItem;
		}

		if (item == null) {
			return null;
		}

		ItemStack item = new ItemStack(this.item, amount);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;

		if (!displayName.trim().isEmpty()) {
			meta.setDisplayName(displayName);
		}

		if (lore != null) {
			meta.setLore(lore);
		}

		if (enchant != null) {
			meta.addEnchant(enchant, enchantLevel, false);
		}

		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == builtItem || obj == item || super.equals(obj);
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + '{' +
				"displayName='" + displayName + '\'' +
				", lore='" + lore + '\'' +
				", item='" + item + '\'' +
				", amount='" + amount + '\'' +
				", enchant=''" + enchant + '\'' +
				", slot='" + slot + '\'' +
				'}';
	}

	public static class Extra {

		public Extra() {
		}

		private String extraName;
		private List<String> extraLore;

		public Extra setExtraName(String extraName) {
			this.extraName = extraName == null ? "" : extraName;
			return this;
		}

		public Extra setExtraLore(List<String> extraLore) {
			this.extraLore = extraLore == null ? new ArrayList<>() : extraLore;
			return this;
		}

		public String getExtraName() {
			return extraName;
		}

		public List<String> getExtraLore() {
			return extraLore;
		}
	}
}
