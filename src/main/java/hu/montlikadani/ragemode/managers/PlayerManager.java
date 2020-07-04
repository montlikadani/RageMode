package hu.montlikadani.ragemode.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.StorePlayerStuffs;

public class PlayerManager {

	private Player player;
	private String game;

	private int lives = 0;

	private StorePlayerStuffs sps = new StorePlayerStuffs();

	public PlayerManager(Player player, String game) {
		this.player = player;
		this.game = game;
	}

	public Player getPlayer() {
		return player;
	}

	public String getGameName() {
		return game;
	}

	/**
	 * @return the player current lives
	 */
	public int getPlayerLives() {
		return lives;
	}

	public void setPlayerLives(int lives) {
		this.lives = lives;
	}

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

	/**
	 * Moves another stored player things to the new one.
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
		if (player == null) {
			return;
		}

		if (spectator) {
			sps.fly = player.isFlying();
			sps.allowFly = player.getAllowFlight();
		} else {
			sps.oldHealth = player.getHealth();
			sps.oldHunger = player.getFoodLevel();

			if (!player.getActivePotionEffects().isEmpty())
				sps.oldEffects = player.getActivePotionEffects();

			if (!player.getDisplayName().equals(player.getDisplayName()))
				sps.oldDisplayName = player.getDisplayName();

			if (!player.getPlayerListName().equals(player.getPlayerListName()))
				sps.oldListName = player.getPlayerListName();

			if (player.getFireTicks() > 0)
				sps.oldFire = player.getFireTicks();

			sps.oldExp = player.getExp();
			sps.oldExpLevel = player.getLevel();

			if (player.isInsideVehicle())
				sps.oldVehicle = player.getVehicle();
		}

		sps.currentBoard = player.getScoreboard();
		sps.oldLocation = player.getLocation();
		sps.oldGameMode = player.getGameMode();

		PlayerInventory inv = player.getInventory();

		sps.oldInventories = inv.getContents();
		sps.oldArmor = inv.getArmorContents();
	}

	/**
	 * Adds back the tools to the player if have stored.
	 */
	public void addBackTools() {
		addBackTools(false);
	}

	/**
	 * Adds back the tools to the player if have stored.
	 * 
	 * @param spectator the player is spectator or not
	 */
	public void addBackTools(boolean spectator) {
		if (player == null) {
			return;
		}

		if (!player.getActivePotionEffects().isEmpty()) {
			player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
		}

		if (ConfigValues.isBungee()) {
			RageMode.getInstance().getBungeeUtils().connectToHub(player);
			return;
		}

		if (sps.oldLocation != null) { // Teleport back to the location
			player.teleport(sps.oldLocation);
			sps.oldLocation = null;
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
					player.setPlayerListName(sps.oldListName);
					sps.oldListName = null;
				}

				if (sps.oldDisplayName != null) { // Give him his display name back.
					player.setDisplayName(sps.oldDisplayName);
					sps.oldDisplayName = null;
				}

				if (sps.oldFire > 0) { // Give him his fire back.
					player.setFireTicks(sps.oldFire);
					sps.oldFire = 0;
				}

				player.setExp(sps.oldExp); // Give him his exp back.

				player.setLevel(sps.oldExpLevel); // Give him his exp level back.

				if (sps.oldVehicle != null) { // Give him his vehicle back.
					sps.oldVehicle.getVehicle().teleport(player);
					sps.oldVehicle = null;
				}

				Configuration conf = RageMode.getInstance().getConfiguration();
				conf.getDatasCfg().set("datas." + player.getName(), null);
				Configuration.saveFile(conf.getDatasCfg(), conf.getDatasFile());
			}
		}
	}
}
