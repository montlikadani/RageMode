package hu.montlikadani.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.area.Selection;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.RmTabCompleter;
import hu.montlikadani.ragemode.config.CommentedConfig;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.events.*;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.holder.holograms.ArmorStandHologram;
import hu.montlikadani.ragemode.holder.holograms.IHoloHolder;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.managers.RewardManager;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.storage.MySqlDB;
import hu.montlikadani.ragemode.storage.SqlDB;
import hu.montlikadani.ragemode.storage.YamlDB;
import hu.montlikadani.ragemode.utils.Debug;
import hu.montlikadani.ragemode.utils.ServerVersion;
import hu.montlikadani.ragemode.utils.UpdateDownloader;
import hu.montlikadani.ragemode.utils.exception.GameRunningException;
import hu.montlikadani.ragemode.utils.stuff.Complement;
import hu.montlikadani.ragemode.utils.stuff.Complement1;
import hu.montlikadani.ragemode.utils.stuff.Complement2;
import net.milkbowl.vault.economy.Economy;

public final class RageMode extends JavaPlugin {

	private Configuration conf;
	private BossbarManager bossManager;
	private Selection selection;
	private RewardManager rewardManager;

	private IHoloHolder holoHolder;
	private Economy econ;
	private Database database;
	private Complement complement;

	private static Language lang;

	private boolean vault = false, kyoriSupported = false, isPaper = false, isSpigot = false;

	private final List<Game> games = new ArrayList<>();

	private final ItemHandler[] gameItems = new ItemHandler[7];
	private final ItemHandler[] lobbyItems = new ItemHandler[4];

	@Override
	public void onEnable() {
		long load = System.currentTimeMillis();

		if (ServerVersion.isCurrentLower(ServerVersion.v1_8_R1)) {
			getLogger().log(Level.SEVERE,
					"[RageMode] This version is not supported by this plugin! Please use larger 1.8+");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		checkServerSoftwares();

		if (!isSpigot) {
			getLogger().log(Level.INFO,
					"[RageMode] Seems your server software is unknown. I guess you use craftbukkit or non-spigot forks?");
		}

		if (ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R3))
			getLogger().log(Level.INFO,
					"[RageMode] This version is not fully supported by this plugin, so some options will not work.");

		(conf = new Configuration(this)).loadConfig();
		(lang = new Language(this)).loadLanguage(ConfigValues.getLang());

		holoHolder = new ArmorStandHologram();
		loadHooks();

		if (ConfigValues.isBungee()) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}

		UpdateDownloader.checkFromGithub(getServer().getConsoleSender());

		registerListeners();
		registerCommands();
		connectDatabase(false);
		loadGames();
		holoHolder.loadHolos();

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_9_R1)) {
			bossManager = new BossbarManager();
		}

		if (ConfigValues.isSignsEnable()) {
			SignCreator.loadSigns();
		}

		Debug.logConsole("Loaded in " + (System.currentTimeMillis() - load) + "ms");
	}

	@Override
	public void onDisable() {
		GameUtils.stopAllGames();
		holoHolder.deleteAllHologram(false);
		database.saveDatabase();
		conf.deleteEmptyFiles();

		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
	}

	@Override
	public CommentedConfig getConfig() {
		return conf.getCfg();
	}

	@Override
	public void saveConfig() {
		getConfig().save();
	}

	private void loadHooks() {
		vault = initEconomy();

		if (isPluginEnabled("PlaceholderAPI")) {
			new Placeholder().register();
		}
	}

	private void checkServerSoftwares() {
		try {
			Class.forName("org.spigotmc.SpigotConfig");
			isSpigot = true;
		} catch (ClassNotFoundException n) {
		}

		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
			isPaper = true;
		} catch (ClassNotFoundException n) {
		}

		try {
			Class.forName("net.kyori.adventure.text.Component");
			kyoriSupported = true;
		} catch (ClassNotFoundException e) {
		}

		complement = (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_16_R3) && kyoriSupported)
				? new Complement2()
				: new Complement1();
	}

	public void connectDatabase(boolean convert) {
		switch (ConfigValues.databaseType.toLowerCase()) {
		case "mysql":
			database = new MySqlDB();
			break;
		case "sql":
		case "sqlite":
			database = new SqlDB();
			break;
		default:
			database = new YamlDB();
			break;
		}

		if (!convert) {
			database.loadDatabase(true);
		}
	}

	private boolean initEconomy() {
		if (econ != null) {
			return true;
		}

		if (!isPluginEnabled("Vault")) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		return rsp != null && (econ = rsp.getProvider()) != null;
	}

	public synchronized boolean reload() {
		HandlerList.unregisterAll(this);

		for (Game game : games) {
			assert game != null;

			if (game.isRunning()) {
				GameUtils.stopGame(game, false);
				GameUtils.broadcastToGame(game, lang.get("game.game-stopped-for-reload"));
			} else if (game.getStatus() == GameStatus.WAITING) {
				GameUtils.kickAllPlayers(game);
			}

			game.getGameLobby().saveToConfig();
			game.saveGamesSettings();
		}

		games.clear();

		loadHooks();

		conf.loadConfig();
		lang.loadLanguage(ConfigValues.getLang());

		loadGames();

		if (ConfigValues.isSignsEnable()) {
			SignCreator.loadSigns();
		}

		registerListeners();

		if (database == null) { // We don't need to save database on reload, due to duplications
			connectDatabase(false);
		} else {
			database.loadDatabase(false);
		}

		holoHolder.loadHolos();
		return true;
	}

	private void registerCommands() {
		Optional.ofNullable(getCommand("ragemode")).ifPresent(cmd -> {
			cmd.setExecutor(new RmCommand());
			cmd.setTabCompleter(new RmTabCompleter(this));
		});
	}

	private void registerListeners() {
		Arrays.asList(new EventListener(this), new GameListener(this))
				.forEach(l -> getServer().getPluginManager().registerEvents(l, this));

		if (isPaper) {
			getServer().getPluginManager().registerEvents(new PaperListener(), this);
		}

		if (ServerVersion.isCurrentEqualOrHigher(ServerVersion.v1_13_R2)) {
			getServer().getPluginManager().registerEvents(new Listener_1_13_R2(), this);
		}

		if (ConfigValues.isBungee()) {
			getServer().getPluginManager().registerEvents(new BungeeListener(this), this);
		}

		getServer().getPluginManager().registerEvents(
				ServerVersion.isCurrentEqualOrLower(ServerVersion.v1_8_R3) ? new Listeners_1_8() : new Listeners_1_9(),
				this);
	}

	private void loadGames() {
		selection = new Selection();

		ConfigurationSection section = conf.getArenasCfg().getConfigurationSection("arenas");
		if (section != null) {
			for (String name : section.getKeys(false)) {
				GameType type;

				try {
					type = GameType.valueOf(section.getString(name + ".gametype", "normal").toUpperCase());
				} catch (IllegalArgumentException e) {
					type = GameType.NORMAL;
				}

				Game game = new Game(name, type);

				// Loads the game locker
				game.setStatus(section.getBoolean(name + ".lock") ? GameStatus.NOTREADY : GameStatus.READY);

				games.add(game);
				Debug.logConsole("Loaded {0} game!", name);
			}
		}

		Configuration.saveFile(conf.getArenasCfg(), conf.getArenasFile());
		rewardManager = new RewardManager(this);
		GameAreaManager.load();
		loadItems();
	}

	private void loadItems() {
		org.bukkit.configuration.file.FileConfiguration c = conf.getItemsCfg();
		String path = "gameitems.combatAxe";

		if (c.contains(path)) {
			gameItems[0] = new ItemHandler().setItem(c.getString(path + ".item", "iron_axe"))
					.setDisplayName(c.getString(path + ".name", "&6CombatAxe")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 3)).setVelocity(c.getDouble(path + ".velocity", 2D));
		}

		if (c.contains(path = "gameitems.grenade")) {
			gameItems[1] = new ItemHandler().setItem(Material.EGG)
					.setDisplayName(c.getString(path + ".name", "&8Grenade"))
					.setCustomName(c.getString(path + ".custom-name", "")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 5)).setAmount(c.getInt(path + ".amount", 2)).setDamage(2.20)
					.setVelocity(c.getDouble(path + ".velocity", 2D));
		}

		if (c.contains(path = "gameitems.rageArrow")) {
			gameItems[2] = new ItemHandler().setItem(Material.ARROW)
					.setDisplayName(c.getString(path + ".name", "&6RageArrow")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 9)).setDamage(c.getDouble(path + ".damage", 3.35));
		}

		if (c.contains(path = "gameitems.rageBow")) {
			gameItems[3] = new ItemHandler().setItem(Material.BOW)
					.setDisplayName(c.getString(path + ".name", "&6RageBow")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot"))
					.setEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE);
		}

		if (c.contains(path = "gameitems.rageKnife")) {
			gameItems[4] = new ItemHandler().setItem(Material.SHEARS)
					.setDisplayName(c.getString(path + ".name", "&6RageKnife")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 1)).setDamage(c.getDouble(path + ".damage", 25));
		}

		if (c.contains(path = "gameitems.flash")) {
			gameItems[5] = new ItemHandler()
					.setItem(ServerVersion.isCurrentLower(ServerVersion.v1_9_R1) ? Material.getMaterial("SNOW_BALL")
							: Material.SNOWBALL)
					.setDisplayName(c.getString(path + ".name", "&fFlash")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 6)).setAmount(c.getInt(path + ".amount", 2));
		}

		if (c.contains(path = "gameitems.pressuremine")) {
			gameItems[6] = new ItemHandler().setItem(Material.STRING)
					.setDisplayName(c.getString(path + ".name", "&8PressureMine"))
					.setLore(c.getStringList(path + ".lore")).setSlot(c.getInt(path + ".slot", 7))
					.setAmount(c.getInt(path + ".amount", 1));
		}

		// Lobby items
		if (c.contains(path = "lobbyitems.force-start")) {
			lobbyItems[0] = new ItemHandler().setItem(c.getString(path + ".item", "lever"))
					.setDisplayName(c.getString(path + ".name", "&2Force the game start"))
					.setLore(c.getStringList(path + ".lore")).setSlot(c.getInt(path + ".slot", 3));
		}

		if (c.contains(path = "lobbyitems.leavegameitem")) {
			lobbyItems[1] = new ItemHandler().setItem(c.getString(path + ".item", "barrier"))
					.setDisplayName(c.getString(path + ".name", "&cExit")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 5));
		}

		path = "lobbyitems.shopitem";
		if (c.contains(path) && c.getBoolean(path + ".enabled")) {
			lobbyItems[2] = new ItemHandler().setItem(c.getString(path + ".item", "emerald"))
					.setDisplayName(c.getString(path + ".name", "&2Shop")).setLore(c.getStringList(path + ".lore"))
					.setSlot(c.getInt(path + ".slot", 1));
		}

		if (c.contains(path = "lobbyitems.hideMessages")) {
			lobbyItems[3] = new ItemHandler().setItem(c.getString(path + ".item", "nether_star"))
					.setDisplayName(c.getString(path + ".name", "&cHide kill messages"))
					.setLore(c.getStringList(path + ".lore")).setSlot(c.getInt(path + ".slot", 8))
					.addExtra(new ItemHandler.Extra()
							.setExtraName(c.getString(path + ".status-off.name", "&aShow kill messages"))
							.setExtraLore(c.getStringList(path + ".status-off.lore")));
		}
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		dataFolder.mkdirs();
		return dataFolder;
	}

	/**
	 * Get the game by index.
	 * 
	 * @param index array index where to get from
	 * @return {@link Game}, or null if the index is out of bounds or the element at
	 *         the given index was not set
	 * @see GameUtils#getGame(String)
	 */
	@Nullable
	public Game getGame(int index) {
		return (index >= games.size() || index < 0) ? null : games.get(index);
	}

	/**
	 * Removes a game from the list.
	 * 
	 * @see #removeGame(String)
	 * @param game {@link Game}
	 * @throws GameRunningException if the given game is currently running
	 */
	public void removeGame(Game game) throws GameRunningException {
		removeGame(game.getName());
	}

	/**
	 * Removes the given game if stopped and the name matches.
	 * 
	 * @param name Game name
	 * @throws GameRunningException if the given game is currently running
	 */
	public void removeGame(String name) throws GameRunningException {
		games.removeIf(g -> {
			if (g.getName().equalsIgnoreCase(name)) {
				if (g.isRunning()) {
					throw new GameRunningException(g.getName() + " game is currently running!");
				}

				return true;
			}

			return false;
		});
	}

	public boolean isPluginEnabled(String name) {
		return getServer().getPluginManager().isPluginEnabled(name);
	}

	/**
	 * @return the database interface of {@link Database}
	 */
	@NotNull
	public Database getDatabase() {
		return database;
	}

	public static Language getLang() {
		return lang;
	}

	public boolean isVaultEnabled() {
		return vault;
	}

	public Configuration getConfiguration() {
		return conf;
	}

	public BossbarManager getBossbarManager() {
		return bossManager;
	}

	public IHoloHolder getHoloHolder() {
		return holoHolder;
	}

	@NotNull
	public List<Game> getGames() {
		return games;
	}

	@NotNull
	public ItemHandler[] getGameItems() {
		return gameItems;
	}

	@NotNull
	public ItemHandler[] getLobbyItems() {
		return lobbyItems;
	}

	public Economy getEconomy() {
		return econ;
	}

	public Selection getSelection() {
		return selection;
	}

	/**
	 * Returns the reward manager object instance.
	 * 
	 * @return {@link RewardManager}
	 */
	@NotNull
	public RewardManager getRewardManager() {
		return rewardManager;
	}

	public Complement getComplement() {
		return complement;
	}

	public boolean isKyoriSupported() {
		return kyoriSupported;
	}

	public boolean isPaper() {
		return isPaper;
	}

	public boolean isSpigot() {
		return isSpigot;
	}
}
