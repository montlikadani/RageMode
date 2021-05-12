package hu.montlikadani.ragemode.gameUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.area.GameArea;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.*;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.modules.TitleSender;
import hu.montlikadani.ragemode.items.*;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.managers.RewardManager.InGameCommand;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.Misc;
import hu.montlikadani.ragemode.utils.SchedulerUtil;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.ServerVersion;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

/**
 * Class used for retrieving and performing some miscellaneous utilities for
 * specific game.
 */
public final class GameUtils {

	/**
	 * Map for storing players who waiting for teleport at the end of the game.
	 */
	public static final Map<String, Boolean> WAITING_GAMES = new HashMap<>();

	private static final RageMode RM = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	private GameUtils() {
		throw new UnsupportedOperationException("GameUtils cannot be instantiated.");
	}

	/**
	 * Broadcast the given message to the currently playing players for the given
	 * game.
	 * 
	 * @param game    {@link Game}
	 * @param message The message
	 */
	public static void broadcastToGame(@Nullable Game game, @Nullable String message) {
		if (game == null || message == null || message.trim().isEmpty()) {
			return;
		}

		for (PlayerManager pm : game.getPlayers()) {
			if (game.equals(pm.getPlayerGame())) {
				sendMessage(pm.getPlayer(), message);
			}
		}
	}

	/**
	 * Checks the game name if contains special characters or the length is too
	 * long.
	 * <p>
	 * Acceptable characters are based on the following regex: {@code a-Z0-9_-}
	 * <p>
	 * An equivalent: {@code Game_Name-12}
	 * 
	 * @param player the {@link Player} where to send the error message if the name
	 *               does not match the acceptable name
	 * @param name   {@link Game}
	 * @return {@code false} if one of the characters in the specified string
	 *         parameter is incorrect or the length is too long
	 */
	public static boolean checkName(@Nullable Player player, @NotNull String name) {
		if (name == null) {
			return false;
		}

		if (!name.matches("^[a-zA-Z0-9\\_\\-]+$")) {
			sendMessage(player, RageMode.getLang().get("setup.addgame.special-chars"));
			return false;
		}

		if (name.length() > 40) {
			sendMessage(player, RageMode.getLang().get("setup.addgame.name-greater"));
			return false;
		}

		return true;
	}

	/**
	 * Counts and returns max players from all games.
	 * 
	 * @return overall max players
	 */
	public static int getOverallMaxPlayers() {
		int max = 0;

		for (Game game : RM.getGames()) {
			max += game.maxPlayers;
		}

		return max;
	}

	/**
	 * Returns the game by player uuid.
	 * 
	 * @see #getGameByPlayer(Player)
	 * @param uuid {@link UUID}
	 * @return {@link Game} if player is in game, otherwise null
	 */
	@Nullable
	public static Game getGameByPlayer(@Nullable UUID uuid) {
		return uuid == null ? null : getGameByPlayer(RM.getServer().getPlayer(uuid));
	}

	/**
	 * Get the game by player.
	 * 
	 * @param player {@link Player}
	 * @return {@link Game} if player is in game, otherwise null
	 */
	@Nullable
	public static Game getGameByPlayer(@Nullable Player player) {
		if (player == null) {
			return null;
		}

		for (Game game : RM.getGames()) {
			if (game.isPlayerInList(player)) {
				return game;
			}
		}

		return null;
	}

	/**
	 * Checks if the given player is currently in game.
	 * 
	 * @see #getGameByPlayer(Player)
	 * @param player {@link Player}
	 * @return true if playing
	 */
	public static boolean isPlayerPlaying(@Nullable Player player) {
		return getGameByPlayer(player) != null;
	}

	/**
	 * Returns the instance of {@link PlayerManager} from the given player if exist.
	 * 
	 * @param player {@link Player}
	 * @return {@link PlayerManager} or null if not exist.
	 */
	@Nullable
	public static PlayerManager getPlayerManager(@Nullable Player player) {
		if (player == null) {
			return null;
		}

		for (Game game : RM.getGames()) {
			Optional<PlayerManager> opt = game.getPlayerManager(player);

			if (opt.isPresent()) {
				return opt.get();
			}
		}

		return null;
	}

	/**
	 * Checks if the given game is exist.
	 * 
	 * @see #getGame(String)
	 * @param name game name
	 * @return true if exist, otherwise false
	 */
	public static boolean isGameExist(@Nullable String name) {
		return getGame(name) != null;
	}

	/**
	 * Get the game by name. This method is case insensitive!
	 * 
	 * @param name Game name
	 * @return {@link Game} if the given game name is exists.
	 */
	@Nullable
	public static Game getGame(@Nullable String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}

		for (Game game : RM.getGames()) {
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
	@Nullable
	public static Game getGame(@Nullable Location loc) {
		if (loc == null) {
			return null;
		}

		for (GameArea ga : GameAreaManager.getAreasByLocation(loc)) {
			if (isGameExist(ga.getGame().getName())) {
				return ga.getGame();
			}
		}

		return null;
	}

	/**
	 * Gives all the game items to the given player.
	 * 
	 * @param player {@link Player}
	 * @param clear  - if true clears the player inventory before adding items
	 */
	public static void addGameItems(@NotNull Player player, boolean clear) {
		if (clear) {
			player.getInventory().clear();
			player.updateInventory();
		}

		ItemHandler flash = Items.getGameItem(5);
		Game gamePlayer = getGameByPlayer(player);

		for (ItemHandler ih : RM.getGameItems()) {
			if (gamePlayer != null && gamePlayer.getGameType() == GameType.APOCALYPSE && ih.equals(flash)) {
				continue; // flash do not affect entities
			}

			if (ih.getSlot() > -1) {
				player.getInventory().setItem(ih.getSlot(), ih.get());
			} else {
				player.getInventory().addItem(ih.get());
			}
		}
	}

	/**
	 * Saves the given player data to a yaml file
	 * <p>
	 * This prevents losing the player data when the server has stopped randomly.
	 * 
	 * @param player {@link Player}
	 */
	public static void savePlayerData(@NotNull Player player) {
		if (ConfigValues.isBungee()) {
			return;
		}

		PlayerManager pm = getPlayerManager(player);
		if (pm != null) {
			pm.storePlayerTools();
		}

		Configuration conf = RM.getConfiguration();

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + player.getName() + ".";

			data.set(path + "location", player.getLocation());
			data.set(path + "contents", player.getInventory().getContents());
			data.set(path + "armor-contents", player.getInventory().getArmorContents());
			data.set(path + "health", player.getHealth());
			data.set(path + "food", player.getFoodLevel());

			java.util.Collection<PotionEffect> activePotions = player.getActivePotionEffects();
			if (!activePotions.isEmpty())
				data.set(path + "potion-effects", activePotions);

			data.set(path + "game-mode", player.getGameMode().name());

			String dName = RM.getComplement().getDisplayName(player);
			if (!dName.equals(player.getName()))
				data.set(path + "display-name", dName);

			if (!(dName = RM.getComplement().getPlayerListName(player)).equals(player.getName()))
				data.set(path + "list-name", dName);

			if (player.getFireTicks() > 0)
				data.set(path + "fire-ticks", player.getFireTicks());

			if (player.getExp() > 0d)
				data.set(path + "exp", player.getExp());

			if (player.getLevel() > 0)
				data.set(path + "level", player.getLevel());

			org.bukkit.entity.Entity vehicle = player.getVehicle();
			if (vehicle != null) {
				data.set(path + "vehicle", vehicle.getType());
				data.set(path + "vehicle", vehicle.getLocation());
			}

			Configuration.saveFile(data, conf.getDatasFile());
			Configuration.loadFile(data, conf.getDatasFile());
		}
	}

	/**
	 * Attempts to join the given player to the game. If the game is running and the
	 * player is not playing, it will be joined as spectator if enabled.
	 * 
	 * @param player {@link Player}
	 * @param game   {@link Game}
	 */
	public static void joinPlayer(@NotNull Player player, @Nullable Game game) {
		if (game == null) {
			return;
		}

		if (game.getStatus() == GameStatus.RUNNING) {
			if (!ConfigValues.isSpectatorEnabled()) {
				sendMessage(player, RageMode.getLang().get("game.running"));
				return;
			}

			if (game.isPlayerInList(player)) {
				sendMessage(player, RageMode.getLang().get("game.player-not-switch-spectate"));
				return;
			}

			if (game.addPlayer(player, true)) {
				IGameSpawn spawn = game.getSpawn(GameSpawn.class);

				if (spawn != null && spawn.haveAnySpawn()) {
					Utils.teleport(player, spawn.getRandomSpawn()).thenAccept(success -> {
						ItemHandler leaveItem = Items.getLobbyItem(1);

						if (leaveItem != null && leaveItem.getSlot() >= 0) {
							player.getInventory().setItem(leaveItem.getSlot(), leaveItem.get());
						}

						player.setGameMode(org.bukkit.GameMode.SPECTATOR);
					});
				}
			}

			return;
		}

		if (game.getStatus() == GameStatus.NOTREADY) {
			sendMessage(player, RageMode.getLang().get("commands.join.game-locked"));
			return;
		}

		if (game.isPlayerInList(player)) {
			sendMessage(player, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return;
		}

		MapChecker mapChecker = new MapChecker(game);
		if (!mapChecker.isValid()) {
			sendMessage(player, mapChecker.getMessage());
			return;
		}

		if (ConfigValues.isRequireEmptyInv()) {
			for (ItemStack armor : player.getInventory().getArmorContents()) {
				if (armor != null && armor.getType() != Material.AIR) {
					sendMessage(player, RageMode.getLang().get("commands.join.empty-inventory.armor"));
					return;
				}
			}

			for (ItemStack content : player.getInventory().getContents()) {
				if (content != null && content.getType() != Material.AIR) {
					sendMessage(player, RageMode.getLang().get("commands.join.empty-inventory.contents"));
					return;
				}
			}
		}

		if (!game.addPlayer(player, false)) {
			Debug.sendMessage(RageMode.getLang().get("game.player-could-not-join", "%player%", player.getName(),
					"%game%", game.getName()));
			return;
		}

		if (!ConfigValues.isSavePlayerData()) {
			// We still need to store some data
			game.getPlayerManager(player).ifPresent(pl -> {
				StorePlayerStuffs sp = pl.getStorePlayer();
				sp.oldLocation = player.getLocation();
				sp.oldGameMode = player.getGameMode();
				sp.currentBoard = player.getScoreboard();
			});

			player.setGameMode(GameMode.SURVIVAL);
		} else {
			savePlayerData(player);
		}
		clearPlayerTools(player);

		GameAreaManager.removeEntitiesFromGame(game);

		if (game.getGameType() == GameType.APOCALYPSE) {
			game.worldTime = player.getWorld().getFullTime();
		}

		Utils.teleport(player, game.getGameLobby().location).thenAccept(success -> {
			game.setStatus(GameStatus.WAITING);

			runCommands(player, game, RewardManager.InGameCommand.CommandType.JOIN);
			sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.JOIN,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.BOSSBAR);
			sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.JOIN,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.ACTIONBAR);
			broadcastToGame(game, RageMode.getLang().get("game.player-joined", "%player%", player.getName()));

			ConfigValues.TitleSettings joinTitleSettings = ConfigValues.getTitleSettings()
					.get(ConfigValues.TitleSettings.TitleType.JOIN_GAME);
			if (joinTitleSettings != null) {
				String title = joinTitleSettings.getTitle(), subTitle = joinTitleSettings.getSubTitle();

				title = title.replace("%game%", game.getName());
				subTitle = subTitle.replace("%game%", game.getName());

				int[] times = joinTitleSettings.getTimes();

				TitleSender.sendTitle(player, times[0], times[1], times[2], Utils.colors(title),
						Utils.colors(subTitle));
			}

			ItemHandler forceStarter = Items.getLobbyItem(0), hideMsgs = Items.getLobbyItem(3);

			for (ItemHandler item : RM.getLobbyItems()) {
				if (item == null || item.getSlot() < 0
						|| (item.equals(forceStarter) && !player.hasPermission("ragemode.admin.item.forcestart"))) {
					continue;
				}

				ItemStack builtItem = item.get();

				if (item.equals(hideMsgs)) {
					org.bukkit.inventory.meta.ItemMeta meta = builtItem.getItemMeta();

					if (meta != null) {
						if (!PlayerManager.DEATH_MESSAGES_TOGGLE.getOrDefault(player.getUniqueId(), false)) {
							RM.getComplement().setDisplayName(meta, item.getDisplayName());
							RM.getComplement().setLore(meta, item.getLore());
						} else if (!item.getExtras().isEmpty()) {
							ItemHandler.Extra first = item.getExtras().get(0);

							RM.getComplement().setDisplayName(meta, first.getExtraName());
							RM.getComplement().setLore(meta, first.getExtraLore());
						}

						builtItem.setItemMeta(meta);
					}
				}

				player.getInventory().setItem(item.getSlot(), builtItem);
			}

			SignCreator.updateAllSigns(game.getName());
		});
	}

	/**
	 * Attempts to leave the player from the given game.
	 * 
	 * @param pm   {@link PlayerManager}
	 * @param game {@link Game}
	 */
	public static void leavePlayer(@NotNull PlayerManager pm, @Nullable Game game) {
		if (pm == null) {
			return;
		}

		if (game == null) {
			game = pm.getPlayerGame();
		}

		if (game == null) {
			return;
		}

		Player player = pm.getPlayer();

		if (pm.isSpectator()) {
			game.removePlayer(player);
		} else {
			RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, player);
			Utils.callEvent(gameLeaveEvent);

			if (!gameLeaveEvent.isCancelled()) {
				game.removePlayer(player);
			}
		}

		SignCreator.updateAllSigns(game.getName());
	}

	/**
	 * Forces the starting of the given game without waiting for timer. The game
	 * will not start if there are no players joined to the game and returns false.
	 * 
	 * @param game {@link Game}
	 * @return true if enough players are in the game or the game type is in
	 *         {@link GameType#APOCALYPSE}, otherwise false
	 */
	public static boolean forceStart(@Nullable Game game) {
		if (game == null || game.getPlayers().isEmpty() || (game.getGameType() != GameType.APOCALYPSE
				&& !ConfigValues.isDeveloperMode() && game.getPlayers().size() == 1)) {
			return false;
		}

		new GameLoader(game).startGame();
		return true;
	}

	/**
	 * Kicks all players from the game.
	 * 
	 * @param game {@link Game}
	 */
	public static void kickAllPlayers(@Nullable Game game) {
		if (game != null) {
			game.getPlayers().forEach(pm -> kickPlayer(pm.getPlayer(), game));
		}
	}

	/**
	 * Kicks the given player from a game.
	 * 
	 * @param player {@link Player}
	 */
	public static void kickPlayer(@NotNull Player player) {
		kickPlayer(player, null);
	}

	/**
	 * Kicks the given player from the game.
	 * 
	 * @param player {@link Player}
	 * @param game   {@link Game}
	 */
	public static void kickPlayer(@NotNull Player player, @Nullable Game game) {
		Validate.notNull(player, "Player can't be null");

		PlayerManager pm = getPlayerManager(player);

		if (pm == null) {
			return;
		}

		if (game == null) {
			game = pm.getPlayerGame();
		}

		if (game == null) {
			return;
		}

		if (game.removePlayer(player) && !pm.isSpectator()) {
			if (game.getStatus() == GameStatus.RUNNING) {
				sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.KICK,
						ConfigValues.MessageAction.ActionMessageType.MessageTypes.COMMAND);
			}

			SignCreator.updateAllSigns(game.getName());
		}
	}

	/**
	 * Clears the given player inventory entirely and removes some other player
	 * related components.
	 * 
	 * @param player {@link Player}
	 */
	public static void clearPlayerTools(@NotNull Player player) {
		player.getInventory().clear();
		player.updateInventory();

		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setSprinting(false);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setSneaking(false);
		player.setScoreboard(RM.getServer().getScoreboardManager().getMainScoreboard());
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setFireTicks(0);
		player.setExp(0);
		player.setLevel(0);

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R2)) {
			player.setArrowsInBody(0);
		}

		player.leaveVehicle();

		for (PotionEffect potion : player.getActivePotionEffects()) {
			player.removePotionEffect(potion.getType());
		}

		RM.getComplement().setDisplayName(player, player.getName());
		RM.getComplement().setPlayerListName(player, player.getName());
	}

	@SuppressWarnings("deprecation")
	public static void spawnZombies(@NotNull Game game, int amount) {
		if (game == null || amount < 1 || game.getStatus() != GameStatus.RUNNING
				|| game.getGameType() != GameType.APOCALYPSE) {
			return;
		}

		IGameSpawn spawn = game.randomSpawnForZombies ? game.getSpawn(GameSpawn.class)
				: game.getSpawn(GameZombieSpawn.class);
		if (spawn == null || !spawn.haveAnySpawn()) {
			return;
		}

		game.getWorld().ifPresent(world -> {
			final GameArea gameArea = game.randomSpawnForZombies ? GameAreaManager.getAreaByGame(game) : null;

			SchedulerUtil.submitSync(() -> {
				world.setTime(14000L); // Force night

				ThreadLocalRandom random = ThreadLocalRandom.current();

				for (int i = 0; i <= amount; i++) {
					Location location = null;

					if (game.randomSpawnForZombies) {
						// Spawn zombies around the center of game area
						if (gameArea != null) {
							Location centerArea = gameArea.getCenter();

							double angle = Math.toRadians(random.nextDouble() * 360);
							double x = centerArea.getX() + (random.nextDouble() * 3 * Math.cos(angle));
							double z = centerArea.getZ() + (random.nextDouble() * 3 * Math.sin(angle));

							location = new Location(world, x, centerArea.getY(), z);
						}
					} else {
						location = spawn.getRandomSpawn().clone();
					}

					// Suppose the area is not exist
					if (location == null) {
						location = spawn.getRandomSpawn().clone().add(15, 0, 15);
					}

					// TODO we need something to do not spawn zombies in-blocks such as in bushes
					// (leaves)

					Zombie zombie = (Zombie) world.spawnEntity(location, org.bukkit.entity.EntityType.ZOMBIE);

					if (RM.isPaper() && ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R3)) {
						zombie.setCanBreakDoors(false);
					}

					// Do not spawn too much baby zombie
					if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R2)) {
						if (!zombie.isAdult() && random.nextInt(0, 10) < 2) {
							zombie.setAdult();
						}
					} else if (zombie.isBaby() && random.nextInt(0, 10) < 2) {
						zombie.setBaby(false);
					}
				}
			}, true);
		});
	}

	/**
	 * Performs specific commands in game, when the player doing something in game.
	 * 
	 * @see #runCommands(Player, Game, InGameCommand.CommandType)
	 * @param game        {@link Game}
	 * @param commandType {@link InGameCommand.CommandType}
	 */
	public static void runCommandsForAll(@NotNull Game game, @NotNull InGameCommand.CommandType commandType) {
		if (game.isRunning() && RM.getRewardManager().isEnabled()) {
			for (PlayerManager pl : game.getPlayers()) {
				RM.getRewardManager().performGameCommands(pl.getPlayer(), game, commandType);
			}
		}
	}

	/**
	 * Performs specific commands in game, when the player doing something in game.
	 * 
	 * @param player      {@link Player}
	 * @param game        {@link Game}
	 * @param commandType {@link InGameCommand.CommandType}
	 */
	public static void runCommands(@NotNull Player player, @NotNull Game game,
			@NotNull InGameCommand.CommandType commandType) {
		if (game.isRunning() && RM.getRewardManager().isEnabled()) {
			RM.getRewardManager().performGameCommands(player, game, commandType);
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
	 * @param game        {@link Game}
	 * @param type        {@link ConfigValues.MessageAction.ActionMessageType}
	 * @param messageType {@link ConfigValues.MessageAction.ActionMessageType.MessageTypes}
	 */
	public static void sendActionMessage(@NotNull Player player, @NotNull Game game,
			@NotNull ConfigValues.MessageAction.ActionMessageType type,
			ConfigValues.MessageAction.ActionMessageType.MessageTypes messageType) {
		if (!game.isRunning())
			return;

		if (messageType == ConfigValues.MessageAction.ActionMessageType.MessageTypes.BOSSBAR
				&& ServerVersion.isCurrentLower(ServerVersion.v1_9_R1)) {
			Debug.logConsole(Level.WARNING, "Your server version does not support Bossbar. Only 1.9+");
			return;
		}

		if ((messageType == ConfigValues.MessageAction.ActionMessageType.MessageTypes.ACTIONBAR
				&& !game.actionbarEnabled)
				|| (messageType == ConfigValues.MessageAction.ActionMessageType.MessageTypes.BOSSBAR
						&& !game.bossbarEnabled))
			return;

		for (ConfigValues.MessageAction action : ConfigValues.getMessageActions()) {
			if (action.getAction() != type) {
				continue;
			}

			String message = action.getMessage();
			message = message.replace("%game%", game.getName()).replace("%player%", player.getName());
			message = Utils.colors(message);

			switch (messageType) {
			case ACTIONBAR:
				TitleSender.sendActionBar(player, message);
				break;
			case BOSSBAR:
				if (action.getBossSettings() != null) {
					BossbarManager manager = RM.getBossbarManager();

					manager.createBossbar(player, message, action.getBossSettings().getColor(),
							action.getBossSettings().getStyle());
					manager.showBossbar(player, action.getBossSettings().getBossExpirementSeconds());
				}

				break;
			case COMMAND:
				RM.getServer().dispatchCommand(RM.getServer().getConsoleSender(), message);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Teleports all players to a random spawn location if have any spawn set.
	 * 
	 * @see #teleportPlayerToGameSpawn(Player, IGameSpawn)
	 * @param gameSpawn {@link IGameSpawn}
	 */
	public static void teleportPlayersToGameSpawns(@Nullable IGameSpawn gameSpawn) {
		if (gameSpawn != null && gameSpawn.haveAnySpawn()) {
			for (PlayerManager pm : gameSpawn.getGame().getPlayers()) {
				Utils.teleport(pm.getPlayer(), gameSpawn.getRandomSpawn());
			}
		}
	}

	/**
	 * Teleports the given player to a random spawn location if have any spawn set.
	 * 
	 * @param player    {@link Player}
	 * @param gameSpawn {@link IGameSpawn}
	 */
	public static void teleportPlayerToGameSpawn(@NotNull Player player, @Nullable IGameSpawn gameSpawn) {
		if (gameSpawn != null && gameSpawn.haveAnySpawn()) {
			Utils.teleport(player, gameSpawn.getRandomSpawn());
		}
	}

	/**
	 * Force stops the given game by ignoring the additional conditions/timers from
	 * scheduling.
	 * 
	 * @param game {@link Game}
	 */
	public static void forceStopGame(@NotNull Game game) {
		Validate.notNull(game, "Game can't be null");

		if (!game.isRunning()) {
			return;
		}

		Utils.callEvent(new RMGameStopEvent(game));
		GameAreaManager.removeEntitiesFromGame(game);

		SchedulerUtil.submitSync(() -> {
			for (PlayerManager pm : new HashSet<>(game.getPlayers())) {
				RageScores.getPlayerPointsMap().remove(pm.getUniqueId());
				game.removePlayer(pm.getPlayer());
			}
		}, true);

		game.getActionMessengers().clear();
		game.setRunning(false);
		game.setStatus(null);
		SignCreator.updateAllSigns(game.getName());
	}

	/**
	 * Performs the game stopping process synchronously if the game is running.
	 * 
	 * @see #stopGame(Game, boolean)
	 * @param game {@link Game}
	 */
	public static void stopGame(@Nullable Game game) {
		if (game != null) {
			SchedulerUtil.submitSync(() -> stopGame(game, true), true);
		}
	}

	/**
	 * Stops the specified game if running. This calculates the players who has the
	 * highest points and announcing to a title message. If there are no winner
	 * player valid, players will be removed from the game with some rewards.
	 * 
	 * @param game      {@link Game}
	 * @param useFreeze if true using game freeze
	 */
	public static void stopGame(@Nullable final Game game, boolean useFreeze) {
		if (game == null || !game.isRunning()) {
			return;
		}

		Utils.callEvent(new RMGameStopEvent(game));

		if (game.getGameType() != GameType.APOCALYPSE) {
			final UUID winnerUUID = RageScores.calculateWinner(game, game.getPlayers());
			final Player winner = winnerUUID != null ? RM.getServer().getPlayer(winnerUUID) : null;

			ConfigValues.TitleSettings titleSettings = ConfigValues.getTitleSettings()
					.get(ConfigValues.TitleSettings.TitleType.PLAYER_WON);

			if (titleSettings != null) {
				String wonTitle = titleSettings.getTitle(), wonSubtitle = titleSettings.getSubTitle();

				if (winner != null) {
					wonTitle = wonTitle.replace("%winner%", winner.getName());
					wonTitle = replaceVariables(wonTitle, winnerUUID);

					wonSubtitle = wonSubtitle.replace("%winner%", winner.getName());
					wonSubtitle = replaceVariables(wonSubtitle, winnerUUID);
				}

				int[] times = titleSettings.getTimes();

				for (PlayerManager pm : game.getPlayers()) {
					Player player = pm.getPlayer();
					game.removePlayerSynced(player);

					if (winner == null) {
						sendMessage(player, RageMode.getLang().get("game.no-won"));
						player.setGameMode(GameMode.SPECTATOR);
					} else if (player != winner) {
						TitleSender.sendTitle(player, times[0], times[1], times[2], wonTitle, wonSubtitle);

						if (ConfigValues.isSwitchGMForPlayers()) {
							player.setGameMode(GameMode.SPECTATOR);
						}
					} else {
						ConfigValues.TitleSettings youWonTitleSettings = ConfigValues.getTitleSettings()
								.get(ConfigValues.TitleSettings.TitleType.YOU_WON);

						if (youWonTitleSettings != null) {
							int[] wonTimes = youWonTitleSettings.getTimes();

							String title = youWonTitleSettings.getTitle(), subTitle = youWonTitleSettings.getSubTitle();

							title = replaceVariables(title, winnerUUID);
							subTitle = replaceVariables(subTitle, winnerUUID);

							TitleSender.sendTitle(player, wonTimes[0], wonTimes[1], wonTimes[2], title, subTitle);
						}
					}
				}
			}

			if (!useFreeze) {
				finishStopping(game, winner, false);
				return;
			}

			WAITING_GAMES.put(game.getName(), true);

			game.setStatus(GameStatus.GAMEFREEZE);
			SignCreator.updateAllSigns(game.getName());

			RM.getServer().getScheduler().runTaskLater(RM, () -> finishStopping(game, winner, true),
					ConfigValues.getGameFreezeTime() * 20);
		} else {
			GameAreaManager.removeEntitiesFromGame(game);

			if (!useFreeze) {
				finishStopping(game, null, false);
				return;
			}

			WAITING_GAMES.put(game.getName(), true);

			game.setStatus(GameStatus.GAMEFREEZE);
			SignCreator.updateAllSigns(game.getName());

			RM.getServer().getScheduler().runTaskLater(RM, () -> finishStopping(game, null, true),
					ConfigValues.getGameFreezeTime() * 20);
		}
	}

	private static void finishStopping(Game game, Player winner, boolean serverStop) {
		WAITING_GAMES.remove(game.getName());

		if (!game.isRunning()) {
			return;
		}

		game.setStatus(null);

		// Lets try to load the stats if empty
		if (RuntimePPManager.getRuntimePPList().isEmpty()) {
			RuntimePPManager.loadPPListFromDatabase();
		}

		for (PlayerManager pm : new HashSet<>(game.getPlayers())) {
			final Player player = pm.getPlayer();

			RageScores.getPlayerPoints(pm.getUniqueId()).ifPresent(pP -> {
				RuntimePPManager.updatePlayerEntry(pP);

				RM.getHoloHolder().updateHologramsName(player);

				// Avoid calling database connection for each player
				// Database should save at stop
				// PLUGIN.getDatabase().addPlayerStatistics(pP);
			});

			if (!pm.isSpectator()) {
				RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, player);
				Utils.callEvent(gameLeaveEvent);

				if (gameLeaveEvent.isCancelled()) {
					continue; // We need this if the event was cancelled only in one player
				}
			}

			runCommands(player, game, RewardManager.InGameCommand.CommandType.STOP);
			sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.STOP,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.BOSSBAR);
			sendActionMessage(player, game, ConfigValues.MessageAction.ActionMessageType.STOP,
					ConfigValues.MessageAction.ActionMessageType.MessageTypes.ACTIONBAR);

			RageScores.getPlayerPointsMap().remove(pm.getUniqueId());

			if (game.removePlayer(player)) {
				sendMessage(player, RageMode.getLang().get("game.stopped", "%game%", game.getName()));

				if (RM.getRewardManager().isEnabled()) {
					if (winner == player) {
						RM.getRewardManager().rewardForWinner(winner, game);
					} else {
						RM.getRewardManager().rewardForPlayers(player, game);
					}
				}
			}
		}

		game.getActionMessengers().clear();
		game.setRunning(false);
		game.setStatus(GameStatus.READY);
		SignCreator.updateAllSigns(game.getName());

		if (serverStop) {
			if (ConfigValues.isRestartServerEnabled()) {
				if (RM.isSpigot()) {
					RM.getServer().spigot().restart();
				} else {
					RM.getServer().dispatchCommand(RM.getServer().getConsoleSender(), "restart");
				}
			} else if (ConfigValues.isStopServerEnabled()) {
				RM.getServer().shutdown();
			}
		}
	}

	/**
	 * Stops all currently running games and saves all the settings of games.
	 */
	public static void stopAllGames() {
		Debug.logConsole("Searching games to stop...");

		for (Game game : RM.getGames()) {
			if (game.getStatus() == GameStatus.RUNNING && game.isRunning()) {
				Debug.logConsole("Stopping " + game.getName() + " ...");

				GameAreaManager.removeEntitiesFromGame(game);
				RageScores.calculateWinner(game, game.getPlayers());

				for (PlayerManager players : new HashSet<>(game.getPlayers())) {
					game.removePlayer(players.getPlayer());
					RageScores.getPlayerPointsMap().remove(players.getUniqueId());
				}

				game.setRunning(false);

				Debug.logConsole(game + " has been stopped.");
			} else if (game.getStatus() == GameStatus.WAITING) {
				for (PlayerManager player : new HashSet<>(game.getPlayers())) {
					game.removePlayer(player.getPlayer());
				}
			}

			game.getGameLobby().saveToConfig();
			game.saveGamesSettings();
		}
	}

	private static String replaceVariables(String s, UUID uuid) {
		Optional<PlayerPoints> score = RageScores.getPlayerPoints(uuid);

		if (score.isPresent()) {
			if (s.contains("%points%"))
				s = s.replace("%points%", Integer.toString(score.get().getPoints()));

			if (s.contains("%kills%"))
				s = s.replace("%kills%", Integer.toString(score.get().getKills()));

			if (s.contains("%deaths%"))
				s = s.replace("%deaths%", Integer.toString(score.get().getDeaths()));
		}

		return s;
	}

	/**
	 * Checks for the player inventory (in hand) if the item is similar for the game
	 * item.
	 * 
	 * @param player {@link Player}
	 * @see #isGameItem(ItemStack)
	 * @return true if similar
	 */
	public static boolean isGameItem(@NotNull Player player) {
		return isGameItem(Misc.getItemInHand(player));
	}

	/**
	 * Checks for the item stack if the item is similar for game item.
	 * 
	 * @param item {@link ItemStack}
	 * @return true if similar
	 */
	private static boolean isGameItem(@Nullable ItemStack item) {
		if (item != null) {
			for (ItemHandler ih : RM.getGameItems()) {
				if (ih != null && ih.get().isSimilar(item)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the given player is in the freeze room, when the game ended.
	 * 
	 * @param player {@link Player}
	 * @return true if player is in
	 */
	public static boolean isPlayerInFreezeRoom(@Nullable Player player) {
		Game gamePlayer = getGameByPlayer(player);
		return gamePlayer != null && isGameInFreezeRoom(gamePlayer);
	}

	/**
	 * Checks if the specified game is in the freeze room. This also checks the game
	 * status if it equal to {@link GameStatus#GAMEFREEZE}
	 * 
	 * @param game {@link Game}
	 * @return true if the game is in freeze status
	 */
	public static boolean isGameInFreezeRoom(@NotNull Game game) {
		return game.getStatus() == GameStatus.GAMEFREEZE && WAITING_GAMES.getOrDefault(game.getName(), false);
	}
}
