package hu.montlikadani.ragemode.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.spawn.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.spawn.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.StorePlayerStuffs;
import hu.montlikadani.ragemode.utils.Utils;
import hu.montlikadani.ragemode.utils.stuff.MovementTicker;

public class PlayerManager implements MovementTicker {

	/**
	 * Cached map to store the "toggleable" death messages
	 */
	public static final Map<UUID, Boolean> DEATH_MESSAGES_TOGGLE = new HashMap<>();

	private UUID playerUUID;
	private String gameName;

	private int lives = 0;

	private boolean spectator = false;

	private final StorePlayerStuffs sps = new StorePlayerStuffs();
	private final RageMode plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(RageMode.class);

	public PlayerManager(UUID playerUUID, String gameName) {
		this.playerUUID = playerUUID;
		this.gameName = gameName;
	}

	/**
	 * Returns the player object retrieved from uuid.
	 * 
	 * @return {@link Player}
	 */
	@Nullable
	public Player getPlayer() {
		return Bukkit.getPlayer(playerUUID);
	}

	@Nullable // Not always, usually when a object created with null uuid parameter
	public UUID getUniqueId() {
		return playerUUID;
	}

	/**
	 * Gets the game name where the player is in currently.
	 * 
	 * @return the name of the game where the player is in
	 */
	@Nullable // Not always, usually when a object created with null parameter
	public String getGameName() {
		return gameName;
	}

	/**
	 * Gets the game instance where the player is in currently.
	 * 
	 * @return the {@link Game} where the player is playing currently.
	 */
	@Nullable
	public Game getPlayerGame() {
		return GameUtils.getGame(gameName);
	}

	/**
	 * @return true if this player is spectator
	 */
	public boolean isSpectator() {
		return spectator;
	}

	/**
	 * Sets this player to spectator.
	 * <p>
	 * This will not changes anything player related components.
	 * 
	 * @param spectator true if this player should be spectator or not
	 */
	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}

	/**
	 * @return the player current lives
	 */
	public int getPlayerLives() {
		return lives;
	}

	/**
	 * Sets the player lives to a new value.
	 * 
	 * @param lives the new lives (lives > -1)
	 */
	public void setPlayerLives(int lives) {
		com.google.common.base.Preconditions.checkArgument(lives > -1, "Player lives should be highest than -1.");

		this.lives = lives;
	}

	/**
	 * Gets the cached player stuffs
	 * 
	 * @return the cached stuffs that are in {@link StorePlayerStuffs}
	 */
	@NotNull
	public StorePlayerStuffs getStorePlayer() {
		return sps;
	}

	/**
	 * Defines if the player can respawn again.
	 * 
	 * @return true if have enough lives
	 */
	public boolean canRespawn() {
		return lives <= ConfigValues.getPlayerLives();
	}

	@Override
	public void tickMovement() {
		Game game = getPlayerGame();

		if (game == null) {
			return;
		}

		Player player = getPlayer();
		Location loc = player.getLocation();

		if (loc.getY() < 0) {
			CompletableFuture<Boolean> comp = null;
			IGameSpawn gameSpawn = game.getSpawn(GameSpawn.class);

			if (gameSpawn != null && gameSpawn.haveAnySpawn()) {
				comp = Utils.teleport(player, gameSpawn.getRandomSpawn());
			} else { // For safety
				for (PlayerManager pm : game.getPlayers()) {
					Player pl = pm.getPlayer();

					if (pl != player && pl.getLocation().getY() > 0) {
						comp = Utils.teleport(player, pl.getLocation());
						break;
					}
				}
			}

			if (comp != null) {
				comp.whenComplete((b, t) -> {
					// Prevent damaging player when respawned
					player.setFallDistance(0);

					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.void-fall", "%player%", player.getName()));
				});
			}

			return;
		}

		if (game.getStatus() == GameStatus.RUNNING) {
			// prevent player moving outside of game area

			if (!GameAreaManager.inArea(loc)) {
				Utils.teleport(player, loc.subtract(loc.getDirection().multiply(1)));
				return;
			}

			if (!spectator) {
				// pressure mine exploding
				GameListener.explodeMine(player, loc);
			}
		}
	}

	/**
	 * Sets the given object properties to this one and caches.
	 * 
	 * @param s {@link StorePlayerStuffs}
	 */
	public void storeFrom(StorePlayerStuffs s) {
		sps.oldHealth = s.oldHealth;
		sps.oldHunger = s.oldHunger;
		sps.oldEffects = s.oldEffects;
		sps.oldDisplayName = s.oldDisplayName;
		sps.oldListName = s.oldListName;
		sps.oldFire = s.oldFire;
		sps.oldExp = s.oldExp;
		sps.oldExpLevel = s.oldExpLevel;
		sps.oldVehicle = s.oldVehicle;
		sps.currentBoard = s.currentBoard;
		sps.oldLocation = s.oldLocation;
		sps.oldGameMode = s.oldGameMode;
		sps.oldInventories = s.oldInventories;
		sps.oldArmor = s.oldArmor;
	}

	/**
	 * Stores the player tools, such as inventory, game mode, location etc.
	 */
	public void storePlayerTools() {
		storePlayerTools(false);
	}

	/**
	 * Stores the player tools, such as inventory, game mode, location etc.
	 * 
	 * @param spectator the player is spectator or not
	 */
	public void storePlayerTools(boolean spectator) {
		Player player = getPlayer();
		if (player == null) {
			return;
		}

		if (spectator) {
			sps.fly = player.isFlying();
			sps.allowFly = player.getAllowFlight();
		} else {
			sps.oldHealth = player.getHealth();
			sps.oldHunger = player.getFoodLevel();
			sps.oldEffects = player.getActivePotionEffects();

			String dName = plugin.getComplement().getDisplayName(player);
			if (!dName.equals(player.getName()))
				sps.oldDisplayName = dName;

			sps.oldListName = plugin.getComplement().getPlayerListName(player);

			if (player.getFireTicks() > 0)
				sps.oldFire = player.getFireTicks();

			sps.oldExp = player.getExp();
			sps.oldExpLevel = player.getLevel();
			sps.oldVehicle = player.getVehicle();
		}

		sps.currentBoard = player.getScoreboard();
		sps.oldLocation = player.getLocation();
		sps.oldGameMode = player.getGameMode();

		sps.oldInventories = player.getInventory().getContents();
		sps.oldArmor = player.getInventory().getArmorContents();
	}

	/**
	 * Gives back the tools to the player if it was stored.
	 */
	public void giveBackTools() {
		Player player = getPlayer();

		if (player == null) {
			return;
		}

		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		if (ConfigValues.isBungee()) {
			Utils.connectToHub(player);
			return;
		}

		if (sps.oldLocation != null) { // Teleport back to the location
			Utils.teleport(player, sps.oldLocation).whenComplete((done, throwable) -> sps.oldLocation = null);
		}

		if (spectator || ConfigValues.isSavePlayerData()) {
			if (sps.oldInventories != null) { // Give him his inventory back.
				player.getInventory().setContents(sps.oldInventories);
				sps.oldInventories = null;
			}

			if (sps.oldArmor != null) { // Give him his armor back.
				player.getInventory().setArmorContents(sps.oldArmor);
				sps.oldArmor = null;
			}
		}

		if (sps.oldGameMode != null) { // Give him his gamemode back.
			player.setGameMode(sps.oldGameMode);
			sps.oldGameMode = null;
		}

		if (spectator) {
			player.setAllowFlight(sps.allowFly);
			player.setFlying(sps.fly);
		} else {
			if (sps.currentBoard != null) {
				player.setScoreboard(sps.currentBoard);
				sps.currentBoard = null;
			}

			if (ConfigValues.isSavePlayerData()) {
				if (sps.oldHealth > 0d) { // Give him his health back.
					player.setHealth(sps.oldHealth);
					sps.oldHealth = 0d;
				}

				if (sps.oldHunger > 0) { // Give him his hunger back.
					player.setFoodLevel(sps.oldHunger);
					sps.oldHunger = 0;
				}

				if (sps.oldEffects != null && !sps.oldEffects.isEmpty()) { // Give him his potion effects back.
					player.addPotionEffects(sps.oldEffects);
					sps.oldEffects.clear();
				}

				if (sps.oldListName != null) { // Give him his list name back.
					plugin.getComplement().setPlayerListName(player, sps.oldListName);
					sps.oldListName = null;
				}

				if (sps.oldDisplayName != null) { // Give him his display name back.
					plugin.getComplement().setDisplayName(player, sps.oldDisplayName);
					sps.oldDisplayName = null;
				}

				if (sps.oldFire > 0) { // Give him his fire back.
					player.setFireTicks(sps.oldFire);
					sps.oldFire = 0;
				}

				player.setExp(sps.oldExp); // Give him his exp back.

				player.setLevel(sps.oldExpLevel); // Give him his exp level back.

				if (sps.oldVehicle != null) { // Give him his vehicle back.
					Utils.teleport(sps.oldVehicle.getVehicle(), player.getLocation());
					sps.oldVehicle = null;
				}

				plugin.getConfiguration().getDatasCfg().set("datas." + player.getName(), null);
				Configuration.saveFile(plugin.getConfiguration().getDatasCfg(),
						plugin.getConfiguration().getDatasFile());
			}
		}
	}
}
