package hu.montlikadani.ragemode.items.shop.pages;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.NavigationType;
import hu.montlikadani.ragemode.items.shop.ShopCategory;
import hu.montlikadani.ragemode.items.shop.ShopItem;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands.CommandSetting;
import hu.montlikadani.ragemode.utils.Utils;

public final class MainPage implements IShop {

	private final List<ShopItem> items = new java.util.LinkedList<>();

	private Inventory inv;

	@Override
	public List<ShopItem> getItems() {
		return items;
	}

	@Override
	public java.util.Optional<ShopItem> getShopItem(ItemStack item) {
		return items.stream().filter(si -> si.getItem().isSimilar(item)).findFirst();
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	@Override
	public void create(Player player) {
		if (inv != null) {
			ItemStack[] contents = inv.getContents();

			// Update existing inventory contents
			for (int i = 0; i < items.size(); i++) {
				if (contents[i] != null && contents[i].getType() != Material.AIR) {
					inv.setItem(i, items.get(i).getItem());
				}
			}

			return;
		}

		org.bukkit.configuration.MemorySection conf = RM.getConfiguration().getItemsCfg();

		if (!conf.getBoolean("lobbyitems.shopitem.enabled")) {
			return;
		}

		String path = "lobbyitems.shopitem.gui.";
		String guiName = Utils.colors(conf.getString(path + "title", "&6RageMode shop"));

		int size = conf.getInt(path + "size", 9);
		if (size > 54) {
			size = 54;
		}

		inv = RM.getComplement().createInventory(this, size, guiName);

		String filler = conf.getString(path + "fillEmptyFields", "air");

		path += "items.";

		for (int i = 0; i < size; i++) {
			Material mat = Material.matchMaterial(conf.getString(path + "slot-" + i + ".item", ""));

			if (mat == null) {
				mat = Material.AIR;
			}

			if (mat == Material.AIR) {
				if (!filler.isEmpty()) {
					if ((mat = Material.matchMaterial(filler)) == null) {
						mat = Material.AIR;
					}

					inv.setItem(i, new ItemStack(mat));
				}

				continue;
			}

			ItemStack iStack = new ItemStack(mat);
			ItemMeta iMeta = iStack.getItemMeta();
			if (iMeta == null) {
				inv.setItem(i, iStack);
				continue;
			}

			List<String> lore = conf.getStringList(path + "slot-" + i + ".lore");
			if (!lore.isEmpty()) {
				RM.getComplement().setLore(iMeta, Utils.colorList(lore));
			}

			String itemName = conf.getString(path + "slot-" + i + ".name", "");
			if (!itemName.isEmpty()) {
				RM.getComplement().setDisplayName(iMeta, Utils.colors(itemName));
			}

			hu.montlikadani.ragemode.utils.Misc.setDurability(iStack,
					(short) conf.getDouble(path + "slot-" + i + ".durability", 0));

			iStack.setItemMeta(iMeta);
			inv.setItem(i, iStack);

			ShopItemCommands itemCmds = null;
			if (conf.isList(path + "slot-" + i + ".commands")) {
				List<String> commands = conf.getStringList(path + "slot-" + i + ".commands");
				List<CommandSetting> commandSettings = new java.util.ArrayList<>(commands.size());

				NavigationType navigationType = null;
				for (String one : commands) {
					if ((navigationType = NavigationType.getByName(one)) == null) {
						navigationType = NavigationType.WITHOUT;
					}

					commandSettings.add(new CommandSetting(one));
				}

				itemCmds = new ShopItemCommands(new ShopItemCommands.ItemSetting(conf, path + "slot-" + i),
						commandSettings, navigationType);
			} else if (conf.isString(path + "slot-" + i + ".command")) {
				String command = conf.getString(path + "slot-" + i + ".command", "");

				NavigationType navigationType = NavigationType.getByName(command);
				if (navigationType == null) {
					navigationType = NavigationType.WITHOUT;
				}

				itemCmds = new ShopItemCommands(new ShopItemCommands.ItemSetting(conf, path + "slot-" + i),
						new CommandSetting(command), navigationType);
			}

			ShopCategory category;
			try {
				category = ShopCategory.valueOf(conf.getString(path + "slot-" + i + ".category", "").toUpperCase());
			} catch (IllegalArgumentException e) {
				category = ShopCategory.MAIN;
			}

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
