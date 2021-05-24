package hu.montlikadani.ragemode.gameLogic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.API.event.SpectatorJoinToGameEvent;
import hu.montlikadani.ragemode.API.event.SpectatorLeaveGameEvent;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.modules.TitleSender;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.exception.GameRunningException;

public final class Game extends GameSettings {

	private final GameLobby gameLobby;
	private final String name;

	private GameType gameType = GameType.NORMAL;
	private GameStatus status = GameStatus.STOPPED;

	// Thread-safety implementation to allow modifying without CME
	private final Set<PlayerManager> players = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final IGameSpawn[] spawns = new IGameSpawn[2];

	private boolean running = false;

	/**
	 * The set of world time to be cached before the game start in apocalypse mode
	 */
	public long worldTime = 0L;

	private final Set<ActionMessengers> acList = new HashSet<>();

	public Game(@NotNull String name) {
		this(name, GameType.NORMAL);
	}

	public Game(@NotNull String name, @Nullable GameType gameType) {
		super(name);

		this.name = name;

		setGameType(gameType);
		loadSpawns();

		gameLobby = new GameLobby(this);
	}

	private void loadSpawns() {
		if (gameType == GameType.APOCALYPSE) {
			spawns[0] = new GameZombieSpawn(this);
		}

		spawns[1] = new GameSpawn(this);
	}

	/**
	 * @return the name of this game
	 */
	@NotNull
	public String getName() {
		return name;
	}

	/**
	 * @return the {@link GameType} of this game
	 */
	@NotNull
	public GameType getGameType() {
		return gameType;
	}

	/**
	 * Sets the game type for this game.
	 * 
	 * @param gameType {@link GameType}
	 */
	public void setGameType(@Nullable GameType gameType) {
		this.gameType = gameType == null ? GameType.NORMAL : gameType;
	}

	/**
	 * Returns this game lobby instance
	 * 
	 * @return {@link GameLobby}
	 */
	@NotNull
	public GameLobby getGameLobby() {
		return gameLobby;
	}

	/**
	 * Returns a set of players who's in this game.
	 * 
	 * @return {@link Set}
	 */
	@NotNull
	public Set<PlayerManager> getPlayers() {
		return players;
	}

	/**
	 * Returns a new set of players who's spectator and currently in this game.
	 * 
	 * @return a new {@link Set} of spectators
	 */
	@NotNull
	public Set<PlayerManager> getSpectatorPlayers() {
		Set<PlayerManager> spec = new HashSet<>();

		for (PlayerManager pm : players) {
			if (pm.isSpectator()) {
				spec.add(pm);
			}
		}

		return spec;
	}

	/**
	 * Checks if the given player is in list.
	 * 
	 * @param player {@link Player}
	 * @return true if cached
	 */
	public boolean isPlayerInList(@Nullable Player player) {
		return getPlayerManager(player).isPresent();
	}

	/**
	 * Checks whatever this game is running or not.
	 * 
	 * @return true if this game running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Marks this game running to true.
	 * 
	 * @param running whenever this game is running or not
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Gets the game current set GameStatus.
	 * 
	 * @return {@link GameStatus}
	 */
	@NotNull
	public GameStatus getStatus() {
		return status;
	}

	/**
	 * Sets the game status to a new status.
	 * 
	 * @param status the new status to be set for this game
	 * @throws GameRunningException if the game is running and a object tries to set
	 *                              status to ready/not
	 */
	public void setStatus(@Nullable GameStatus status) throws GameRunningException {
		if (status == null) {
			status = GameStatus.STOPPED;
		} else if (running && (status == GameStatus.NOTREADY || status == GameStatus.READY)) {
			throw new GameRunningException("Cannot set game status to ready/not if running.");
		}

		Utils.callEvent(new RMGameStatusChangeEvent(this, status));
		this.status = status;
	}

	/**
	 * Returns a player who's playing in a game if present. Otherwise
	 * {@link Optional#empty()}
	 * 
	 * @param player {@link Player}
	 * @return {@link PlayerManager}
	 * @see #getPlayerManager(UUID)
	 */
	@NotNull
	public Optional<PlayerManager> getPlayerManager(@Nullable Player player) {
		return getPlayerManager(player.getUniqueId());
	}

	/**
	 * Returns a player who's playing in a game if present. Otherwise
	 * {@link Optional#empty()}
	 * 
	 * @param uuid {@link UUID}
	 * @return {@link PlayerManager}
	 */
	@NotNull
	public Optional<PlayerManager> getPlayerManager(@Nullable UUID uuid) {
		if (uuid != null) {
			for (PlayerManager pm : players) {
				if (uuid.equals(pm.getUniqueId())) {
					return Optional.of(pm);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns this game spawn object with the given type.
	 * 
	 * <pre>
	 * getSpawn(GameSpawn.class)
	 * </pre>
	 * 
	 * This will returns the game spawn for players.
	 * <p>
	 * 
	 * @param spawnType the type of the spawn
	 * @return {@link IGameSpawn}
	 */
	@Nullable
	public IGameSpawn getSpawn(@NotNull Class<? extends IGameSpawn> spawnType) {
		if (spawnType != null) {
			for (IGameSpawn s : spawns) {
				if (s != null && s.getGame().getName().equalsIgnoreCase(name) && spawnType.isAssignableFrom(s.getClass())) {
					return s;
				}
			}
		}

		return null;
	}

	/**
	 * Returns a set of action messengers which contains action bar, score board and
	 * team.
	 * 
	 * @return set of {@link ActionMessengers}
	 */
	@NotNull
	public Set<ActionMessengers> getActionMessengers() {
		return acList;
	}

	public boolean addPlayer(Player player, boolean spectator) {
		if (spectator) {
			PlayerManager pm = new PlayerManager(player.getUniqueId(), name);
			players.add(pm);

			if (!ConfigValues.isBungee()) {
				pm.storePlayerTools(true);
			}

			player.getInventory().clear();
			player.updateInventory();

			Utils.callEvent(new SpectatorJoinToGameEvent(this, player));
			return true;
		}

		if (running) {
			player.sendMessage(RageMode.getLang().get("game.running"));
			return false;
		}

		if (isPlayerInList(player)) {
			player.sendMessage(RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		acList.add(new ActionMessengers(player.getUniqueId()));

		PlayerManager pm = new PlayerManager(player.getUniqueId(), name);

		if (players.size() < maxPlayers) {
			players.add(pm);

			player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

			if (players.size() == minPlayers) {
				gameLobby.getLobbyTimer().beginScheduling(this);
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
				playerToKick = com.google.common.collect.Iterables.get(players, kickposition).getPlayer();
				isVIP = playerToKick.hasPermission("ragemode.vip");
			} while (isVIP);

			playerToKick.getInventory().clear();
			playerToKick.updateInventory();

			getPlayerManager(playerToKick).ifPresent(pmToKick -> {
				pmToKick.giveBackTools();
				players.remove(pmToKick);
			});

			playerToKick.sendMessage(RageMode.getLang().get("game.player-kicked-for-vip"));

			if (players.add(pm)) {
				player.sendMessage(RageMode.getLang().get("game.you-joined-the-game", "%game%", name));

				if (players.size() == minPlayers) {
					gameLobby.getLobbyTimer().beginScheduling(this);
				}
			}

			return true;
		}

		player.sendMessage(RageMode.getLang().get("game.full"));
		return false;
	}

	public boolean removePlayer(final Player player) {
		return removePlayer(player, false);
	}

	public boolean removePlayer(final Player player, boolean switchToSpec) {
		Optional<PlayerManager> opt = getPlayerManager(player);

		if (!opt.isPresent()) {
			player.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		return removePlayer(opt.get(), switchToSpec);
	}

	public boolean removePlayer(PlayerManager pm) {
		return removePlayer(pm, false);
	}

	public boolean removePlayer(PlayerManager pm, boolean switchToSpec) {
		Player player = pm.getPlayer();

		player.getInventory().clear();
		player.updateInventory();

		if (pm.isSpectator()) {
			pm.giveBackTools();

			Utils.callEvent(new SpectatorLeaveGameEvent(this, player));
			return players.remove(pm);
		}

		acList.remove(removePlayerSynced(player));
		player.setCustomNameVisible(true);

		if (switchToSpec) {
			pm.setSpectator(true);
		} else {
			pm.giveBackTools();
			players.remove(pm);
		}

		// Send left message when switched to spec or not
		player.sendMessage(RageMode.getLang().get("game.player-left"));
		return true;
	}

	/**
	 * Check whatever has free room for VIP players. VIP players should have
	 * <code>ragemode.vip</code> permission to count in.
	 * 
	 * @return true if the players size not equal to vips size
	 */
	public boolean hasRoomForVIP() {
		int vipsInGame = 0;

		for (PlayerManager player : players) {
			if (equals(player.getPlayerGame()) && player.getPlayer().hasPermission("ragemode.vip")) {
				vipsInGame++;
			}
		}

		return vipsInGame != players.size();
	}

	/**
	 * Removes all displayed scoreboards and tablist things from the given player.
	 * 
	 * @param player {@link Player}
	 * @return {@link ActionMessengers}
	 */
	@Nullable
	public ActionMessengers removePlayerSynced(@NotNull Player player) {
		if (player != null) {
			for (ActionMessengers action : acList) {
				if (player.getUniqueId().equals(action.getUniqueId())) {
					action.getScoreboard().remove(player);
					TitleSender.sendTabTitle(player, "", "");
					return action;
				}
			}
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof Game && ((Game) obj).getName().equalsIgnoreCase(name));
	}
}
