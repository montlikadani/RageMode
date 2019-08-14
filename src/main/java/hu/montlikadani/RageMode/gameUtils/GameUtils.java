package hu.montlikadani.ragemode.gameUtils;

import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.CombatAxe;
import hu.montlikadani.ragemode.items.ForceStarter;
import hu.montlikadani.ragemode.items.Grenade;
import hu.montlikadani.ragemode.items.LeaveGame;
import hu.montlikadani.ragemode.items.RageArrow;
import hu.montlikadani.ragemode.items.RageBow;
import hu.montlikadani.ragemode.items.RageKnife;
import hu.montlikadani.ragemode.signs.SignCreator;

public class GameUtils {

	private static GameStatus status = GameStatus.STOPPED;

	/**
	 * Broadcast for in-game players to the specified game
	 * @param game Game
	 * @param message Message
	 */
	public static void broadcastToGame(String game, String message) {
		for (Entry<String, String> players : PlayerList.getPlayers().entrySet()) {
			if (players != null) {
				Player p = Bukkit.getPlayer(UUID.fromString(players.getValue()));
				if (p != null && PlayerList.getPlayersGame(p).equals(game))
					p.sendMessage(message);
			}
		}
	}

	/**
	 * Checks whatever the specified game is exists or no.
	 * @param game Game
	 * @return true if game exists
	 */
	public static boolean isGameWithNameExists(String game) {
		return GetGames.isGameExistent(game);
	}

	/**
	 * Get the game spawn by name.
	 * @param name Game name
	 * @return GameSpawnGetter
	 */
	public static GameSpawnGetter getGameSpawnByName(String name) {
		for (GameSpawnGetter gsg : RageMode.getInstance().getSpawns()) {
			if (gsg.getGameName().equalsIgnoreCase(name))
				return gsg;
		}
		return null;
	}

	/**
	 * Give game items to the specified player. If the item slot not found
	 * in configuration, then adds the item to the inventory.
	 * @param p Player
	 * @param clear - if true clears the player inventory before add items
	 */
	public static void addGameItems(Player p, boolean clear) {
		PlayerInventory inv = p.getInventory();
		if (clear)
			Utils.clearPlayerInventory(p);

		ItemStack result = null;
		FileConfiguration f = RageMode.getInstance().getConfiguration().getCfg();
		String path = "items.";
		if (f.contains(path + "rageBow.slot"))
			inv.setItem(f.getInt("items.rageBow.slot"), RageBow.getItem());
		else
			result = RageBow.getItem();

		if (f.contains(path + "rageKnife.slot"))
			inv.setItem(f.getInt("items.rageKnife.slot"), RageKnife.getItem());
		else
			result = RageKnife.getItem();

		if (f.contains(path + "combatAxe.slot"))
			inv.setItem(f.getInt("items.combatAxe.slot"), CombatAxe.getItem());
		else
			result = CombatAxe.getItem();

		if (f.contains(path + "rageArrow.slot"))
			inv.setItem(f.getInt("items.rageArrow.slot"), RageArrow.getItem());
		else
			result = RageArrow.getItem();

		if (f.contains(path + "grenade.slot"))
			inv.setItem(f.getInt("items.grenade.slot"), Grenade.getItem());
		else
			result = Grenade.getItem();

		if (result != null)
			inv.addItem(result);
	}

	/**
	 * Saves the player data to a yaml file
	 * <p>This prevents losing the player data when the server has stopped randomly.
	 * @param p Player
	 */
	public static void savePlayerData(Player p) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();

		PlayerList.oldLocations.addToBoth(p, p.getLocation());
		PlayerList.oldInventories.addToBoth(p, inv.getContents());
		PlayerList.oldArmor.addToBoth(p, inv.getArmorContents());
		PlayerList.oldHealth.addToBoth(p, p.getHealth());
		PlayerList.oldHunger.addToBoth(p, p.getFoodLevel());
		if (!p.getActivePotionEffects().isEmpty())
			PlayerList.oldEffects.addToBoth(p, p.getActivePotionEffects());

		PlayerList.oldGameMode.addToBoth(p, p.getGameMode());

		if (!p.getDisplayName().equals(p.getDisplayName()))
			PlayerList.oldDisplayName.addToBoth(p, p.getDisplayName());

		if (!p.getPlayerListName().equals(p.getPlayerListName()))
			PlayerList.oldListName.addToBoth(p, p.getPlayerListName());

		if (p.getFireTicks() > 0)
			PlayerList.oldFire.addToBoth(p, p.getFireTicks());

		if (p.getExp() > 0d)
			PlayerList.oldExp.addToBoth(p, p.getExp());

		if (p.getLevel() > 0)
			PlayerList.oldExpLevel.addToBoth(p, p.getLevel());

		if (p.isInsideVehicle())
			PlayerList.oldVehicle.addToBoth(p, p.getVehicle());

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			org.bukkit.configuration.file.FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + p.getName() + ".";

			data.set(path + "location", p.getLocation());
			data.set(path + "contents", inv.getContents());
			data.set(path + "armor-contents", inv.getArmorContents());
			data.set(path + "health", p.getHealth());
			data.set(path + "food", p.getFoodLevel());
			if (!p.getActivePotionEffects().isEmpty())
				data.set(path + "potion-effects", p.getActivePotionEffects());

			// Using the gamemode name to prevent InvalidConfiguration error
			data.set(path + "game-mode", p.getGameMode().name());

			if (!p.getDisplayName().equals(p.getDisplayName()))
				data.set(path + "display-name", p.getDisplayName());

			if (!p.getPlayerListName().equals(p.getPlayerListName()))
				data.set(path + "list-name", p.getPlayerListName());

			if (p.getFireTicks() > 0)
				data.set(path + "fire-ticks", p.getFireTicks());

			if (p.getExp() > 0d)
				data.set(path + "exp", p.getExp());

			if (p.getLevel() > 0)
				data.set(path + "level", p.getLevel());

			if (p.isInsideVehicle()) {
				data.set(path + "vehicle", p.getVehicle().getType());
				data.set(path + "vehicle", p.getVehicle().getLocation());
			}

			Configuration.saveFile(data, conf.getDatasFile());
		}

		clearPlayerTools(p);
	}

	/**
	 * Connect the specified player to the game. If the game is
	 * running and the player is not playing, then if want to
	 * join to the game, switching to spectator mode.
	 * 
	 * @param p Player
	 * @param game GameName
	 */
	public static void joinPlayer(Player p, String game) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (status == GameStatus.RUNNING) {
			if (conf.getCfg().getBoolean("spectator.enable")) {
				if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
					if (PlayerList.addSpectatorPlayer(p)) {
						getGameSpawnByName(game).randomSpawn(p);

						p.setAllowFlight(true);
						p.setFlying(true);
						p.setGameMode(GameMode.SPECTATOR);

						if (conf.getCfg().contains("items.leavegameitem"))
							inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());
					}
				} else
					p.sendMessage(RageMode.getLang().get("game.player-not-switch-spectate"));
			} else
				p.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
		} else {
			if (status == GameStatus.NOTREADY) {
				p.sendMessage(RageMode.getLang().get("commands.join.game-locked"));
				return;
			}

			if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
				p.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
				return;
			}

			MapChecker mapChecker = new MapChecker(game);
			if (mapChecker.isValid()) {
				if (conf.getCfg().getBoolean("require-empty-inventory-to-join")) {
					for (ItemStack armor : inv.getArmorContents()) {
						if (armor != null && !armor.getType().equals(Material.AIR)) {
							p.sendMessage(RageMode.getLang().get("commands.join.empty-inventory.armor"));
							return;
						}
					}

					for (ItemStack content : inv.getContents()) {
						if (content != null && !content.getType().equals(Material.AIR)) {
							p.sendMessage(RageMode.getLang().get("commands.join.empty-inventory.contents"));
							return;
						}
					}
				} else if (conf.getCfg().getBoolean("save-player-datas-to-file"))
					savePlayerData(p);

				if (PlayerList.addPlayer(p, game)) {
					p.teleport(GetGameLobby.getLobbyLocation(game));

					runCommands(p, game, "join");
					sendActionBarMessages(p, game, "join");

					if (conf.getCfg().contains("items.leavegameitem"))
						inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

					if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
						inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

					broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

					String title = conf.getCfg().getString("titles.join-game.title");
					String subtitle = conf.getCfg().getString("titles.join-game.subtitle");
					if (title != null || subtitle != null) {
						title = title.replace("%game%", game);
						subtitle = subtitle.replace("%game%", game);
						Titles.sendTitle(p, conf.getCfg().getInt("titles.join-game.fade-in"), conf.getCfg().getInt("titles.join-game.stay"),
								conf.getCfg().getInt("titles.join-game.fade-out"), title, subtitle);
					}
					SignCreator.updateAllSigns(game);
				} else
					Bukkit.getConsoleSender().sendMessage(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", game));
			} else
				p.sendMessage(mapChecker.getMessage());
		}
	}

	/**
	 * Kicks the specified player from the game and server.
	 * @param p Player
	 */
	public static void kickPlayer(Player p) {
		// Just removes the spec player
		PlayerList.removeSpectatorPlayer(p);

		if (status == GameStatus.RUNNING && PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			if (PlayerList.removePlayer(p)) {
				Debug.logConsole("Player " + p.getName() + " left the server while playing.");

				List<String> list = RageMode.getInstance().getConfiguration().getCfg()
						.getStringList("game.global.run-commands-for-player-left-while-playing");
				if (list != null && !list.isEmpty()) {
					for (String cmds : list) {
						cmds = cmds.replace("%player%", p.getName());
						// For ipban
						cmds = cmds.replace("%player-ip%", p.getAddress().getAddress().getHostAddress());
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), RageMode.getLang().colors(cmds));
					}
				}
			}
		}

		HoloHolder.deleteHoloObjectsOfPlayer(p);
	}

	/**
	 * Fully clears the specified player inventory, remove effects, food and health set to 0 and
	 * more related to player.
	 * @param p Player
	 */
	public static void clearPlayerTools(Player p) {
		Utils.clearPlayerInventory(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFlying(false);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);

		if (p.isInsideVehicle())
			p.leaveVehicle();

		p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));

		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}

	/**
	 * Run commands in game, when the player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * @param game Game name
	 * @param cmdType Command type, such as death, join or other
	 */
	public static void runCommandsForAll(String game, String cmdType) {
		for (Entry<String, String> players : PlayerList.getPlayers().entrySet()) {
			Player p = PlayerList.getPlayerByUUID(players.getValue());
			runCommands(p, game, cmdType);
		}
	}

	/**
	 * Run commands in game, when the player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * @param p Player
	 * @param game Game name
	 * @param cmdType Command type, such as death, join or other
	 */
	public static void runCommands(Player p, String game, String cmdType) {
		List<String> list = RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.run-commands");

		if (list != null && !list.isEmpty()) {
			for (String cmd : list) {
				if (cmd.split(":").length < 3 && cmd.split(":").length > 4) {
					Debug.logConsole(Level.WARNING, "In the rewards file the in-game commands the split length is equal to 3.");
					continue;
				}

				if (cmd.contains("chance:")) {
					String value = cmd;
					value = value.split("chance:")[1].replaceAll("[^0-9]+", "");
					double chance = Double.parseDouble(value);

					if (ThreadLocalRandom.current().nextInt(0, 100) > chance)
						continue;

					cmd = cmd.replace("chance:" + value + "-", "");

					String type = cmd.split(":")[0];
					if (type.equals(cmdType)) {
						String consoleOrPlayer = cmd.split(":")[1];

						cmd = cmd.split(":")[2].replace("%world%", p.getWorld().getName())
								.replace("%game%", game)
								.replace("%player%", p.getName());
						cmd = RageMode.getLang().colors(cmd);

						if (consoleOrPlayer.equals("console"))
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						else if (consoleOrPlayer.equals("player"))
							p.performCommand(cmd);
					}
				} else {
					String type = cmd.split(":")[0];
					if (type.equals(cmdType)) {
						String consoleOrPlayer = cmd.split(":")[1];

						cmd = cmd.split(":")[2].replace("%world%", p.getWorld().getName())
								.replace("%game%", game)
								.replace("%player%", p.getName());
						cmd = RageMode.getLang().colors(cmd);

						if (consoleOrPlayer.equals("console"))
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						else if (consoleOrPlayer.equals("player"))
							p.performCommand(cmd);
					}
				}
			}
		}
	}

	/**
	 * Send action bar messages to the player when doing something,
	 * such as joining, leave, starting or stopping game.
	 * <p>This returns if the actionbar option is disabled in configurations.
	 * @param p Player
	 * @param game Game name
	 * @param type Action type
	 */
	public static void sendActionBarMessages(Player p, String game, String type) {
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getArenasCfg().isSet("arenas." + game + ".actionbar")) {
			if (!conf.getArenasCfg().getBoolean("arenas." + game + ".actionbar"))
				return;
		} else if (!conf.getCfg().getBoolean("game.global.defaults.actionbar"))
			return;

		List<String> list = conf.getCfg().getStringList("actionbar-messages.actions");

		if (list != null && !list.isEmpty()) {
			for (String msg : list) {
				if (msg.split(":").length < 2 && msg.split(":").length > 2) {
					Debug.logConsole(Level.WARNING, "In the config file the actionbar messages the split length is equal to 2.");
					continue;
				}

				String action = msg.split(":")[0];
				if (action.equals(type)) {
					String message = msg.split(":")[1];
					message = message.replace("%game%", game).replace("%player%", p.getName());
					ActionBar.sendActionBar(p, RageMode.getLang().colors(message));
				}
			}
		}
	}

	/**
	 * Teleports players to a random location.
	 * This will return if the spawns size 0 because with value 0 are not possible.
	 * @param spawn GameSpawnGetter
	 */
	public static void teleportPlayersToGameSpawns(GameSpawnGetter spawn) {
		for (Entry<String, String> uuids : PlayerList.getPlayers().entrySet()) {
			Player player = Bukkit.getPlayer(UUID.fromString(uuids.getValue()));
			teleportPlayerToGameSpawn(player, spawn);
		}
	}

	/**
	 * Teleports the specified player to a random location.
	 * This will return if the spawns size 0 because with value 0 are not possible.
	 * @param p Player who is in game
	 * @param spawn GameSpawnGetter
	 */
	public static void teleportPlayerToGameSpawn(Player p, GameSpawnGetter spawn) {
		Random r = new Random();
		if (spawn.getSpawnLocations().size() > 0) {
			int x = r.nextInt(spawn.getSpawnLocations().size());
			Location location = spawn.getSpawnLocations().get(x);
			p.teleport(location);
		}
	}

	public static boolean getLookingAt(Player player, LivingEntity livingEntity) {
		Location eye = player.getEyeLocation();
		Vector toEntity = livingEntity.getLocation().toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());

		return dot >= 0.99D;
	}

	/**
	 * Get the GameStatus
	 * @return {@link GameStatus}
	 */
	public static GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to new status
	 * @param status the status to be set
	 */
	public static void setStatus(GameStatus status) {
		GameUtils.status = status;
	}
}
