package hu.montlikadani.ragemode.managers.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.managers.gui.interfaces.Applier;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.stuff.Complement;

public final class GuiItem implements Applier<ItemStack> {

	private final Complement complement;

	private ItemStack item = new ItemStack(Material.STONE);
	private List<String> lore = new ArrayList<>();
	private String displayName = "";

	private int slot = 1;

	private Consumer<InventoryClickEvent> clickEvent;

	public GuiItem(Complement complement) {
		this.complement = complement;
	}

	public ItemStack getItem() {
		return item;
	}

	public int getSlot() {
		return slot;
	}

	public Consumer<InventoryClickEvent> getClickEvent() {
		return clickEvent;
	}

	public void setDisplayName(String displayName) {
		if (displayName != null) {
			this.displayName = displayName;
		}
	}

	public void setLore(List<String> lore) {
		if (lore != null) {
			this.lore = lore;
		}
	}

	public void setItem(int slot, Material mat, String displayName) {
		setItem(slot, mat, displayName, null);
	}

	public void setItem(int slot, Material mat, String displayName, List<String> lore) {
		if (mat != null) {
			item = new ItemStack(mat);
		}

		setDisplayName(displayName);
		setLore(lore);

		if (slot >= 0) {
			this.slot = slot;
		}
	}

	public void setClickEvent(Consumer<InventoryClickEvent> clickEvent) {
		this.clickEvent = clickEvent;
	}

	@Override
	public ItemStack apply() {
		org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			complement.setDisplayName(meta, Utils.colors(displayName));
			complement.setLore(meta, Utils.colorList(lore));

			if (!meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES) || !meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
			}

			item.setItemMeta(meta);
		}

		return item;
	}
}