package hu.montlikadani.ragemode.managers.gui;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.Utils;

public final class InventoryGuiHandler implements InventoryHolder {

	public static InvGuiBuilder ofBuilder() {
		return new InvGuiBuilder();
	}

	private static final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private Inventory inv;
	private String gameName = "", title = "";

	public boolean isUpdating = false;

	private final Set<GuiViewer> viewers = new HashSet<>();
	private final Set<GuiItem> guiItems = new HashSet<>();

	private InventoryGuiHandler(String title, Set<GuiItem> guiItems) {
		this.title = title;
		this.guiItems.addAll(guiItems);

		inv = getNewInventory();
		setItems();
	}

	private Inventory getNewInventory() {
		return rm.getComplement().createInventory(this, InventoryType.ENDER_CHEST, Utils.colors(title));
	}

	public void setSourceGame(String gameName) {
		if (gameName != null) {
			this.gameName = gameName;
		}
	}

	public void setTitle(String title) {
		if (title != null) {
			this.title = title;
			inv = getNewInventory();
		}
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

	@Override
	public Inventory getInventory() {
		return inv;
	}

	public void setItems() {
		inv.clear();

		for (GuiItem guiItem : guiItems) {
			inv.setItem(guiItem.getSlot(), guiItem.getItem());
		}
	}

	public static final class InvGuiBuilder {

		private String title = "";

		private final Set<GuiItem> items = new HashSet<>();

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
			GuiItem guiItem = new GuiItem(rm.getComplement());
			con.accept(guiItem);
			items.add(guiItem);
			return this;
		}

		public InventoryGuiHandler build() {
			items.forEach(hu.montlikadani.ragemode.managers.gui.interfaces.Applier::apply);
			return new InventoryGuiHandler(title, items);
		}
	}
}
