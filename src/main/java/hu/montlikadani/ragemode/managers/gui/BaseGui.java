package hu.montlikadani.ragemode.managers.gui;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.gui.type.SetupGui;

public abstract class BaseGui implements org.bukkit.event.Listener {

	private static final BaseGui setupGui = new SetupGui();

	public static BaseGui getSetupGui() {
		return setupGui;
	}

	protected final Set<InventoryGuiHandler> activeInventories = new HashSet<>();

	protected final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public final Set<InventoryGuiHandler> getActiveInventories() {
		return activeInventories;
	}

	public final GuiViewer openGui(HumanEntity source, Game sourceGame) {
		InventoryGuiHandler igh = load(sourceGame);

		GuiViewer guiViewer = igh.getViewers().stream().filter(gv -> gv.getSource() == source).findFirst()
				.orElseGet(() -> {
					GuiViewer gv = new GuiViewer(source, igh);
					igh.getViewers().add(gv);
					return gv;
				});

		guiViewer.closeCurrent();
		guiViewer.open();

		rm.getServer().getPluginManager().registerEvents(this, rm);

		activeInventories.add(igh);
		return guiViewer;
	}

	public abstract InventoryGuiHandler load(Game game);

	public abstract InventoryGuiHandler load(HumanEntity human);

	public abstract void updateContents(GuiViewer guiViewer);

	public abstract void updateTitle(GuiViewer guiViewer);

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (!rm.equals(event.getPlugin())) {
			return;
		}

		activeInventories.forEach(igh -> new HashSet<>(igh.getViewers()).forEach(viewer -> {
			viewer.closeCurrent();
			viewer.getSource().closeInventory();
		}));

		HandlerList.unregisterAll(this);
		activeInventories.clear();
	}

	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		activeInventories.removeIf(igh -> {
			if (!igh.isUpdating) {
				for (GuiViewer viewer : new HashSet<>(igh.getViewers())) {
					if (viewer.getSource() == event.getPlayer()) {
						viewer.closeCurrent();
						HandlerList.unregisterAll(this);
						return true;
					}
				}
			}

			return false;
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onItemClick(InventoryClickEvent event) {
		if (!(event.getInventory().getHolder() instanceof InventoryGuiHandler)) {
			return;
		}

		org.bukkit.inventory.ItemStack currentItem = event.getCurrentItem();
		if (currentItem == null) {
			return;
		}

		event.setCancelled(true);

		HumanEntity who = event.getWhoClicked();

		for (InventoryGuiHandler invGui : activeInventories) {
			if (invGui.isUpdating) {
				continue;
			}

			Game sourceGame = GameUtils.getGame(invGui.getSourceGameName());
			if (sourceGame != null && sourceGame.isRunning()) {
				continue;
			}

			for (GuiViewer guiViewer : invGui.getViewers()) {
				if (guiViewer.getSource() == who) {
					for (GuiItem guiItem : invGui.getGuiItems()) {
						if (guiItem.getClickEvent() != null && guiItem.getItem().isSimilar(currentItem)) {
							guiItem.getClickEvent().accept(event);
							updateContents(guiViewer);
							break;
						}
					}

					break;
				}
			}
		}
	}
}
