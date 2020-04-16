package hu.montlikadani.ragemode.items.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.shop.inventory.CustomInventoryType;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

// it's an ugly class
public class LobbyShop implements Listener, IShop {

	public static Map<Player, BoughtElements> boughtItems = new HashMap<>();

	private final Set<Player> players = new HashSet<>();
	private final Map<ShopItem, Player> shopItems = new HashMap<>();
	private final HashMap<ItemStack, ShopItemCommands> items = new HashMap<>();

	private RageMode plugin;
	private CustomInventoryType type;

	private Inventory inv;

	public LobbyShop(CustomInventoryType type) {
		this.type = type;
		this.plugin = RageMode.getInstance();

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		Player player = e.getPlayer();

		remove(player);
		boughtItems.remove(player);
	}

	@EventHandler
	public void onGameStart(RMGameStartEvent event) {
		for (PlayerManager pm : event.getPlayers()) {
			pm.getPlayer().closeInventory();
			remove(pm.getPlayer());
		}
	}

	@EventHandler
	public void onClickEvent(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player) ev.getWhoClicked();
		if (!isOpened(p)) {
			return;
		}

		ItemStack item = ev.getCurrentItem();
		if (item == null) {
			return;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			remove(p);
			return;
		}

		if (GameUtils.getGameByPlayer(p).getStatus() != GameStatus.WAITING) {
			remove(p);
			return;
		}

		ev.setCancelled(true);

		if (ev.getClick() != ClickType.LEFT && ev.getClick() != ClickType.RIGHT) {
			return;
		}

		doItemClick(ev);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player p = (Player) event.getPlayer();

		if (event.getInventory().getHolder() instanceof LobbyShop && type == CustomInventoryType.RAGEMODE) {
			remove(p);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getPlayer();

		if (GameUtils.isPlayerPlaying(p) && GameUtils.getGameByPlayer(p).getStatus() == GameStatus.WAITING
				&& !players.contains(p) && type == CustomInventoryType.RAGEMODE) {
			players.add(p);
		}
	}

	@Override
	public CustomInventoryType getType() {
		return type;
	}

	@Override
	public void setType(CustomInventoryType type) {
		this.type = type;
	}

	@Override
	public Set<Player> getPlayers() {
		return players;
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

	@Override
	public boolean isOpened(Player player) {
		return player != null && players.contains(player);
	}

	@Override
	public void removeAll() {
		players.clear();
	}

	@Override
	public void remove(Player player) {
		if (isOpened(player)) {
			players.remove(player);
		}
	}

	@Override
	public void create(Player player) {
		Configuration conf = plugin.getConfiguration();
		String path = "lobbyitems.shopitem.gui.";

		String guiName = Utils.colors(conf.getItemsCfg().getString(path + "title", "&6RageMode shop"));

		int size = conf.getItemsCfg().getInt(path + "size", 9);
		if (size > 54) {
			size = 54;
		}

		inv = Bukkit.createInventory(player, size, guiName);

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
						Debug.logConsole(Level.WARNING, "Unknown filler item type: " + item);
						mat = Material.AIR;
					}

					inv.setItem(i, new ItemStack(mat));
				}

				continue;
			}

			ItemStack iStack = new ItemStack(mat);

			String t = conf.getItemsCfg().getString(path + "slot-" + i + ".type", "");
			ShopType type = !t.isEmpty() ? ShopType.valueOf(t.toUpperCase()) : null;
			if (type == null) {
				type = ShopType.MAIN;
			}

			ItemMeta iMeta = iStack.getItemMeta();
			if (iMeta == null) {
				inv.setItem(i, new ItemStack(mat));
				continue;
			}

			ArrayList<String> lore = new ArrayList<>();
			List<String> list = conf.getItemsCfg().getStringList(path + "slot-" + i + ".lore");
			if (!list.isEmpty()) {
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

			ShopItem shopItem = new ShopItem(iStack, type, guiName);
			shopItems.put(shopItem, player);
		}
	}

	private void openEffectsShop(Player player) {
		items.clear();

		String mainPath = "lobbyitems.shopitem.gui.";
		FileConfiguration conf = plugin.getConfiguration().getItemsCfg();

		ConfigurationSection section = conf.getConfigurationSection(mainPath + "items");
		if (section == null) {
			return;
		}

		mainPath += "items.";

		for (String guiItems : section.getKeys(false)) {
			ShopType shopType = ShopType.valueOf(section.getString(guiItems + ".type").toUpperCase());
			if (shopType != ShopType.POTIONEFFECTS) {
				continue;
			}

			mainPath += guiItems + ".gui";

			String guiName = Utils.colors(conf.getString(mainPath + ".title", "&6RageMode shop"));

			int size = conf.getInt(mainPath + ".size", 9);
			if (size > 54) {
				size = 54;
			}

			inv = Bukkit.createInventory(player, size, guiName);

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
					String filler = sec.getString(mainPath + ".fillEmptyFields", "air");
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

				ArrayList<String> lore = new ArrayList<>();
				List<String> list = sec.getStringList(effectItems + ".lore");
				if (!list.isEmpty()) {
					for (String l : list) {
						l = Utils.colors(l);

						BoughtElements elements = boughtItems.get(player);
						double cost = elements != null ? elements.getCost()
								: sec.getInt(effectItems + ".cost.value", 0);
						int points = elements != null ? elements.getPoints()
								: sec.getInt(effectItems + ".cost.points", 0);

						l = l.replace("%cost%", Double.toString(cost));
						l = l.replace("%required_points%", Integer.toString(points));

						if (plugin.isVaultEnabled()) {
							l = l.replace("%money%", Double.toString(plugin.getEconomy().getBalance(player)));
						}

						PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());
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

				String command = sec.getString(effectItems + ".command", "");
				ShopItemCommands itemCmds = new ShopItemCommands(mainPath + ".items." + effectItems, command,
						ShopType.POTIONEFFECTS);
				items.put(iStack, itemCmds);
			}
		}

		player.openInventory(inv);
	}

	private void doItemClick(InventoryClickEvent ev) {
		ItemStack item = ev.getCurrentItem();

		for (Map.Entry<ShopItem, Player> items : shopItems.entrySet()) {
			ShopItem si = items.getKey();

			if (!si.getItem().equals(item)) {
				continue;
			}

			String title = Version.isCurrentEqualOrHigher(Version.v1_13_R1) ? ev.getView().getTitle()
					: ((Player) ev.getWhoClicked()).getOpenInventory().getTitle();
			if (!title.equalsIgnoreCase(si.getInventoryName())) {
				continue;
			}

			if (si.getType() == ShopType.POTIONEFFECTS) {
				openEffectsShop(items.getValue());
				return;
			}
		}

		for (Entry<ItemStack, ShopItemCommands> i : items.entrySet()) {
			ItemStack stack = i.getKey();
			ShopItemCommands cmds = i.getValue();

			if (item.getItemMeta().getDisplayName().equalsIgnoreCase(stack.getItemMeta().getDisplayName())) {
				if (!cmds.getCommand().isEmpty()) {
					final Player player = ((Player) ev.getWhoClicked());

					switch (cmds.getCommand().toLowerCase()) {
					case "mainpage":
					case "main":
						player.closeInventory();

						remove(player);
						create(player);

						player.openInventory(inv);
						break;
					case "close":
						player.closeInventory();
						break;
					default:
						break;
					}

					break;
				}

				buyElement(ev, cmds.getConfigPath(), cmds.getType());
				break;
			}
		}
	}

	private void buyElement(InventoryClickEvent e, String path, ShopType shopType) {
		Player player = (Player) e.getWhoClicked();

		Configuration conf = plugin.getConfiguration();
		BoughtElements elements = boughtItems.get(player);

		double cost = conf.getItemsCfg().getDouble(path + ".cost.value", 0d);
		double finalCost = elements != null ? elements.getCost() : cost;

		int points = conf.getItemsCfg().getInt(path + "cost.points", 0);
		int finalPoints = elements != null ? elements.getPoints() : points;

		if (conf.getItemsCfg().contains(path + ".effect") && shopType == ShopType.POTIONEFFECTS) {
			String[] effect = conf.getItemsCfg().getString(path + ".effect").split(":");
			if (effect.length < 1) {
				return;
			}

			PotionEffectType type = PotionEffectType.getByName(effect[0]);
			if (type == null) {
				return;
			}

			int duration = effect.length > 1 ? Integer.parseInt(effect[1]) : 5;
			int amplifier = effect.length > 2 ? Integer.parseInt(effect[2]) : 1;

			if (boughtItems.containsKey(player)) {
				finalCost += cost;
				finalPoints += points;
				duration += duration;

				if (amplifier < 2) {
					amplifier++;
				}
			}

			PotionEffect pe = new PotionEffect(type, duration * 20, amplifier);

			if (elements == null) {
				elements = new BoughtElements(pe, finalCost, finalPoints);
			} else {
				elements.setPotion(pe);
			}
		}

		if (elements != null) {
			if (boughtItems.containsKey(player)) {
				elements.setCost(finalCost);
				elements.setPoints(finalPoints);

				changeInventoryItems(player, e.getInventory(), shopType, e.getCurrentItem(), elements,
						conf.getItemsCfg().getStringList(path + ".lore"));
			} else {
				boughtItems.put(player, elements);
			}
		}
	}

	public void changeInventoryItems(Player player, Inventory inv, ShopType shopType, ItemStack item,
			BoughtElements elements, List<String> lore) {
		if (!isOpened(player)) {
			return;
		}

		ItemStack[] contents = inv.getContents();

		if (shopType == ShopType.POTIONEFFECTS) {
			for (int i = 0; i < contents.length; i++) {
				if (contents[i] == null || !contents[i].equals(item)) {
					continue;
				}

				ItemMeta iMeta = contents[i].getItemMeta();
				if (iMeta == null) {
					break;
				}

				ArrayList<String> list = new ArrayList<>();
				if (!lore.isEmpty()) {
					for (String l : lore) {
						l = Utils.colors(l);

						if (elements != null) {
							l = l.replace("%cost%", Double.toString(elements.getCost()));
							l = l.replace("%required_points%", Integer.toString(elements.getPoints()));
						}

						if (player != null) {
							if (plugin.isVaultEnabled()) {
								l = l.replace("%money%", Double.toString(plugin.getEconomy().getBalance(player)));
							}

							PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());
							if (pp != null) {
								l = l.replace("%points%", Integer.toString(pp.getPoints()));
							}
						}

						list.add(l);
					}

					iMeta.setLore(list);
				}

				contents[i].setItemMeta(iMeta);
				inv.setItem(i, contents[i]);
			}
		}
	}
}
