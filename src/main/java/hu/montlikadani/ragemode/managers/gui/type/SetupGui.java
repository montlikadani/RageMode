package hu.montlikadani.ragemode.managers.gui.type;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.managers.gui.BaseGui;
import hu.montlikadani.ragemode.managers.gui.GuiItem;
import hu.montlikadani.ragemode.managers.gui.GuiViewer;
import hu.montlikadani.ragemode.managers.gui.InventoryGuiHandler;
import hu.montlikadani.ragemode.managers.gui.InventoryGuiHandler.InvGuiBuilder;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.Utils;

public class SetupGui extends BaseGui {

	@Override
	public void updateContents(GuiViewer guiViewer) {
		InventoryGuiHandler igh = guiViewer.getCurrent();

		igh.isUpdating = true;

		// Update item display name and lore
		for (GuiItem guiItem : igh.getGuiItems()) {
			for (SetupItems setupItem : SetupItems.values()) {
				if (setupItem.getMaterial() == guiItem.getItem().getType()) {
					guiItem.setDisplayName(setupItem.getDisplayName());
					guiItem.setLore(setupItem.getLore());
					break;
				}
			}

			guiItem.apply();
		}

		guiViewer.refreshInventory();

		igh.isUpdating = false;
	}

	@Override
	public void updateTitle(GuiViewer guiViewer) {
		InventoryGuiHandler igh = guiViewer.getCurrent();

		igh.isUpdating = true;
		igh.setTitle("&6&lRageMode setup GUI");

		guiViewer.getSource().openInventory(igh.getInventory());

		igh.isUpdating = false;
	}

	@Override
	public final InventoryGuiHandler load(org.bukkit.entity.HumanEntity human) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final InventoryGuiHandler load(Game game) {
		if (game == null) {
			return InventoryGuiHandler.ofBuilder().build();
		}

		InvGuiBuilder builder = InventoryGuiHandler.ofBuilder().title("&6&lRageMode setup GUI");

		for (SetupItems setupItem : SetupItems.values()) {
			setupItem.accept(game); // TODO add setter instead
		}

		builder.addItem(guiItem -> {
			guiItem.setItem(0, SetupItems.BOSSBAR.getMaterial(), SetupItems.BOSSBAR.getDisplayName(),
					SetupItems.BOSSBAR.getLore());
			guiItem.setClickEvent(click -> game.bossbarEnabled = !game.bossbarEnabled);
		}).addItem(item -> {
			item.setItem(1, SetupItems.ACTIONBAR.getMaterial(), SetupItems.ACTIONBAR.getDisplayName(),
					SetupItems.ACTIONBAR.getLore());
			item.setClickEvent(click -> game.actionbarEnabled = !game.actionbarEnabled);
		}).addItem(item -> {
			item.setItem(2, SetupItems.MAXPLAYERS.getMaterial(), SetupItems.MAXPLAYERS.getDisplayName(),
					SetupItems.MAXPLAYERS.getLore());

			item.setClickEvent(event -> {
				if (event.isRightClick()) {
					if (game.maxPlayers - 1 < 2) {
						Misc.sendMessage(event.getWhoClicked(), RageMode.getLang().get("setup.at-least-two"));
						return;
					}

					game.maxPlayers--;
				} else if (event.isLeftClick()) {
					game.maxPlayers++;
				}
			});
		}).addItem(item -> {
			item.setItem(3, SetupItems.MINPLAYERS.getMaterial(), SetupItems.MINPLAYERS.getDisplayName(),
					SetupItems.MINPLAYERS.getLore());

			item.setClickEvent(e -> {
				if (e.isRightClick()) {
					if (game.minPlayers - 1 < 1) {
						Misc.sendMessage(e.getWhoClicked(), RageMode.getLang().get("setup.at-least-two"));
						return;
					}

					game.minPlayers--;
				} else if (e.isLeftClick()) {
					game.minPlayers++;
				}
			});
		}).addItem(item -> {
			item.setItem(4, SetupItems.GAMETIME.getMaterial(), SetupItems.GAMETIME.getDisplayName(),
					SetupItems.GAMETIME.getLore());

			item.setClickEvent(event -> {
				if (event.isRightClick() && game.gameTime - (5 * 60) > 30) {
					game.gameTime -= 5 * 60;
				} else if (event.isLeftClick() && game.gameTime + (5 * 60) < 3600) {
					game.gameTime += 5 * 60;
				}
			});
		}).addItem(item -> {
			item.setItem(5, SetupItems.LOBBYTIME.getMaterial(), SetupItems.LOBBYTIME.getDisplayName(),
					SetupItems.LOBBYTIME.getLore());

			item.setClickEvent(event -> {
				if (event.isRightClick() && game.getGameLobby().lobbyTime - 5 > 5) {
					game.getGameLobby().lobbyTime -= 5;
				} else if (event.isLeftClick()) {
					game.getGameLobby().lobbyTime += 5;
				}
			});
		});

		IGameSpawn zombieSpawn = game.getSpawn(GameZombieSpawn.class);

		builder.filter(igh -> game.getGameType() == GameType.APOCALYPSE)
				.ifPresent(invBuild -> invBuild.addItem(item -> {
					item.setItem(6, SetupItems.RANDOM_ZOMBIE_SPAWN.getMaterial(),
							SetupItems.RANDOM_ZOMBIE_SPAWN.getDisplayName(), SetupItems.RANDOM_ZOMBIE_SPAWN.getLore());

					item.setClickEvent(event -> {
						if (event.isLeftClick() && (game.randomSpawnForZombies = !game.randomSpawnForZombies)
								&& zombieSpawn != null) {
							zombieSpawn.removeAllSpawn();
						}
					});
				}));

		builder.filter(igh -> !game.randomSpawnForZombies && game.getGameType() == GameType.APOCALYPSE)
				.ifPresent(i -> i.addItem(item -> {
					item.setItem(7, SetupItems.ADD_ZOMBIE_SPAWN.getMaterial(),
							SetupItems.ADD_ZOMBIE_SPAWN.getDisplayName(), SetupItems.ADD_ZOMBIE_SPAWN.getLore());

					item.setClickEvent(event -> {
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
					});
				}));

		IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);

		builder.addItem(item -> {
			item.setItem(8, SetupItems.ADD_PLAYER_SPAWN.getMaterial(), SetupItems.ADD_PLAYER_SPAWN.getDisplayName(),
					SetupItems.ADD_PLAYER_SPAWN.getLore());

			item.setClickEvent(event -> {
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
			});
		}).addItem(item -> {
			item.setItem(17, SetupItems.SET_LOBBY.getMaterial(), SetupItems.SET_LOBBY.getDisplayName(),
					SetupItems.SET_LOBBY.getLore());

			item.setClickEvent(event -> {
				if (event.isLeftClick()) {
					game.getGameLobby().location = event.getWhoClicked().getLocation();
				}
			});
		});

		InventoryGuiHandler value = builder.build();

		value.setSourceGame(game.getName());
		return value;
	}

	public enum SetupItems implements Consumer<Game> {
		BOSSBAR(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.WITHER_SKELETON_SKULL;
			}

			@Override
			public String getDisplayName() {
				return (game.bossbarEnabled ? "&cDis" : "&5En") + "able boss bar";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("", "&aLeft click to switch");
			}
		},

		ACTIONBAR(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.COMPARATOR;
			}

			@Override
			public String getDisplayName() {
				return (game.actionbarEnabled ? "&cDis" : "&aEn") + "able action bar";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("", "&aLeft click to switch");
			}
		},

		MAXPLAYERS(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.FEATHER;
			}

			@Override
			public String getDisplayName() {
				return "&6Change maximum players";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("&eCurrent amount: " + game.maxPlayers, " ", "&6Right click to decrease",
						"&6Left click to increase");
			}
		},

		MINPLAYERS(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.INK_SAC;
			}

			@Override
			public String getDisplayName() {
				return "&2Change minimum players";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("&eCurrent amount: " + game.minPlayers, " ", "&6Right click to decrease",
						"&6Left click to increase");
			}
		},

		GAMETIME(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.EMERALD;
			}

			@Override
			public String getDisplayName() {
				return "&eChange game time";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("&eCurrent time: " + Utils.getFormattedTime(game.gameTime), " ",
						"&6Right click to decrease with 5 minute", "&6Left click to increase with 5 minute");
			}
		},

		LOBBYTIME(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.LAPIS_LAZULI;
			}

			@Override
			public String getDisplayName() {
				return "&bChange lobby time";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("&eCurrent time: " + Utils.getFormattedTime(game.getGameLobby().lobbyTime), " ",
						"&6Right click to decrease with 5 seconds", "&6Left click to increase with 5 seconds");
			}
		},

		RANDOM_ZOMBIE_SPAWN(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.ARROW;
			}

			@Override
			public String getDisplayName() {
				return "&7Set random zombie spawn";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList("&eSets the zombie spawn", "&emode to random, so you",
						"&edon't need to set spawns", "&6Status:&a " + game.randomSpawnForZombies, " ",
						"&cSettings this to true will", "&cremoves all added spawns!");
			}
		},

		ADD_ZOMBIE_SPAWN(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.FLOWER_POT;
			}

			@Override
			public String getDisplayName() {
				return "&4Add zombie spawn";
			}

			@Override
			public List<String> getLore() {
				IGameSpawn zombieSpawn = game.getSpawn(GameZombieSpawn.class);

				return Arrays.asList(
						"&eCurrent amount of spawns: "
								+ (zombieSpawn != null ? zombieSpawn.getSpawnLocations().size() : 0),
						" ", "&6Left click to add a spawn to your curren position");
			}
		},

		ADD_PLAYER_SPAWN(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.IRON_SHOVEL;
			}

			@Override
			public String getDisplayName() {
				return "&7Add player spawn";
			}

			@Override
			public List<String> getLore() {
				IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);

				return Arrays.asList(
						"&eCurrent amount of spawns: " + (gameSpawn != null ? gameSpawn.getSpawnLocations().size() : 0),
						" ", "&6Left click to add a spawn to your current position");
			}
		},

		SET_LOBBY(game -> {
		}) {
			@Override
			public Material getMaterial() {
				return Material.BLUE_WOOL;
			}

			@Override
			public String getDisplayName() {
				return "&9Set lobby position";
			}

			@Override
			public List<String> getLore() {
				return Arrays.asList(game.getGameLobby().location != null ? "&a\u2714 Lobby set" : "", " ",
						"&6Left click to set lobby to your current position");
			}
		};

		private Consumer<Game> cons;
		protected Game game;

		SetupItems(Consumer<Game> cons) {
			this.cons = cons;
		}

		public abstract Material getMaterial();

		public abstract String getDisplayName();

		public abstract java.util.List<String> getLore();

		@Override
		public void accept(Game game) {
			cons.accept(this.game = game);
		}
	}
}
