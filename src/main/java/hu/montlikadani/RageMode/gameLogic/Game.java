package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.GameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveFromGameEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGameLobby;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.scores.RageScores;

public class Game {

	public static Location oldLocation;
	public static ItemStack[] oldInventories;
	public static ItemStack[] oldArmor;
	public static Double oldHealth = 0d;
	public static Integer oldHunger = Integer.valueOf(0);
	public static Collection<PotionEffect> oldEffects;
	public static GameMode oldGameMode;
	public static String oldDisplayName;
	public static String oldListName;
	public static Integer oldFire = Integer.valueOf(0);
	public static Float oldExp = 0f;
	public static Integer oldExpLevel = Integer.valueOf(0);
	public static Entity oldVehicle;

	private String name;

	// Game name | Player UUID
	private static Map<String, String> players = new HashMap<>();
	private static Map<UUID, String> specPlayer = new HashMap<>();

	static Location loc;
	static GameMode gMode = GameMode.SURVIVAL;
	static boolean fly = false;
	static boolean allowFly = false;

	private static boolean running;

	private static LobbyTimer lobbyTimer;

	public Game(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Map<String, String> getPlayersInList() {
		return Collections.unmodifiableMap(players);
	}

	public static Map<UUID, String> getSpectatorPlayers() {
		return Collections.unmodifiableMap(specPlayer);
	}

	public static boolean isSpectator(UUID uuid) {
		Validate.notNull(uuid, "UUID can't be null!");

		return specPlayer != null && specPlayer.containsKey(uuid);
	}

	public static boolean containsPlayerInList(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");
		Validate.notEmpty(uuid, "UUID can't be empty!");

		return players != null && players.containsValue(uuid);
	}

	public static boolean addPlayer(Player player, String game) {
		if (!GameUtils.isGameWithNameExists(game)) {
			player.sendMessage(RageMode.getLang().get("game.does-not-exist"));
			return false;
		}

		if (isGameRunning(game)) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		GameJoinAttemptEvent event = new GameJoinAttemptEvent(player, game);
		Utils.callEvent(event);
		if (event.isCancelled())
			return false;

		String uuid = player.getUniqueId().toString();

		if (containsPlayerInList(uuid)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		Configuration conf = RageMode.getInstance().getConfiguration();

		if (!conf.getCV().isSavePlayerData()) {
			// We still need some data saving
			oldLocation = player.getLocation();
			oldGameMode = player.getGameMode();
			player.setGameMode(GameMode.SURVIVAL);

			hu.montlikadani.ragemode.gameUtils.GameUtils.clearPlayerTools(player);
		}

		int time = GetGameLobby.getLobbyTime(game);

		if (players.size() < GetGames.getMaxPlayers(game)) {
			players.put(game, uuid);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));

			if (conf.getCV().getMinPlayers() > 1) {
				if (players.size() == conf.getCV().getMinPlayers()) {
					if (lobbyTimer == null) { // do not create double class instance
						lobbyTimer = new LobbyTimer(game, time);
						lobbyTimer.loadTimer();
					}
				}
			} else {
				if (players.size() == 2 && lobbyTimer == null) {
					lobbyTimer = new LobbyTimer(game, time);
					lobbyTimer.loadTimer();
				}
			}
			return true;
		}

		// Gets a random player who is in game and kicks from the game
		// to join the VIP player.
		if (player.hasPermission("ragemode.vip") && hasRoomForVIP(game)) {
			Random random = new Random();
			boolean isVIP = false;
			Player playerToKick;

			do {
				int kickposition = random.nextInt(GetGames.getMaxPlayers(game) - 1);
				playerToKick = Bukkit.getPlayer(UUID.fromString(getPlayersFromList().get(kickposition)));
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

			Utils.clearPlayerInventory(playerToKick);

			addBackTools(player);

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));
			final Player pl = playerToKick;
			players.entrySet().removeIf(u -> u.getValue().equals(pl.getUniqueId().toString()));

			if (conf.getCV().getMinPlayers() > 1) {
				if (players.size() == conf.getCV().getMinPlayers()) {
					if (lobbyTimer == null) {
						lobbyTimer = new LobbyTimer(game, time);
						lobbyTimer.loadTimer();
					}
				} else {
					lobbyTimer = null;
					return false;
				}
			} else {
				if (players.size() == 2) {
					if (lobbyTimer == null) {
						lobbyTimer = new LobbyTimer(game, time);
						lobbyTimer.loadTimer();
					}
				} else {
					lobbyTimer = null;
					return false;
				}
			}

			if (RageScores.getPlayerPoints(uuid) == null) {
				RageScores.getPlayerPointsMap().put(uuid, new PlayerPoints(uuid));
			}

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", game));
			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public static boolean addSpectatorPlayer(Player player, String game) {
		if (!RageMode.getInstance().getConfiguration().getCV().isBungee()) {
			loc = player.getLocation();
			gMode = player.getGameMode();
			fly = player.isFlying();
			allowFly = player.getAllowFlight();
		}

		SpectatorJoinToGameEvent spec = new SpectatorJoinToGameEvent(game, player);
		Utils.callEvent(spec);

		specPlayer.put(player.getUniqueId(), game);
		return specPlayer.containsKey(player.getUniqueId());
	}

	public static boolean removeSpectatorPlayer(Player player) {
		if (!RageMode.getInstance().getConfiguration().getCV().isSpectatorEnabled())
			return false;

		if (isSpectator(player.getUniqueId())) {
			if (!RageMode.getInstance().getConfiguration().getCV().isBungee()) {
				player.teleport(loc);
				player.setGameMode(gMode);
				player.setFlying(fly);
				player.setAllowFlight(allowFly);
			}

			SpectatorLeaveFromGameEvent spec = new SpectatorLeaveFromGameEvent(specPlayer.get(player.getUniqueId()), player);
			Utils.callEvent(spec);

			specPlayer.remove(player.getUniqueId());
			return true;
		}
		return false;
	}

	public static boolean removePlayer(Player player) {
		if (!containsPlayerInList(player.getUniqueId().toString())) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		String game = getPlayersGame(player);
		if (game == null) {
			return false;
		}

		GameLeaveAttemptEvent gameLeaveEvent = new GameLeaveAttemptEvent(player, game);
		Utils.callEvent(gameLeaveEvent);
		if (gameLeaveEvent.isCancelled())
			return false;

		if (!player.hasMetadata("leavingRageMode")) {
			player.setMetadata("leavingRageMode", new FixedMetadataValue(RageMode.getInstance(), true));

			removePlayerSynced(player);

			Utils.clearPlayerInventory(player);
			player.sendMessage(RageMode.getLang().get("game.player-left"));

			player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

			addBackTools(player);

			players.entrySet().removeIf(u -> u.getValue().equals(player.getUniqueId().toString()));

			if (players.size() < RageMode.getInstance().getConfiguration().getCV().getMinPlayers()) {
				lobbyTimer = null; // Remove the lobby timer instance when not enough players to start
			}

			player.removeMetadata("leavingRageMode", RageMode.getInstance());
			return true;
		}

		player.removeMetadata("leavingRageMode", RageMode.getInstance());
		return false;
	}

	public static void removePlayerSynced(Player player) {
		String game = getPlayersGame(player);
		// Just a null check if the player not find in the list
		if (game == null) {
			return;
		}

		if (ScoreBoard.allScoreBoards.containsKey(game))
			ScoreBoard.allScoreBoards.get(game).removeScoreBoard(player, true);

		if (TabTitles.allTabLists.containsKey(game))
			TabTitles.allTabLists.get(game).removeTabList(player);

		if (ScoreTeam.allTeams.containsKey(game))
			ScoreTeam.allTeams.get(game).removeTeam(player);
	}

	/**
	 * Checks whatever the game is running or not.
	 * @param game Game
	 * @return true if the game is exist and running.
	 */
	public static boolean isGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (GetGames.isGameExistent(game) && running) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the game running.
	 * @param game Game
	 * @return true if game exist and the game not running
	 */
	public static boolean setGameRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (running) {
			return false;
		}

		running = true;
		return true;
	}

	/**
	 * Sets the game not running
	 * @param game Game
	 * @return true if game exist and the game is running
	 */
	public static boolean setGameNotRunning(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (!GetGames.isGameExistent(game))
			return false;

		if (running) {
			running = false;
			return true;
		}

		return false;
	}

	/**
	 * Check if the specified player is currently in game.
	 * @param playerUUID Player UUID
	 * @return true if player is in list and playing
	 */
	public static boolean isPlayerPlaying(String playerUUID) {
		Validate.notNull(playerUUID, "Player UUID can not be null!");

		if (containsPlayerInList(playerUUID))
			return true;

		return false;
	}

	/**
	 * Check whatever has free room for VIP players.
	 * @param game Game
	 * @return true if the players size not equal to vips size
	 */
	public static boolean hasRoomForVIP(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		if (players != null) {
			return false;
		}

		int vipsInGame = 0;

		for (Entry<String, String> playerUUIDs : players.entrySet()) {
			if (playerUUIDs.getKey().equalsIgnoreCase(game)) {
				Player p = Bukkit.getPlayer(UUID.fromString(playerUUIDs.getValue()));

				if (p.hasPermission("ragemode.vip"))
					vipsInGame++;
			}
		}

		if (vipsInGame == players.size())
			return false;

		return true;
	}

	/**
	 * Gets the specified player game from list.
	 * @param player Player
	 * @return game if player playing
	 */
	public static String getPlayersGame(Player player) {
		Validate.notNull(player, "Player can't be null!");

		if (players != null) {
			for (Entry<String, String> playerUUIDs : players.entrySet()) {
				if (Bukkit.getPlayer(UUID.fromString(playerUUIDs.getValue())).equals(player)) {
					return playerUUIDs.getKey();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the game by player uuid from list.
	 * @param uuid Player UUID
	 * @return game if player playing
	 */
	public static String getPlayersGame(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");
		Validate.notEmpty(uuid, "UUID can't be empty!");

		return getPlayersGame(Bukkit.getPlayer(UUID.fromString(uuid)));
	}

	/**
	 * Get the players uuid who added to the list.
	 * @return Players UUID
	 */
	public static Map<String, String> getPlayers() {
		return players;
	}

	/**
	 * Gets the players converted to list.
	 * @return list Players
	 */
	public static List<String> getPlayersFromList() {
		List<String> list = new ArrayList<>();

		if (players != null) {
			for (Entry<String, String> entries : players.entrySet()) {
				list.add(entries.getValue());
			}
		}

		return list;
	}

	/**
	 * Get the player who playing in game.
	 * @param game Game
	 * @return Player who in game currently.
	 */
	public static Player getPlayerInGame(String game) {
		Validate.notNull(game, "Game name can't be null!");
		Validate.notEmpty(game, "Game name can't be empty!");

		return players != null && players.get(game) != null ? Bukkit.getPlayer(UUID.fromString(players.get(game)))
				: null;
	}

	/**
	 * Get the player by uuid from list.
	 * @param uuid Player UUID
	 * @return Player
	 */
	public static Player getPlayerByUUID(UUID uuid) {
		Validate.notNull(uuid, "UUID can't be null!");

		return getPlayerByUUID(uuid.toString());
	}

	/**
	 * Get the player by uuid from list.
	 * @param uuid Player UUID
	 * @return Player
	 */
	public static Player getPlayerByUUID(String uuid) {
		Validate.notNull(uuid, "UUID can't be null!");

		if (players != null) {
			for (Entry<String, String> list : players.entrySet()) {
				if (list.getValue().equals(uuid)) {
					return Bukkit.getPlayer(list.getValue());
				}
			}
		}

		return null;
	}

	public static LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}

	/**
	 * Cancels the lobby timer and nulls the class to create new instance
	 * for this class. This prevents that bug when the player re-join to the
	 * game, then lobby timer does not start.
	 */
	public static void removeLobbyTimer() {
		if (lobbyTimer != null) {
			lobbyTimer.cancel();
			lobbyTimer = null;
		}
	}

	private static void addBackTools(Player player) {
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getCV().isBungee()) {
			RageMode.getInstance().getBungeeUtils().connectToHub(player);
		} else if (conf.getCV().isSavePlayerData()) {
			if (oldLocation != null) { // Teleport back to the location
				player.teleport(oldLocation);
				oldLocation = null;
			}

			if (oldInventories != null) { // Give him his inventory back.
				player.getInventory().setContents(oldInventories);
				oldInventories = null;
			}

			if (oldArmor != null) { // Give him his armor back.
				player.getInventory().setArmorContents(oldArmor);
				oldArmor = null;
			}

			if (oldHealth > 0d) { // Give him his health back.
				player.setHealth(oldHealth);
				oldHealth = 0d;
			}

			if (oldHunger > 0) { // Give him his hunger back.
				player.setFoodLevel(oldHunger);
				oldHunger = 0;
			}

			if (oldEffects != null && !oldEffects.isEmpty()) { // Give him his potion effects back.
				player.addPotionEffects(oldEffects);
				oldEffects.clear();
			}

			if (oldGameMode != null) { // Give him his gamemode back.
				player.setGameMode(oldGameMode);
				oldGameMode = null;
			}

			if (oldListName != null) { // Give him his list name back.
				player.setPlayerListName(oldListName);
				oldListName = null;
			}

			if (oldDisplayName != null) { // Give him his display name back.
				player.setDisplayName(oldDisplayName);
				oldDisplayName = null;
			}

			if (oldFire > 0) { // Give him his fire back.
				player.setFireTicks(oldFire);
				oldFire = 0;
			}

			if (oldExp > 0f) { // Give him his exp back.
				player.setExp(oldExp);
				oldExp = 0f;
			}

			if (oldExpLevel > 0) { // Give him his exp level back.
				player.setLevel(oldExpLevel);
				oldExpLevel = 0;
			}

			if (oldVehicle != null) { // Give him his vehicle back.
				oldVehicle.getVehicle().teleport(player);
				oldVehicle = null;
			}

			conf.getDatasCfg().set("datas." + player.getName(), null);
			Configuration.saveFile(conf.getDatasCfg(), conf.getDatasFile());
		} else {
			if (oldLocation != null) { // Get him back to his old location.
				player.teleport(oldLocation);
				oldLocation = null;
			}

			if (oldGameMode != null) { // Give him his gamemode back.
				player.setGameMode(oldGameMode);
				oldGameMode = null;
			}
		}
	}
}
