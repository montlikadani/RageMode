package hu.montlikadani.ragemode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import hu.montlikadani.ragemode.database.DB;
import hu.montlikadani.ragemode.database.DBType;
import hu.montlikadani.ragemode.database.Database;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.events.EventListener;
import hu.montlikadani.ragemode.events.GameListener;
import hu.montlikadani.ragemode.events.Listeners_1_8;
import hu.montlikadani.ragemode.events.Listeners_1_9;
import hu.montlikadani.ragemode.events.PurpurListener;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameLogic.IGameSpawn;
import hu.montlikadani.ragemode.gameUtils.BungeeUtils;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.ArmorStandHologram;
import hu.montlikadani.ragemode.holder.HolographicDisplaysHolder;
import hu.montlikadani.ragemode.holder.IHoloHolder;
import hu.montlikadani.ragemode.items.ItemHandler;
import hu.montlikadani.ragemode.managers.BossbarManager;
import hu.montlikadani.ragemode.metrics.Metrics;
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
	private Selection selection;

	private IHoloHolder holoHolder;
	private Economy econ;
	private Database database;

	private DBType dbType = DBType.YAML;

	private static RageMode instance;
	private static Language lang;
	private static ServerVersion serverVersion;

	private static ServerSoftwareType softwareType = ServerSoftwareType.SPIGOT;

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

		initServerSoftwares();

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

		if (serverVersion.getVersion().isEqualOrHigher(Version.v1_9_R1)) {
			bossManager = new BossbarManager();
		}

		if (ConfigValues.isSignsEnable()) {
			SignCreator.loadSigns();
		}

		Metrics metrics = new Metrics(this, 5076);
		if (metrics.isEnabled()) {
			metrics.addCustomChart(new Metrics.SingleLineChart("amount_of_games", games::size));

			metrics.addCustomChart(new Metrics.SimplePie("total_players",
					() -> String.valueOf(database.getAllPlayerStatistics().size())));

			metrics.addCustomChart(new Metrics.SimplePie("statistic_type", dbType.name()::toLowerCase));
		}

		Debug.logConsole("Loaded in " + (System.currentTimeMillis() - load) + "ms");
	}

	@Override
	public void onDisable() {
		if (instance == null) return;

		holoHolder.deleteAllHologram();
		GameUtils.stopAllGames();
		// After any player stats modifications should contain a database save
		//dbHandler.saveDatabase();

		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		instance = null;
	}

	private void loadHooks() {
		holoHolder = isPluginEnabled("HolographicDisplays") ? new HolographicDisplaysHolder()
				: new ArmorStandHologram();
		vault = initEconomy();

		if (isPluginEnabled("PlaceholderAPI")) {
			new Placeholder().register();
		}
	}

	private void initServerSoftwares() {
		softwareType = ServerSoftwareType.UNKNOWN;

		try {
			Class.forName("org.spigotmc.SpigotConfig");
			softwareType = ServerSoftwareType.SPIGOT;
		} catch (ClassNotFoundException n) {
		}

		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
			softwareType = ServerSoftwareType.PAPER;
		} catch (ClassNotFoundException n) {
		}

		try {
			Class.forName("net.pl3x.purpur.event.entity.EntityMoveEvent");
			softwareType = ServerSoftwareType.PURPUR;
		} catch (ClassNotFoundException n) {
		}
	}

	private void connectDatabase() {
		switch (ConfigValues.databaseType.toLowerCase()) {
		case "mysql":
			database = new MySQLDB();
			break;
		case "sql":
		case "sqlite":
			database = new SQLDB();
			break;
		case "yml":
		case "yaml":
		default:
			database = new YAMLDB();
			break;
		}

		if (database.getClass().isAnnotationPresent(DB.class)) {
			dbType = database.getClass().getAnnotation(DB.class).type();
		}

		database.loadDatabase(true);
	}

	private boolean initEconomy() {
		if (econ != null) {
			return true;
		}

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

		loadHooks();

		conf.loadConfig();
		lang.loadLanguage(ConfigValues.getLang());

		loadGames();

		if (ConfigValues.isSignsEnable()) {
			SignCreator.loadSigns();
		}

		registerListeners();
		if (database == null) { // We don't need to save database on reload, due to duplications
			connectDatabase();
		}

		holoHolder.loadHolos();
		return true;
	}

	private void registerCommands() {
		Optional.ofNullable(getCommand("ragemode")).ifPresent(cmd -> {
			cmd.setExecutor(new RmCommand());
			cmd.setTabCompleter(new RmTabCompleter());
		});
	}

	private void registerListeners() {
		Arrays.asList(new EventListener(), new GameListener(this))
				.forEach(l -> getServer().getPluginManager().registerEvents(l, this));

		if (softwareType == ServerSoftwareType.PURPUR) {
			getManager().registerEvents(new PurpurListener(), this);
		}

		if (ConfigValues.isBungee()) {
			getManager().registerEvents(new BungeeListener(), this);
		}

		getManager().registerEvents(
				serverVersion.getVersion().isEqualOrLower(Version.v1_8_R3) ? new Listeners_1_8() : new Listeners_1_9(),
				this);
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
			gameItems[0] = new ItemHandler().setItem(c.getString(path + ".item", "iron_axe"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6CombatAxe")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 3));
		}

		path = "gameitems.grenade";
		if (c.contains(path)) {
			gameItems[1] = new ItemHandler().setItem(Material.EGG)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&8Grenade")))
					.setCustomName(Utils.colors(c.getString(path + ".custom-name", "")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 5))
					.setAmount(c.getInt(path + ".amount", 2)).setDamage(2.20);
		}

		path = "gameitems.rageArrow";
		if (c.contains(path)) {
			gameItems[2] = new ItemHandler().setItem(Material.ARROW)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageArrow")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 9))
					.setDamage(c.getDouble(path + ".damage", 3.35));
		}

		path = "gameitems.rageBow";
		if (c.contains(path)) {
			gameItems[3] = new ItemHandler().setItem(Material.BOW)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageBow")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 0))
					.setEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE);
		}

		path = "gameitems.rageKnife";
		if (c.contains(path)) {
			gameItems[4] = new ItemHandler().setItem(Material.SHEARS)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&6RageKnife")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 1))
					.setDamage(c.getDouble(path + ".damage", 25));
		}

		path = "gameitems.flash";
		if (c.contains(path)) {
			gameItems[5] = new ItemHandler()
					.setItem(serverVersion.getVersion().isLower(Version.v1_9_R1) ? Material.getMaterial("SNOW_BALL")
							: Material.SNOWBALL)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&fFlash")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 6))
					.setAmount(c.getInt(path + ".amount", 2));
		}

		path = "gameitems.pressuremine";
		if (c.contains(path)) {
			gameItems[6] = new ItemHandler().setItem(Material.STRING)
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&8PressureMine")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 7))
					.setAmount(c.getInt(path + ".amount", 1));
		}

		// Lobby items
		path = "lobbyitems.force-start";
		if (c.contains(path)) {
			lobbyItems[0] = new ItemHandler().setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&2Force the game start")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 3));
		}

		path = "lobbyitems.leavegameitem";
		if (c.contains(path)) {
			lobbyItems[1] = new ItemHandler().setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&cExit")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 5));
		}

		path = "lobbyitems.shopitem";
		if (c.contains(path) && c.getBoolean(path + ".enabled")) {
			lobbyItems[2] = new ItemHandler().setItem(c.getString(path + ".item"))
					.setDisplayName(Utils.colors(c.getString(path + ".name", "&2Shop")))
					.setLore(Utils.colorList(c.getStringList(path + ".lore"))).setSlot(c.getInt(path + ".slot", 1));
		}
	}

	public File getFolder() {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdir();

		return dataFolder;
	}

	/**
	 * Get the game by index.
	 * 
	 * @param index (index <= gamesSize && index >= 0)
	 * @return {@link Game}
	 * @see GameUtils#getGame(String)
	 */
	public Game getGame(int index) {
		return (index > games.size() || index < 0) ? null : games.get(index);
	}

	/**
	 * Removes a game from the list.
	 * 
	 * @see #removeGame(String)
	 * @param game Game
	 */
	public void removeGame(Game game) {
		removeGame(game.getName());
	}

	/**
	 * Removes a game from the list by name.
	 * 
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
	 * Removes all set of spawns from the given game.
	 * 
	 * @see #removeSpawn(String)
	 * @param game Game
	 */
	public void removeSpawn(Game game) {
		removeSpawn(game.getName());
	}

	/**
	 * Removes all set of spawns from the given game.
	 * 
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
	 * @return the database interface of {@link Database}
	 */
	public Database getDatabase() {
		return database;
	}

	/**
	 * @return the current set of {@link DBType}
	 */
	public DBType getDatabaseType() {
		return dbType;
	}

	/**
	 * Sets the database to a new one by the given. If it changed it will tries to
	 * connect to the new database, but the last database will still remains opens.
	 * 
	 * @param type the type which should be {@link DBType}
	 * @return {@link DBType}
	 */
	public DBType setDatabase(String type) {
		if (type != null && !type.trim().isEmpty()) {
			dbType = DBType.valueOf(type.trim().toUpperCase());
			if (dbType == null) {
				dbType = DBType.YAML;
			}

			connectDatabase();
		}

		return dbType;
	}

	public static RageMode getInstance() {
		return instance;
	}

	public static Language getLang() {
		return lang;
	}

	public static ServerVersion getServerVersion() {
		return serverVersion;
	}

	public static ServerSoftwareType getSoftwareType() {
		return softwareType;
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

	public IHoloHolder getHoloHolder() {
		return holoHolder;
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
