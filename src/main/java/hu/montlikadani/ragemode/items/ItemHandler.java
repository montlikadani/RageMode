package hu.montlikadani.ragemode.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.Utils;

public class ItemHandler implements Cloneable, Supplier<ItemStack> {

	private ItemStack builtItem;
	private Material item;
	private String displayName = "", customName = "";

	private List<String> lore;

	private Enchantment enchant;

	private int amount = 1, enchantLevel = 1, slot = -1;

	private double damage = 0, velocity = 0;

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

	public double getVelocity() {
		return velocity;
	}

	public List<Extra> getExtras() {
		return extras;
	}

	public ItemHandler addExtra(Extra... extra) {
		if (extra != null) {
			for (Extra ext : extra) {
				extras.add(ext);
			}
		}

		return this;
	}

	public ItemHandler setSlot(int slot) {
		this.slot = (slot < 0 || slot > 40) ? 1 : slot;
		return this;
	}

	public ItemHandler setDamage(double damage) {
		this.damage = damage;
		return this;
	}

	public ItemHandler setItem(String name) {
		return (name == null || name.isEmpty()) ? this : setItem(Material.matchMaterial(name.toUpperCase()));
	}

	public ItemHandler setItem(Material item) {
		if (item != null) {
			this.item = item;
		}

		return this;
	}

	public ItemHandler setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public ItemHandler setDisplayName(String displayName) {
		if (displayName != null) {
			this.displayName = displayName;
		}

		return this;
	}

	public ItemHandler setCustomName(String customName) {
		if (customName != null) {
			this.customName = customName;
		}

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

	public ItemHandler setVelocity(double velocity) {
		this.velocity = velocity;
		return this;
	}

	@Override
	public ItemStack get() {
		if (amount < 1 || builtItem != null || item == null) {
			return builtItem;
		}

		ItemStack item = new ItemStack(this.item, amount);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return builtItem = item;
		}

		final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

		if (!displayName.isEmpty()) {
			plugin.getComplement().setDisplayName(meta, Utils.colors(displayName));
		}

		if (lore != null) {
			plugin.getComplement().setLore(meta, Utils.colorList(lore));
		}

		if (enchant != null) {
			meta.addEnchant(enchant, enchantLevel, false);
		}

		item.setItemMeta(meta);
		return builtItem = item;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == item || this == obj || (obj instanceof ItemStack && ((ItemStack) obj).isSimilar(builtItem));
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
				", enchant='" + enchant + '\'' +
				", slot='" + slot + '\'' +
				'}';
	}

	public static final class Extra {

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
