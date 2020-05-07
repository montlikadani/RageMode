package hu.montlikadani.ragemode.items.shop.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.items.shop.NavigationType;
import hu.montlikadani.ragemode.items.shop.ShopCategory;
import hu.montlikadani.ragemode.items.shop.ShopItem;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class PotionEffectPage implements IShop {

	private final List<ShopItem> items = new ArrayList<>();

	private Inventory inv;

	@Override
	public List<ShopItem> getItems() {
		return items;
	}

	@Override
	public ShopItem getShopItem(ItemStack item) {
		if (item == null) {
			return null;
		}

		for (ShopItem si : items) {
			if (si.getItem().isSimilar(item)) {
				return si;
			}
		}

		return null;
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	@Override
	public void create(Player player) {
		String mainPath = "lobbyitems.shopitem.gui.";
		FileConfiguration conf = RageMode.getInstance().getConfiguration().getItemsCfg();

		ConfigurationSection section = conf.getConfigurationSection(mainPath + "items");
		if (section == null) {
			return;
		}

		mainPath += "items.";

		for (String guiItems : section.getKeys(false)) {
			String type = section.getString(guiItems + ".category", "").toUpperCase();
			if (type.isEmpty()) {
				continue;
			}

			ShopCategory shopCategory = ShopCategory.valueOf(type);

			mainPath += guiItems + ".gui";

			String guiName = Utils.colors(conf.getString(mainPath + ".title", "&6RageMode shop"));

			int size = conf.getInt(mainPath + ".size", 9);
			if (size > 54) {
				size = 54;
			}

			inv = Bukkit.createInventory(this, size, guiName);

			ConfigurationSection sec = conf.getConfigurationSection(mainPath + ".items");
			if (sec == null) {
				break;
			}

			for (int i = 0; i < size; i++) {
				String effectItems = "slot-" + i;
				String item = sec.getString(effectItems + ".item", "");
				if (item.isEmpty()) {
					item = "air";
				}

				Material mat = Material.getMaterial(item.toUpperCase());
				if (mat == null) {
					Debug.logConsole(Level.WARNING, "Unknown item type: " + item);
					mat = Material.AIR;
				}

				if (mat == Material.AIR) {
					String filler = conf.getString(mainPath + ".fillEmptyFields", "air");
					if (!filler.isEmpty()) {
						mat = Material.getMaterial(filler.toUpperCase());
						if (mat == null) {
							Debug.logConsole(Level.WARNING, "Unknown filler item type: " + item);
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

				List<String> list = sec.getStringList(effectItems + ".lore");
				if (!list.isEmpty()) {
					List<String> lore = new ArrayList<>();

					for (String l : list) {
						PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());

						double costValue = sec.getDouble(effectItems + ".cost.value", 0d);
						int pointsValue = sec.getInt(effectItems + ".cost.points", 0);

						String cost = Double.toString(costValue);
						String points = Integer.toString(pointsValue);

						if (RageMode.getInstance().isVaultEnabled()
								&& !RageMode.getInstance().getEconomy().has(player, costValue)) {
							cost = "&c" + cost;
						}

						if (pp != null && !pp.hasPoints(pointsValue)) {
							points = "&c" + pointsValue;
						}

						if (LobbyShop.BOUGHTITEMS.containsKey(player)) {
							BoughtElements elements = LobbyShop.BOUGHTITEMS.get(player);

							cost = Double.toString(elements.getCost());
							if (RageMode.getInstance().isVaultEnabled()
									&& !RageMode.getInstance().getEconomy().has(player, elements.getCost())) {
								cost = "&c" + cost;
							}

							points = Integer.toString(elements.getPoints());
							if (pp != null && !pp.hasPoints(elements.getPoints())) {
								points = "&c" + elements.getPoints();
							}
						}

						l = l.replace("%cost%", cost);
						l = l.replace("%required_points%", points);

						if (RageMode.getInstance().isVaultEnabled()) {
							if (LobbyShop.BOUGHTITEMS.containsKey(player)) {
								l = l.replace("%money%",
										Double.toString(RageMode.getInstance().getEconomy().getBalance(player)
												- LobbyShop.BOUGHTITEMS.get(player).getCost()));
							} else {
								l = l.replace("%money%",
										Double.toString(RageMode.getInstance().getEconomy().getBalance(player)));
							}
						}

						if (pp != null) {
							l = l.replace("%points%", Integer.toString(pp.getPoints()));
						}

						lore.add(Utils.colors(l));
					}

					iMeta.setLore(lore);
				}

				String itemName = sec.getString(effectItems + ".name", "");
				if (!itemName.isEmpty()) {
					iMeta.setDisplayName(itemName.replace("&", "\u00a7"));
				}

				hu.montlikadani.ragemode.NMS.setDurability(iStack,
						(short) sec.getDouble(effectItems + ".durability", 0));

				iStack.setItemMeta(iMeta);
				inv.setItem(i, iStack);

				ShopItemCommands itemCmds = null;
				if (sec.isList(effectItems + ".commands")) {
					List<String> commands = sec.getStringList(effectItems + ".commands");

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

					itemCmds = new ShopItemCommands(mainPath + ".items." + effectItems, commands, navigationType);
				} else if (sec.isString(effectItems + ".command")) {
					String command = sec.getString(effectItems + ".command", "");
					NavigationType navigationType = NavigationType.getByName(command.toLowerCase());
					if (navigationType == null) {
						navigationType = NavigationType.WITHOUT;
					}

					itemCmds = new ShopItemCommands(mainPath + ".items." + effectItems, command, navigationType);
				}

				ShopItem shopItem = new ShopItem(iStack, shopCategory, guiName);
				if (itemCmds != null) {
					shopItem = new ShopItem(iStack, shopCategory, guiName, itemCmds);
				}

				items.add(shopItem);
			}
		}

		player.openInventory(inv);
	}
}
