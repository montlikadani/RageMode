package hu.montlikadani.ragemode.items.shop.pages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.items.shop.NavigationType;
import hu.montlikadani.ragemode.items.shop.ShopCategory;
import hu.montlikadani.ragemode.items.shop.ShopItem;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands.CommandSetting;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.Utils;

public final class NextPage implements IShop {

	private final List<ShopItem> items = new java.util.LinkedList<>();
	private final List<InventoryContent> invContent = new ArrayList<>();

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

	private void update(Player player) {
		ItemStack[] contents = inv.getContents();

		for (int i = 0; i < items.size(); i++) {
			if (contents[i] == null) {
				continue;
			}

			InventoryContent content = invContent.get(i);

			if (contents[i].getType() == content.fillerItem) {
				continue;
			}

			ItemStack item = content.shopItem.getItem().clone();
			ItemMeta meta = item.getItemMeta().clone();

			String itemName = content.itemName;
			if (!itemName.isEmpty()) {
				RM.getComplement().setDisplayName(meta, Utils.colors(itemName));
			}

			if (!content.lore.isEmpty()) {
				setLore(player, meta, new ArrayList<>(content.lore),
						content.shopItem.getItemCommands().getItemSetting());
			}

			item.setItemMeta(meta);

			inv.setItem(i, item);
			items.set(i, new ShopItem(item, content.shopItem.getCategory(), content.shopItem.getInventoryName(),
					content.shopItem.getItemCommands()));
		}
	}

	@Override
	public void create(Player player) {
		create(player, ShopCategory.MAIN);
	}

	@Override
	public void create(Player player, ShopCategory type) {
		if (inv != null && !items.isEmpty()) {
			update(player);
			player.openInventory(inv);
			return;
		}

		ConfigurationSection section = RM.getConfiguration().getItemsCfg()
				.getConfigurationSection("lobbyitems.shopitem.gui.items");
		if (section == null) {
			return;
		}

		for (String guiItem : section.getKeys(false)) {
			ShopCategory category = null;
			try {
				category = ShopCategory.valueOf(section.getString(guiItem + ".category", "").toUpperCase());
			} catch (IllegalArgumentException e) {
			}

			if (category != type || (type == ShopCategory.ITEMTRAILS && !ConfigValues.isUseArrowTrails())) {
				continue;
			}

			String guiName = Utils.colors(section.getString(guiItem + ".gui.title", "&6RageMode shop"));

			int size = section.getInt(guiItem + ".gui.size", 9);
			if (size > 54) {
				size = 54;
			}

			inv = RM.getComplement().createInventory(this, size, guiName);

			ConfigurationSection sec = section.getConfigurationSection(guiItem + ".gui.items");
			if (sec == null) {
				break;
			}

			String filler = section.getString(guiItem + ".gui.fillEmptyFields", "air");
			Material fMat = Material.AIR;

			if (!filler.isEmpty() && (fMat = Material.matchMaterial(filler)) == null) {
				fMat = Material.AIR;
			}

			ItemStack fillerItem = new ItemStack(fMat);

			for (int i = 0; i < size; i++) {
				String currentSlot = "slot-" + i;

				Material mat = Material.matchMaterial(sec.getString(currentSlot + ".item", ""));
				if (mat == null) {
					mat = Material.AIR;
				}

				InventoryContent inventoryContent = new InventoryContent();

				if (mat == Material.AIR) {
					inv.setItem(i, fillerItem);
					inventoryContent.fillerItem = fMat;
					invContent.add(inventoryContent);
					continue;
				}

				ItemStack iStack = new ItemStack(mat);
				ItemMeta iMeta = iStack.getItemMeta();
				if (iMeta == null) {
					inv.setItem(i, iStack);
					continue;
				}

				final ShopItemCommands.ItemSetting itemSetting = new ShopItemCommands.ItemSetting(sec, currentSlot);

				List<String> lore = sec.getStringList(currentSlot + ".lore");
				if (!lore.isEmpty()) {
					setLore(player, iMeta, new ArrayList<>(lore), itemSetting);
				}

				String itemName = sec.getString(currentSlot + ".name", "");
				if (!itemName.isEmpty()) {
					RM.getComplement().setDisplayName(iMeta, Utils.colors(itemName));
				}

				hu.montlikadani.ragemode.utils.Misc.setDurability(iStack,
						(short) sec.getDouble(currentSlot + ".durability", 0));

				iStack.setItemMeta(iMeta);
				inv.setItem(i, iStack);

				inventoryContent.itemName = itemName;
				inventoryContent.lore = lore;

				ShopItemCommands itemCmds = null;
				if (sec.isList(currentSlot + ".commands")) {
					List<String> commands = sec.getStringList(currentSlot + ".commands");
					List<CommandSetting> commandSettings = new ArrayList<>(commands.size());

					NavigationType navigationType = null;
					for (String one : commands) {
						if ((navigationType = NavigationType.getByName(one)) == null) {
							navigationType = NavigationType.WITHOUT;
						}

						commandSettings.add(new CommandSetting(one));
					}

					itemCmds = new ShopItemCommands(itemSetting, commandSettings, navigationType);
				} else if (sec.isString(currentSlot + ".command")) {
					String command = sec.getString(currentSlot + ".command", "");

					NavigationType navigationType = NavigationType.getByName(command);
					if (navigationType == null) {
						navigationType = NavigationType.WITHOUT;
					}

					itemCmds = new ShopItemCommands(itemSetting, new CommandSetting(command), navigationType);
				}

				if (itemCmds == null) {
					itemCmds = new ShopItemCommands(itemSetting, new ArrayList<>(), NavigationType.WITHOUT);
				}

				inventoryContent.shopItem = new ShopItem(iStack, type, guiName, itemCmds);
				invContent.add(inventoryContent);
				this.items.add(inventoryContent.shopItem);
			}
		}

		if (inv != null)
			player.openInventory(inv);
	}

	private void setLore(Player player, ItemMeta meta, List<String> lore, ShopItemCommands.ItemSetting itemSetting) {
		for (int lo = 0; lo < lore.size(); lo++) {
			String one = lore.get(lo);

			double costValue = itemSetting.getCost();
			int itemAmount = itemSetting.getGiveGameItem() == null ? 0 : itemSetting.getGiveGameItem().getAmount();
			int pointsValue = itemSetting.getPoints();

			BoughtElements boughtElements = LobbyShop.BOUGHT_ITEMS.get(player.getUniqueId());
			if (boughtElements != null) {
				costValue = boughtElements.getCost();
				pointsValue = boughtElements.getPoints();

				if (boughtElements.getBought() instanceof ItemStack) {
					itemAmount = boughtElements.<ItemStack>getBought().getAmount();
				}

				ShopItemCommands.ItemSetting.ItemTrail itemTrail = itemSetting.getItemTrail();

				if (boughtElements.getBought() != null && itemTrail != null && itemTrail.getParticle() != null
						&& boughtElements.getBought().toString().equalsIgnoreCase(itemTrail.getParticle().toString())) {
					one = one.replace("%activated%", "&a&lActivated!");
				} else {
					one = one.replace("%activated%", "");
				}
			} else {
				one = one.replace("%activated%", "");
			}

			String cost = Double.toString(costValue);
			String points = Integer.toString(pointsValue);

			if (RM.isVaultEnabled() && !RM.getEconomy().has(player, costValue)) {
				cost = "&c" + cost;
			}

			PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());
			if (pp != null) {
				one = one.replace("%points%", Integer.toString(pp.getPoints()));

				if (!pp.hasPoints(pointsValue)) {
					points = "&c" + points;
				}
			} else {
				one = one.replace("%points%", "0");
			}

			one = one.replace("%cost%", cost);
			one = one.replace("%required_points%", points);
			one = one.replace("%amount%", Integer.toString(itemAmount));

			if (RM.isVaultEnabled()) {
				if (boughtElements != null) {
					one = one.replace("%money%",
							Double.toString(RM.getEconomy().getBalance(player) - boughtElements.getCost()));
				} else {
					one = one.replace("%money%", Double.toString(RM.getEconomy().getBalance(player)));
				}
			}

			lore.set(lo, Utils.colors(one));
		}

		RM.getComplement().setLore(meta, lore);
	}

	private class InventoryContent {

		private Material fillerItem;

		private List<String> lore;
		private String itemName = "";
		private ShopItem shopItem;
	}
}
