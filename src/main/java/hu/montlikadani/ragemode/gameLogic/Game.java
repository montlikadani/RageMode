package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameLobby;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.gameUtils.modules.TabTitles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class Game {

	private String name;
	private GameType gameType;
	private GameStatus status = GameStatus.STOPPED;

	private final Map<UUID, PlayerManager> players = new HashMap<>(), specPlayer = new HashMap<>();

	private boolean running = false;
	private LobbyTimer lobbyTimer;

	public long worldTime = 0L;

	private final Set<ActionMessengers> acList = new HashSet<>();

	public Game(String name) {
		this(name, GameType.NORMAL);
	}

	// TODO: In the future add ability to work with ids
	public Game(String name, GameType gameType) {
		this.name = name == null ? "" : name;

		setGameType(gameType);
	}

	/**
	 * @return the game name that can't be null.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the {@link GameType} of this game
	 */
	public GameType getGameType() {
		return gameType;
	}

	/**
	 * Sets the game type for this game.
	 * 
	 * @param gameType {@link GameType}
	 */
	public void setGameType(GameType gameType) {
		this.gameType = gameType == null ? GameType.NORMAL : gameType;
	}

	/**
	 * Get the players who added to the list.
	 * @return {@link Map}
	 */
	public Map<UUID, PlayerManager> getPlayers() {
		return players;
	}

	/**
	 * Gets the spectator players who added to the list.
	 * @return {@link Map}
	 */
	public Map<UUID, PlayerManager> getSpectatorPlayers() {
		return specPlayer;
	}

	/**
	 * Checks if the player is in spectator list.
	 * @param p Player
	 * @return true if in the list
	 */
	public boolean isSpectatorInList(Player p) {
		return specPlayer.containsKey(p.getUniqueId());
	}

	/**
	 * Checks if the player is in list.
	 * @param p Player
	 * @return true if in the list
	 */
	public boolean isPlayerInList(Player p) {
		return players.containsKey(p.getUniqueId());
	}

	public boolean addPlayer(Player player) {
		if (running) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		if (isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		RMGameJoinAttemptEvent event = new RMGameJoinAttemptEvent(this, player);
		Utils.callEvent(event);
		if (event.isCancelled())
			return false;

		ActionMessengers ac = new ActionMessengers(this, player);
		acList.add(ac);

		PlayerManager pm = new PlayerManager(player, name);

		int time = GameLobby.getLobbyTime(name),
				maxPlayers = GetGames.getMaxPlayers(name),
				minPlayers = GetGames.getMinPlayers(name);

		if (players.size() < maxPlayers) {
			players.put(player.getUniqueId(), pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				lobbyTimer = new LobbyTimer(this, time);
				lobbyTimer.loadTimer();
			}

			return true;
		}

		// Gets a random player who is in game and kicks from the game to join the VIP
		// player.
		if (ConfigValues.isKickRandomPlayerIfJoinsVip() && player.hasPermission("ragemode.vip") && hasRoomForVIP()) {
			boolean isVIP = false;
			Player playerToKick;

			do {
				int kickposition = maxPlayers < 2 ? 0 : ThreadLocalRandom.current().nextInt(maxPlayers - 1);
				playerToKick = getPlayersFromList().get(kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			playerToKick.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));

			Utils.clearPlayerInventory(playerToKick);
			getPlayerManager(playerToKick).ifPresent(PlayerManager::addBackTools);
			players.remove(playerToKick.getUniqueId());

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			players.put(player.getUniqueId(), pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				lobbyTimer = new LobbyTimer(this, time);
				lobbyTimer.loadTimer();
			}

			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public boolean addSpectatorPlayer(Player player) {
		PlayerManager pm = new PlayerManager(player, name);
		specPlayer.put(player.getUniqueId(), pm);

		if (!ConfigValues.isBungee()) {
			pm.storePlayerTools(true);
		}
		Utils.clearPlayerInventory(player);

		Utils.callEvent(new SpectatorJoinToGameEvent(this, player));

		return isSpectatorInList(player);
	}

	public boolean removeSpectatorPlayer(Player player) {
		if (!ConfigValues.isSpectatorEnabled() || !isSpectatorInList(player)) {
			return false;
		}

		Utils.clearPlayerInventory(player);
		getSpectatorPlayerManager(player).ifPresent(p -> p.addBackTools(true));

		Utils.callEvent(new SpectatorLeaveGameEvent(this, player));

		return specPlayer.remove(player.getUniqueId()) != null;
	}

	public boolean removePlayer(final Player player) {
		return removePlayer(player, false);
	}

	public boolean removePlayer(final Player player, boolean switchToSpec) {
		if (!isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		hu.montlikadani.ragemode.gameUtils.StorePlayerStuffs oldStuffs = null;
		if (getPlayerManager(player).isPresent()) {
			oldStuffs = getPlayerManager(player).get().getStorePlayer();
		}

		Utils.clearPlayerInventory(player);
		if (!switchToSpec) {
			getPlayerManager(player).ifPresent(PlayerManager::addBackTools);
		}

		acList.remove(removePlayerSynced(player));
		players.remove(player.getUniqueId());

		if (!player.isCustomNameVisible()) {
			player.setCustomNameVisible(true);
		}

		if (!switchToSpec) {
			player.sendMessage(RageMode.getLang().get("game.player-left"));
			player.setMetadata("Leaving", new FixedMetadataValue(RageMode.getInstance(), true));
		}

		if (switchToSpec) {
			PlayerManager pm = new PlayerManager(player, name);

			if (oldStuffs != null) {
				pm.storeFrom(oldStuffs);
			}

			specPlayer.put(player.getUniqueId(), pm);
		}

		return true;
	}

	/**
	 * Removes all of scoreboard, tablist and score team things from player.
	 * @param player Player
	 * @return {@link ActionMessengers}
	 */
	public ActionMessengers removePlayerSynced(Player player) {
		for (ActionMessengers action : acList) {
			if (action.getPlayer().equals(player)) {
				action.getScoreboard().remove(player);
				TabTitles.sendTabTitle(player, "", "");
				action.getScoreTeam().remove();
				return action;
			}
		}

		return null;
	}

	/**
	 * Checks whatever the game is running or not.
	 * @return true if the game running currently.
	 */
	public boolean isGameRunning() {
		return running;
	}

	/**
	 * Sets the game to running state.
	 * @return true if the game not running currently
	 */
	public boolean setGameRunning() {
		if (running) {
			return false;
		}

		running = true;
		return true;
	}

	/**
	 * Sets the game to not running state.
	 * @return true if the game is running currently
	 */
	public boolean setGameNotRunning() {
		if (running) {
			running = false;
			return true;
		}

		return false;
	}

	/**
	 * Gets the game current set GameStatus.
	 * @return {@link GameStatus}
	 */
	public GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to new status.
	 * @param status the new status to be set for the game
	 */
	public void setStatus(GameStatus status) {
		if (status == null) {
			status = GameStatus.STOPPED;
		}

		RMGameStatusChangeEvent event = new RMGameStatusChangeEvent(this, status);
		Utils.callEvent(event);

		this.status = status;
	}

	/**
	 * Check whatever has free room for VIP players.
	 * @return true if the players size not equal to vips size
	 */
	public boolean hasRoomForVIP() {
		int vipsInGame = 0;

		for (Entry<UUID, PlayerManager> players : players.entrySet()) {
			if (players.getValue().getGameName().equalsIgnoreCase(name)
					&& Bukkit.getPlayer(players.getKey()).hasPermission("ragemode.vip")) {
				vipsInGame++;
			}
		}

		if (vipsInGame == players.size())
			return false;

		return true;
	}

	/**
	 * Get the player by name from list.
	 * @param name Player name
	 * @return Player
	 */
	public Player getPlayer(String name) {
		Validate.notEmpty(name, "Name can't be empty/null");

		for (UUID uuid : players.keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null && player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}

		return null;
	}

	/**
	 * Get the spectator player by name from list.
	 * @param name Player name
	 * @return Player
	 */
	public Player getSpectatorPlayer(String name) {
		Validate.notEmpty(name, "Name can't be empty/null");

		for (UUID uuid : specPlayer.keySet()) {
			Player specs = Bukkit.getPlayer(uuid);
			if (specs != null && specs.getName().equalsIgnoreCase(name)) {
				return specs;
			}
		}

		return null;
	}

	/**
	 * Gets all players including spectators from this game.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getAllPlayers() {
		List<PlayerManager> list = new ArrayList<>();
		players.values().forEach(list::add);
		specPlayer.values().forEach(list::add);
		return list;
	}

	/**
	 * Gets the {@link PlayerManager} player converted to list.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getPlayersFromList() {
		return new ArrayList<>(players.values());
	}

	/**
	 * Gets the {@link PlayerManager} spectator players converted to list.
	 * @return list of {@link PlayerManager}
	 */
	public List<PlayerManager> getSpectatorPlayersFromList() {
		return new ArrayList<>(specPlayer.values());
	}

	/**
	 * Gets the {@link PlayerManager} by spectator player.
	 * @param p Spectator player
	 * @return {@link PlayerManager} by spectator player
	 */
	public Optional<PlayerManager> getSpectatorPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		return Optional.ofNullable(specPlayer.get(p.getUniqueId()));
	}

	/**
	 * Gets the {@link PlayerManager} by player.
	 * @param p Player
	 * @return {@link PlayerManager} by player
	 */
	public Optional<PlayerManager> getPlayerManager(Player p) {
		Validate.notNull(p, "Player can't be null!");

		return Optional.ofNullable(players.get(p.getUniqueId()));
	}

	public LobbyTimer getLobbyTimer() {
		return lobbyTimer;
	}

	public Set<ActionMessengers> getActionMessengers() {
		return acList;
	}

	/**
	 * Cancels the lobby timer.
	 */
	public void cancelLobbyTimer() {
		if (lobbyTimer != null) {
			lobbyTimer.cancel();
		}
	}
}
