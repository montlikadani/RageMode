package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.ServerSoftwareType;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.*;
import hu.montlikadani.ragemode.gameUtils.modules.ActionBar;
import hu.montlikadani.ragemode.gameUtils.modules.Titles;
import hu.montlikadani.ragemode.items.*;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public final class GameUtils {

	/**
	 * When the game ended and players waiting for teleport.
	 */
	public static final HashMap<String, Boolean> WAITINGGAMES = new HashMap<>();

	private GameUtils() {
	}

	/**
	 * Broadcast a message to the currently playing players for that given game.
	 * 
	 * @see #broadcastToGame(Game, String)
	 * @param name Game Name
	 * @param message The message
	 */
	public static void broadcastToGame(String name, String message) {
		broadcastToGame(getGame(name), message);
	}

	/**
	 * Broadcast a message to the currently playing players for that given game.
	 * 
	 * @param game Game
	 * @param message The message
	 */
	public static void broadcastToGame(Game game, String message) {
		Validate.notNull(game, "Game can't be null");

		if (message.trim().isEmpty()) {
			return;
		}

		for (PlayerManager pm : game.getPlayersFromList()) {
			if (game.getName().equalsIgnoreCase(pm.getGameName())) {
				sendMessage(pm.getPlayer(), message);
			}
		}

		for (PlayerManager pm2 : game.getSpectatorPlayersFromList()) {
			if (game.getName().equalsIgnoreCase(pm2.getGameName())) {
				sendMessage(pm2.getPlayer(), message);
			}
		}
	}

	/**
	 * Checks the game name if contains special chars or too long.
	 * 
	 * @param pl   Player
	 * @param name Game
	 * @return this returns false if:
	 * 
	 * <pre>
	 * - contains special chars
	 * - the name is too long
	 * </pre>
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

		return true;
	}

	/**
	 * Checks whatever the specified game is exists or no.
	 * 
	 * @param game Game
	 * @return true if game exists
	 */
	public static boolean isGameWithNameExists(String game) {
		return GetGames.isGameExistent(game);
	}

	/**
	 * Get the game spawn by name.
	 * 
	 * @param name game name
	 * @return {@link GameSpawn}
	 */
	public static GameSpawn getGameSpawn(String name) {
		Validate.notEmpty(name, "Game name can't be empty/null");

		for (IGameSpawn gsg : RageMode.getInstance().getSpawns()) {
			if (name.equals(gsg.getGame().getName()) && gsg instanceof GameSpawn) {
				return (GameSpawn) gsg;
			}
		}

		return null;
	}

	/**
	 * Get the game spawn by game.
	 * 
	 * @see #getGameSpawn(String)
	 * @param game {@link Game}
	 * @return {@link GameSpawn}
	 */
	public static GameSpawn getGameSpawn(Game game) {
		Validate.notNull(game, "Game can't be null");

		return getGameSpawn(game.getName());
	}

	/**
	 * Get the game spawn by game name.
	 * 
	 * @param name game name
	 * @return {@link GameZombieSpawn}
	 */
	public static GameZombieSpawn getGameZombieSpawn(String name) {
		Validate.notEmpty(name, "Game name can't be empty/null");

		for (IGameSpawn gsg : RageMode.getInstance().getSpawns()) {
			if (name.equals(gsg.getGame().getName()) && gsg instanceof GameZombieSpawn) {
				return (GameZombieSpawn) gsg;
			}
		}

		return null;
	}

	/**
	 * Get the game spawn by game.
	 * 
	 * @see #getGameZombieSpawn(String)
	 * @param game {@link Game}
	 * @return {@link GameZombieSpawn}
	 */
	public static GameZombieSpawn getGameZombieSpawn(Game game) {
		Validate.notNull(game, "Game can't be null");

		return getGameZombieSpawn(game.getName());
	}

	/**
	 * Get the game by player uuid.
	 * 
	 * @see #getGameByPlayer(Player)
	 * @param uuid UUID
	 * @return Game if player is in game.
	 */
	public static Game getGameByPlayer(UUID uuid) {
		Validate.notNull(uuid, "Player UUID can not be null");

		return getGameByPlayer(Bukkit.getPlayer(uuid));
	}

	/**
	 * Get the game by player.
	 * 
	 * @param p Player
	 * @return Game if player is in game.
	 */
	public static Game getGameByPlayer(Player p) {
		if (p == null) {
			return null;
		}

		for (Game game : RageMode.getInstance().getGames()) {
			for (PlayerManager pl : game.getPlayersFromList()) {
				if (p == pl.getPlayer()) {
					return game;
				}
			}
		}

		return null;
	}

	/**
	 * Get the game by player uuid.
	 * 
	 * @see #getGameBySpectator(Player)
	 * @param uuid UUID
	 * @return Game if player is in game.
	 */
	public static Game getGameBySpectator(UUID uuid) {
		return uuid == null ? null : getGameBySpectator(Bukkit.getPlayer(uuid));
	}

	/**
	 * Get the game by spectator player.
	 * 
	 * @param p Player
	 * @return Game if player is in spectator mode and in game.
	 */
	public static Game getGameBySpectator(Player p) {
		if (p == null) {
			return null;
		}

		for (Game game : RageMode.getInstance().getGames()) {
			for (PlayerManager pl : game.getSpectatorPlayersFromList()) {
				if (p == pl.getPlayer()) {
					return game;
				}
			}
		}

		return null;
	}

	/**
	 * Checks if the given player is currently in game.
	 * 
	 * @param p Player
	 * @return true if playing
	 */
	public static boolean isPlayerPlaying(Player p) {
		return getGameByPlayer(p) != null;
	}

	/**
	 * Checks if the given spectator player is currently in game.
	 * 
	 * @param p Player
	 * @return true if in game
	 */
	public static boolean isSpectatorPlaying(Player p) {
		return getGameBySpectator(p) != null;
	}

	/**
	 * Get the game by name.
	 * 
	 * @param name Game
	 * @return Game if the given name is exists.
	 */
	public static Game getGame(String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}

		for (Game game : RageMode.getInstance().getGames()) {
			if (name.equalsIgnoreCase(game.getName())) {
				return game;
			}
		}

		return null;
	}

	/**
	 * Gets the game by area location.
	 * 
	 * @param loc the location
	 * @return {@link Game}
	 */
	public static Game getGame(Location loc) {
		if (loc == null) {
			return null;
		}

		for (GameArea ga : GameAreaManager.getAreasByLocation(loc)) {
			Game g = getGame(ga.getGame().getName());
			if (g != null) {
				return g;
			}
		}

		return null;
	}

	/**
	 * Give game items to the given player. If the item slot not found in
	 * configuration, manually adding the item to the inventory.
	 * 
	 * @param p Player
	 * @param clear - if true clears the player inventory before adding items
	 */
	public static void addGameItems(Player p, boolean clear) {
		if (clear)
			Utils.clearPlayerInventory(p);

		PlayerInventory inv = p.getInventory();
		for (ItemHandler ih : RageMode.getInstance().getGameItems()) {
			// flash do not affect entities
			if (isPlayerPlaying(p) && getGameByPlayer(p).getGameType() == GameType.APOCALYPSE
					&& ih.equals(Items.getGameItem(5))) {
				continue;
			}

			if (ih.getSlot() != -1) {
				inv.setItem(ih.getSlot(), ih.get());
			} else {
				inv.addItem(ih.get());
			}
		}
	}

	/**
	 * Saves the player data to a yaml file
	 * <p>This prevents losing the player data when the server has stopped randomly.
	 * 
	 * @param p Player
	 */
	public static void savePlayerData(Player p) {
		if (ConfigValues.isBungee()) {
			return;
		}

		if (isPlayerPlaying(p)) {
			getGameByPlayer(p).getPlayerManager(p).ifPresent(PlayerManager::storePlayerTools);
		}

		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + p.getName() + ".";

			PlayerInventory inv = p.getInventory();

			data.set(path + "location", p.getLocation());
			data.set(path + "contents", inv.getContents());
			data.set(path + "armor-contents", inv.getArmorContents());
			data.set(path + "health", p.getHealth());
			data.set(path + "food", p.getFoodLevel());

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
	 * Connect the given player to the game. If the game is running and the player
	 * is not playing, then if want to join to the game, switching to spectator
	 * mode.
	 * 
	 * @param p Player
	 * @param game {@link Game}
	 */
	public static void joinPlayer(Player p, Game game) {
		final PlayerInventory inv = p.getInventory();
		final String name = game.getName();
		final GameStatus status = game.getStatus();

		if (status == GameStatus.RUNNING) {
			if (!ConfigValues.isSpectatorEnabled()) {
				sendMessage(p, RageMode.getLang().get("game.running"));
				return;
			}

			if (game.isPlayerInList(p)) {
				sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
				return;
			}

			if (game.addSpectatorPlayer(p)) {
				IGameSpawn spawn = getGameSpawn(name);
				if (spawn != null && spawn.haveAnySpawn()) {
					Utils.teleport(p, spawn.getRandomSpawn());
				}

				if (Items.getLobbyItem(1) != null) {
					inv.setItem(Items.getLobbyItem(1).getSlot(), Items.getLobbyItem(1).get());
				}

				// delay to avoid bad work of spectator
				Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
						() -> p.setGameMode(org.bukkit.GameMode.SPECTATOR), 5L);
			}

			return;
		}

		if (status == GameStatus.NOTREADY) {
			sendMessage(p, RageMode.getLang().get("commands.join.game-locked"));
			return;
		}

		if (game.isPlayerInList(p)) {
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
				if (armor != null && armor.getType() != Material.AIR) {
					sendMessage(p, RageMode.getLang().get("commands.join.empty-inventory.armor"));
					return;
				}
			}

			for (ItemStack content : inv.getContents()) {
				if (content != null && content.getType() != Material.AIR) {
					sendMessage(p, RageMode.getLang().get("commands.join.empty-inventory.contents"));
					return;
				}
			}
		}

		if (!game.addPlayer(p)) {
			Debug.sendMessage(
					RageMode.getLang().get("game.player-could-not-join", "%player%", p.getName(), "%game%", name));
			return;
		}

		if (!ConfigValues.isSavePlayerData()) {
			// We still need some data saving
			game.getPlayerManager(p).ifPresent(pl -> {
				StorePlayerStuffs sp = pl.getStorePlayer();
				sp.oldLocation = p.getLocation();
				sp.oldGameMode = p.getGameMode();
				sp.currentBoard = p.getScoreboard();
			});

			p.setGameMode(GameMode.SURVIVAL);
		} else {
			savePlayerData(p);
		}
		clearPlayerTools(p);

		GameAreaManager.removeEntitiesFromGame(game);

		if (game.getGameType() == GameType.APOCALYPSE) {
			game.worldTime = p.getWorld().getFullTime();
		}

		Utils.teleport(p, GameLobby.getLobbyLocation(name)).thenAccept(success -> {
			runCommands(p, name, "join");
			sendActionMessage(p, name, ActionMessageType.JOIN, ActionMessageType.asBossbar());
			sendActionMessage(p, name, ActionMessageType.JOIN, ActionMessageType.asActionbar());
			game.setStatus(GameStatus.WAITING);
			broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", p.getName()));

			String title = ConfigValues.getTitleJoinGame(), subtitle = ConfigValues.getSubTitleJoinGame(),
					times = ConfigValues.getJoinTitleTime();

			title = title.replace("%game%", game.getName());
			subtitle = subtitle.replace("%game%", game.getName());

			sendTitleMessages(p, title, subtitle, times);

			for (ItemHandler items : RageMode.getInstance().getLobbyItems()) {
				if (items == null || (items.equals(Items.getLobbyItem(0))
						&& !hu.montlikadani.ragemode.utils.Misc.hasPerm(p, "ragemode.admin.item.forcestart"))) {
					continue;
				}

				ItemStack builtItem = items.get();

				if (items.equals(Items.getLobbyItem(3))) {
					org.bukkit.inventory.meta.ItemMeta meta = builtItem.getItemMeta();
					if (meta != null) {
						if (!PlayerManager.DEATHMESSAGESTOGGLE.getOrDefault(p.getUniqueId(), false)) {
							meta.setDisplayName(items.getDisplayName());
							meta.setLore(items.getLore());
						} else if (!items.getExtras().isEmpty()) {
							meta.setDisplayName(items.getExtras().get(0).getExtraName());
							meta.setLore(items.getExtras().get(0).getExtraLore());
						}

						builtItem.setItemMeta(meta);
					}
				}

				inv.setItem(items.getSlot(), builtItem);
			}

			SignCreator.updateAllSigns(name);
		});
	}

	/**
	 * Attempt to leave the player from the game.
	 * 
	 * @param p Player
	 * @param name the game name
	 * @see #leavePlayer(Player, Game)
	 */
	public static void leavePlayer(Player p, String name) {
		leavePlayer(p, getGame(name));
	}

	/**
	 * Attempt to leave the player from the game and calls the
	 * {@link RMGameLeaveAttemptEvent}. This also removes the spectator player from
	 * game of currently in it.
	 * 
	 * @param p Player
	 * @param game {@link Game}
	 */
	public static void leavePlayer(Player p, Game game) {
		Validate.notNull(game, "Game can't be null");

		if (isPlayerPlaying(p)) {
			RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, p);
			Utils.callEvent(gameLeaveEvent);
			if (!gameLeaveEvent.isCancelled()) {
				game.removePlayer(p);
			}
		} else if (isSpectatorPlaying(p)) {
			game.removeSpectatorPlayer(p);
		}

		SignCreator.updateAllSigns(game.getName());
	}

	/**
	 * Sends title messages to the given player.
	 * 
	 * @param p Player
	 * @param title Title
	 * @param subtitle SubTitle
	 * @param times The times separates with <code>", "</code>
	 */
	public static void sendTitleMessages(Player p, String title, String subtitle, String times) {
		String[] split = times.split(", ");

		int fadeIn = split.length > 1 ? Integer.parseInt(split[0]) : 20,
				stay = split.length > 2 ? Integer.parseInt(split[1]) : 30,
				fadeOut = split.length > 3 ? Integer.parseInt(split[2]) : 20;

		title = Utils.colors(title);
		subtitle = Utils.colors(subtitle);

		Titles.sendTitle(p, fadeIn, stay, fadeOut, title, subtitle);
	}

	/**
	 * Forces the given game start.
	 * 
	 * @param game {@link Game}
	 * @return true if enough players are in the game
	 */
	public static boolean forceStart(Game game) {
		Validate.notNull(game, "Game can't be null");

		if (game.getPlayers().size() < 2) {
			return false;
		}

		GameLoader loader = new GameLoader(game);
		loader.startGame();

		SignCreator.updateAllSigns(game.getName());
		return true;
	}

	/**
	 * Kicks all players from the game.
	 * 
	 * @param game {@link Game}
	 */
	public static void kickAllPlayers(Game game) {
		game.getPlayersFromList().forEach(pm -> kickPlayer(pm.getPlayer(), game));
	}

	/**
	 * Kicks the given player from a game.
	 * 
	 * @param p {@link Player}
	 */
	public static void kickPlayer(Player p) {
		kickPlayer(p, null);
	}

	/**
	 * Kicks the given player from the game.
	 * 
	 * @param p Player
	 * @param game {@link Game}
	 */
	public static void kickPlayer(Player p, Game game) {
		Validate.notNull(p, "Player can't be null");

		if (game == null) {
			game = getGameByPlayer(p);
		}

		if (game == null) {
			return;
		}

		GameStatus status = game.getStatus();

		if ((status == GameStatus.RUNNING || status == GameStatus.WAITING) && game.removePlayer(p)) {

			// Will execute some tasks when the player left the server, while the game running
			if (status == GameStatus.RUNNING) {
				Debug.logConsole("Player " + p.getName() + " left the server while playing.");

				for (String cmds : ConfigValues.getExecuteCommandsOnPlayerLeaveWhilePlaying()) {
					cmds = cmds.replace("%player%", p.getName());
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.colors(cmds));
				}
			}

			SignCreator.updateAllSigns(game.getName());
		}
	}

	/**
	 * Kicks the given spectator player from a game.
	 * 
	 * @param p {@link Player}
	 */
	public static void kickSpectator(Player p) {
		kickSpectator(p, null);
	}

	/**
	 * Kicks the given spectator player from game.
	 * 
	 * @param p Player to be kick
	 * @param game the game where kick from
	 */
	public static void kickSpectator(Player p, Game game) {
		Validate.notNull(p, "Player can't be null");

		if (game == null) {
			game = getGameBySpectator(p);
		}

		if (game != null) {
			game.removeSpectatorPlayer(p);
		}
	}

	/**
	 * Fully clears the given player inventory, remove effects, food, health set to
	 * 0 and more related to player.
	 * 
	 * @param p Player
	 */
	public static void clearPlayerTools(Player p) {
		Utils.clearPlayerInventory(p);

		p.setGameMode(GameMode.SURVIVAL);
		p.setFlying(false);
		p.setSprinting(false);
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setSneaking(false);
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);

		if (Version.isCurrentEqualOrHigher(Version.v1_16_R2)) {
			p.setArrowsInBody(0);
		}

		if (p.isInsideVehicle())
			p.leaveVehicle();

		if (!p.getActivePotionEffects().isEmpty())
			p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));

		p.setDisplayName(p.getName());
		p.setPlayerListName(p.getName());
	}

	@SuppressWarnings("deprecation")
	public static void spawnZombies(Game game, int amount) {
		if (game.getStatus() != GameStatus.RUNNING || game.getGameType() != GameType.APOCALYPSE) {
			return;
		}

		IGameSpawn zombieSpawn = getGameZombieSpawn(game);
		if (zombieSpawn == null || !zombieSpawn.haveAnySpawn()) {
			return;
		}

		GetGames.getWorld(game.getName()).ifPresent(world -> {
			org.bukkit.World w = Bukkit.getWorld(world);
			if (w == null) {
				return;
			}

			// Force night
			w.setTime(14000L);

			for (int i = 0; i <= amount; i++) {
				Location location = zombieSpawn.getRandomSpawn().clone();
				org.bukkit.util.Vector vec = location.getDirection();
				location.add(vec.setY(0).normalize().multiply(3));
				while (!w.getBlockAt(location).getType().isAir()) {
					location.add(vec.setY(0).normalize().multiply(3));
				}

				Zombie zombie = (Zombie) w.spawnEntity(location, org.bukkit.entity.EntityType.ZOMBIE);

				// Do not spawn too much baby zombie
				if (Version.isCurrentEqualOrHigher(Version.v1_16_R2)) {
					if (!zombie.isAdult() && ThreadLocalRandom.current().nextInt(0, 10) < 2) {
						zombie.setAdult();
					}
				} else if (zombie.isBaby() && ThreadLocalRandom.current().nextInt(0, 10) < 2) {
					zombie.setBaby(false);
				}
			}
		});
	}

	/**
	 * Run commands for all players in game, when a player doing something in game
	 * such as it died, joining, starting or stopping game.
	 * 
	 * @see #runCommands(Player, String, String)
	 * @param game Game name
	 * @param cmdType CommandProcessor type, such as death, join or other
	 */
	public static void runCommandsForAll(String game, String cmdType) {
		for (PlayerManager pl : getGame(game).getPlayersFromList()) {
			runCommands(pl.getPlayer(), game, cmdType);
		}
	}

	/**
	 * Run commands in game, when the player doing something in game such as it
	 * died, joining, starting or stopping game.
	 * 
	 * @param p Player
	 * @param game Game name
	 * @param cmdType CommandProcessor type, such as death, join or other
	 */
	public static void runCommands(Player p, String game, String cmdType) {
		if (!ConfigValues.isRewardEnabled()) {
			return;
		}

		List<String> list = RageMode.getInstance().getConfiguration().getRewardsCfg()
				.getStringList("rewards.in-game.run-commands");
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
	 * Sends an action message to the given player with the specified type of action
	 * message.
	 * <p>
	 * The method will return if the message type is bossbar and in the current
	 * version its not supported. If the given game not exist with the name,
	 * returns. If the message type is disabled per configuration, returns.
	 * 
	 * @param player      {@link Player}
	 * @param game        game name
	 * @param type        {@link ActionMessageType}
	 * @param messageType {@link ActionMessageType.MessageTypes}
	 */
	public static void sendActionMessage(Player player, String game, ActionMessageType type,
			ActionMessageType.MessageTypes messageType) {
		if (messageType == ActionMessageType.MessageTypes.BOSSBAR && Version.isCurrentLower(Version.v1_9_R1)) {
			Debug.logConsole(Level.WARNING, "Your server version does not support for Bossbar. Only 1.9+");
			return;
		}

		if (!isGameWithNameExists(game))
			return;

		if (RageMode.getInstance().getConfiguration().getArenasCfg()
				.isSet("arenas." + game + "." + messageType.toString())) {
			if (!RageMode.getInstance().getConfiguration().getArenasCfg()
					.getBoolean("arenas." + game + "." + messageType.toString()))
				return;
		} else if ((messageType == ActionMessageType.MessageTypes.ACTIONBAR
				&& !ConfigValues.isDefaultActionbarEnabled())
				|| (messageType == ActionMessageType.MessageTypes.BOSSBAR && !ConfigValues.isDefaultBossbarEnabled()))
			return;

		for (String msg : ConfigValues.getMessageActions()) {
			if (!msg.startsWith("[" + messageType.toString() + "];")) {
				continue;
			}

			msg = msg.replace("[" + messageType.toString() + "];", "");

			String[] split = msg.split(":");
			if (split.length < 2) {
				continue;
			}

			String action = split[0];
			// Bossbar leave
			if (action.equalsIgnoreCase("leave") && !type.isBothAllowed()) {
				continue; // do we allow for user to use all actions?
			}

			if (action.equalsIgnoreCase(type.name())) {
				String message = split[1];
				message = message.replace("%game%", game).replace("%player%", player.getName());
				message = Utils.colors(message);

				if (messageType == ActionMessageType.MessageTypes.ACTIONBAR) {
					ActionBar.sendActionBar(player, message);
				} else if (messageType == ActionMessageType.MessageTypes.BOSSBAR) {
					BarColor color = split.length > 2 ? BarColor.valueOf(split[2].toUpperCase()) : BarColor.BLUE;
					BarStyle style = split.length > 3 ? BarStyle.valueOf(split[3].toUpperCase()) : BarStyle.SOLID;

					int second = 6;
					if (split.length > 4 && Utils.isInt(split[4])) {
						second = Integer.parseInt(split[4]);
					}

					BossbarManager manager = RageMode.getInstance().getBossbarManager();
					manager.createBossbar(player, message, color, style);
					manager.showBossbar(player, second);
				}
			}
		}
	}

	/**
	 * Teleports all players to a random spawn location.
	 * 
	 * @see #teleportPlayerToGameSpawn(Player, IGameSpawn)
	 * @param gameSpawn {@link IGameSpawn}
	 */
	public static void teleportPlayersToGameSpawns(IGameSpawn gameSpawn) {
		if (gameSpawn != null) {
			gameSpawn.getGame().getPlayersFromList().forEach(pm -> teleportPlayerToGameSpawn(pm.getPlayer(), gameSpawn));
		}
	}

	/**
	 * Teleports the given player to a random spawn location. This will return if
	 * there are no spawn added to list.
	 * 
	 * @param p     Player
	 * @param gameSpawn {@link IGameSpawn}
	 */
	public static void teleportPlayerToGameSpawn(Player p, IGameSpawn gameSpawn) {
		if (gameSpawn != null && gameSpawn.haveAnySpawn()) {
			Utils.teleport(p, gameSpawn.getRandomSpawn());
		}
	}

	/**
	 * Force stopping the given game when an error or something else occurs during
	 * starting the game.
	 * 
	 * @param game Game
	 */
	public static void forceStopGame(Game game) {
		Validate.notNull(game, "Game can't be null");

		if (!game.isGameRunning()) {
			return;
		}

		Utils.callEvent(new RMGameStopEvent(game));
		GameAreaManager.removeEntitiesFromGame(game);

		Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> {
			for (PlayerManager pm : game.getPlayersFromList()) {
				final Player p = pm.getPlayer();

				RageScores.removePointsForPlayer(p.getUniqueId());
				game.removePlayer(p);
			}
		});

		game.getActionMessengers().clear();
		game.setGameRunning(false);
		game.setStatus(null);
		SignCreator.updateAllSigns(game.getName());
	}

	/**
	 * Stops the specified game on the main thread if running.
	 * 
	 * @see #stopGame(Game, boolean)
	 * @param game {@link Game}
	 */
	public static void stopGame(Game game) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> stopGame(game, true));
	}

	/**
	 * Stops the specified game if running. This calculates the players who has the
	 * highest points and announcing to a title message. If there are no winner
	 * player valid, players will be removed from the game with some rewards. This
	 * will saves the player statistic to the database and finally stopping the
	 * game.
	 * 
	 * @param game {@link Game}
	 * @param useFreeze if true using game freeze
	 */
	public static void stopGame(final Game game, boolean useFreeze) {
		Validate.notNull(game, "Game can't be null");

		if (!game.isGameRunning()) {
			return;
		}

		Utils.callEvent(new RMGameStopEvent(game));

		final List<PlayerManager> players = game.getPlayersFromList();
		final String name = game.getName();

		if (game.getGameType() != GameType.APOCALYPSE) {
			boolean winnervalid = false;
			UUID winnerUUID = RageScores.calculateWinner(game, players);
			if (winnerUUID != null && Bukkit.getPlayer(winnerUUID) != null) {
				winnervalid = true;

				Player winner = Bukkit.getPlayer(winnerUUID);

				String wonTitle = ConfigValues.getWonTitle(), wonSubtitle = ConfigValues.getWonSubTitle(),
						youWonTitle = ConfigValues.getYouWonTitle(), youWonSubtitle = ConfigValues.getYouWonSubTitle();

				wonTitle = wonTitle.replace("%winner%", winner.getName());
				wonTitle = replaceVariables(wonTitle, winnerUUID);
				youWonTitle = replaceVariables(youWonTitle, winnerUUID);

				wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
				wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);
				youWonSubtitle = replaceVariables(youWonSubtitle, winnerUUID);

				for (PlayerManager pm : players) {
					final Player p = pm.getPlayer();

					if (p != winner) {
						String wonTime = ConfigValues.getWonTitleTime();
						sendTitleMessages(p, wonTitle, wonSubtitle, wonTime);

						if (ConfigValues.isSwitchGMForPlayers()) {
							p.setGameMode(GameMode.SPECTATOR);
						}
					} else {
						String wonTime = ConfigValues.getYouWonTitleTime();
						sendTitleMessages(p, youWonTitle, youWonSubtitle, wonTime);
					}
				}
			}

			if (!winnervalid) {
				broadcastToGame(game, RageMode.getLang().get("game.no-won"));

				players.forEach(pm -> pm.getPlayer().setGameMode(GameMode.SPECTATOR));
			}

			players.forEach(pm -> game.removePlayerSynced(pm.getPlayer()));

			final Player winner = winnerUUID != null ? Bukkit.getPlayer(winnerUUID) : null;
			if (!useFreeze) {
				finishStopping(game, winner, false);
				return;
			}

			if (WAITINGGAMES.containsKey(name)) {
				WAITINGGAMES.remove(name);
			}

			WAITINGGAMES.put(name, true);

			game.setStatus(GameStatus.GAMEFREEZE);
			SignCreator.updateAllSigns(name);

			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> {
				if (WAITINGGAMES.containsKey(name)) {
					WAITINGGAMES.remove(name);
				}

				finishStopping(game, winner, true);
			}, ConfigValues.getGameFreezeTime() * 20);
		} else {
			GameAreaManager.removeEntitiesFromGame(game);

			players.forEach(pm -> game.removePlayerSynced(pm.getPlayer()));

			if (!useFreeze) {
				finishStopping(game, null, false);
				return;
			}

			if (WAITINGGAMES.containsKey(name)) {
				WAITINGGAMES.remove(name);
			}

			WAITINGGAMES.put(name, true);

			game.setStatus(GameStatus.GAMEFREEZE);
			SignCreator.updateAllSigns(name);

			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(), () -> {
				if (WAITINGGAMES.containsKey(name)) {
					WAITINGGAMES.remove(name);
				}

				finishStopping(game, null, true);
			}, ConfigValues.getGameFreezeTime() * 20);
		}
	}

	private static void finishStopping(Game game, Player winner, boolean serverStop) {
		if (!game.isGameRunning()) {
			return;
		}

		String gName = game.getName();

		game.setStatus(null);

		// Lets try to load the stats if empty
		if (RuntimePPManager.getRuntimePPList().isEmpty()) {
			RuntimePPManager.loadPPListFromDatabase();
		}

		for (PlayerManager spec : new HashMap<>(game.getSpectatorPlayers()).values()) {
			game.removeSpectatorPlayer(spec.getPlayer());
		}

		game.getActionMessengers().clear();

		RewardManager reward = new RewardManager(gName);
		for (PlayerManager pm : game.getPlayersFromList()) {
			final Player p = pm.getPlayer();

			RageScores.getPlayerPoints(p.getUniqueId()).ifPresent(pP -> {
				RuntimePPManager.updatePlayerEntry(pP);

				RageMode.getInstance().getHoloHolder().updateHolosForPlayer(p);

				// Avoid calling database connection for each player
				// Database should save at stop
				//RageMode.getInstance().getDatabase().addPlayerStatistics(pP);
			});

			RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, p);
			Utils.callEvent(gameLeaveEvent);
			if (gameLeaveEvent.isCancelled()) {
				continue; // We need this if the event was cancelled only in one player
			}

			runCommands(p, gName, "stop");
			sendActionMessage(p, gName, ActionMessageType.STOP, ActionMessageType.asBossbar());
			sendActionMessage(p, gName, ActionMessageType.STOP, ActionMessageType.asActionbar());
			RageScores.removePointsForPlayer(p.getUniqueId());
			if (game.removePlayer(p)) {
				sendMessage(p, RageMode.getLang().get("game.stopped", "%game%", gName));

				if (ConfigValues.isRewardEnabled()) {
					if (winner == p) {
						reward.rewardForWinner(winner);
						continue;
					}

					reward.rewardForPlayers(p);
				}
			}
		}

		game.setGameRunning(false);
		SignCreator.updateAllSigns(gName);
		game.setStatus(GameStatus.READY);

		if (serverStop) {
			if (ConfigValues.isRestartServerEnabled()) {
				if (RageMode.getSoftwareType() == ServerSoftwareType.SPIGOT) {
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
	 * Stops all currently running games.
	 */
	public static void stopAllGames() {
		Debug.logConsole("Searching games to stop...");

		for (String game : GetGames.getGameNames()) {
			Game g = getGame(game);
			if (g == null) {
				continue;
			}

			GameStatus status = g.getStatus();

			List<PlayerManager> pList = new java.util.ArrayList<>(g.getPlayersFromList());
			if (status == GameStatus.RUNNING && g.isGameRunning()) {
				Debug.logConsole("Stopping " + game + " ...");

				GameAreaManager.removeEntitiesFromGame(g);
				RageScores.calculateWinner(g, pList);

				for (PlayerManager players : pList) {
					Player p = players.getPlayer();

					g.removePlayer(p);
					RageScores.removePointsForPlayer(p.getUniqueId());
				}

				for (PlayerManager spec : new HashMap<>(g.getSpectatorPlayers()).values()) {
					g.removeSpectatorPlayer(spec.getPlayer());
				}

				g.setGameRunning(false);

				Debug.logConsole(game + " has been stopped.");
			} else if (status == GameStatus.WAITING) {
				pList.forEach(pl -> g.removePlayer(pl.getPlayer()));
			}
		}
	}

	private static String replaceVariables(String s, UUID uuid) {
		if (!RageScores.getPlayerPoints(uuid).isPresent()) {
			return s;
		}

		PlayerPoints score = RageScores.getPlayerPoints(uuid).get();

		if (s.contains("%points%"))
			s = s.replace("%points%", Integer.toString(score.getPoints()));
		if (s.contains("%kills%"))
			s = s.replace("%kills%", Integer.toString(score.getKills()));
		if (s.contains("%deaths%"))
			s = s.replace("%deaths%", Integer.toString(score.getDeaths()));

		return s;
	}

	/**
	 * Checks for the player inventory (in hand) if the item is similar for the game
	 * item.
	 * 
	 * @param p Player
	 * @see #isGameItem(ItemStack)
	 * @return true if similar
	 */
	public static boolean isGameItem(Player p) {
		return isGameItem(NMS.getItemInHand(p));
	}

	/**
	 * Checks for the item stack if the item is similar for game item.
	 * 
	 * @param item Material
	 * @return true if similar
	 */
	private static boolean isGameItem(ItemStack item) {
		for (ItemHandler ih : RageMode.getInstance().getGameItems()) {
			if (ih != null && ih.get().isSimilar(item)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given player is in the freeze room, when the game ended.
	 * 
	 * @param player Player
	 * @return true if player is in
	 */
	public static boolean isPlayerInFreezeRoom(Player player) {
		return isPlayerPlaying(player) && isGameInFreezeRoom(getGameByPlayer(player));
	}

	/**
	 * Checks if the specified game is in the freeze room.
	 * 
	 * @see #isGameInFreezeRoom(String)
	 * @param game Game
	 * @return true if the game is in
	 */
	public static boolean isGameInFreezeRoom(Game game) {
		return isGameInFreezeRoom(game.getName());
	}

	/**
	 * Checks if the specified game is in the freeze room.
	 * 
	 * @param game Game name
	 * @return true if the game is in
	 */
	public static boolean isGameInFreezeRoom(String game) {
		return WAITINGGAMES.getOrDefault(game, false);
	}

	/**
	 * The enum for send various message types, like bossbar to player when
	 * something happen in the game
	 */
	public enum ActionMessageType {
		JOIN, LEAVE(false), START, STOP;

		private boolean both;

		ActionMessageType() {
			this(true);
		}

		ActionMessageType(boolean both) {
			this.both = both;
		}

		@Override
		public String toString() {
			return name().toLowerCase(java.util.Locale.ENGLISH);
		}

		public boolean isBothAllowed() {
			return both;
		}

		public static MessageTypes asActionbar() {
			return MessageTypes.ACTIONBAR;
		}

		public static MessageTypes asBossbar() {
			return MessageTypes.BOSSBAR;
		}

		private enum MessageTypes {
			ACTIONBAR, BOSSBAR
		}
	}
}
