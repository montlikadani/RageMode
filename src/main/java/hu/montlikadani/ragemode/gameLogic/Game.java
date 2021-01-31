package hu.montlikadani.ragemode.gameLogic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameJoinAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.modules.TabTitles;
import hu.montlikadani.ragemode.managers.PlayerManager;

public final class Game extends GameSettings {

	private final GameLobby gameLobby;

	private String name;
	private GameType gameType;
	private GameStatus status = GameStatus.STOPPED;

	private final Set<PlayerManager> players = new HashSet<>(), specPlayers = new HashSet<>();
	private final Set<IGameSpawn> spawns = new HashSet<>();

	private boolean running = false;

	/**
	 * The set of world time to be cached before the game start in apocalypse mode
	 */
	public long worldTime = 0L;

	private final Set<ActionMessengers> acList = new HashSet<>();

	public Game(String name) {
		this(name, GameType.NORMAL);
	}

	public Game(String name, GameType gameType) {
		super(name);
		this.name = name == null ? "" : name;

		setGameType(gameType);
		loadSpawns();
		gameLobby = new GameLobby(this);
	}

	private void loadSpawns() {
		if (gameType == GameType.APOCALYPSE) {
			spawns.add(new GameZombieSpawn(this));
		}

		spawns.add(new GameSpawn(this));
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
	 * Returns this game lobby instance
	 * 
	 * @return {@link GameLobby}
	 */
	public GameLobby getGameLobby() {
		return gameLobby;
	}

	/**
	 * Returns a copy of players who's in this game.
	 * 
	 * @return {@link Set}
	 */
	public Set<PlayerManager> getPlayers() {
		return new HashSet<>(players);
	}

	/**
	 * Returns a copy of spectator players who's in this game.
	 * 
	 * @return {@link Set}
	 */
	public Set<PlayerManager> getSpectatorPlayers() {
		return new HashSet<>(specPlayers);
	}

	/**
	 * Checks if the given player is in spectator list.
	 * 
	 * @param p {@link Player}
	 * @return true if cached
	 */
	public boolean isSpectatorInList(Player p) {
		if (p == null) {
			return false;
		}

		for (PlayerManager pm : specPlayers) {
			if (pm.getPlayer() == p) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given player is in list.
	 * 
	 * @param p {@link Player}
	 * @return true if cached
	 */
	public boolean isPlayerInList(Player p) {
		if (p == null) {
			return false;
		}

		for (PlayerManager pm : players) {
			if (pm.getPlayer() == p) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whatever this game is running or not.
	 * 
	 * @return true if this game running.
	 */
	public boolean isGameRunning() {
		return running;
	}

	/**
	 * Sets this game running state to a new state.
	 * 
	 * @param running this game should run or not
	 */
	public void setGameRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Gets the game current set GameStatus.
	 * 
	 * @return {@link GameStatus}
	 */
	public GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to a new status.
	 * 
	 * @param status the new status to be set for this game
	 */
	public void setStatus(GameStatus status) {
		if (status == null) {
			status = GameStatus.STOPPED;
		}

		Utils.callEvent(new RMGameStatusChangeEvent(this, status));
		this.status = status;
	}

	/**
	 * Get the player by its uuid from this game.
	 * 
	 * @param uuid Player uuid
	 * @return {@link Player}
	 */
	public Player getPlayer(UUID uuid) {
		Validate.notNull(uuid, "UUID can't be null");

		for (PlayerManager pm : players) {
			if (pm.getPlayer().getUniqueId().equals(uuid)) {
				return pm.getPlayer();
			}
		}

		return null;
	}

	/**
	 * Get the spectator player by its uuid from this game.
	 * 
	 * @param uuid Player uuid
	 * @return {@link Player}
	 */
	public Player getSpectatorPlayer(UUID uuid) {
		Validate.notNull(uuid, "UUID can't be null");

		for (PlayerManager pm : specPlayers) {
			if (pm.getPlayer().getUniqueId().equals(uuid)) {
				return pm.getPlayer();
			}
		}

		return null;
	}

	/**
	 * Returns an immutable list of all players including spectators who's in this
	 * game.
	 * 
	 * @return immutable list of {@link PlayerManager}
	 */
	public ImmutableList<PlayerManager> getAllPlayers() {
		List<PlayerManager> list = new java.util.ArrayList<>();
		players.forEach(list::add);
		specPlayers.forEach(list::add);
		return ImmutableList.copyOf(list);
	}

	/**
	 * Returns a spectator player who's in a game if present. Otherwise
	 * {@link Optional#empty()}
	 * 
	 * @param p Spectator player
	 * @return {@link PlayerManager}
	 */
	public Optional<PlayerManager> getSpectatorPlayerManager(Player p) {
		if (p != null) {
			for (PlayerManager pm : specPlayers) {
				if (pm.getPlayer() == p) {
					return Optional.of(pm);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns a player who's playing in a game if present. Otherwise
	 * {@link Optional#empty()}
	 * 
	 * @param p {@link Player}
	 * @return {@link PlayerManager}
	 */
	public Optional<PlayerManager> getPlayerManager(Player p) {
		if (p != null) {
			for (PlayerManager pm : players) {
				if (pm.getPlayer() == p) {
					return Optional.of(pm);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Removes all set of spawns including zombie and player spawns from this game.
	 */
	public void removeSpawn() {
		spawns.removeIf(s -> s.getGame().getName().equalsIgnoreCase(name));
	}

	/**
	 * Removes all set of spawns from this game with the given type.
	 * 
	 * <pre>
	 * removeSpawn(GameZombieSpawn.class)
	 * </pre>
	 * 
	 * This will removes all zombie spawns from this game.
	 * 
	 * @param spawnType the type of the spawn
	 */
	public void removeSpawn(Class<? extends IGameSpawn> spawnType) {
		spawns.removeIf(s -> spawnType.isAssignableFrom(s.getClass()) && s.getGame().getName().equalsIgnoreCase(name));
	}

	/**
	 * Returns this game spawn object with the given type.
	 * 
	 * <pre>
	 * getSpawn(GameSpawn.class)
	 * </pre>
	 * 
	 * This will returns the game spawn for players.
	 * 
	 * @param spawnType the type of the spawn
	 * @return {@link IGameSpawn}
	 */
	public IGameSpawn getSpawn(Class<? extends IGameSpawn> spawnType) {
		for (IGameSpawn s : spawns) {
			if (s.getGame().getName().equalsIgnoreCase(name) && spawnType.isAssignableFrom(s.getClass())) {
				return s;
			}
		}

		return null;
	}

	/**
	 * Returns a set of spawns from this game including zombie and player spawns.
	 * 
	 * @return set of {@link IGameSpawn}
	 */
	public Set<IGameSpawn> getSpawns() {
		return spawns;
	}

	/**
	 * Returns a set of action messengers which contains action bar, score board and
	 * team.
	 * 
	 * @return set of {@link ActionMessengers}
	 */
	public Set<ActionMessengers> getActionMessengers() {
		return acList;
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

		acList.add(new ActionMessengers(this, player.getUniqueId()));

		PlayerManager pm = new PlayerManager(player.getUniqueId(), name);

		if (players.size() < maxPlayers) {
			players.add(pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				gameLobby.getLobbyTimer().beginScheduling();
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
				playerToKick = Iterables.get(players, kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			Utils.clearPlayerInventory(playerToKick);
			getPlayerManager(playerToKick).ifPresent(pmToKick -> {
				players.remove(pmToKick);
				pmToKick.addBackTools();
			});

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			if (players.add(pm)) {
				player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

				if (players.size() == minPlayers) {
					gameLobby.getLobbyTimer().beginScheduling();
				}
			}

			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public boolean addSpectatorPlayer(Player player) {
		PlayerManager pm = new PlayerManager(player.getUniqueId(), name);
		specPlayers.add(pm);

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
		PlayerManager pm = getSpectatorPlayerManager(player).orElse(null);
		if (pm != null) {
			pm.addBackTools(true);
		}

		Utils.callEvent(new SpectatorLeaveGameEvent(this, player));

		return pm != null && specPlayers.remove(pm);
	}

	public boolean removePlayer(final Player player) {
		return removePlayer(player, false);
	}

	public boolean removePlayer(final Player player, boolean switchToSpec) {
		if (!isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		acList.remove(removePlayerSynced(player));
		player.setCustomNameVisible(true);

		getPlayerManager(player).ifPresent(oldPm -> {
			hu.montlikadani.ragemode.gameUtils.StorePlayerStuffs oldStuffs = oldPm.getStorePlayer();

			Utils.clearPlayerInventory(player);
			if (!switchToSpec) {
				oldPm.addBackTools();
			}

			if (players.remove(oldPm) && switchToSpec) {
				PlayerManager pm = new PlayerManager(player.getUniqueId(), name);

				if (oldStuffs != null) {
					pm.storeFrom(oldStuffs);
				}

				specPlayers.add(pm);
			}
		});

		// Send left message when switched to spec or not
		player.sendMessage(RageMode.getLang().get("game.player-left"));
		return true;
	}

	/**
	 * Check whatever has free room for VIP players.
	 * 
	 * @return true if the players size not equal to vips size
	 */
	public boolean hasRoomForVIP() {
		int vipsInGame = 0;

		for (PlayerManager player : players) {
			if (player.getGameName().equalsIgnoreCase(name) && player.getPlayer().hasPermission("ragemode.vip")) {
				vipsInGame++;
			}
		}

		return vipsInGame != players.size();
	}

	/**
	 * Removes all of scoreboard, tablist and score team things from player.
	 * 
	 * @param player {@link Player}
	 * @return {@link ActionMessengers}
	 */
	public ActionMessengers removePlayerSynced(Player player) {
		for (ActionMessengers action : acList) {
			if (action.getPlayer() == player) {
				action.getScoreboard().remove(player);
				TabTitles.sendTabTitle(player, "", "");
				action.getScoreTeam().remove();
				return action;
			}
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof Game && ((Game) obj).getName().equalsIgnoreCase(name));
	}
}
