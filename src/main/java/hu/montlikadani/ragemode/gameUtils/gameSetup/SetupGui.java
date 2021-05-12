package hu.montlikadani.ragemode.gameUtils.gameSetup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.gameSetup.InventoryGuiHandler.GuiItem;
import hu.montlikadani.ragemode.gameUtils.gameSetup.InventoryGuiHandler.InvGuiBuilder;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.Utils;

public class SetupGui implements Listener {

	private final Set<InventoryGuiHandler> activeInventories = new HashSet<>();

	private final RageMode rm;

	public SetupGui(RageMode rm) {
		this.rm = rm;
	}

	public Set<InventoryGuiHandler> getActiveInventories() {
		return activeInventories;
	}

	public GuiViewer openGui(Player source, Game sourceGame) {
		if (source == null || sourceGame == null) {
			return null;
		}

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
		if (!(event.getWhoClicked() instanceof Player)
				|| !(event.getInventory().getHolder() instanceof InventoryGuiHandler)) {
			return;
		}

		org.bukkit.inventory.ItemStack currentItem = event.getCurrentItem();
		if (currentItem == null) {
			return;
		}

		event.setCancelled(true);

		Player who = (Player) event.getWhoClicked();

		for (InventoryGuiHandler invGui : activeInventories) {
			if (invGui.isUpdating) {
				continue;
			}

			Game sourceGame = GameUtils.getGame(invGui.getSourceGameName());
			if (sourceGame == null || sourceGame.isRunning()) {
				continue;
			}

			for (GuiViewer guiViewer : invGui.getViewers()) {
				if (guiViewer.getSource() == who) {
					for (GuiItem guiItem : invGui.getGuiItems()) {
						if (guiItem.getClickEvent() != null && guiItem.getItem().isSimilar(currentItem)) {
							guiItem.getClickEvent().accept(event);

							InventoryGuiHandler igh = load(sourceGame, false);
							guiViewer.refreshInventory();
							igh.isUpdating = false;
							break;
						}
					}

					break;
				}
			}
		}
	}

	private final AtomicReference<InventoryGuiHandler> ref = new AtomicReference<>();

	private InventoryGuiHandler load(final Game game, boolean firstLoad) {
		ref.set(null); // Set to null every time to display items after 3th open

		// We're using here references to update inventory item components
		if (!firstLoad) {
			activeInventories.stream().filter(a -> a.getSourceGameName().equals(game.getName())).findFirst()
					.ifPresent(igh -> {
						igh.isUpdating = true;
						igh.getGuiItems().clear();
						ref.set(igh);
					});
		}

		IGameSpawn zombieSpawn = game.getSpawn(GameZombieSpawn.class);
		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);

		InvGuiBuilder builder = InventoryGuiHandler.ofBuilder(game.getName()).title("&6&lRageMode setup GUI");

		builder.addItem(guiItem -> guiItem
				.item(0, Material.WITHER_SKELETON_SKULL, (game.bossbarEnabled ? "&cDis" : "&5En") + "able boss bar")
				.clickEvent(click -> game.bossbarEnabled = !game.bossbarEnabled).build())

				.addItem(g -> g
						.item(1, Material.COMPARATOR, (game.actionbarEnabled ? "&cDis" : "&aEn") + "able action bar")
						.clickEvent(click -> game.actionbarEnabled = !game.actionbarEnabled).build())

				.addItem(i -> i.item(2, Material.FEATHER, "&6Change maximum players",
						Arrays.asList("&eCurrent amount: " + game.maxPlayers, " ", "&6Right click to decrease",
								"&6Left click to increase"))
						.clickEvent(event -> {
							if (event.isRightClick()) {
								if (game.maxPlayers - 1 < 2) {
									Misc.sendMessage(event.getWhoClicked(),
											RageMode.getLang().get("setup.at-least-two"));
									return;
								}

								game.maxPlayers--;
							} else if (event.isLeftClick()) {
								game.maxPlayers++;
							}
						}).build())

				.addItem(a -> a.item(3, Material.INK_SAC, "&2Change minimum players",
						Arrays.asList("&eCurrent amount: " + game.minPlayers, " ", "&6Right click to decrease",
								"&6Left click to increase"))
						.clickEvent(e -> {
							if (e.isRightClick()) {
								if (game.minPlayers - 1 < 1) {
									Misc.sendMessage(e.getWhoClicked(), RageMode.getLang().get("setup.at-least-two"));
									return;
								}

								game.minPlayers--;
							} else if (e.isLeftClick()) {
								game.minPlayers++;
							}
						}).build())

				.addItem(item -> item.item(4, Material.EMERALD, "&eChange game time",
						Arrays.asList("&eCurrent time: " + Utils.getFormattedTime(game.gameTime), " ",
								"&6Right click to decrease with 5 minute", "&6Left click to increase with 5 minute"))
						.clickEvent(event -> {
							if (event.isRightClick() && game.gameTime - (5 * 60) > 30) {
								game.gameTime -= 5 * 60;
							} else if (event.isLeftClick() && game.gameTime + (5 * 60) < 3600) {
								game.gameTime += 5 * 60;
							}
						}).build())

				.addItem(i -> i.item(5, Material.LAPIS_LAZULI, "&bChange lobby time",
						Arrays.asList("&eCurrent time: " + Utils.getFormattedTime(game.getGameLobby().lobbyTime), " ",
								"&6Right click to decrease with 5 seconds", "&6Left click to increase with 5 seconds"))
						.clickEvent(event -> {
							if (event.isRightClick() && game.getGameLobby().lobbyTime - 5 > 5) {
								game.getGameLobby().lobbyTime -= 5;
							} else if (event.isLeftClick()) {
								game.getGameLobby().lobbyTime += 5;
							}
						}).build());

		builder.filter(igh -> game.getGameType() == GameType.APOCALYPSE)
				.ifPresent(invBuild -> invBuild.addItem(item -> item
						.item(6, Material.ARROW, "&7Set random zombie spawn",
								Arrays.asList("&eSets the zombie spawn", "&emode to random, so you",
										"&edon't need to set spawns", "&6Status:&a " + game.randomSpawnForZombies, " ",
										"&cSettings this to true will", "&cremoves all added spawns!"))
						.clickEvent(event -> {
							if (event.isLeftClick() && (game.randomSpawnForZombies = !game.randomSpawnForZombies)
									&& zombieSpawn != null) {
								zombieSpawn.removeAllSpawn();
							}
						}).build()));

		builder.filter(igh -> !game.randomSpawnForZombies && game.getGameType() == GameType.APOCALYPSE)
				.ifPresent(
						i -> i.addItem(
								item -> item
										.item(7, Material.FLOWER_POT, "&4Add zombie spawn", Arrays.asList(
												"&eCurrent amount of spawns: "
														+ (zombieSpawn != null ? zombieSpawn.getSpawnLocations().size()
																: 0),
												" ", "&6Left click to add a spawn to your curren position"))
										.clickEvent(event -> {
											if (!event.isLeftClick()) {
												return;
											}

											FileConfiguration aFile = rm.getConfiguration().getArenasCfg();
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
											Configuration.saveFile(aFile, rm.getConfiguration().getArenasFile());

											if (zombieSpawn != null) {
												zombieSpawn.addSpawn(loc);
											}
										}).build()));

		builder.addItem(item -> item.item(8, Material.IRON_SHOVEL, "&7Add player spawn",
				Arrays.asList(
						"&eCurrent amount of spawns: " + (gameSpawn != null ? gameSpawn.getSpawnLocations().size() : 0),
						" ", "&6Left click to add a spawn to your current position"))
				.clickEvent(event -> {
					if (!event.isLeftClick()) {
						return;
					}

					FileConfiguration aFile = rm.getConfiguration().getArenasCfg();
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
					Configuration.saveFile(aFile, rm.getConfiguration().getArenasFile());

					if (gameSpawn != null) {
						gameSpawn.addSpawn(loc);
					}
				}).build());

		builder.addItem(item -> item.item(17, Material.BLUE_WOOL, "&9Set lobby position",
				Arrays.asList(game.getGameLobby().location != null ? "&a\u2714 Lobby set" : "", " ",
						"&6Left click to set lobby to your current position"))
				.clickEvent(event -> {
					if (event.isLeftClick()) {
						game.getGameLobby().location = event.getWhoClicked().getLocation();
					}
				}).build());

		InventoryGuiHandler value = ref.get();

		if (value != null) {
			value.getGuiItems().addAll(builder.getItems());
		}

		return value != null ? value : builder.build();
	}
}
