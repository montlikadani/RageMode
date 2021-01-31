package hu.montlikadani.ragemode.items.shop.pages;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.items.shop.BoughtElements;
import hu.montlikadani.ragemode.items.shop.IShop;
import hu.montlikadani.ragemode.items.shop.LobbyShop;
import hu.montlikadani.ragemode.items.shop.NavigationType;
import hu.montlikadani.ragemode.items.shop.ShopCategory;
import hu.montlikadani.ragemode.items.shop.ShopItem;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

public class NextPage implements IShop {

	private final List<ShopItem> items = new LinkedList<>();

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
		create(player, ShopCategory.MAIN);
	}

	@Override
	public void create(Player player, ShopCategory type) {
		String mainPath = "lobbyitems.shopitem.gui.";
		FileConfiguration conf = RageMode.getInstance().getConfiguration().getItemsCfg();

		ConfigurationSection section = conf.getConfigurationSection(mainPath + "items");
		if (section == null) {
			return;
		}

		mainPath += "items.";

		for (String guiItem : section.getKeys(false)) {
			String category = section.getString(guiItem + ".category", "").toUpperCase();
			if (category.isEmpty() || ShopCategory.valueOf(category) != type) {
				continue;
			}

			if (type == ShopCategory.ITEMTRAILS && !ConfigValues.isUseArrowTrails()) {
				continue;
			}

			mainPath += guiItem + ".gui";

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
				String slots = "slot-" + i;
				String item = sec.getString(slots + ".item", "");
				if (item.isEmpty()) {
					item = "air";
				}

				Material mat = Material.matchMaterial(item);
				if (mat == null) {
					Debug.logConsole(Level.WARNING, "Unknown item type: " + item);
					mat = Material.AIR;
				}

				if (mat == Material.AIR) {
					String filler = conf.getString(mainPath + ".fillEmptyFields", "air");
					if (!filler.isEmpty()) {
						mat = Material.matchMaterial(filler);
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
					inv.setItem(i, iStack);
					continue;
				}

				List<String> list = sec.getStringList(slots + ".lore");
				if (!list.isEmpty()) {
					List<String> lore = new java.util.ArrayList<>();

					for (String l : list) {
						PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());

						String[] itemSplit = sec.getString(slots + ".giveitem", "").split(":");
						int itemAmount = itemSplit.length > 1 ? Integer.parseInt(itemSplit[1]) : 1;

						double costValue = sec.getDouble(slots + ".cost.value", 0d);
						int pointsValue = sec.getInt(slots + ".cost.points", 0);

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

							if (elements.getBought() instanceof ItemStack) {
								itemAmount = elements.<ItemStack>getBought().getAmount();
							}

							l = l.replace("%activated%", (elements.getBought() != null
									&& elements.getBought().toString().equalsIgnoreCase(sec.getString(slots + ".trail")))
											? "&a&lActivated!"
											: "");
						} else {
							l = l.replace("%activated%", "");
						}

						l = l.replace("%cost%", cost);
						l = l.replace("%required_points%", points);
						l = l.replace("%amount%", String.valueOf(itemAmount));

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

				String itemName = sec.getString(slots + ".name", "");
				if (!itemName.isEmpty()) {
					iMeta.setDisplayName(itemName.replace("&", "\u00a7"));
				}

				hu.montlikadani.ragemode.utils.Misc.setDurability(iStack, (short) sec.getDouble(slots + ".durability", 0));

				iStack.setItemMeta(iMeta);
				inv.setItem(i, iStack);

				ShopItemCommands itemCmds = null;
				if (sec.isList(slots + ".commands")) {
					List<String> commands = sec.getStringList(slots + ".commands");

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

					itemCmds = new ShopItemCommands(mainPath + ".items." + slots, commands, navigationType);
				} else if (sec.isString(slots + ".command")) {
					String command = sec.getString(slots + ".command", "");
					NavigationType navigationType = NavigationType.getByName(command.toLowerCase());
					if (navigationType == null) {
						navigationType = NavigationType.WITHOUT;
					}

					itemCmds = new ShopItemCommands(mainPath + ".items." + slots, command, navigationType);
				}

				if (itemCmds == null) {
					itemCmds = new ShopItemCommands(mainPath + ".items." + slots, "", NavigationType.WITHOUT);
				}

				ShopItem shopItem = new ShopItem(iStack, type, guiName, itemCmds);
				this.items.add(shopItem);
			}
		}

		if (inv != null)
			player.openInventory(inv);
	}
}
