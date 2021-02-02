package hu.montlikadani.ragemode.gameUtils.gameSetup;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.gameSetup.InventoryGuiHandler.GuiItem;
import hu.montlikadani.ragemode.gameUtils.gameSetup.InventoryGuiHandler.InvGuiBuilder;
import hu.montlikadani.ragemode.utils.Utils;

public class SetupGui implements Listener {

	private final Set<InventoryGuiHandler> activeInventories = new HashSet<>();

	public Set<InventoryGuiHandler> getActiveInventories() {
		return activeInventories;
	}

	public GuiViewer openGui(Player source, Game sourceGame) {
		Validate.notNull(source, "Source player cannot be null");
		Validate.notNull(sourceGame, "Game cannot be null");

		InventoryGuiHandler igh = load(sourceGame, true);

		GuiViewer guiViewer = igh.getViewers().stream().filter(gv -> gv.getSource() == source).findFirst()
				.orElseGet(() -> {
					GuiViewer gv = new GuiViewer(source, igh);
					igh.getViewers().add(gv);
					return gv;
				});

		guiViewer.closeCurrent();
		guiViewer.open();

		activeInventories.add(igh);
		return guiViewer;
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (!org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(RageMode.class).equals(event.getPlugin())) {
			return;
		}

		activeInventories.forEach(igh -> new HashSet<>(igh.getViewers()).forEach(viewer -> {
			viewer.closeCurrent();
			viewer.getSource().closeInventory();
		}));

		activeInventories.clear();
	}

	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		activeInventories.removeIf(igh -> {
			if (!igh.isUpdating) {
				for (GuiViewer viewer : new HashSet<>(igh.getViewers())) {
					if (viewer.getSource() == event.getPlayer()) {
						viewer.closeCurrent();
						return true;
					}
				}
			}

			return false;
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onItemClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player) || event.getCurrentItem() == null
				|| !(event.getInventory().getHolder() instanceof InventoryGuiHandler)) {
			return;
		}

		event.setCancelled(true);

		for (InventoryGuiHandler invGui : activeInventories) {
			if (invGui.isUpdating || !GameUtils.isGameExist(invGui.getSourceGame().getName())
					|| invGui.getSourceGame().isGameRunning()) {
				continue;
			}

			for (GuiViewer guiViewer : invGui.getViewers()) {
				if (guiViewer.getSource() == (Player) event.getWhoClicked()) {
					boolean found = false;

					for (GuiItem guiItem : invGui.getGuiItems()) {
						if (guiItem.getClickEvent() != null && guiItem.getItem().isSimilar(event.getCurrentItem())) {
							guiItem.getClickEvent().accept(event);
							found = true;
							break;
						}
					}

					if (found) {
						InventoryGuiHandler igh = load(invGui.getSourceGame(), false);
						guiViewer.refreshInventory();
						igh.isUpdating = false;
					}

					break;
				}
			}
		}
	}

	private final AtomicReference<InventoryGuiHandler> ref = new AtomicReference<>();

	private InventoryGuiHandler load(Game game, boolean firstLoad) {
		ref.set(null); // Set to null every time to display items after 3th open

		// hack - we're using references to update inventory item components
		if (!firstLoad) {
			activeInventories.stream().filter(a -> a.getSourceGame().equals(game)).findFirst().ifPresent(igh -> {
				igh.isUpdating = true;
				igh.getGuiItems().clear();
				ref.set(igh);
			});
		}

		InvGuiBuilder builder = InventoryGuiHandler.ofBuilder(game).title("&6&lRageMode setup GUI");

		builder.addItem(guiItem -> guiItem
				.item(0, Material.WITHER_SKELETON_SKULL,
						is -> is.setDisplayName((game.bossbarEnabled ? "&cDis" : "&5En") + "able boss bar"))
				.clickEvent(click -> game.bossbarEnabled = !game.bossbarEnabled).build())
				.addItem(g -> g
						.item(1, Material.COMPARATOR,
								is -> is.setDisplayName((game.actionbarEnabled ? "&cDis" : "&aEn") + "able action bar"))
						.clickEvent(click -> game.actionbarEnabled = !game.actionbarEnabled).build())
				.addItem(i -> i.item(2, Material.FEATHER, is -> {
					is.setDisplayName("&6Change maximum players");
					is.setLore(Arrays.asList("&eCurrent amount: " + game.maxPlayers, " ", "&6Right click to decrease",
							"&6Left click to increase"));
				}).clickEvent(event -> {
					if (event.isRightClick()) {
						if (game.maxPlayers - 1 < 2) {
							sendMessage(event.getWhoClicked(), RageMode.getLang().get("setup.at-least-two"));
							return;
						}

						game.maxPlayers--;
					} else if (event.isLeftClick()) {
						game.maxPlayers++;
					}
				}).build()).addItem(a -> a.item(3, Material.INK_SAC, item -> {
					item.setDisplayName("&2Change minimum players");
					item.setLore(Arrays.asList("&eCurrent amount: " + game.minPlayers, " ", "&6Right click to decrease",
							"&6Left click to increase"));
				}).clickEvent(e -> {
					if (e.isRightClick()) {
						if (game.minPlayers - 1 < 1) {
							sendMessage(e.getWhoClicked(), RageMode.getLang().get("setup.at-least-two"));
							return;
						}

						game.minPlayers--;
					} else if (e.isLeftClick()) {
						game.minPlayers++;
					}
				}).build()).addItem(item -> item.item(4, Material.EMERALD, c -> {
					c.setDisplayName("&eChange game time");
					c.setLore(Arrays.asList("&eCurrent time: " + Utils.getFormattedTime(game.gameTime), " ",
							"&6Right click to decrease with 5 minute", "&6Left click to increase with 5 minute"));
				}).clickEvent(event -> {
					if (event.isRightClick() && game.gameTime - (5 * 60) > 30) {
						game.gameTime -= 5 * 60;
					} else if (event.isLeftClick() && game.gameTime + (5 * 60) < 3600) {
						game.gameTime += 5 * 60;
					}
				}).build()).addItem(i -> i.item(5, Material.LAPIS_LAZULI, item -> {
					item.setDisplayName("&bChange lobby time");
					item.setLore(Arrays.asList(
							"&eCurrent time: " + Utils.getFormattedTime(game.getGameLobby().lobbyTime), " ",
							"&6Right click to decrease with 5 seconds", "&6Left click to increase with 5 seconds"));
				}).clickEvent(event -> {
					if (event.isRightClick() && game.getGameLobby().lobbyTime - 5 > 5) {
						game.getGameLobby().lobbyTime -= 5;
					} else if (event.isLeftClick()) {
						game.getGameLobby().lobbyTime += 5;
					}
				}).build());

		builder.filter(igh -> igh.getGame().getGameType() == GameType.APOCALYPSE)
				.ifPresent(i -> i.addItem(item -> item.item(7, Material.FLOWER_POT, co -> {
					co.setDisplayName("&4Add zombie spawn");
					co.setLore(
							Arrays.asList("&eCurrent amount of spawns: " + (game.getSpawn(GameZombieSpawn.class) != null
									? game.getSpawn(GameZombieSpawn.class).getSpawnLocations().size()
									: 0), " ", "&6Left click to add a spawn to your curren position"));
				}).clickEvent(event -> {
					if (!event.isLeftClick()) {
						return;
					}

					FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
					String path = "arenas." + game.getName();
					if (!aFile.isSet(path)) {
						return;
					}

					path += ".zombie-spawns.";

					int index = 1;
					while (aFile.isSet(path + index))
						index++;

					path += index + ".";

					Location loc = event.getWhoClicked().getLocation();
					aFile.set(path + "world", loc.getWorld().getName());
					aFile.set(path + "x", loc.getX());
					aFile.set(path + "y", loc.getY());
					aFile.set(path + "z", loc.getZ());
					aFile.set(path + "yaw", loc.getYaw());
					aFile.set(path + "pitch", loc.getPitch());
					Configuration.saveFile(aFile, RageMode.getInstance().getConfiguration().getArenasFile());

					IGameSpawn spawn = game.getSpawn(GameZombieSpawn.class);
					if (spawn != null) {
						spawn.addSpawn(loc);
					}
				}).build()));

		builder.addItem(item -> item.item(8, Material.IRON_SHOVEL, cond -> {
			cond.setDisplayName("&7Add player spawn");
			cond.setLore(Arrays.asList("&eCurrent amount of spawns: " + (game.getSpawn(GameSpawn.class) != null
					? game.getSpawn(GameSpawn.class).getSpawnLocations().size()
					: 0), " ", "&6Left click to add a spawn to your current position"));
		}).clickEvent(event -> {
			if (!event.isLeftClick()) {
				return;
			}

			FileConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
			String path = "arenas." + game.getName();
			if (!aFile.isSet(path)) {
				return;
			}

			path += ".spawns.";

			int index = 1;
			while (aFile.isSet(path + index))
				index++;

			path += index + ".";

			Location loc = event.getWhoClicked().getLocation();
			aFile.set(path + "world", loc.getWorld().getName());
			aFile.set(path + "x", loc.getX());
			aFile.set(path + "y", loc.getY());
			aFile.set(path + "z", loc.getZ());
			aFile.set(path + "yaw", loc.getYaw());
			aFile.set(path + "pitch", loc.getPitch());
			Configuration.saveFile(aFile, RageMode.getInstance().getConfiguration().getArenasFile());

			IGameSpawn spawn = game.getSpawn(GameSpawn.class);
			if (spawn != null) {
				spawn.addSpawn(loc);
			}
		}).build());

		builder.addItem(item -> item.item(17, Material.BLUE_WOOL, cond -> {
			cond.setDisplayName("&9Set lobby position");
			cond.setLore(Arrays.asList(game.getGameLobby().location != null ? "&a\u2714 Lobby set" : "", " ",
					"&6Left click to set lobby to your current position"));
		}).clickEvent(event -> {
			if (event.isLeftClick()) {
				game.getGameLobby().location = event.getWhoClicked().getLocation();
			}
		}).build());

		if (ref.get() != null) {
			ref.get().getGuiItems().addAll(builder.getItems());
		}

		return ref.get() != null ? ref.get() : builder.build();
	}
}
