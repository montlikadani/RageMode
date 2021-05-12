package hu.montlikadani.ragemode.items.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStartEvent;
import hu.montlikadani.ragemode.API.event.RMPlayerBuyFromShopEvent;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.items.shop.ShopItemCommands.CommandSetting;
import hu.montlikadani.ragemode.items.shop.pages.MainPage;
import hu.montlikadani.ragemode.items.shop.pages.NextPage;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.Utils;

public final class LobbyShop implements Listener {

	public static final Map<UUID, BoughtElements> BOUGHT_ITEMS = new HashMap<>();
	public static final Map<UUID, Enum<?>> USER_PARTICLES = new HashMap<>();

	private static final RageMode RM = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private final Map<UUID, IShop> playerShops = new HashMap<>();

	private final IShop mainPage = new MainPage(), nextPage = new NextPage();

	public LobbyShop() {
		RM.getServer().getPluginManager().registerEvents(this, RM);
	}

	public Map<UUID, IShop> getPlayerShops() {
		return playerShops;
	}

	public boolean isOpened(Player player) {
		return getCurrentPage(player) != null;
	}

	public IShop getCurrentPage(Player player) {
		return playerShops.get(player.getUniqueId());
	}

	public void removeShop(Player player) {
		IShop shop = playerShops.remove(player.getUniqueId());

		if (shop != null) {
			shop.getItems().clear();
		}
	}

	public void openMainPage(Player player) {
		removeShop(player);

		mainPage.create(player);

		if (mainPage.getInventory() == null) {
			return;
		}

		playerShops.put(player.getUniqueId(), mainPage);
		player.openInventory(mainPage.getInventory());
	}

	public void openNextPage(Player player, ShopCategory category) {
		nextPage.create(player, category);

		if (nextPage.getInventory() == null) {
			return;
		}

		removeShop(player);

		RM.getServer().getScheduler().runTaskLater(RM, () -> {
			player.openInventory(nextPage.getInventory());
			playerShops.put(player.getUniqueId(), nextPage);
		}, 2L);
	}

	@EventHandler
	public void onGameLeave(RMGameLeaveAttemptEvent e) {
		Player player = e.getPlayer();

		removeShop(player);

		BOUGHT_ITEMS.remove(player.getUniqueId());
		player.closeInventory();
	}

	@EventHandler
	public void onGameStart(RMGameStartEvent event) {
		for (PlayerManager pm : event.getGame().getPlayers()) {
			Player player = pm.getPlayer();

			if (player != null) {
				removeShop(player);
				player.closeInventory();
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		removeShop((Player) e.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClickEvent(InventoryClickEvent ev) {
		if (!(ev.getWhoClicked() instanceof Player)) {
			return;
		}

		Player who = (Player) ev.getWhoClicked();
		if (!isOpened(who)) {
			return;
		}

		hu.montlikadani.ragemode.gameLogic.Game game = GameUtils.getGameByPlayer(who);
		if (game == null || game.getStatus() != GameStatus.WAITING) {
			removeShop(who);
			return;
		}

		ev.setCancelled(true);

		if (ev.getClick() != ClickType.UNKNOWN) {
			handleItemClick(ev);
		}
	}

	private void handleItemClick(InventoryClickEvent ev) {
		final ItemStack currentItem = ev.getCurrentItem();
		if (currentItem == null) {
			return;
		}

		final Player player = (Player) ev.getWhoClicked();

		getCurrentPage(player).getShopItem(currentItem).ifPresent(shopItem -> {
			if (shopItem.getItem().getType() != currentItem.getType()) {
				return;
			}

			String title = RM.getComplement()
					.getTitle(ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R1) ? ev.getView()
							: player.getOpenInventory());

			if (!title.equalsIgnoreCase(shopItem.getInventoryName())) {
				return;
			}

			ShopItemCommands itemCommand = shopItem.getItemCommands();
			if (itemCommand != null) {
				if (itemCommand.getNavigationType() == NavigationType.WITHOUT
						&& RM.getComplement().getDisplayName(currentItem.getItemMeta()).equalsIgnoreCase(
								RM.getComplement().getDisplayName(shopItem.getItem().getItemMeta()))) {
					if (buyElement(player, itemCommand.getItemSetting(), shopItem.getCategory())) {
						for (CommandSetting cmd : itemCommand.getCommands()) {
							if (cmd.getType() == CommandSetting.SenderType.CONSOLE) {
								String command = Utils.setPlaceholders(cmd.getCommand(), player);
								command = command.replace("%player%", player.getName());
								RM.getServer().dispatchCommand(RM.getServer().getConsoleSender(), command);
							}
						}
					}

					return;
				}

				if (itemCommand.getNavigationType() == NavigationType.MAIN) {
					openMainPage(player);
				} else {
					player.closeInventory();
				}

				return;
			}

			openNextPage(player, shopItem.getCategory());
		});
	}

	private boolean buyElement(Player player, ShopItemCommands.ItemSetting itemSetting, ShopCategory shopCategory) {
		BoughtElements elements = BOUGHT_ITEMS.get(player.getUniqueId());
		PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());

		final double cost = itemSetting.getCost();
		final int points = itemSetting.getPoints();

		double finalCost = elements != null ? elements.getCost() : cost;
		int finalPoints = elements != null ? elements.getPoints() : points;

		if (shopCategory == ShopCategory.POTIONEFFECTS && itemSetting.getEffectSetting() != null) {
			PotionEffectType type = itemSetting.getEffectSetting().getEffectType();

			if (type == null) {
				return false;
			}

			int duration = itemSetting.getEffectSetting().getDuration();
			int amplifier = itemSetting.getEffectSetting().getAmplifier();

			if (elements != null) {
				finalCost += cost;
				finalPoints += points;
				duration += duration;

				if (amplifier < 2) {
					amplifier++;
				}
			}

			if ((RM.isVaultEnabled() && !RM.getEconomy().has(player, finalCost))
					|| (pp != null && !pp.hasPoints(finalPoints))) {
				return false;
			}

			PotionEffect pe = new PotionEffect(type, duration * 20, amplifier);

			if (elements == null) {
				elements = new BoughtElements(pe, finalCost, finalPoints);
			} else {
				elements.setBought(pe);
			}
		} else if (shopCategory == ShopCategory.GAMEITEMS && itemSetting.getGiveGameItem() != null) {
			ItemHandler item = itemSetting.getGiveGameItem().getItem();

			if (item == null) {
				return false;
			}

			int amount = itemSetting.getGiveGameItem().getAmount();

			if (elements != null) {
				finalCost += cost;
				finalPoints += points;
				amount += amount;
			}

			if ((RM.isVaultEnabled() && !RM.getEconomy().has(player, finalCost))
					|| (pp != null && !pp.hasPoints(finalPoints))) {
				return false;
			}

			item = (ItemHandler) item.clone();
			item.setAmount(amount);

			if (elements == null) {
				elements = new BoughtElements(item.get(), finalCost, finalPoints);
			} else {
				elements.setBought(item.get());
			}
		} else if (ConfigValues.isUseArrowTrails() && shopCategory == ShopCategory.ITEMTRAILS
				&& itemSetting.getItemTrail() != null) {
			Object particle = itemSetting.getItemTrail().getParticle();

			if (particle == null || (elements != null && particle == elements.getBought())) {
				return false;
			}

			if ((RM.isVaultEnabled() && !RM.getEconomy().has(player, finalCost))
					|| (pp != null && !pp.hasPoints(finalPoints))) {
				return false;
			}

			if (elements == null) {
				elements = new BoughtElements(particle, finalCost, finalPoints);
			} else {
				elements.setBought(particle);
			}
		}

		if (elements != null) {
			Utils.callEvent(
					new RMPlayerBuyFromShopEvent(GameUtils.getGameByPlayer(player), player, elements, shopCategory));

			if (BOUGHT_ITEMS.containsKey(player.getUniqueId())) {
				elements.setCost(finalCost);
				elements.setPoints(finalPoints);
			} else {
				BOUGHT_ITEMS.put(player.getUniqueId(), elements);
			}
		}

		// update inventory components
		openNextPage(player, shopCategory);
		return true;
	}

	public static void buyElements(Player player) {
		BoughtElements bought = BOUGHT_ITEMS.remove(player.getUniqueId());

		if (bought == null) {
			return;
		}

		boolean enough = false;

		if (RM.isVaultEnabled()) {
			net.milkbowl.vault.economy.Economy economy = RM.getEconomy();
			double cost = bought.getCost();

			if (cost > 0d && economy.has(player, cost)) {
				economy.withdrawPlayer(player, cost);
				enough = true;
			}
		}

		if (bought.getPoints() > 0) {
			PlayerPoints pp = RuntimePPManager.getPPForPlayer(player.getUniqueId());

			if (pp != null && pp.hasPoints(bought.getPoints())) {
				pp.takePoints(bought.getPoints());
				enough = true;
			} else {
				enough = false;
			}
		}

		if (!enough) {
			Misc.sendMessage(player, RageMode.getLang().get("game.cant-bought-elements"));
			return;
		}

		// This also should be on main thread, spigot async catchop
		RM.getServer().getScheduler().runTaskLater(RM, () -> {
			if (bought.getBought() instanceof PotionEffect) {
				player.addPotionEffect(bought.<PotionEffect>getBought());
			}

			if (bought.getBought() instanceof ItemStack) {
				player.getInventory().addItem(bought.<ItemStack>getBought());
			}
		}, 1L);

		if (bought.getBought() instanceof Effect || bought.getBought() instanceof Particle) {
			USER_PARTICLES.put(player.getUniqueId(), bought.getBought());
		}
	}
}
