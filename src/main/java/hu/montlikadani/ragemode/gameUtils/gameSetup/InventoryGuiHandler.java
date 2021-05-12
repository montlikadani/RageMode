package hu.montlikadani.ragemode.gameUtils.gameSetup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.Utils;

public final class InventoryGuiHandler implements InventoryHolder {

	public static InvGuiBuilder ofBuilder(String gameName) {
		return new InvGuiBuilder(gameName);
	}

	private static final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private Inventory inv;
	private String gameName;

	public boolean isUpdating = false;

	private final Set<GuiViewer> viewers = new HashSet<>();
	private final Set<GuiItem> guiItems = new HashSet<>();

	private InventoryGuiHandler(String gameName, String title, Set<GuiItem> guiItems) {
		this.gameName = gameName;
		this.guiItems.addAll(guiItems);

		inv = rm.getComplement().createInventory(this, InventoryType.ENDER_CHEST, Utils.colors(title));

		setItems();
	}

	public String getSourceGameName() {
		return gameName;
	}

	public Set<GuiItem> getGuiItems() {
		return guiItems;
	}

	public Set<GuiViewer> getViewers() {
		return viewers;
	}

	public void setItems() {
		inv.clear();

		for (GuiItem guiItem : guiItems) {
			inv.setItem(guiItem.getSlot(), guiItem.getItem());
		}
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	public static final class InvGuiBuilder {

		private String title = "", gameName;

		private final Set<GuiItem> items = new HashSet<>();

		InvGuiBuilder(String gameName) {
			this.gameName = gameName;
		}

		public Set<GuiItem> getItems() {
			return items;
		}

		public InvGuiBuilder title(String title) {
			if (title != null) {
				this.title = title;
			}

			return this;
		}

		public Optional<InvGuiBuilder> filter(Predicate<InvGuiBuilder> predicate) {
			return predicate.test(this) ? Optional.of(this) : Optional.empty();
		}

		public InvGuiBuilder addItem(Consumer<GuiItem> con) {
			GuiItem guiItem = new GuiItem();
			con.accept(guiItem);
			items.add(guiItem);
			return this;
		}

		public InventoryGuiHandler build() {
			return new InventoryGuiHandler(gameName, title, items);
		}
	}

	public static final class GuiItem {

		private ItemStack item;
		private String displayName;
		private List<String> lore;
		private int slot;

		private Consumer<InventoryClickEvent> clickEvent;

		public GuiItem item(int slot, Material mat, String displayName) {
			return item(slot, mat, displayName, new ArrayList<>());
		}

		public GuiItem item(int slot, Material mat, String displayName, List<String> lore) {
			item = new ItemStack(mat);

			this.displayName = displayName == null ? "" : displayName;
			this.lore = lore == null ? new ArrayList<>() : lore;
			this.slot = slot < 0 ? 1 : slot;
			return this;
		}

		public GuiItem clickEvent(Consumer<InventoryClickEvent> clickEvent) {
			this.clickEvent = clickEvent;
			return this;
		}

		public void build() {
			if (item == null) {
				return;
			}

			org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

			if (meta != null) {
				rm.getComplement().setDisplayName(meta, Utils.colors(displayName));
				rm.getComplement().setLore(meta, Utils.colorList(lore));

				if (!meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES) || !meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
					meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
				}

				item.setItemMeta(meta);
			}
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
	}
}
