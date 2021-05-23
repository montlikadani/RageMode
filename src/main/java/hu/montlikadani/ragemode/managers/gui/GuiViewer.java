package hu.montlikadani.ragemode.managers.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;

public class GuiViewer {

	private HumanEntity source;
	private InventoryGuiHandler current;

	public GuiViewer(HumanEntity source, InventoryGuiHandler current) {
		this.source = source;
		this.current = current;
	}

	public HumanEntity getSource() {
		return source;
	}

	public InventoryGuiHandler getCurrent() {
		return current;
	}

	public InventoryView getOpenInventory() {
		return source.getOpenInventory();
	}

	public boolean isOpened() {
		return getOpenInventory().getTopInventory().getHolder() instanceof InventoryGuiHandler;
	}

	public void refreshInventory() {
		source.openInventory(current.getInventory());
		current.setItems();
	}

	public void closeCurrent() {
		if (isOpened()) {
			current.getInventory().clear();
			current.getViewers().remove(this);
		}
	}

	public void open() {
		if (!isOpened()) {
			source.openInventory(current.getInventory());
		}
	}
}
