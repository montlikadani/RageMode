package hu.montlikadani.ragemode.items.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerBuyFromShopEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.Items;
import hu.montlikadani.ragemode.items.shop.pages.MainPage;
import hu.montlikadani.ragemode.items.shop.pages.NextPage;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;

public class LobbyShop implements Listener {

	public static final Map<Player, BoughtElements> BOUGHTITEMS = new HashMap<>();

	private final Map<Player, IShop> shops = new HashMap<>();

	private RageMode plugin = RageMode.getInstance();

	public LobbyShop() {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public Map<Player, IShop> getShops() {
		return shops;
	}

	public boolean isOpened(Player player) {
		return shops.containsKey(player);
	}

	public IShop getCurrentPage(Player player) {
		return shops.get(player);
	}

	public void removeShop(Player player) {
		if (isOpened(player)) {
			shops.get(player).getItems().clear();
			shops.remove(player);
		}
	}

	public void openMainPage(Player player) {
		removeShop(player);

		MainPage main = new MainPage();
		main.create(player);

		if (main.getInventory() == null) {
			return;
		}

		shops.put(player, main);
		player.openInventory(main.getInventory());
	}

	public void openNextPage(Player player, ShopCategory category) {
		NextPage next = new NextPage();
		next.create(player, category);

		if (next.getInventory() == null) {
			return; // When some options are disabled
		}

		removeShop(player);
		shops.put(player, next);
		player.openInventory(next.getInventory());
	}

	public List<ShopItem> getItems(IShop currentPage, ShopCategory category) {
		return (currentPage == null || category == null) ? new ArrayList<>()
				: currentPage.getItems().stream().filter(si -> category.equals(si.getCategory()))
						.collect(Collectors.toList());
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		Player player = e.getPlayer();

		removeShop(player);
		BOUGHTITEMS.remove(player);
	}

	@EventHandler
	public void onGameStart(RMGameStartEvent event) {
		for (PlayerManager pm : event.getPlayers()) {
			Player player = pm.getPlayer();
			removeShop(player);
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof IShop) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onClickEvent(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player) || !(ev.getInventory().getHolder() instanceof IShop)) {
			return;
		}

		Player p = (Player) ev.getWhoClicked();
		if (!GameUtils.isPlayerPlaying(p) || GameUtils.getGameByPlayer(p).getStatus() != GameStatus.WAITING) {
			removeShop(p);
			return;
		}

		ev.setCancelled(true);

		if (ev.getClick() != ClickType.LEFT && ev.getClick() != ClickType.RIGHT) {
			return;
		}

		doItemClick(ev);
		return; // ?
	}

	private void doItemClick(InventoryClickEvent ev) {
		final Player player = (Player) ev.getWhoClicked();
		if (!isOpened(player)) {
			return;
		}

		final ItemStack currentItem = ev.getCurrentItem();
		if (currentItem == null) {
			return;
		}

		shops.get(player).getShopItem(currentItem).ifPresent(shopItem -> {
			if (!shopItem.getItem().equals(currentItem)) {
				return;
			}

			String title = Version.isCurrentEqualOrHigher(Version.v1_13_R1) ? ev.getView().getTitle()
					: player.getOpenInventory().getTitle();
			if (!title.equalsIgnoreCase(shopItem.getInventoryName())) {
				return;
			}

			ShopItemCommands cmds = shopItem.getItemCommands();
			if (cmds != null) {
				NavigationType type = cmds.getNavigationType();

				if (type == NavigationType.WITHOUT && currentItem.getItemMeta().getDisplayName()
						.equalsIgnoreCase(shopItem.getItem().getItemMeta().getDisplayName())) {
					if (buyElement(ev, cmds.getConfigPath(), shopItem.getCategory())) {
						for (String c : cmds.getCommands()) {
							if (c.startsWith("console:")) {
								c = c.replace("console:", "");

								if (c.startsWith("/")) {
									c = c.substring(1);
								}

								c = Utils.setPlaceholders(c, player);
								c = c.replace("%player%", player.getName());

								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
							}
						}
					}

					return;
				}

				player.closeInventory();

				if (type == NavigationType.MAIN) {
					openMainPage(player);
				}

				return;
			}

			openNextPage(player, shopItem.getCategory());
		});
	}

	private boolean buyElement(InventoryClickEvent e, String path, ShopCategory shopCategory) {
		Player player = (Player) e.getWhoClicked();
		if (!isOpened(player)) {
			return false;
		}

		Configuration conf = plugin.getConfiguration();
		BoughtElements elements = BOUGHTITEMS.get(player);

		final double cost = conf.getItemsCfg().getDouble(path + ".cost.value", 0d);
		final double currentCost = elements != null ? elements.getCost() : cost;

		final int points = conf.getItemsCfg().getInt(path + "cost.points", 0);
		final int currentPoints = elements != null ? elements.getPoints() : points;

		double finalCost = currentCost;
		int finalPoints = currentPoints;

		if (shopCategory == ShopCategory.POTIONEFFECTS && conf.getItemsCfg().contains(path + ".effect")) {
			String[] effect = conf.getItemsCfg().getString(path + ".effect").split(":");
			if (effect.length < 1) {
				return false;
			}

			PotionEffectType type = PotionEffectType.getByName(effect[0]);
			if (type == null) {
				return false;
			}

			int duration = effect.length > 1 ? Integer.parseInt(effect[1]) : 5;
			int amplifier = effect.length > 2 ? Integer.parseInt(effect[2]) : 1;

			if (BOUGHTITEMS.containsKey(player)) {
				finalCost += cost;
				finalPoints += points;
				duration += duration;

				if (amplifier < 2) {
					amplifier++;
				}
			}

			if ((plugin.isVaultEnabled() && !plugin.getEconomy().has(player, finalCost))
					|| !RuntimePPManager.hasPoints(player.getUniqueId(), finalPoints)) {
				return false;
			}

			PotionEffect pe = new PotionEffect(type, duration * 20, amplifier);

			if (elements == null) {
				elements = new BoughtElements(pe, finalCost, finalPoints);
			} else {
				elements.setPotion(pe);
			}
		} else if (shopCategory == ShopCategory.GAMEITEMS && conf.getItemsCfg().contains(path + ".giveitem")) {
			String[] splitItem = conf.getItemsCfg().getString(path + ".giveitem").split(":");
			if (splitItem.length < 1) {
				return false;
			}

			int amount = splitItem.length > 1 ? Integer.parseInt(splitItem[1]) : 1;
			if (amount < 1) {
				amount = 1;
			}

			if (BOUGHTITEMS.containsKey(player)) {
				finalCost += cost;
				finalPoints += points;
				amount += amount;
			}

			if ((plugin.isVaultEnabled() && !plugin.getEconomy().has(player, finalCost))
					|| !RuntimePPManager.hasPoints(player.getUniqueId(), finalPoints)) {
				return false;
			}

			ItemHandler item = null;
			if ("grenade".equals(splitItem[0])) {
				item = Items.getGrenade();
			} else if ("combataxe".equals(splitItem[0])) {
				item = Items.getCombatAxe();
			} else if ("flash".equals(splitItem[0])) {
				item = Items.getFlash();
			} else if ("pressuremine".equals(splitItem[0]) || "mine".equals(splitItem[0])) {
				item = Items.getPressureMine();
			}

			if (item == null) {
				return false;
			}

			item = (ItemHandler) item.clone();
			item.setAmount(amount);

			if (elements == null) {
				elements = new BoughtElements(item.build(), finalCost, finalPoints);
			} else {
				elements.setItem(item.build());
			}
		} else if (ConfigValues.isUseArrowTrails() && shopCategory == ShopCategory.ITEMTRAILS
				&& conf.getItemsCfg().contains(path + ".trail")) {
			String name = conf.getItemsCfg().getString(path + ".trail", "");
			if (name.isEmpty()) {
				return false;
			}

			Particle particle = null;
			for (Particle p : Particle.values()) {
				if (p.toString().equalsIgnoreCase(name)) {
					particle = p;
					break;
				}
			}

			if (particle == null) {
				Debug.logConsole("Trail particle type is not exist with this name: " + name);
				return false;
			}

			if (elements != null && particle.equals(elements.getTrail())) {
				return false;
			}

			if ((plugin.isVaultEnabled() && !plugin.getEconomy().has(player, finalCost))
					|| !RuntimePPManager.hasPoints(player.getUniqueId(), finalPoints)) {
				return false;
			}

			if (elements == null) {
				elements = new BoughtElements(particle, finalCost, finalPoints);
			} else {
				elements.setTrail(particle);
			}
		}

		if (elements != null) {
			Utils.callEvent(
					new RMPlayerBuyFromShopEvent(GameUtils.getGameByPlayer(player), player, elements, shopCategory));

			if (BOUGHTITEMS.containsKey(player)) {
				elements.setCost(finalCost);
				elements.setPoints(finalPoints);
			} else {
				BOUGHTITEMS.put(player, elements);
			}
		}

		// update shop items
		player.closeInventory();
		openNextPage(player, shopCategory);
		return true;
	}
}
