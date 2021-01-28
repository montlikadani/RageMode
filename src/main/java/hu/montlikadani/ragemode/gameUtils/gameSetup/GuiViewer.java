package hu.montlikadani.ragemode.gameUtils.gameSetup;

import org.bukkit.entity.Player;

public class GuiViewer {

	private Player source;
	private InventoryGuiHandler current;

	public GuiViewer(Player source, InventoryGuiHandler current) {
		this.source = source;
		this.current = current;
	}

	public Player getSource() {
		return source;
	}

	public InventoryGuiHandler getCurrent() {
		return current;
	}

	public boolean isOpened() {
		return source.getOpenInventory().getTopInventory().getHolder() instanceof InventoryGuiHandler;
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
