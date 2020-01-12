package hu.montlikadani.ragemode.gameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.gameLogic.*;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.*;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.statistics.DBThreads;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class GameUtils {

	private static Map<String, GameStatus> status = new HashMap<>();

	private static Bonus bonuses = null;

	/**
	 * Broadcast a message to the currently playing players for that given game.
	 * @see #broadcastToGame(Game, String)
	 * @param name Game Name
	 * @param message The message
	 */
	public static void broadcastToGame(String name, String message) {
		broadcastToGame(getGame(name), message);
	}

	/**
	 * Broadcast a message to the currently playing players for that given game.
	 * @param game Game
	 * @param message The message
	 */
	public static void broadcastToGame(Game game, String message) {
		Validate.notNull(game, "Game can't be null!");

		for (PlayerManager pm : game.getPlayersFromList()) {
			if (pm.getGameName().equalsIgnoreCase(game.getName())) {
				pm.getPlayer().sendMessage(message);
			}
		}

		for (PlayerManager pm2 : game.getSpectatorPlayersFromList()) {
			if (pm2.getGameName().equalsIgnoreCase(game.getName())) {
				pm2.getPlayer().sendMessage(message);
			}
		}
	}

	/**
	 * Checks the game name if contains special chars, too long or contains
	 * a ragemode command.
	 * @param pl Player
	 * @param name Game
	 * @return false if:
	 * <br>- contains special chars
	 * <br>- the name is too long
	 * <br>- in the name contains a ragemode command
	 */
	public static boolean checkName(Player pl, String name) {
		if (!name.matches("^[a-zA-Z0-9\\_\\-]+$")) {
			sendMessage(pl, RageMode.getLang().get("setup.addgame.special-chars"));
			return false;
		}

		if (name.length() > 40) {
			sendMessage(pl, RageMode.getLang().get("setup.addgame.name-greater"));
			return false;
		}

		if (reservedNames.contains(name)) { // hehe
			sendMessage(pl, RageMode.getLang().get("setup.addgame.bad-name"));
			return false;
		}

		return true;
	}

	private static List<String> reservedNames = buildReservedNameList();

	private static List<String> buildReservedNameList() {
		List<String> reservedNames = new ArrayList<>();
		for (Entry<String, String> cmds : RmCommand.arg.entrySet()) {
			reservedNames.add(cmds.getKey());
		}

		return reservedNames;
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
	 * Get the game spawn by game.
	 * @see #getGameSpawn(String)
	 * @param game {@link Game}
	 * @return {@link GameSpawn}
	 */
	public static GameSpawn getGameSpawn(Game game) {
		Validate.notNull(game, "Game can't be null!");

		for (GameSpawn gsg : RageMode.getInstance().getSpawns()) {
			if (gsg.getGame().equals(game)) {
				return gsg;
			}
		}

		return null;
	}

	/**
	 * Get the game spawn by name.
	 * @param name Game name
	 * @return {@link GameSpawn}
	 */
	public static GameSpawn getGameSpawn(String name) {
		Validate.notNull(name, "Game name can't be null!");
		Validate.notEmpty(name, "Game name can't be empty!");

		for (GameSpawn gsg : RageMode.getInstance().getSpawns()) {
			if (gsg.getGame().getName().equalsIgnoreCase(name)) {
				return gsg;
			}
		}

		return null;
	}

	/**
	 * Get the game by player uuid.
	 * @deprecated converting string to uuid is too long time
	 * @param uuid UUID
	 * @see #getGameByPlayer(UUID)
	 * @return Game if player is in game.
	 */
	@Deprecated
	public static Game getGameByPlayer(String uuid) {
		Validate.notNull(uuid, "Player UUID can not be null!");
		Validate.notEmpty(uuid, "Player UUID can't be empty!");

		return getGameByPlayer(Bukkit.getPlayer(UUID.fromString(uuid)));
	}

	/**
	 * Get the game by player uuid.
	 * @see #getGameByPlayer(Player)
	 * @param uuid UUID
	 * @return Game if player is in game.
	 */
	public static Game getGameByPlayer(UUID uuid) {
		Validate.notNull(uuid, "Player UUID can not be null!");

		return getGameByPlayer(Bukkit.getPlayer(uuid));
	}

	/**
	 * Get the game by player.
	 * @param p Player
	 * @return Game if player is in game.
	 */
	public static Game getGameByPlayer(Player p) {
		Validate.notNull(p, "Player can not be null!");

		for (Game game : RageMode.getInstance().getGames()) {
			if (game.getPlayers().containsKey(p)) {
				return game;
			}
		}

		return null;
	}

	/**
	 * Get the game by player uuid.
	 * @deprecated converting string to uuid is too long time
	 * @param uuid UUID
	 * @see #getGameBySpectator(UUID)
	 * @return Game if player is in game.
	 */
	@Deprecated
	public static Game getGameBySpectator(String uuid) {
		Validate.notNull(uuid, "Player UUID can not be null!");
		Validate.notEmpty(uuid, "Player UUID can't be empty!");

		return getGameBySpectator(Bukkit.getPlayer(UUID.fromString(uuid)));
	}

	/**
	 * Get the game by player uuid.
	 * @see #getGameBySpectator(Player)
	 * @param uuid UUID
	 * @return Game if player is in game.
	 */
	public static Game getGameBySpectator(UUID uuid) {
		Validate.notNull(uuid, "Player UUID can not be null!");

		return getGameBySpectator(Bukkit.getPlayer(uuid));
	}

	/**
	 * Get the game by spectator player.
	 * @param p Player
	 * @return Game if player is in spectator mode and in game.
	 */
	public static Game getGameBySpectator(Player p) {
		Validate.notNull(p, "Player can not be null!");

		for (Game game : RageMode.getInstance().getGames()) {
			if (game.getSpectatorPlayers().containsKey(p)) {
				return game;
			}
		}

		return null;
	}

	/**
	 * Checks if the given player is currently in game.
	 * @param p Player
	 * @return true if playing
	 */
	public static boolean isPlayerPlaying(Player p) {
		return getGameByPlayer(p) != null;
	}

	/**
	 * Checks if the given spectator player is currently in game.
	 * @param p Player
	 * @return true if in game
	 */
	public static boolean isSpectatorPlaying(Player p) {
		return getGameBySpectator(p) != null;
	}

	/**
	 * Get the game by name.
	 * @param name Game
	 * @return Game if the given name is exists.
	 */
	public static Game getGame(String name) {
		Validate.notNull(name, "Game name can not be null!");
		Validate.notEmpty(name, "Game name can't be empty!");

		for (Game game : RageMode.getInstance().getGames()) {
			if (game.getName().equalsIgnoreCase(name)) {
				return game;
			}
		}

		return null;
	}

	/**
	 * Gets the Bonus
	 * @return {@link Bonus}
	 */
	public static Bonus getBonus() {
		if (bonuses == null) {
			bonuses = new Bonus();
		}

		return bonuses;
	}

	/**
	 * Give game items to the given player. If the item slot not found
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
		if (getGameByPlayer(p) != null) {
			getGameByPlayer(p).getPlayerManager(p).storePlayerTools();
		}

		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + p.getName() + ".";

			PlayerInventory inv = p.getInventory();

			data.set(path + "location", p.getLocation());
			if (inv.getContents() != null) {
				data.set(path + "contents", inv.getContents());
			}
			if (inv.getArmorContents() != null) {
				data.set(path + "armor-contents", inv.getArmorContents());
			}
			if (p.getHealth() < NMS.getMaxHealth(p)) {
				data.set(path + "health", p.getHealth());
			}
			if (p.getFoodLevel() < 20) {
				data.set(path + "food", p.getFoodLevel());
			}
			if (!p.getActivePotionEffects().isEmpty())
				data.set(path + "potion-effects", p.getActivePotionEffects());

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
			Configuration.loadFile(data, conf.getDatasFile());
		}
	}

	/**
	 * Connect the given player to the game. If the game is
	 * running and the player is not playing, then if want to
	 * join to the game, switching to spectator mode.
	 * 
	 * @param p Player
	 * @param game {@link Game}
	 */
	public static void joinPlayer(Player p, Game game) {
		PlayerInventory inv = p.getInventory();
		Configuration conf = RageMode.getInstance().getConfiguration();
		String name = game.getName();

		if (getStatus(name) == GameStatus.RUNNING) {
			if (!ConfigValues.isSpectatorEnabled()) {
				sendMessage(p, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
				return;
			}

			if (isPlayerPlaying(p)) {
				sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
				return;
			}

			if (game.addSpectatorPlayer(p, name)) {
				p.teleport(getGameSpawn(name).getRandomSpawn());

				p.setAllowFlight(true);
				p.setFlying(true);
				p.setGameMode(GameMode.SPECTATOR);

				if (conf.getCfg().contains("items.leavegameitem"))
					inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());
			}
		} else {
			if (getStatus(name) == GameStatus.NOTREADY) {
				sendMessage(p, RageMode.getLang().get("commands.join.game-locked"));
				return;
			}

			if (isPlayerPlaying(p) || getStatus(name) == GameStatus.WAITING && game.getPlayers().containsKey(p)) {
				sendMessage(p, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
				return;
			}

			MapChecker mapChecker = new MapChecker(name);
			if (!mapChecker.isValid()) {
				sendMessage(p, mapChecker.getMessage());
				return;
			}

			if (ConfigValues.isRequireEmptyInv()) {
				for (ItemStack armor : inv.getArmorContents()) {
					if (armor != null && !armor.getType().equals(Material.AIR)) {
						sendMessage(p, RageMode.getLang().get("commands.join.empty-inventory.armor"));
						return;
					}
				}

				for (ItemStack content : inv.getContents()) {
					if (content != null && !content.getType().equals(Material.AIR)) {
						sendMessage(p, RageMode.getLang().get("commands.join.empty-inventory.contents"));
						return;
					}
				}
			}

			if (!game.addPlayer(p)) {
				Debug.sendMessage(RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", name));
				return;
			}

			if (!ConfigValues.isSavePlayerData()) {
				// We still need some data saving
				game.getPlayerManager(p).getStorePlayer().oldLocation = p.getLocation();
				game.getPlayerManager(p).getStorePlayer().oldGameMode = p.getGameMode();

				p.setGameMode(GameMode.SURVIVAL);
			} else {
				savePlayerData(p);
			}
			clearPlayerTools(p);

			p.teleport(GameLobby.getLobbyLocation(name));

			runCommands(p, name, "join");
			sendBossBarMessages(p, name, "join");
			sendActionBarMessages(p, name, "join");
			setStatus(name, GameStatus.WAITING, false);

			if (conf.getCfg().contains("items.leavegameitem"))
				inv.setItem(conf.getCfg().getInt("items.leavegameitem.slot"), LeaveGame.getItem());

			if (conf.getCfg().contains("items.force-start") && p.hasPermission("ragemode.admin.item.forcestart"))
				inv.setItem(conf.getCfg().getInt("items.force-start.slot"), ForceStarter.getItem());

			broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

			String title = ConfigValues.getTitleJoinGame();
			String subtitle = ConfigValues.getSubTitleJoinGame();
			title = title.replace("%game%", name);
			subtitle = subtitle.replace("%game%", name);

			String[] split = ConfigValues.getJoinTitleTime().split(", ");
			Titles.sendTitle(p, (split.length > 1 ? Integer.parseInt(split[0]) : 20),
					(split.length > 2 ? Integer.parseInt(split[1]) : 30),
					(split.length > 3 ? Integer.parseInt(split[2]) : 20), title, subtitle);

			SignCreator.updateAllSigns(name);
		}
	}

	/**
	 * Forces the given game start.
	 * @param game {@link Game}
	 */
	public static void forceStart(Game game) {
		Validate.notNull(game, "Game can't be null!");

		GameLoader loder = new GameLoader(game);
		loder.startGame();
	}

	/**
	 * Kicks the given player from the game.
	 * @param p Player
	 * @param game {@link Game}
	 */
	public static void kickPlayer(Player p, Game game) {
		Validate.notNull(p, "Player can't be null!");

		if (game != null && getStatus(game.getName()) == GameStatus.RUNNING && game.removePlayer(p)) {
			Debug.logConsole("Player " + p.getName() + " left the server while playing.");

			List<String> list = ConfigValues.getCmdsForPlayerLeave();
			for (String cmds : list) {
				cmds = cmds.replace("%player%", p.getName());
				// For ipban
				cmds = cmds.replace("%player-ip%", p.getAddress().getAddress().getHostAddress());
				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.colors(cmds));
			}
		}
	}

	/**
	 * Kicks the given spectator player from game.
	 * @param p Player to be kick
	 * @param game the game where kick from
	 */
	public static void kickSpectator(Player p, Game game) {
		Validate.notNull(p, "Player can't be null!");

		if (game != null) {
			game.removeSpectatorPlayer(p);
		}
	}

	/**
	 * Fully clears the given player inventory, remove effects, food, health set to 0 and
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

		if (!p.getActivePotionEffects().isEmpty())
			p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));

		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}

	/**
	 * Run commands for all players in game, when a player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * @see #runCommands(Player, String, String)
	 * @param game Game name
	 * @param cmdType Command type, such as death, join or other
	 */
	public static void runCommandsForAll(String game, String cmdType) {
		for (PlayerManager pl : getGame(game).getPlayersFromList()) {
			runCommands(pl.getPlayer(), game, cmdType);
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
		if (!ConfigValues.isRewardEnabled()) {
			return;
		}

		List<String> list = RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.run-commands");
		if (list == null) {
			return;
		}

		for (String cmd : list) {
			if (cmd.split(":").length < 3 && cmd.split(":").length > 4) {
				Debug.logConsole(Level.WARNING,
						"In the rewards file the in-game commands the split length is equal to 3.");
				continue;
			}

			if (cmd.contains("chance:")) {
				String value = cmd;
				value = value.split("chance:")[1].replaceAll("[^0-9]+", "");
				double chance = Double.parseDouble(value);

				if (ThreadLocalRandom.current().nextInt(0, 100) > chance)
					continue;

				cmd = cmd.replace("chance:" + value + "-", "");
			}

			String type = cmd.split(":")[0];
			if (type.equalsIgnoreCase(cmdType)) {
				String consoleOrPlayer = cmd.split(":")[1];

				cmd = cmd.split(":")[2].replace("%world%", p.getWorld().getName()).replace("%game%", game)
						.replace("%player%", p.getName());
				cmd = Utils.colors(cmd);

				if (consoleOrPlayer.equalsIgnoreCase("console"))
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
				else if (consoleOrPlayer.equalsIgnoreCase("player"))
					p.performCommand(cmd);
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
		} else if (!ConfigValues.isActionbarEnabled())
			return;

		List<String> list = ConfigValues.getActionbarActions();
		if (list == null) {
			return;
		}

		for (String msg : list) {
			if (msg.split(":").length < 2 && msg.split(":").length > 2) {
				Debug.logConsole(Level.WARNING,
						"In the config file the actionbar messages the split length is equal to 2.");
				continue;
			}

			String action = msg.split(":")[0];
			if (action.equalsIgnoreCase(type)) {
				String message = msg.split(":")[1];
				message = message.replace("%game%", game).replace("%player%", p.getName());
				ActionBar.sendActionBar(p, Utils.colors(message));
			}
		}
	}

	/**
	 * Send boss bar messages to all current playing players, when
	 * doing something, such as joining, leave, starting or stopping game.
	 * @see #sendBossBarMessages(Player, String, String)
	 * @param game Game name
	 * @param type Action type
	 */
	public static void sendBossBarMessages(String game, String type) {
		for (PlayerManager pm : getGame(game).getPlayersFromList()) {
			sendBossBarMessages(pm.getPlayer(), game, type);
		}
	}

	/**
	 * Send boss bar messages to the current playing player, when
	 * doing something, such as joining, leave, starting or stopping game.
	 * <p>This returns if the boosbar option is disabled in configurations.
	 * @param p Player
	 * @param game Game name
	 * @param type Action type
	 */
	public static void sendBossBarMessages(final Player p, String game, String type) {
		if (Version.isCurrentLower(Version.v1_9_R1)) {
			Debug.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			return;
		}

		if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + game + ".bossbar")) {
			if (!RageMode.getInstance().getConfiguration().getArenasCfg().getBoolean("arenas." + game + ".bossbar"))
				return;
		} else if (!ConfigValues.isActionbarEnabled())
			return;

		List<String> list = ConfigValues.getBossbarActions();
		if (list == null) {
			return;
		}

		for (String msg : list) {
			String[] split = msg.split(":");
			if (split.length < 2) {
				Debug.logConsole(Level.WARNING,
						"In the config file the bossbar messages the split length should be 2 or 4.");
				continue;
			}

			if (split[0].equalsIgnoreCase(type)) {
				String message = split[1];
				if (message == null) {
					continue;
				}

				message = message.replace("%game%", game).replace("%player%", p.getName());
				message = Utils.colors(message);

				BarColor color = split.length > 2 ? BarColor.valueOf(split[2].toUpperCase()) : BarColor.BLUE;
				BarStyle style = split.length > 3 ? BarStyle.valueOf(split[3].toUpperCase()) : BarStyle.SOLID;

				int second = 6;
				if (split.length > 4 && Utils.isInt(split[4])) {
					second = Integer.parseInt(split[4]);
				}

				BossbarManager manager = RageMode.getInstance().getBossbarManager();
				manager.createBossbar(p, message, color, style);
				manager.showBossbar(p, second);
			}
		}
	}

	/**
	 * Teleports all players to a random spawn location.
	 * @see #teleportPlayerToGameSpawn(Player, GameSpawn)
	 * @param spawn {@link GameSpawn}
	 */
	public static void teleportPlayersToGameSpawns(GameSpawn spawn) {
		for (PlayerManager pm : spawn.getGame().getPlayersFromList()) {
			teleportPlayerToGameSpawn(pm.getPlayer(), spawn);
		}
	}

	/**
	 * Teleports the given player to a random spawn location.
	 * This will return if there are no spawn added to list.
	 * @param p Player
	 * @param spawn {@link GameSpawn}
	 */
	public static void teleportPlayerToGameSpawn(Player p, GameSpawn spawn) {
		if (spawn.getSpawnLocations().size() > 0) {
			// Why this always happens?
			// IllegalStateException: PlayerTeleportEvent may only be triggered synchronously.
			Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> p.teleport(spawn.getRandomSpawn()));
		}
	}

	/**
	 * Stops the specified game if running.
	 * @see #stopGame(Game, boolean)
	 * @param name Game name
	 */
	public static void stopGame(String name) {
		stopGame(getGame(name), true);
	}

	/**
	 * Stops the specified game if running.
	 * This calculates the players who has the highest points and announcing
	 * to a title message. If there are no winner player valid, players
	 * will be removed from the game with some rewards.
	 * This will saves the player statistic to the database and finally stopping the game.
	 * @param game {@link Game}
	 * @param useFreeze if true using game freeze
	 */
	public static void stopGame(final Game game, boolean useFreeze) {
		Validate.notNull(game, "Game can't be null!");

		if (!game.isGameRunning()) {
			return;
		}

		final String name = game.getName();

		final List<PlayerManager> players = game.getPlayersFromList();

		Utils.callEvent(new RMGameStopEvent(game, players));

		boolean winnervalid = false;
		UUID winnerUUID = RageScores.calculateWinner(name, players);
		if (winnerUUID != null && Bukkit.getPlayer(winnerUUID) != null) {
			winnervalid = true;

			Player winner = Bukkit.getPlayer(winnerUUID);

			String wonTitle = ConfigValues.getWonTitle();
			String wonSubtitle = ConfigValues.getWonSubTitle();

			String youWonTitle = ConfigValues.getYouWonTitle();
			String youWonSubtitle = ConfigValues.getYouWonSubTitle();

			wonTitle = wonTitle.replace("%winner%", winner.getName());
			wonTitle = replaceVariables(wonTitle, winnerUUID);
			youWonTitle = replaceVariables(youWonTitle, winnerUUID);

			wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
			wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);
			youWonSubtitle = replaceVariables(youWonSubtitle, winnerUUID);

			for (PlayerManager pm : players) {
				final Player p = pm.getPlayer();

				if (p != winner) {
					String[] split = ConfigValues.getWonTitleTime().split(", ");
					Titles.sendTitle(p, (split.length > 1 ? Integer.parseInt(split[0]) : 20),
							(split.length > 2 ? Integer.parseInt(split[1]) : 30),
							(split.length > 3 ? Integer.parseInt(split[2]) : 20), wonTitle, wonSubtitle);

					if (ConfigValues.isSwitchGMForPlayers()) {
						// Why?
						Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
								() -> p.setGameMode(GameMode.SPECTATOR));
					}
				} else {
					String[] split = ConfigValues.getYouWonTitleTime().split(", ");
					Titles.sendTitle(p, (split.length > 1 ? Integer.parseInt(split[0]) : 20),
							(split.length > 2 ? Integer.parseInt(split[1]) : 30),
							(split.length > 3 ? Integer.parseInt(split[2]) : 20), youWonTitle, youWonSubtitle);
				}

				game.removePlayerSynced(p);
			}
		}

		if (!winnervalid) {
			broadcastToGame(game, RageMode.getLang().get("game.no-won"));

			for (final PlayerManager pm : players) {
				// Why?
				Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> pm.getPlayer().setGameMode(GameMode.SPECTATOR));

				game.removePlayerSynced(pm.getPlayer());
			}
		}

		final Player winner = winnerUUID != null ? Bukkit.getPlayer(winnerUUID) : null;
		if (!useFreeze) {
			finishStopping(game, winner, false);
			return;
		}

		if (EventListener.waitingGames.containsKey(name)) {
			EventListener.waitingGames.remove(name);
		}

		EventListener.waitingGames.put(name, true);

		setStatus(name, GameStatus.GAMEFREEZE);

		RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						if (EventListener.waitingGames.containsKey(name)) {
							EventListener.waitingGames.remove(name);
						}

						finishStopping(game, winner, true);
					}
				}, ConfigValues.getGameFreezeTime() * 20);
	}

	private static void finishStopping(Game game, Player winner, boolean serverStop) {
		if (!game.isGameRunning()) {
			return;
		}

		String gName = game.getName();

		setStatus(gName, null);

		// Lets try to load the stats if empty
		if (RuntimePPManager.getRuntimePPList().isEmpty()) {
			RuntimePPManager.loadPPListFromDatabase();
		}

		for (Iterator<Entry<Player, PlayerManager>> it = game.getSpectatorPlayers().entrySet().iterator(); it
				.hasNext();) {
			Player pl = it.next().getKey();
			game.removeSpectatorPlayer(pl);
		}

		List<PlayerManager> players = game.getPlayersFromList();

		for (PlayerManager pm : players) {
			final Player p = pm.getPlayer();
			final PlayerPoints pP = RageScores.getPlayerPoints(p.getUniqueId());
			if (pP == null) {
				continue;
			}

			Bukkit.getServer().getScheduler().runTaskAsynchronously(RageMode.getInstance(), () -> {
				RuntimePPManager.updatePlayerEntry(pP);

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> HoloHolder.updateHolosForPlayer(p));
			});

			Thread th = new Thread(new DBThreads(pP));
			th.start();

			RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, p);
			Utils.callEvent(gameLeaveEvent);
			if (gameLeaveEvent.isCancelled()) {
				continue; // We need this if the event was cancelled only in one player
			}

			runCommands(p, gName, "stop");
			sendBossBarMessages(p, gName, "stop");
			sendActionBarMessages(p, gName, "stop");
			RageScores.removePointsForPlayer(p.getUniqueId());
			if (game.removePlayer(p)) {
				sendMessage(p, RageMode.getLang().get("game.stopped", "%game%", gName));

				if (ConfigValues.isRewardEnabled()) {
					RewardManager reward = new RewardManager(gName);
					if (winner != null && p == winner) {
						reward.rewardForWinner(winner);
					}

					reward.rewardForPlayers(winner, p);
				}
			}
		}

		game.setGameNotRunning();
		SignCreator.updateAllSigns(gName);
		setStatus(gName, GameStatus.READY);

		if (serverStop) {
			if (ConfigValues.isRestartServerEnabled()) {
				if (RageMode.isSpigot()) {
					Bukkit.spigot().restart();
				} else {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
				}
			} else if (ConfigValues.isStopServerEnabled()) {
				Bukkit.shutdown();
			}
		}
	}

	/**
	 * Stops all currently running games. If there are no running games
	 * will returns.
	 */
	public static void stopAllGames() {
		Debug.logConsole("Searching games to stop...");

		String[] games = GetGames.getGameNames();
		if (games == null)
			return;

		int i = 0;
		while (i < games.length) {
			String name = games[i];
			if (name != null) {
				Game g = getGame(name);
				if (g != null) {
					List<PlayerManager> pList = g.getPlayersFromList();
					if (getStatus(name) == GameStatus.RUNNING && g.isGameRunning()) {
						Debug.logConsole("Stopping " + name + " ...");

						RageScores.calculateWinner(name, pList);

						for (PlayerManager players : pList) {
							Player p = players.getPlayer();

							p.removeMetadata("killedWith", RageMode.getInstance());
							g.removePlayer(p);
							RageScores.removePointsForPlayer(p.getUniqueId());
						}

						for (Iterator<Entry<Player, PlayerManager>> it = g.getSpectatorPlayers().entrySet()
								.iterator(); it.hasNext();) {
							g.removeSpectatorPlayer(it.next().getKey());
						}

						g.setGameNotRunning();

						Debug.logConsole(name + " has been stopped.");
					} else if (getStatus(name) == GameStatus.WAITING) {
						pList.forEach(pl -> g.removePlayer(pl.getPlayer()));
					}
				}
			}

			i++;
		}
	}

	private static String replaceVariables(String s, UUID uuid) {
		PlayerPoints score = RageScores.getPlayerPoints(uuid);

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(score.getPoints()));
		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(score.getKills()));
		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(score.getDeaths()));
		return s;
	}

	/**
	 * Gets the given game current set GameStatus by player.
	 * @see #getStatus(String)
	 * @param p Player
	 * @return {@link GameStatus} if the player is in game
	 */
	public static GameStatus getStatus(Player p) {
		return isPlayerPlaying(p) ? getStatus(getGameByPlayer(p).getName()) : null;
	}

	/**
	 * Gets the given game current set GameStatus.
	 * @param game Game name
	 * @return {@link GameStatus}
	 */
	public static GameStatus getStatus(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be null!");

		return status.get(game);
	}

	/**
	 * Sets the game status to new status. If the status param is null
	 * will set to stopped status.
	 * @see #setStatus(String, GameStatus, boolean)
	 * @param game the game name to set
	 * @param status the new status to be set for the game
	 */
	public static void setStatus(String game, GameStatus status) {
		setStatus(game, status, true);
	}

	/**
	 * Sets the game status to new status.
	 * @param game the game name to set
	 * @param status the new status to be set for the game
	 * @param forceRemove to force remove the existing game status from list
	 */
	public static void setStatus(String game, GameStatus status, boolean forceRemove) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be null!");

		if (forceRemove && GameUtils.status.containsKey(game)) {
			GameUtils.status.remove(game);
		}

		if (status == null) {
			status = GameStatus.STOPPED;
		}

		RMGameStatusChangeEvent event = new RMGameStatusChangeEvent(getGame(game), status);
		Utils.callEvent(event);

		GameUtils.status.put(game, status);
	}
}
