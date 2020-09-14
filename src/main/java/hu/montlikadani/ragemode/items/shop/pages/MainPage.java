package hu.montlikadani.ragemode.items.shop.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.NavigationType;
import hu.montlikadani.ragemode.items.shop.ShopCategory;
import hu.montlikadani.ragemode.items.shop.ShopItem;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands;

public class MainPage implements IShop {

	private final List<ShopItem> items = new ArrayList<>();

	private Inventory inv;

	@Override
	public List<ShopItem> getItems() {
		return items;
	}

	@Override
	public Optional<ShopItem> getShopItem(ItemStack item) {
		return items.stream().filter(si -> si.getItem().isSimilar(item)).findFirst();
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	@Override
	public void create(Player player) {
		Configuration conf = RageMode.getInstance().getConfiguration();
		if (!conf.getItemsCfg().contains("lobbyitems.shopitem")
				|| !conf.getItemsCfg().getBoolean("lobbyitems.shopitem.enabled")) {
			return;
		}

		String path = "lobbyitems.shopitem.gui.";
		String guiName = Utils.colors(conf.getItemsCfg().getString(path + "title", "&6RageMode shop"));

		int size = conf.getItemsCfg().getInt(path + "size", 9);
		if (size > 54) {
			size = 54;
		}

		inv = Bukkit.createInventory(this, size, guiName);

		path += "items.";

		for (int i = 0; i < size; i++) {
			String item = conf.getItemsCfg().getString(path + "slot-" + i + ".item", "");
			if (item.isEmpty()) {
				item = "air";
			}

			Material mat = Material.getMaterial(item.toUpperCase());
			if (mat == null) {
				Debug.logConsole(Level.WARNING, "Unknown item type: " + item);
				mat = Material.AIR;
			}

			if (mat == Material.AIR) {
				String filler = conf.getItemsCfg().getString("lobbyitems.shopitem.gui.fillEmptyFields", "air");
				if (!filler.isEmpty()) {
					mat = Material.getMaterial(filler.toUpperCase());
					if (mat == null) {
						Debug.logConsole(Level.WARNING, "Unknown filler item type: " + filler);
						mat = Material.AIR;
					}

					inv.setItem(i, new ItemStack(mat));
				}

				continue;
			}

			ItemStack iStack = new ItemStack(mat);
			ItemMeta iMeta = iStack.getItemMeta();
			if (iMeta == null) {
				inv.setItem(i, new ItemStack(mat));
				continue;
			}

			List<String> list = conf.getItemsCfg().getStringList(path + "slot-" + i + ".lore");
			if (!list.isEmpty()) {
				List<String> lore = new ArrayList<>();
				list.forEach(l -> lore.add(Utils.colors(l)));
				iMeta.setLore(lore);
			}

			String itemName = conf.getItemsCfg().getString(path + "slot-" + i + ".name", "");
			if (!itemName.isEmpty()) {
				iMeta.setDisplayName(itemName.replace("&", "\u00a7"));
			}

			hu.montlikadani.ragemode.NMS.setDurability(iStack,
					(short) conf.getItemsCfg().getDouble(path + "slot-" + i + ".durability", 0));

			iStack.setItemMeta(iMeta);
			inv.setItem(i, iStack);

			ShopItemCommands itemCmds = null;
			if (conf.getItemsCfg().isList(path + "slot-" + i + ".commands")) {
				List<String> commands = conf.getItemsCfg().getStringList(path + "slot-" + i + ".commands");

				NavigationType navigationType = null;
				for (String n : commands) {
					navigationType = NavigationType.getByName(n.toLowerCase());
					if (navigationType == null) {
						navigationType = NavigationType.WITHOUT;
					}

					if (navigationType != null) {
						break;
					}
				}

				itemCmds = new ShopItemCommands(path + "slot-" + i, commands, navigationType);
			} else if (conf.getItemsCfg().isString(path + "slot-" + i + ".command")) {
				String command = conf.getItemsCfg().getString(path + "slot-" + i + ".command", "");
				NavigationType navigationType = NavigationType.getByName(command.toLowerCase());
				if (navigationType == null) {
					navigationType = NavigationType.WITHOUT;
				}

				itemCmds = new ShopItemCommands(path + "slot-" + i, command, navigationType);
			}

			String type = conf.getItemsCfg().getString(path + "slot-" + i + ".category", "").toUpperCase();
			if (type.isEmpty()) {
				type = "MAIN";
			}

			ShopCategory category = ShopCategory.valueOf(type);

			ShopItem shopItem = new ShopItem(iStack, category, guiName);
			if (itemCmds != null) {
				shopItem = new ShopItem(iStack, category, guiName, itemCmds);
			}

			items.add(shopItem);
		}
	}

	@Override
	public void create(Player player, ShopCategory type) {
		create(player);
	}
}
