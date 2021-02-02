package hu.montlikadani.ragemode.gameUtils.gameSetup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.utils.Utils;

public final class InventoryGuiHandler implements InventoryHolder {

	public static InvGuiBuilder ofBuilder(Game game) {
		return new InvGuiBuilder(game);
	}

	private Inventory inv;
	private Game game;

	public boolean isUpdating = false;

	private final Set<GuiViewer> viewers = new HashSet<>();
	private final Set<GuiItem> guiItems = new HashSet<>();

	private InventoryGuiHandler(Game game, String title, Set<GuiItem> guiItems) {
		this.game = game;
		this.guiItems.addAll(guiItems);

		inv = Bukkit.createInventory(this, InventoryType.ENDER_CHEST, Utils.colors(title));
		setItems();
	}

	public Game getSourceGame() {
		return game;
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

		private String title = "";
		private Game game;

		private final Set<GuiItem> items = new HashSet<>();

		InvGuiBuilder(Game game) {
			this.game = game;
		}

		public Game getGame() {
			return game;
		}

		public Set<GuiItem> getItems() {
			return items;
		}

		public InvGuiBuilder title(String title) {
			this.title = title == null ? "" : title;
			return this;
		}

		public Optional<InvGuiBuilder> filter(Predicate<InvGuiBuilder> predicate) {
			return predicate.test(this) ? Optional.of(this) : Optional.empty();
		}

		public InvGuiBuilder addItem(Consumer<GuiItem> con) {
			GuiItem guiItem = new GuiItem(this);
			con.accept(guiItem);
			items.add(guiItem);
			return this;
		}

		public InventoryGuiHandler build() {
			return new InventoryGuiHandler(game, title, items);
		}
	}

	public static final class GuiItem {

		private InvGuiBuilder igb;
		private ItemStack item;
		private int slot;

		private Consumer<InventoryClickEvent> clickEvent;

		private final List<String> extraCommands = new ArrayList<>();

		GuiItem(InvGuiBuilder igb) {
			this.igb = igb;
		}

		public GuiItem item(int slot, org.bukkit.Material mat, Consumer<ItemStack> con) {
			ItemStack is = new ItemStack(mat);
			con.accept(is);
			item = is;
			this.slot = slot < 0 ? 1 : slot;
			return this;
		}

		public GuiItem addExtraCommand(String command) {
			extraCommands.add(command);
			return this;
		}

		public GuiItem clickEvent(Consumer<InventoryClickEvent> clickEvent) {
			this.clickEvent = clickEvent;
			return this;
		}

		public InvGuiBuilder build() {
			if (item != null && item.hasItemMeta()) {
				if (item.hasDisplayName()) {
					item.setDisplayName(Utils.colors(item.getDisplayName()));
				}

				if (item.hasLore()) {
					item.setLore(Utils.colorList(item.getLore()));
				}
			}

			return igb;
		}

		public ItemStack getItem() {
			return item;
		}

		public int getSlot() {
			return slot;
		}

		public List<String> getExtraCommands() {
			return extraCommands;
		}

		public Consumer<InventoryClickEvent> getClickEvent() {
			return clickEvent;
		}
	}
}
