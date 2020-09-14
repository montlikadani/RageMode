package hu.montlikadani.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.area.Selection;
import hu.montlikadani.ragemode.commands.RmCommand;
import hu.montlikadani.ragemode.commands.RmTabCompleter;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.config.Language;
import hu.montlikadani.ragemode.database.DatabaseHandler;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.events.Listeners_1_8;
import hu.montlikadani.ragemode.events.Listeners_1_9;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.BungeeUtils;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.metrics.Metrics;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.storage.MySQLDB;
import hu.montlikadani.ragemode.storage.SQLDB;
import hu.montlikadani.ragemode.storage.YAMLDB;
import hu.montlikadani.ragemode.utils.UpdateDownloader;
import net.milkbowl.vault.economy.Economy;

public class RageMode extends JavaPlugin {

	private Configuration conf;
	private BungeeUtils bungee;
	private BossbarManager bossManager;
	private DatabaseHandler dbHandler;
	private Selection selection;

	private Economy econ;

	private static RageMode instance;
	private static Language lang;
	private static ServerVersion serverVersion;

	private static boolean isSpigot = false;

	private boolean hologram = false;
	private boolean vault = false;

	private final List<Game> games = new ArrayList<>();
	private final Set<IGameSpawn> spawns = new HashSet<>();

	private final ItemHandler[] gameItems = new ItemHandler[7];
	private final ItemHandler[] lobbyItems = new ItemHandler[3];

	@Override
	public void onEnable() {
		long load = System.currentTimeMillis();

		instance = this;
		serverVersion = new ServerVersion();

		if (serverVersion.getVersion().isLower(Version.v1_8_R1)) {
			getLogger().log(Level.SEVERE,
					"[RageMode] This version is not supported by this plugin! Please use larger 1.8+");
			getManager().disablePlugin(this);
			return;
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
			isSpigot = true;
		} catch (ClassNotFoundException c) {
			isSpigot = false;
		}

		if (serverVersion.getVersion().isEqualOrLower(Version.v1_8_R3))
			getLogger().log(Level.INFO,
					"[RageMode] This version not fully supported by this plugin, so some options will not work.");

		conf = new Configuration(this);
		conf.loadConfig();

		lang = new Language(this);
		lang.loadLanguage(ConfigValues.getLang());

		loadHooks();

		if (ConfigValues.isBungee()) {
			bungee = new BungeeUtils(this);
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}

		UpdateDownloader.checkFromGithub(getServer().getConsoleSender());

		registerListeners();
		registerCommands();
		connectDatabase();
		loadGames();

		bossManager = new BossbarManager();

		if (ConfigValues.isSignsEnable()) {
			SignConfiguration.initSignConfiguration();
			SignCreator.loadSigns();
		}

		Metrics metrics = new Metrics(this, 5076);
		if (metrics.isEnabled()) {
			metrics.addCustomChart(new Metrics.SingleLineChart("amount_of_games", games::size));

			metrics.addCustomChart(new Metrics.SimplePie("total_players", () -> {
				int totalPlayers = 0;
				switch (dbHandler.getDBType()) {
				case MYSQL:
					totalPlayers = MySQLDB.getAllPlayerStatistics().size();
					break;
				case SQLITE:
					totalPlayers = SQLDB.getAllPlayerStatistics().size();
					break;
				case YAML:
					totalPlayers = YAMLDB.getAllPlayerStatistics().size();
					break;
				default:
					break;
				}

				return String.valueOf(totalPlayers);
			}));

			metrics.addCustomChart(new Metrics.SimplePie("statistic_type", dbHandler.getDBType().name()::toLowerCase));
		}

		Debug.logConsole("Loaded in " + (System.currentTimeMillis() - load) + "ms");
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		GameUtils.stopAllGames();
		dbHandler.saveDatabase();

		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		instance = null;
	}

	private void loadHooks() {
		if (isPluginEnabled("HolographicDisplays")) {
			hologram = true;
			HoloHolder.initHoloHolder();
		} else {
			hologram = false;
		}

		vault = initEconomy();

		if (isPluginEnabled("PlaceholderAPI")) {
			new Placeholder().register();
		}
	}

	private void connectDatabase() {
		dbHandler = new DatabaseHandler(this);

		String type = "yaml";
		switch (ConfigValues.getDatabaseType().toLowerCase()) {
		case "mysql":
			type = "mysql";
			break;
		case "sql":
		case "sqlite":
			type = "sqlite";
			break;
		case "yml":
		case "yaml":
			type = "yaml";
			break;
		default:
			break;
		}

		dbHandler.setDatabaseType(type);
		dbHandler.connectDatabase();
		dbHandler.loadDatabase(true);
	}

	private boolean initEconomy() {
		if (!isPluginEnabled("Vault")) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		econ = rsp == null ? null : rsp.getProvider();
		return econ != null;
	}

	public synchronized boolean reload() {
		HandlerList.unregisterAll(this);

		for (Game game : games) {
			assert game != null;

			if (game.isGameRunning()) {
				GameUtils.stopGame(game, false);
				GameUtils.broadcastToGame(game, getLang().get("game.game-stopped-for-reload"));
			} else if (game.getStatus() == GameStatus.WAITING) {
				GameUtils.kickAllPlayers(game);
			}
		}

		games.clear();
		spawns.clear();

		conf.loadConfig();
		lang.loadLanguage(ConfigValues.getLang());

		loadGames();

		if (ConfigValues.isSignsEnable()) {
			SignConfiguration.initSignConfiguration();
			SignCreator.loadSigns();
		}

		registerListeners();
		if (dbHandler != null) {
			dbHandler.saveDatabase();
			dbHandler.loadDatabase(false);
		} else {
			connectDatabase();
		}

		if (hologram)
			HoloHolder.initHoloHolder();

		return true;
	}

	private void registerCommands() {
		org.bukkit.command.PluginCommand cmd = getCommand("ragemode");
		cmd.setExecutor(new RmCommand());
		cmd.setTabCompleter(new RmTabCompleter());
	}

	private void registerListeners() {
		Arrays.asList(new EventListener(), new GameListener(this))
				.forEach(l -> getServer().getPluginManager().registerEvents(l, this));

		if (ConfigValues.isBungee()) {
			getManager().registerEvents(new BungeeListener(), this);
		}

		if (serverVersion.getVersion().isEqualOrLower(Version.v1_8_R3))
			getManager().registerEvents(new Listeners_1_8(), this);
		else
			getManager().registerEvents(new Listeners_1_9(), this);
	}

	private void loadGames() {
		selection = new Selection();

		if (conf.getArenasCfg().contains("arenas")) {
			for (String game : GetGames.getGameNames()) {
				if (!conf.getArenasCfg().contains("arenas." + game + ".gametype")) {
					conf.getArenasCfg().set("arenas." + game + ".gametype", "normal");
				}

				GameType gameType = GameType
						.valueOf(conf.getArenasCfg().getString("arenas." + game + ".gametype", "normal").toUpperCase());
				Game g = new Game(game, gameType);
				games.add(g);

				if (gameType == GameType.APOCALYPSE) {
					spawns.add(new GameZombieSpawn(g));
				}

				spawns.add(new GameSpawn(g));

				// Loads the game locker
				g.setStatus(conf.getArenasCfg().getBoolean("arenas." + game + ".lock", false) ? GameStatus.NOTREADY
						: GameStatus.READY);

				Debug.logConsole("Loaded {0} game!", game);
			}
		}

		Configuration.saveFile(conf.getArenasCfg(), conf.getArenasFile());
		GameAreaManager.load();
		loadItems();
	}

	private void loadItems() {
		org.bukkit.configuration.file.FileConfiguration c = conf.getItemsCfg();

		String path = "gameitems.combatAxe";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(c.getString(path + ".item", "iron_axe"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6CombatAxe")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 3));
			gameItems[0] = itemHandler;
		}

		path = "gameitems.grenade";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.EGG).setDisplayName(Utils.colors(c.getString(path + ".name", "&8Grenade")))
					.setCustomName(Utils.colors(c.getString(path + ".custom-name", "")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 5))
					.setAmount(c.getInt(path + ".amount", 2)).setDamage(2.20);
			gameItems[1] = itemHandler;
		}

		path = "gameitems.rageArrow";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.ARROW).setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageArrow")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 9))
					.setDamage(c.getDouble(path + ".damage", 3.35));
			gameItems[2] = itemHandler;
		}

		path = "gameitems.rageBow";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.BOW).setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageBow")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 0))
					.setEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE);
			gameItems[3] = itemHandler;
		}

		path = "gameitems.rageKnife";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.SHEARS)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageKnife")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 1))
					.setDamage(c.getDouble(path + ".damage", 25));
			gameItems[4] = itemHandler;
		}

		path = "gameitems.flash";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler
					.setItem(serverVersion.getVersion().isLower(Version.v1_9_R1) ? Material.getMaterial("SNOW_BALL")
							: Material.SNOWBALL)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&fFlash")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 6))
					.setAmount(c.getInt(path + ".amount", 2));
			gameItems[5] = itemHandler;
		}

		path = "gameitems.pressuremine";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(Material.STRING)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&8PressureMine")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 7))
					.setAmount(c.getInt(path + ".amount", 1));
			gameItems[6] = itemHandler;
		}

		// Lobby items
		path = "lobbyitems.force-start";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&2Force the game start")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 3));
			lobbyItems[0] = itemHandler;
		}

		path = "lobbyitems.leavegameitem";
		if (c.contains(path)) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&cExit")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 5));
			lobbyItems[1] = itemHandler;
		}

		path = "lobbyitems.shopitem";
		if (c.contains(path) && c.getBoolean(path + ".enabled")) {
			ItemHandler itemHandler = new ItemHandler();
			itemHandler.setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&2Shop")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 1));
			lobbyItems[2] = itemHandler;
		}
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();

		return dataFolder;
	}

	/**
	 * Gets the database handler which handles the database connection.
	 * @return {@link DatabaseHandler}
	 */
	public DatabaseHandler getDatabaseHandler() {
		return dbHandler;
	}

	/**
	 * Gets the given player statistics from database.
	 * @param uuid Player uuid
	 * @return {@link PlayerPoints}
	 */
	public static PlayerPoints getPPFromDatabase(UUID uuid) {
		if (uuid == null) {
			return null;
		}

		switch (instance.getDatabaseHandler().getDBType()) {
		case SQLITE:
			return SQLDB.getPlayerStatsFromData(uuid);
		case MYSQL:
			return MySQLDB.getPlayerStatsFromData(uuid);
		case YAML:
			return YAMLDB.getPlayerStatsFromData(uuid);
		default:
			return null;
		}
	}

	/**
	 * Removes a game from the list.
	 * @see #removeGame(String)
	 * @param game Game
	 */
	public void removeGame(Game game) {
		removeGame(game.getName());
	}

	/**
	 * Removes a game from the list by name.
	 * @param name Game name
	 */
	public void removeGame(String name) {
		for (Iterator<Game> gt = games.iterator(); gt.hasNext();) {
			if (gt.next().getName().equalsIgnoreCase(name)) {
				gt.remove();
				break;
			}
		}
	}

	/**
	 * Removes the given game all spawns.
	 * @see #removeSpawn(String)
	 * @param game Game
	 */
	public void removeSpawn(Game game) {
		removeSpawn(game.getName());
	}

	/**
	 * Removes the given game name all spawns.
	 * @param name Game name
	 */
	public void removeSpawn(String name) {
		for (Iterator<IGameSpawn> it = spawns.iterator(); it.hasNext();) {
			if (it.next().getGame().getName().equalsIgnoreCase(name)) {
				it.remove();
			}
		}
	}

	public boolean isPluginEnabled(String name) {
		return getManager().getPlugin(name) != null && getManager().isPluginEnabled(name);
	}

	/**
	 * Gets the plugin instance
	 * @return RageMode instance
	 */
	public static RageMode getInstance() {
		return instance;
	}

	/**
	 * @return {@link Language}
	 */
	public static Language getLang() {
		return lang;
	}

	/**
	 * @return {@link ServerVersion}
	 */
	public static ServerVersion getServerVersion() {
		return serverVersion;
	}

	public static boolean isSpigot() {
		return isSpigot;
	}

	public boolean isHologramEnabled() {
		return hologram;
	}

	public boolean isVaultEnabled() {
		return vault;
	}

	public Configuration getConfiguration() {
		return conf;
	}

	public BungeeUtils getBungeeUtils() {
		return bungee;
	}

	public BossbarManager getBossbarManager() {
		return bossManager;
	}

	public List<Game> getGames() {
		return games;
	}

	public Set<IGameSpawn> getSpawns() {
		return spawns;
	}

	public ItemHandler[] getGameItems() {
		return gameItems;
	}

	public ItemHandler[] getLobbyItems() {
		return lobbyItems;
	}

	private PluginManager getManager() {
		return getServer().getPluginManager();
	}

	public Economy getEconomy() {
		return econ;
	}

	public Selection getSelection() {
		return selection;
	}
}
