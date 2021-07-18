package hu.montlikadani.ragemode.gameLogic.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameStatusChangeEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.GameLobby;
import hu.montlikadani.ragemode.gameLogic.GameSettings;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.StorePlayerStuffs;
import hu.montlikadani.ragemode.gameUtils.modules.TitleSender;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.exception.GameRunningException;

public abstract class BaseGame extends GameSettings {

	protected final GameLobby gameLobby;
	protected final String gameName;

	private GameType gameType = GameType.NORMAL;
	private GameStatus status = GameStatus.STOPPED;

	protected final RageMode rm = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	// Thread-safety implementation to allow modifying without CME
	protected final Set<PlayerManager> players = Collections.newSetFromMap(new ConcurrentHashMap<>());

	protected final IGameSpawn[] spawns = new IGameSpawn[2];
	protected final Set<ActionMessengers> acList = new HashSet<>();

	protected boolean running = false;

	public BaseGame(@NotNull String gameName, @Nullable GameType gameType) {
		super(gameName);

		this.gameName = gameName;
		this.gameType = gameType == null ? GameType.NORMAL : gameType;

		gameLobby = new GameLobby(this);
		loadSpawns();
	}

	private void loadSpawns() {
		if (gameType == GameType.APOCALYPSE) {
			spawns[0] = new GameZombieSpawn((ZombieGame) this);
		}

		spawns[1] = new GameSpawn(this);
	}

	/**
	 * @return the name of this game
	 */
	@NotNull
	public final String getName() {
		return gameName;
	}

	/**
	 * @return the {@link GameType} of this game
	 */
	@NotNull
	public final GameType getGameType() {
		return gameType;
	}

	/**
	 * Sets the new game type for this game and tries to convert some game settings.
	 * <p>
	 * This method will returns immediately if these conditions met:
	 * <ul>
	 * <li>If the given type is equal to the current one.</li>
	 * <li>If the given type is <code>null</code> and the current one is set to
	 * {@link GameType#NORMAL}.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param gameType the new game type to set
	 */
	public final void setGameType(@Nullable GameType gameType) {
		if (this.gameType == gameType || (gameType == null && this.gameType == GameType.NORMAL)) {
			return;
		}

		this.gameType = gameType == null ? GameType.NORMAL : gameType;

		boolean found = false;
		int i;
		for (i = 0; i < rm.getGames().size(); i++) {
			if (rm.getGames().get(i).equals(this)) {
				found = true;
				break;
			}
		}

		if (!found) {
			return; // In case if there no such game found, but hopefully
		}

		BaseGame game = null;

		if (this.gameType == GameType.APOCALYPSE) {
			game = new ZombieGame(gameName);
			spawns[0] = new GameZombieSpawn((ZombieGame) game);
			game.gameType = this.gameType;
			game.randomSpawnForZombies = randomSpawnForZombies;
		} else if (this.gameType == GameType.NORMAL) {
			game = new PlayerGame(gameName);
		}

		if (game == null) {
			return;
		}

		game.actionbarEnabled = actionbarEnabled;
		game.bossbarEnabled = bossbarEnabled;
		game.gameTime = gameTime;
		game.maxPlayers = maxPlayers;
		game.minPlayers = minPlayers;
		game.status = status;
		game.worldName = worldName;

		rm.getGames().set(i, game);
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
	 * Returns a set of players who's in this game. This set includes normal and
	 * spectator players.
	 * 
	 * @return the {@link Set} of {@link PlayerManager}
	 */
	@NotNull
	public Set<@NotNull PlayerManager> getPlayers() {
		return players;
	}

	/**
	 * Returns a new set of players who's spectator and currently in this game.
	 * 
	 * @return a new {@link Set} of spectators
	 */
	@NotNull
	public Set<@NotNull PlayerManager> getSpectatorPlayers() {
		Set<PlayerManager> spec = new HashSet<>();

		for (PlayerManager pm : players) {
			if (pm.isSpectator()) {
				spec.add(pm);
			}
		}

		return spec;
	}

	/**
	 * Checks if the given player is in this game.
	 * 
	 * @param player {@link Player}
	 * @return true if present
	 * @see #getPlayerManager(UUID)
	 */
	public boolean isPlayerInList(@NotNull Player player) {
		return getPlayerManager(player).isPresent();
	}

	/**
	 * Checks whatever this game is running or not.
	 * 
	 * @return true if this game running
	 */
	public final boolean isRunning() {
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
	public final GameStatus getStatus() {
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
	public Optional<PlayerManager> getPlayerManager(@NotNull Player player) {
		return player == null ? Optional.empty() : getPlayerManager(player.getUniqueId());
	}

	/**
	 * Returns a player who's playing in a game if present. Otherwise
	 * {@link Optional#empty()}
	 * 
	 * @param uuid {@link UUID} a valid player uuid
	 * @return {@link PlayerManager}
	 */
	@NotNull
	public Optional<PlayerManager> getPlayerManager(@NotNull UUID uuid) {
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
	 * Returns a set of action messengers which contains action bar, score board and
	 * team scores.
	 * 
	 * @return set of {@link ActionMessengers}
	 */
	@NotNull
	public final Set<@NotNull ActionMessengers> getActionMessengers() {
		return acList;
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
				if (s != null && s.getGame().equals(this) && spawnType.isAssignableFrom(s.getClass())) {
					return s;
				}
			}
		}

		return null;
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
			if (equals(player.getPlayerGame())) {
				Player pl = player.getPlayer();

				if (pl != null && pl.hasPermission("ragemode.vip")) {
					vipsInGame++;
				}
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
				if (player.getUniqueId().equals(action.getPlayerId())) {
					action.getScoreboard().remove(player);
					TitleSender.sendTabTitle(player, "", "");
					return action;
				}
			}
		}

		return null;
	}

	public abstract boolean addPlayer(Player player, boolean spectator);

	public boolean removePlayer(Player player) {
		return removePlayer(player, false);
	}

	public abstract boolean removePlayer(final Player player, boolean switchToSpec);

	public boolean removePlayer(PlayerManager pm) {
		return removePlayer(pm, false);
	}

	public abstract boolean removePlayer(PlayerManager pm, boolean switchToSpec);

	protected void savePlayerData(PlayerManager pm) {
		Player player = pm.getPlayer();

		if (player == null) {
			return;
		}

		if (!ConfigValues.isSavePlayerData()) {
			// We still need to store some data
			StorePlayerStuffs sp = pm.getStorePlayer();

			sp.oldLocation = player.getLocation();
			sp.oldGameMode = player.getGameMode();
			sp.currentBoard = player.getScoreboard();

			player.setGameMode(org.bukkit.GameMode.SURVIVAL);
			return;
		}

		if (ConfigValues.isBungee()) {
			return;
		}

		pm.storePlayerTools();

		Configuration conf = rm.getConfiguration();

		if (conf.getDatasFile() != null && conf.getDatasFile().exists()) {
			org.bukkit.configuration.file.FileConfiguration data = conf.getDatasCfg();
			String path = "datas." + player.getName() + ".";

			data.set(path + "location", player.getLocation());
			data.set(path + "contents", player.getInventory().getContents());
			data.set(path + "armor-contents", player.getInventory().getArmorContents());
			data.set(path + "health", player.getHealth());
			data.set(path + "food", player.getFoodLevel());

			java.util.Collection<org.bukkit.potion.PotionEffect> activePotions = player.getActivePotionEffects();
			if (!activePotions.isEmpty())
				data.set(path + "potion-effects", activePotions);

			data.set(path + "game-mode", player.getGameMode().name());

			String dName = rm.getComplement().getDisplayName(player);
			if (!dName.equals(player.getName()))
				data.set(path + "display-name", dName);

			if (!(dName = rm.getComplement().getPlayerListName(player)).equals(player.getName()))
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

		clearPlayerTools(player);
	}

	private void clearPlayerTools(Player player) {
		player.getInventory().clear();
		player.updateInventory();

		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setSprinting(false);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setSneaking(false);
		player.setScoreboard(rm.getServer().getScoreboardManager().getMainScoreboard());
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

		rm.getComplement().setDisplayName(player, player.getName());
		rm.getComplement().setPlayerListName(player, player.getName());
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof BaseGame && ((BaseGame) obj).gameName.equalsIgnoreCase(gameName));
	}
}
